// SPDX-License-Identifier: AGPL-3.0-or-later
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
import eu.albina.model.AbstractPersistentObject;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.BulletinLock;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.User;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.rest.websocket.AvalancheBulletinEndpoint;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.HibernateUtil;
import jakarta.persistence.EntityManager;

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
		return HibernateUtil.getInstance().run(entityManager -> {
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
	public synchronized Map<String, AvalancheBulletin> saveBulletins(List<AvalancheBulletin> newBulletins, Instant startDate,
			Instant endDate, Region region, User user) throws AlbinaException {
		Map<String, AvalancheBulletin> resultBulletins = new HashMap<String, AvalancheBulletin>();

		if (checkBulletinsForDuplicateRegion(newBulletins, region))
			throw new AlbinaException("duplicateRegion");

		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> loadedBulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();
			Map<String, AvalancheBulletin> originalBulletins = loadedBulletins.stream().collect(Collectors.toMap(AbstractPersistentObject::getId, b -> b));

			List<String> ids = new ArrayList<String>();
			for (AvalancheBulletin newBulletin : newBulletins) {
				ids.add(newBulletin.getId());
				AvalancheBulletin originalBulletin = originalBulletins.get(newBulletin.getId());
				if (originalBulletin != null) {
					// Bulletin already exists
					if (isOwnBulletin(originalBulletin, region)) {
						modifyOwnBulletin(region, newBulletin, originalBulletin);
					} else {
						modifyForeignBulletin(region, newBulletin, originalBulletin);
					}
					entityManager.merge(originalBulletin);
					resultBulletins.put(originalBulletin.getId(), originalBulletin);
				} else if (isOwnBulletin(newBulletin, region)) {
					// own bulletin
					// Bulletin has to be created
					newBulletin.setId(null);
					entityManager.persist(newBulletin);
					resultBulletins.put(newBulletin.getId(), newBulletin);
				} else {
					// foreign bulletin
					// do not create the bulletin (it was removed by another user)
				}
				logger.info("Bulletin {} for region {} updated by {}", newBulletin.getId(), region.getId(), user);
			}

			// Delete obsolete bulletins
			for (AvalancheBulletin avalancheBulletin : originalBulletins.values()) {

				// bulletin has to be removed
				if (avalancheBulletin.affectsRegion(region) && !ids.contains(avalancheBulletin.getId())
						&& isOwnBulletin(avalancheBulletin, region)) {
					entityManager.remove(avalancheBulletin);
					resultBulletins.remove(avalancheBulletin.getId());
				}
			}

			for (AvalancheBulletin bulletin : resultBulletins.values())
				initializeBulletin(bulletin);

			AvalancheReportController.getInstance().saveReport(resultBulletins, startDate, region, user, entityManager);
			// save report for super regions
			for (Region superRegion : region.getSuperRegions()) {
				AvalancheReportController.getInstance().saveReport(resultBulletins, startDate, superRegion, user, entityManager);
			}

			return resultBulletins;
		});
	}

	private static boolean isOwnBulletin(AvalancheBulletin newBulletin, Region region) {
		return newBulletin.getOwnerRegion().startsWith(region.getId());
	}

	private static void modifyOwnBulletin(Region region, AvalancheBulletin newBulletin, AvalancheBulletin originalBulletin) {
		// own bulletin
		// TODO: split needed if other region was published???
		// What if the bulletin is still published for other region and is now changed only for this region?
		// Do we have to split the bulletin and create a new one for this region with only savedRegions?
		// Check: if !originalBulletin.hasPublishedRegions().startsWith(region) => split bulletin in two

		// get all own micro-regions from new bulletin
		Set<String> newSavedRegions = newBulletin.getSavedRegions();
		newSavedRegions.removeIf(r -> !r.startsWith(region.getId()));

		// get all foreign micro-regions from original bulletin
		Set<String> savedRegions = originalBulletin.getSavedRegions();
		savedRegions.removeIf(r -> r.startsWith(region.getId()));
		savedRegions.addAll(newSavedRegions);

		originalBulletin.copy(newBulletin);
		originalBulletin.setSavedRegions(savedRegions);

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
	}

	private static void modifyForeignBulletin(Region region, AvalancheBulletin newBulletin, AvalancheBulletin originalBulletin) {
		// foreign bulletin
		// no split of bulletin needed, because the published bulletin for the other region remains
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

		// remove own published regions from original bulletin
		tmpRegions = new HashSet<String>();
		for (String publishedRegion : originalBulletin.getPublishedRegions()) {
			if (publishedRegion.startsWith(region.getId()))
				tmpRegions.add(publishedRegion);
		}
		for (String tmpRegion : tmpRegions)
			originalBulletin.getPublishedRegions().remove(tmpRegion);

		// add own published regions from new bulletin as saved regions to original bulletin
		tmpRegions = new HashSet<String>();
		for (String publishedRegion : newBulletin.getPublishedRegions()) {
			if (publishedRegion.startsWith(region.getId()))
				tmpRegions.add(publishedRegion);
		}
		for (String tmpRegion : tmpRegions)
			originalBulletin.addSavedRegion(tmpRegion);
	}

	/**
	 * Creates a {@code bulletin} in the database.
	 *
	 * @param startDate
	 *            the start date the bulletin is valid from
	 * @param endDate
	 *            the end date the bulletin is valid until
	 * @param region
	 *            the active region of the user who is creating the bulletin
	 * @return a map of all bulletin ids and bulletins for this day
	 */
	public synchronized Map<String, AvalancheBulletin> createBulletin(AvalancheBulletin newBulletin, Instant startDate, Instant endDate,
			Region region, EntityManager entityManager) {
		Map<String, AvalancheBulletin> resultBulletins = new HashMap<String, AvalancheBulletin>();

			List<AvalancheBulletin> loadedBulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();

			for (AvalancheBulletin loadedBulletin : loadedBulletins) {
				// check micro-regions for each bulletin to prevent duplicates
				for (String microRegion : newBulletin.getPublishedAndSavedRegions()) {
					loadedBulletin.getPublishedRegions().remove(microRegion);
					loadedBulletin.getSavedRegions().remove(microRegion);
				}
				if (!loadedBulletin.getPublishedAndSavedRegions().isEmpty()) {
					entityManager.merge(loadedBulletin);
					resultBulletins.put(loadedBulletin.getId(), loadedBulletin);
				} else {
					entityManager.remove(loadedBulletin);
				}
			}

			// Bulletin has to be created
			newBulletin.setId(null);
			entityManager.persist(newBulletin);
			resultBulletins.put(newBulletin.getId(), newBulletin);

			for (AvalancheBulletin bulletin : resultBulletins.values())
				initializeBulletin(bulletin);

			return resultBulletins;
	}

	/**
	 * Update a {@code bulletin} in the database.
	 *
	 * @param startDate
	 *            the start date the bulletin is valid from
	 * @param endDate
	 *            the end date the bulletin is valid until
	 * @param region
	 *            the active region of the user who is updating the bulletin
	 * @return a map of all bulletin ids and bulletins for this day
	 */
	public Map<String, AvalancheBulletin> updateBulletin(AvalancheBulletin updatedBulletin, Instant startDate, Instant endDate, Region region, User user, EntityManager entityManager) {
		Map<String, AvalancheBulletin> resultBulletins = new HashMap<String, AvalancheBulletin>();

			List<AvalancheBulletin> loadedBulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();

			for (AvalancheBulletin loadedBulletin : loadedBulletins) {
				if (!loadedBulletin.getId().equals(updatedBulletin.getId())) {
					// check micro-regions for each bulletin to prevent duplicates
					// and add general headline from currently updated bulletin
					for (String microRegion : updatedBulletin.getPublishedAndSavedRegions()) {
						loadedBulletin.getPublishedRegions().remove(microRegion);
						loadedBulletin.getSavedRegions().remove(microRegion);

						if (loadedBulletin.getOwnerRegion().equals(updatedBulletin.getOwnerRegion()) && region.isEnableGeneralHeadline()) {
							loadedBulletin.setGeneralHeadlineComment(updatedBulletin.getGeneralHeadlineComment());
							loadedBulletin.setGeneralHeadlineCommentTextcat(updatedBulletin.getGeneralHeadlineCommentTextcat());
							loadedBulletin.setGeneralHeadlineCommentNotes(updatedBulletin.getGeneralHeadlineCommentNotes());
						}
					}
					if (!loadedBulletin.getPublishedAndSavedRegions().isEmpty()) {
						entityManager.merge(loadedBulletin);
						resultBulletins.put(loadedBulletin.getId(), loadedBulletin);
					} else {
						entityManager.remove(loadedBulletin);
					}
				}
			}

			// Bulletin has to be updated
			entityManager.merge(updatedBulletin);
			resultBulletins.put(updatedBulletin.getId(), updatedBulletin);

			for (AvalancheBulletin bulletin : resultBulletins.values())
				initializeBulletin(bulletin);

			AvalancheReportController.getInstance().saveReport(resultBulletins, startDate, region, user, entityManager);
			// save report for super regions
			for (Region superRegion : region.getSuperRegions()) {
				AvalancheReportController.getInstance().saveReport(resultBulletins, startDate, superRegion, user, entityManager);
			}

			logger.info("Bulletin {} for region {} updated by {}", updatedBulletin.getId(), region.getId(), user);

			return resultBulletins;
	}

	/**
	 * Deletes a {@code bulletin} from the database.
	 *
	 * @param startDate
	 *            the start date the bulletin is valid from
	 * @param endDate
	 *            the end date the bulletin is valid until
	 * @param region
	 *            the active region of the user who is deleting the bulletin
	 * @return a map of all bulletin ids and bulletins for this day
	 */
    public synchronized Map<String, AvalancheBulletin> deleteBulletin(String bulletinId, Instant startDate, Instant endDate,
			Region region, User user, EntityManager entityManager) {
		Map<String, AvalancheBulletin> resultBulletins = new HashMap<String, AvalancheBulletin>();

			List<AvalancheBulletin> loadedBulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();

			for (AvalancheBulletin loadedBulletin : loadedBulletins) {
				if (!loadedBulletin.getId().equals(bulletinId))
					resultBulletins.put(loadedBulletin.getId(), loadedBulletin);
				else
					entityManager.remove(loadedBulletin);
			}

			for (AvalancheBulletin bulletin : resultBulletins.values())
				initializeBulletin(bulletin);

			AvalancheReportController.getInstance().saveReport(resultBulletins, startDate, region, user, entityManager);
			// save report for super regions
			for (Region superRegion : region.getSuperRegions()) {
				AvalancheReportController.getInstance().saveReport(resultBulletins, startDate, superRegion, user, entityManager);
			}

			return resultBulletins;
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
	public List<AvalancheBulletin> getBulletins(Instant startDate, Instant endDate, List<Region> regions, EntityManager entityManager) {
			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins, AvalancheBulletin.class)
				.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();
			List<AvalancheBulletin> results = bulletins.stream()
				.filter(bulletin -> regions.stream()
					.anyMatch(bulletin::affectsRegionWithoutSuggestions))
				.collect(Collectors.toList());

			for (AvalancheBulletin bulletin : results)
				initializeBulletin(bulletin);

			return results;
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
					ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance(entityManager);
					if (user != null && !Objects.equals(user.getEmail(), serverInstance.getUserName())) {
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
		return HibernateUtil.getInstance().run(entityManager -> getAllBulletins(startDate, endDate, entityManager));
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
							|| bulletin.getForenoon().dangerRating(true) == DangerRating.missing
							|| (bulletin.getForenoon() != null && bulletin.getForenoon().isHasElevationDependency()
									&& bulletin.getForenoon().dangerRating(false) == DangerRating.missing)) {
						missingDangerRating = true;
					}

					if (missingDangerRating || (bulletin.isHasDaytimeDependency() && bulletin.getAfternoon() == null)
							|| (bulletin.isHasDaytimeDependency()
									&& bulletin.getAfternoon().dangerRating(true) == DangerRating.missing)
							|| (bulletin.isHasDaytimeDependency() && bulletin.getAfternoon() != null
									&& bulletin.getAfternoon().isHasElevationDependency()
									&& bulletin.getAfternoon().dangerRating(false) == DangerRating.missing)) {
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
		BulletinLock hit = bulletinLocks.stream()
			.filter(bulletinLock -> bulletinLock.getDate().toEpochMilli() == lock.getDate().toEpochMilli()
				&& Objects.equals(bulletinLock.getBulletin(), lock.getBulletin()))
			.findFirst().orElse(null);

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

	/**
	 * Return all bulletin locks that are locked for {@code region}.
	 *
	 * @param date
	 *            the date of interest
	 * @return all bulletin locks that are locked for {@code date}
	 */
	public List<BulletinLock> getLockedBulletins(Instant date) {
		return bulletinLocks.stream()
			.filter(bulletinLock -> bulletinLock.getDate().equals(date))
			.collect(Collectors.toList());
	}

}
