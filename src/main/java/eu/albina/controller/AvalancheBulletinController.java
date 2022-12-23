/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.BulletinLock;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.rest.websocket.AvalancheBulletinEndpoint;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.HibernateUtil;

import javax.persistence.EntityManager;

/**
 * Controller for avalanche bulletins.
 *
 * @author Norbert Lanzanasto
 *
 */
public class AvalancheBulletinController {

	private static Logger logger = LoggerFactory.getLogger(AvalancheBulletinController.class);

	private static AvalancheBulletinController instance = null;
	private final List<BulletinLock> bulletinLocks;

	/**
	 * Private constructor.
	 */
	private AvalancheBulletinController() {
		bulletinLocks = new ArrayList<BulletinLock>();
	}

	/**
	 * Returns the {@code AvalancheBulletinController} object associated with the
	 * current Java application.
	 *
	 * @return the {@code AvalancheBulletinController} object associated with the
	 *         current Java application.
	 */
	public static AvalancheBulletinController getInstance() {
		if (instance == null) {
			instance = new AvalancheBulletinController();
		}
		return instance;
	}

	/**
	 * Retrieve an avalanche bulletin from the database by {@code bulletinID}.
	 *
	 * @param bulletinId
	 *            The ID of the desired avalanche bulletin.
	 * @return The avalanche bulletin with the given ID.
	 */
	public AvalancheBulletin getBulletin(String bulletinId) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			AvalancheBulletin bulletin = entityManager.find(AvalancheBulletin.class, bulletinId);
			if (bulletin == null) {
				throw new HibernateException("No bulletin with ID: " + bulletinId);
			}
			initializeBulletin(bulletin);
			return bulletin;
		});
	}

	/**
	 * Initialize all fields of the {@code bulletin} to be able to access it after
	 * the DB transaction was closed.
	 *
	 * @param bulletin
	 *            the bulletin that should be initialized
	 */
	private void initializeBulletin(AvalancheBulletin bulletin) {
		Hibernate.initialize(bulletin.getAvActivityComment());
		Hibernate.initialize(bulletin.getAvActivityHighlights());
		Hibernate.initialize(bulletin.getSnowpackStructureComment());
		Hibernate.initialize(bulletin.getSnowpackStructureHighlights());
		Hibernate.initialize(bulletin.getSynopsisComment());
		Hibernate.initialize(bulletin.getSynopsisHighlights());
		Hibernate.initialize(bulletin.getTravelAdvisoryComment());
		Hibernate.initialize(bulletin.getTravelAdvisoryHighlights());
		if (bulletin.getForenoon() != null) {
			bulletin.getForenoon().getAvalancheProblems().forEach(s -> {
				if (s != null)
					Hibernate.initialize(s.getAspects());
			});
		}
		if (bulletin.getAfternoon() != null) {
			bulletin.getAfternoon().getAvalancheProblems().forEach(s -> {
				if (s != null)
					Hibernate.initialize(s.getAspects());
			});
		}
		Hibernate.initialize(bulletin.getSuggestedRegions());
		Hibernate.initialize(bulletin.getSavedRegions());
		Hibernate.initialize(bulletin.getPublishedRegions());
		Hibernate.initialize(bulletin.getUser());
		Hibernate.initialize(bulletin.getAdditionalAuthors());
	}

	/**
	 * Saves a list of {@code bulletins} to the database. The new bulletins are
	 * checked if they belong to the users {@code region} and if they are already in
	 * the database. Depending on this the handling is different.
	 *
	 * @param newBulletins
	 *            the bulletins to be saved
	 * @param startDate
	 *            the start date the bulletins are valid from
	 * @param endDate
	 *            the end date the bulletins are valid until
	 * @param region
	 *            the active region of the user who is saving the bulletins
	 * @param publicationDate
	 *            the publication date of the bulletins
	 * @return a map of all affected bulletin ids and bulletins
	 * @throws AlbinaException
	 *             if a micro region is defined twice in the bulletins
	 */
	public Map<String, AvalancheBulletin> saveBulletins(List<AvalancheBulletin> newBulletins, Instant startDate,
			Instant endDate, Region region) throws AlbinaException {
		Map<String, AvalancheBulletin> resultBulletins = new HashMap<String, AvalancheBulletin>();

		if (checkBulletinsForDuplicateRegion(newBulletins, region))
			throw new AlbinaException("duplicateRegion");

		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> loadedBulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();
			Map<String, AvalancheBulletin> originalBulletins = new HashMap<String, AvalancheBulletin>();

			for (AvalancheBulletin loadedBulletin : loadedBulletins)
				originalBulletins.put(loadedBulletin.getId(), loadedBulletin);

			List<String> ids = new ArrayList<String>();
			for (AvalancheBulletin newBulletin : newBulletins) {

				ids.add(newBulletin.getId());

				// Bulletin already exists
				if (originalBulletins.containsKey(newBulletin.getId())) {
					AvalancheBulletin originalBulletin = originalBulletins.get(newBulletin.getId());

					// own bulletin
					// TODO: split needed if other region was published???
					// What if the bulletin is still published for other region and is now changed only for this region?
					// Do we have to split the bulletin and create a new one for this region with only savedRegions?
					// Check: if !originalBulletin.hasPublishedRegions().startsWith(region) => split bulletin in two
					if (originalBulletins.get(newBulletin.getId()).getOwnerRegion().startsWith(region.getId())) {
						Set<String> savedRegions = originalBulletin.getSavedRegions();
						originalBulletin.copy(newBulletin);
						for (String savedRegion : savedRegions) {
							if (!savedRegion.startsWith(region.getId())) {
								if (!originalBulletin.getSavedRegions().contains(savedRegion))
									originalBulletin.addSavedRegion(savedRegion);
							}
						}
						Set<String> tmpRegions = new HashSet<String>();
						for (String suggestedRegion : originalBulletin.getSuggestedRegions()) {
							if (newBulletin.getSavedRegions().contains(suggestedRegion))
								tmpRegions.add(suggestedRegion);
						}
						for (String tmpRegion : tmpRegions)
							originalBulletin.getSuggestedRegions().remove(tmpRegion);

						tmpRegions = new HashSet<String>();
						for (String publishedRegion : originalBulletin.getPublishedRegions()) {
							if (!publishedRegion.startsWith(region.getId())) {
								if (newBulletin.getSavedRegions().contains(publishedRegion))
									tmpRegions.add(publishedRegion);
							}
						}
						for (String tmpRegion : tmpRegions)
							originalBulletin.getPublishedRegions().remove(tmpRegion);

						originalBulletin.setOwnerRegion(newBulletin.getOwnerRegion());

					// foreign bulletin
					// no split of bulletin needed, because the published bulletin for the other region remains
					} else {

						// remove own saved regions from original bulletin which are not present in new bulletin
						Set<String> tmpRegions = new HashSet<String>();
						for (String savedRegion : originalBulletin.getSavedRegions()) {
							if (savedRegion.startsWith(region.getId())) {
								if (!newBulletin.getSavedRegions().contains(savedRegion))
									tmpRegions.add(savedRegion);
							}
						}
						for (String tmpRegion : tmpRegions)
							originalBulletin.getSavedRegions().remove(tmpRegion);

						// add own saved regions from new bulletin which are not present in original bulletin
						for (String savedRegion : newBulletin.getSavedRegions()) {
							if (savedRegion.startsWith(region.getId())) {
								if (!originalBulletin.getSavedRegions().contains(savedRegion))
									originalBulletin.addSavedRegion(savedRegion);
							}
						}

						// remove own suggested regions from original bulletin which are not present in new bulletin
						tmpRegions = new HashSet<String>();
						for (String suggestedRegion : originalBulletin.getSuggestedRegions()) {
							if (suggestedRegion.startsWith(region.getId())) {
								if (!newBulletin.getSuggestedRegions().contains(suggestedRegion))
									tmpRegions.add(suggestedRegion);
							}
						}
						for (String tmpRegion : tmpRegions)
							originalBulletin.getSuggestedRegions().remove(tmpRegion);

						// own suggested regions are not possible (they are always in saved regions) -> nothing to add

						// remove own published regions from original bulletin which are not present in new bulletin
						tmpRegions = new HashSet<String>();
						for (String publishedRegion : originalBulletin.getPublishedRegions()) {
							if (publishedRegion.startsWith(region.getId()))
								tmpRegions.add(publishedRegion);
						}
						for (String tmpRegion : tmpRegions)
							originalBulletin.getPublishedRegions().remove(tmpRegion);

						// own published regions are not possible (they are always in saved regions) -> nothing to add
					}

					entityManager.merge(originalBulletin);
					resultBulletins.put(originalBulletin.getId(), originalBulletin);

				} else {

					// own bulletin
					if (newBulletin.getOwnerRegion().startsWith(region.getId())) {

						// Bulletin has to be created
						newBulletin.setId(null);
						entityManager.persist(newBulletin);
						resultBulletins.put(newBulletin.getId(), newBulletin);

					// foreign bulletin
					} else {
						// do not create the bulletin (it was removed by another user)
					}
				}
			}

			// Delete obsolete bulletins
			for (AvalancheBulletin avalancheBulletin : originalBulletins.values()) {

				// bulletin has to be removed
				if (avalancheBulletin.affectsRegion(region) && !ids.contains(avalancheBulletin.getId())
						&& avalancheBulletin.getOwnerRegion().startsWith(region.getId())) {
					entityManager.remove(avalancheBulletin);
					if (resultBulletins.containsKey(avalancheBulletin.getId()))
						resultBulletins.remove(avalancheBulletin.getId());
				}
			}

			for (AvalancheBulletin bulletin : resultBulletins.values())
				initializeBulletin(bulletin);

			return resultBulletins;
		});
	}

	/**
	 * Returns the highest {@code DangerRating} of all bulletins with status
	 * {@code published} for a given {@code date} and in a specific {@code regions}.
	 *
	 * @param date
	 *            the date of the time period of interest
	 * @param regions
	 *            the regions of interest
	 * @return the highest {@code DangerRating} for the given time period and in the
	 *         specific region
	 * @throws AlbinaException
	 *             if the published bulletins for this time period and in this
	 *             region could not be loaded
	 */
	public DangerRating getHighestDangerRating(Instant date, List<Region> regions) throws AlbinaException {
		Collection<AvalancheBulletin> result = AvalancheReportController.getInstance().getPublishedBulletins(date,
				regions);

		if (result != null) {
			DangerRating dangerRating = DangerRating.missing;
			for (AvalancheBulletin bulletin : result) {
				if (bulletin.getHighestDangerRating().compareTo(dangerRating) <= 0)
					dangerRating = bulletin.getHighestDangerRating();
			}
			return dangerRating;
		} else
			throw new AlbinaException("Published bulletins could not be loaded!");
	}

	/**
	 * Returns the most recent bulletins for a given time period and
	 * {@code regions}.
	 *
	 * @param startDate
	 *            the start date the bulletins should be valid from
	 * @param endDate
	 *            the end date the bulletins should be valid until
	 * @param regions
	 *            the regions of the bulletins
	 * @return the most recent bulletins for the given time period and regions
	 */
	public List<AvalancheBulletin> getBulletins(Instant startDate, Instant endDate, List<Region> regions) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
				.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();
			List<AvalancheBulletin> results = bulletins.stream()
				.filter(bulletin -> regions.stream()
					.anyMatch(bulletin::affectsRegionWithoutSuggestions))
				.collect(Collectors.toList());

			for (AvalancheBulletin bulletin : results)
				initializeBulletin(bulletin);

			return results;
		});
	}

	/**
	 * Set the author for the bulletins affected by the submission. Delete all
	 * suggested regions, because they are not valid anymore after the bulletin has
	 * been submitted.
	 *
	 * @param startDate
	 *            the start date of the time period
	 * @param endDate
	 *            the end date of the time period
	 * @param region
	 *            the region that should be submitted
	 * @param user
	 *            the user who submits the bulletins
	 * @return a list of all bulletins
	 */
	public List<AvalancheBulletin> submitBulletins(Instant startDate, Instant endDate, Region region, User user) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();

			List<AvalancheBulletin> results = bulletins.stream()
				.filter(bulletin -> bulletin.affectsRegion(region))
				.collect(Collectors.toList());

			// select bulletins within the region

			Set<String> result = new HashSet<String>();
			for (AvalancheBulletin bulletin : results) {

				// set author
				if (!bulletin.getAdditionalAuthors().contains(user.getName()))
					bulletin.addAdditionalAuthor(user.getName());
				bulletin.setUser(user);

				// delete suggestions within the region
				result = new HashSet<String>();
				for (String entry : bulletin.getSuggestedRegions())
					if (entry.startsWith(region.getId()))
						result.add(entry);
				for (String entry : result)
					bulletin.getSuggestedRegions().remove(entry);

				entityManager.merge(bulletin);
			}

			for (AvalancheBulletin avalancheBulletin : bulletins)
				initializeBulletin(avalancheBulletin);

			logger.info("Bulletins for region {} submitted", region.getId());

			return bulletins;
		});
	}

	/**
	 * Publish all bulletins for the given {@code regions} in the given time period
	 * if the status of the corresponding report is {@code submitted} or
	 * {@code resubmitted}.
	 *
	 * @param startDate
	 *            the start date of the time period
	 * @param endDate
	 *            the end date of the time period
	 * @param regions
	 *            the regions that should be published
	 * @param publicationDate
	 *            the timestamp of the publication
	 * @param user
	 *            the user who publishes the bulletins
	 */
	public void publishBulletins(Instant startDate, Instant endDate, List<Region> regions,
			Instant publicationDate, User user) throws AlbinaException {
		Map<String, AvalancheBulletin> results = new HashMap<String, AvalancheBulletin>();

		for (Region region : regions) {
			logger.info("Publish bulletins for region {}", region.getId());
			BulletinStatus internalStatus = AvalancheReportController.getInstance().getInternalStatusForDay(startDate,
					region);

			logger.info("Internal status for region {} is {}", region.getId(), internalStatus);

			if (internalStatus == BulletinStatus.submitted || internalStatus == BulletinStatus.resubmitted) {
				this.publishBulletins(startDate, endDate, region, publicationDate, user);
			}
		}
	}

	/**
	 * Publish all bulletins for the given {@code region} in the given time period.
	 * Sets the author of the bulletins and moves all saved regions to published
	 * regions.
	 *
	 * @param startDate
	 *            the start date of the time period
	 * @param endDate
	 *            the end date of the time period
	 * @param region
	 *            the region that should be published
	 * @param publicationDate
	 *            the timestamp of the publication
	 * @param user
	 *            the user who publishes the bulletins
	 */
	public void publishBulletins(Instant startDate, Instant endDate, Region region,
			Instant publicationDate, User user) {

		HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> bulletins = getAllBulletins(startDate, endDate, entityManager);

			for (AvalancheBulletin bulletin : bulletins) {

				// select bulletins within the region
				if (bulletin.affectsRegionWithoutSuggestions(region)) {

					// set author
					if (user != null && !Objects.equals(user.getEmail(), ServerInstanceController.getInstance().getLocalServerInstance().getUserName())) {
						if (!bulletin.getAdditionalAuthors().contains(user.getName()))
							bulletin.addAdditionalAuthor(user.getName());
						bulletin.setUser(user);
					}

					// publish all saved regions
					Set<String> result = bulletin.getSavedRegions().stream()
						.filter(entry -> entry.startsWith(region.getId()))
						.collect(Collectors.toSet());
					for (String entry : result) {
						bulletin.getSavedRegions().remove(entry);
						bulletin.getPublishedRegions().add(entry);
					}

					bulletin.setPublicationDate(publicationDate.atZone(ZoneId.of("UTC")));
					entityManager.merge(bulletin);
				}

				// set publication date for all bulletins
				bulletin.setPublicationDate(publicationDate.atZone(ZoneId.of("UTC")));
				entityManager.merge(bulletin);
			}
			return null;
		});
	}

	public List<AvalancheBulletin> getAllBulletins(Instant startDate, Instant endDate) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> getAllBulletins(startDate, endDate, entityManager));
	}

	private List<AvalancheBulletin> getAllBulletins(Instant startDate, Instant endDate, EntityManager entityManager) {
		final List<AvalancheBulletin> bulletins = entityManager
			.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
			.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate))
			.setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate))
			.getResultList();
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			initializeBulletin(avalancheBulletin);
		}
		return bulletins;
	}

	/**
	 * Check if a micro region of the specified {@code region} was defined twice in
	 * the given {@code bulletins}.
	 *
	 * @param bulletins
	 *            the bulletins to be checked
	 * @param region
	 *            the region to check the micro regions for
	 * @return true if one micro region was defined twice
	 */
	private boolean checkBulletinsForDuplicateRegion(List<AvalancheBulletin> bulletins, Region region) {
		boolean duplicateRegion = false;
		Set<String> definedRegions = new HashSet<String>();
		for (AvalancheBulletin bulletin : bulletins) {
			for (String entry : bulletin.getSavedRegions())
				if (entry.startsWith(region.getId()))
					if (!definedRegions.add(entry.toLowerCase()))
						duplicateRegion = true;
			for (String entry : bulletin.getPublishedRegions())
				if (entry.startsWith(region.getId()))
					if (!definedRegions.add(entry.toLowerCase()))
						duplicateRegion = true;
		}
		return duplicateRegion;
	}

	/**
	 * Return all micro regions that are contained by {@code regions} and part of
	 * {@code region}.
	 *
	 * @param regions
	 *            the micro regions to be checked
	 * @param region
	 *            the region of interest
	 * @return a set of micro regions that are contained by {@code regions} and part
	 *         of {@code region}
	 */
	private Set<String> getOwnRegions(Set<String> regions, Region region) {
		return regions.stream()
			.filter(entry -> entry.startsWith(region.getId()))
			.collect(Collectors.toSet());
	}

	/**
	 * Check all bulletins in a given time period and a given {@code region} for
	 * missing and duplicate micro regions, missing text parts, pending suggestions,
	 * missing danger ratings and incomplete translations.
	 *
	 * @param startDate
	 *            the start date of the time period
	 * @param endDate
	 *            the end date of the time period
	 * @param region
	 *            the region of interest
	 * @return a JSON array containing all warnings (empty if no warning was found)
	 */
	public JSONArray checkBulletins(Instant startDate, Instant endDate, Region region) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			JSONArray json = new JSONArray();
			boolean missingAvActivityHighlights = false;
			boolean missingAvActivityComment = false;
			boolean missingSnowpackStructureHighlights = false;
			boolean missingSnowpackStructureComment = false;
			boolean pendingSuggestions = false;
			boolean missingDangerRating = false;

			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();

			List<AvalancheBulletin> results = bulletins.stream()
				.filter(bulletin -> bulletin.affectsRegion(region))
				.collect(Collectors.toList());
			// select bulletins within the region

			if (checkBulletinsForDuplicateRegion(bulletins, region))
				json.put("duplicateRegion");

			Set<String> definedRegions = new HashSet<String>();
			for (AvalancheBulletin bulletin : results) {
				definedRegions.addAll(getOwnRegions(bulletin.getSavedRegions(), region));
				definedRegions.addAll(getOwnRegions(bulletin.getPublishedRegions(), region));

				if (!pendingSuggestions)
					for (String entry : bulletin.getSuggestedRegions())
						if (entry.startsWith(region.getId()))
							pendingSuggestions = true;

				if (bulletin.affectsRegionWithoutSuggestions(region)) {
					if (missingAvActivityHighlights || bulletin.getAvActivityHighlightsTextcat() == null
							|| bulletin.getAvActivityHighlightsTextcat().isEmpty())
						missingAvActivityHighlights = true;
					if (missingAvActivityComment || bulletin.getAvActivityCommentTextcat() == null
							|| bulletin.getAvActivityCommentTextcat().isEmpty())
						missingAvActivityComment = true;
					if (missingSnowpackStructureComment || bulletin.getSnowpackStructureCommentTextcat() == null
							|| bulletin.getSnowpackStructureCommentTextcat().isEmpty())
						missingSnowpackStructureComment = true;

					if (bulletin.getForenoon() == null
							|| bulletin.getForenoon().getDangerRatingAbove() == DangerRating.missing
							|| (bulletin.getForenoon() != null && bulletin.getForenoon().isHasElevationDependency()
									&& bulletin.getForenoon().getDangerRatingBelow() == DangerRating.missing)) {
						missingDangerRating = true;
					}

					if (missingDangerRating || (bulletin.isHasDaytimeDependency() && bulletin.getAfternoon() == null)
							|| (bulletin.isHasDaytimeDependency()
									&& bulletin.getAfternoon().getDangerRatingAbove() == DangerRating.missing)
							|| (bulletin.isHasDaytimeDependency() && bulletin.getAfternoon() != null
									&& bulletin.getAfternoon().isHasElevationDependency()
									&& bulletin.getAfternoon().getDangerRatingBelow() == DangerRating.missing)) {
						missingDangerRating = true;
					}
				}
			}

			if (definedRegions.size() < region.getMicroRegions())
				json.put("missingRegion");
			if (missingAvActivityHighlights)
				json.put("missingAvActivityHighlights");
			if (missingAvActivityComment)
				json.put("missingAvActivityComment");
			if (missingSnowpackStructureHighlights)
				json.put("missingSnowpackStructureHighlights");
			if (missingSnowpackStructureComment)
				json.put("missingSnowpackStructureComment");
			if (pendingSuggestions)
				json.put("pendingSuggestions");
			if (missingDangerRating)
				json.put("missingDangerRating");

			return json;
		});
	}

	/**
	 * Lock a specific bulletin due to current modification.
	 *
	 * @param lock
	 *            the bulletin lock
	 * @throws AlbinaException
	 *             if the bulletin was already locked
	 */
	public void lockBulletin(BulletinLock lock) throws AlbinaException {
		for (BulletinLock bulletinLock : bulletinLocks) {
			if (bulletinLock.getDate().equals(lock.getDate())
					&& bulletinLock.getBulletin().equals(lock.getBulletin()))
				throw new AlbinaException("Bulletin already locked!");
		}
		bulletinLocks.add(lock);
	}

	/**
	 * Unlock a specific bulletin.
	 *
	 * @param lock
	 *            the bulletin lock
	 * @throws AlbinaException
	 *             if the bulletin was not locked
	 */
	public void unlockBulletin(BulletinLock lock) throws AlbinaException {
		unlockBulletin(lock.getBulletin(), lock.getDate());
	}

	/**
	 * Unlock a specific {@code bulletin} for a specific {@code date}.
	 *
	 * @param bulletin
	 *            the bulletin to be unlocked
	 * @param date
	 *            the timestamp
	 * @throws AlbinaException
	 *             if the bulletin was not locked
	 */
	public void unlockBulletin(String bulletin, Instant date) throws AlbinaException {
		BulletinLock hit = null;
		for (BulletinLock bulletinLock : bulletinLocks) {
			if (bulletinLock.getDate().toEpochMilli() == date.toEpochMilli() && bulletinLock.getBulletin().equals(bulletin))
				hit = bulletinLock;
		}

		if (hit != null)
			bulletinLocks.remove(hit);
		else
			throw new AlbinaException("Bulletin not locked!");
	}

	/**
	 * Unlock all bulletins locked by a specific {@code sessionId}.
	 *
	 * @param sessionId
	 *            the session id
	 */
	public void unlockBulletins(String sessionId) {
		List<BulletinLock> hits = bulletinLocks.stream()
			.filter(bulletinLock -> Objects.equals(bulletinLock.getSessionId(), sessionId))
			.collect(Collectors.toList());
		for (BulletinLock bulletinLock : hits) {
			bulletinLocks.remove(bulletinLock);
			bulletinLock.setLock(false);
			AvalancheBulletinEndpoint.broadcast(bulletinLock);
		}
	}
}
