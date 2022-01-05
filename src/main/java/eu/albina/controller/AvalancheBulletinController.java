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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.openjson.JSONArray;

import eu.albina.caaml.CaamlVersion;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.BulletinLock;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.rest.websocket.AvalancheBulletinEndpoint;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;
import eu.albina.util.XmlUtil;

/**
 * Controller for avalanche bulletins.
 *
 * @author Norbert Lanzanasto
 *
 */
public class AvalancheBulletinController {

	// private static Logger logger = LoggerFactory.getLogger(AvalancheBulletinController.class);

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
			bulletin.getForenoon().getAvalancheSituations().forEach(s -> {
				if (s != null)
					Hibernate.initialize(s.getAspects());
			});
		}
		if (bulletin.getAfternoon() != null) {
			bulletin.getAfternoon().getAvalancheSituations().forEach(s -> {
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
	 * @param bulletins
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
	@SuppressWarnings("unchecked")
	public Map<String, AvalancheBulletin> saveBulletins(List<AvalancheBulletin> bulletins, Instant startDate,
			Instant endDate, String region, Instant publicationDate) throws AlbinaException {
		Map<String, AvalancheBulletin> resultBulletins = new HashMap<String, AvalancheBulletin>();

		if (checkBulletinsForDuplicateRegion(bulletins, region))
			throw new AlbinaException("duplicateRegion");

		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> originalBulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();
			Map<String, AvalancheBulletin> results = new HashMap<String, AvalancheBulletin>();

			for (AvalancheBulletin bulletin : originalBulletins)
				results.put(bulletin.getId(), bulletin);

			List<String> ids = new ArrayList<String>();
			for (AvalancheBulletin bulletin : bulletins) {

				ids.add(bulletin.getId());

				if (publicationDate != null)
					bulletin.setPublicationDate(publicationDate.atZone(ZoneId.of("UTC")));

				if (results.containsKey(bulletin.getId())) {
					// Bulletin already exists
					AvalancheBulletin b = results.get(bulletin.getId());
					if (results.get(bulletin.getId()).getOwnerRegion().startsWith(region)) {
						// own bulletin - save the bulletin
						Set<String> savedRegions = b.getSavedRegions();
						b.copy(bulletin);
						for (String r : savedRegions) {
							if (!r.startsWith(region)) {
								if (!b.getSavedRegions().contains(r))
									b.addSavedRegion(r);
							}
						}
						Set<String> tmpRegions = new HashSet<String>();
						for (String r : b.getSuggestedRegions()) {
							if (bulletin.getSavedRegions().contains(r))
								tmpRegions.add(r);
						}
						for (String r : tmpRegions)
							b.getSuggestedRegions().remove(r);

						tmpRegions = new HashSet<String>();
						for (String r : b.getPublishedRegions()) {
							if (!r.startsWith(region)) {
								if (bulletin.getSavedRegions().contains(r))
									tmpRegions.add(r);
							}
						}
						for (String r : tmpRegions)
							b.getPublishedRegions().remove(r);

						b.setOwnerRegion(bulletin.getOwnerRegion());
					} else {
						// foreign bulletin
						Set<String> tmpRegions = new HashSet<String>();
						for (String r : b.getSavedRegions()) {
							if (r.startsWith(region)) {
								if (!bulletin.getSavedRegions().contains(r))
									tmpRegions.add(r);
							}
						}
						for (String r : tmpRegions)
							b.getSavedRegions().remove(r);

						for (String r : bulletin.getSavedRegions()) {
							if (r.startsWith(region)) {
								if (!b.getSavedRegions().contains(r))
									b.addSavedRegion(r);
							}
						}
						tmpRegions = new HashSet<String>();
						for (String r : b.getSuggestedRegions()) {
							if (r.startsWith(region)) {
								if (!bulletin.getSuggestedRegions().contains(r))
									tmpRegions.add(r);
							}
						}
						for (String r : tmpRegions)
							b.getSuggestedRegions().remove(r);

						tmpRegions = new HashSet<String>();
						for (String r : b.getPublishedRegions()) {
							if (r.startsWith(region))
								tmpRegions.add(r);
						}
						for (String r : tmpRegions)
							b.getPublishedRegions().remove(r);
					}
					entityManager.merge(b);
					resultBulletins.put(b.getId(), b);
				} else {
					if (bulletin.getOwnerRegion().startsWith(region)) {
						// own bulletin
						// Bulletin has to be created
						bulletin.setId(null);
						entityManager.persist(bulletin);
						resultBulletins.put(bulletin.getId(), bulletin);
					} else {
						// foreign bulletin
						// do not create the bulletin (it was removed by another user)
					}
				}
			}

			// Delete obsolete bulletins
			for (AvalancheBulletin avalancheBulletin : results.values()) {
				// bulletin has to be removed
				if (avalancheBulletin.affectsRegion(region) && !ids.contains(avalancheBulletin.getId())
						&& avalancheBulletin.getOwnerRegion().startsWith(region)) {
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
	 * Returns a XML (CAAML) string of all bulletins with status {@code published}
	 * for a given {@code date} and {@code regions} in a given {@code language}
	 * (ordered by danger rating).
	 *
	 * @param date
	 *            the date the bulletins should be valid from
	 * @param regions
	 *            the regions of the bulletins
	 * @param language
	 *            the language in which the texts of the bulletins should be added
	 *            to the XML (CAAML) string
	 * @param version
	 *            the CAAML version to generate
	 * @return the XML (CAAML) string of all published bulletins for the given time
	 *         period and regions in the given language
	 * @throws TransformerException
	 *             if the transformation of the bulletins in XML fails
	 * @throws AlbinaException
	 *             if the published bulletins can not be loaded from DB
	 * @throws ParserConfigurationException
	 *             if the XML document can not be initialized
	 */
	public String getPublishedBulletinsCaaml(Instant date, List<String> regions, LanguageCode language,
			CaamlVersion version) throws TransformerException, AlbinaException, ParserConfigurationException {
		ArrayList<AvalancheBulletin> result = AvalancheReportController.getInstance().getPublishedBulletins(date,
				regions);

		Document caamlDoc = XmlUtil.createCaaml(result, language, version);
		return XmlUtil.convertDocToString(caamlDoc);
	}

	/**
	 * Returns a XML (CAAML) string of the most recent bulletins for a given time
	 * period and {@code regions} in a given {@code language} (also if the bulletins
	 * are not in status {@code published}). This method is only for internal use.
	 * Currently TN and BZ need it for the AINEVA forecast.
	 *
	 * @param startDate
	 *            the start date the bulletins should be valid from
	 * @param endDate
	 *            the end date the bulletins should be valid until
	 * @param regions
	 *            the regions of the bulletins
	 * @param language
	 *            the language in which the texts of the bulletins should be added
	 *            to the XML (CAAML) string
	 * @return the XML (CAAML) string of the most recent bulletins for the given
	 *         time period and regions in the given language
	 * @throws TransformerException
	 *             if the transformation of the bulletins in XML fails
	 * @throws ParserConfigurationException
	 *             if the XML document can not be initialized
	 */
	public String getAinevaBulletinsCaaml(Instant startDate, Instant endDate, List<String> regions,
			LanguageCode language) throws TransformerException, ParserConfigurationException {
		List<AvalancheBulletin> bulletins = getBulletins(startDate, endDate, regions);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = CaamlVersion.V5.setNamespaceAttributes(doc.createElement("ObsCollection"));

		if (bulletins != null && !bulletins.isEmpty()) {
			Element observations = doc.createElement("observations");
			for (AvalancheBulletin bulletin : bulletins) {
				Set<String> tmpRegions = new HashSet<String>();
				for (String desiredRegion : regions) {
					for (String region : bulletin.getSavedRegions()) {
						if (region.startsWith(desiredRegion))
							tmpRegions.add(region);
					}
					for (String region : bulletin.getPublishedRegions()) {
						if (region.startsWith(desiredRegion))
							tmpRegions.add(region);
					}
				}
				if (!tmpRegions.isEmpty()) {
					bulletin.setPublishedRegions(tmpRegions);
					for (Element element : bulletin.toCAAMLv5(doc, language)) {
						observations.appendChild(element);
					}
				}
			}
			rootElement.appendChild(observations);
		}

		doc.appendChild(rootElement);

		return XmlUtil.convertDocToString(doc);
	}

	/**
	 * Returns a JSON array of all bulletins with status {@code published} for a
	 * given {@code date} and {@code regions} in a given {@code language} (ordered
	 * by danger rating).
	 *
	 * @param date
	 *            the date the bulletins should be valid from
	 * @param regions
	 *            the regions of the bulletins
	 * @param language
	 *            the language in which the texts of the bulletins should be added
	 *            to the JSON array
	 * @return the JSON array of all published bulletins for the given time period
	 *         and regions in the given language
	 * @throws AlbinaException
	 *             if the published bulletins can not be loaded from DB
	 */
	public JSONArray getPublishedBulletinsJson(Instant date, List<String> regions) throws AlbinaException {
		Collection<AvalancheBulletin> result = AvalancheReportController.getInstance().getPublishedBulletins(date,
				regions);

		if (result != null) {
			JSONArray jsonResult = new JSONArray();
			for (AvalancheBulletin bulletin : result)
				jsonResult.put(bulletin.toSmallJSON());

			return jsonResult;
		} else
			throw new AlbinaException("Published bulletins could not be loaded!");
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
	public DangerRating getHighestDangerRating(Instant date, List<String> regions) throws AlbinaException {
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
	@SuppressWarnings("unchecked")
	public List<AvalancheBulletin> getBulletins(Instant startDate, Instant endDate, List<String> regions) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
				.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();
			List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();
			for (AvalancheBulletin bulletin : bulletins) {
				if (regions.stream().anyMatch(bulletin::affectsRegionWithoutSuggestions)) {
					results.add(bulletin);
				}
			}

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
	 * @return a list of all affected bulletins
	 */
	@SuppressWarnings("unchecked")
	public List<AvalancheBulletin> submitBulletins(Instant startDate, Instant endDate, String region, User user) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();

			List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();

			// select bulletins within the region
			for (AvalancheBulletin bulletin : bulletins)
				if (bulletin.affectsRegion(region))
					results.add(bulletin);

			Set<String> result = new HashSet<String>();
			for (AvalancheBulletin bulletin : results) {

				// set author
				if (!bulletin.getAdditionalAuthors().contains(user.getName()))
					bulletin.addAdditionalAuthor(user.getName());
				bulletin.setUser(user);

				// delete suggestions within the region
				result = new HashSet<String>();
				for (String entry : bulletin.getSuggestedRegions())
					if (entry.startsWith(region))
						result.add(entry);
				for (String entry : result)
					bulletin.getSuggestedRegions().remove(entry);

				entityManager.merge(bulletin);
			}

			for (AvalancheBulletin avalancheBulletin : bulletins)
				initializeBulletin(avalancheBulletin);

			return results;
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
	 * @return a map of all affected bulletin ids and bulletins
	 */
	public Map<String, AvalancheBulletin> publishBulletins(Instant startDate, Instant endDate, List<String> regions,
			Instant publicationDate, User user) throws AlbinaException {
		Map<String, AvalancheBulletin> results = new HashMap<String, AvalancheBulletin>();

		for (String region : regions) {
			BulletinStatus internalStatus = AvalancheReportController.getInstance().getInternalStatusForDay(startDate,
					region);

			if (internalStatus == BulletinStatus.submitted || internalStatus == BulletinStatus.resubmitted) {
				List<AvalancheBulletin> bulletins = this.publishBulletins(startDate, endDate, region, publicationDate,
						user);
				for (AvalancheBulletin avalancheBulletin : bulletins)
					results.put(avalancheBulletin.getId(), avalancheBulletin);
			}
		}

		return results;
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
	 * @return a list of all affected bulletins
	 */
	@SuppressWarnings("unchecked")
	public List<AvalancheBulletin> publishBulletins(Instant startDate, Instant endDate, String region,
			Instant publicationDate, User user) {

		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();
			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();

			// select bulletins within the region
			for (AvalancheBulletin bulletin : bulletins)
				if (bulletin.affectsRegionWithoutSuggestions(region))
					results.add(bulletin);

			Set<String> result = new HashSet<String>();
			for (AvalancheBulletin bulletin : results) {

				// set author
				if (!Objects.equals(user.getEmail(), GlobalVariables.avalancheReportUsername)) {
					if (!bulletin.getAdditionalAuthors().contains(user.getName()))
						bulletin.addAdditionalAuthor(user.getName());
					bulletin.setUser(user);
				}

				// publish all saved regions
				result = new HashSet<String>();
				for (String entry : bulletin.getSavedRegions())
					if (entry.startsWith(region))
						result.add(entry);
				for (String entry : result) {
					bulletin.getSavedRegions().remove(entry);
					bulletin.getPublishedRegions().add(entry);
				}

				bulletin.setPublicationDate(publicationDate.atZone(ZoneId.of("UTC")));
				entityManager.merge(bulletin);
			}

			for (AvalancheBulletin avalancheBulletin : bulletins)
				initializeBulletin(avalancheBulletin);

			return bulletins;
		});
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
	private boolean checkBulletinsForDuplicateRegion(List<AvalancheBulletin> bulletins, String region) {
		boolean duplicateRegion = false;
		Set<String> definedRegions = new HashSet<String>();
		for (AvalancheBulletin bulletin : bulletins) {

			for (String entry : bulletin.getSavedRegions())
				if (entry.startsWith(region))
					if (!definedRegions.add(entry.toLowerCase()))
						duplicateRegion = true;
			for (String entry : bulletin.getPublishedRegions())
				if (entry.startsWith(region))
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
	private Set<String> getOwnRegions(Set<String> regions, String region) {
		Set<String> result = new HashSet<String>();
		for (String entry : regions)
			if (entry.startsWith(region))
				result.add(entry);
		return result;
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
	@SuppressWarnings("unchecked")
	public JSONArray checkBulletins(Instant startDate, Instant endDate, String region) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			JSONArray json = new JSONArray();
			boolean missingAvActivityHighlights = false;
			boolean missingAvActivityComment = false;
			boolean missingSnowpackStructureHighlights = false;
			boolean missingSnowpackStructureComment = false;
			boolean pendingSuggestions = false;
			boolean missingDangerRating = false;
			boolean incompleteTranslation = false;

			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate)).getResultList();

			List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();
			// select bulletins within the region
			for (AvalancheBulletin bulletin : bulletins)
				if (bulletin.affectsRegion(region))
					results.add(bulletin);

			if (checkBulletinsForDuplicateRegion(bulletins, region))
				json.put("duplicateRegion");

			Set<String> definedRegions = new HashSet<String>();
			for (AvalancheBulletin bulletin : results) {
				definedRegions.addAll(getOwnRegions(bulletin.getSavedRegions(), region));
				definedRegions.addAll(getOwnRegions(bulletin.getPublishedRegions(), region));

				if (!pendingSuggestions)
					for (String entry : bulletin.getSuggestedRegions())
						if (entry.startsWith(region))
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

					if ((bulletin.getAvActivityHighlightsIn(LanguageCode.de) != null
							&& bulletin.getAvActivityHighlightsIn(LanguageCode.de)
									.equals(LanguageCode.de.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getAvActivityHighlightsIn(LanguageCode.it) != null
									&& bulletin.getAvActivityHighlightsIn(LanguageCode.it)
											.equals(LanguageCode.it.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getAvActivityHighlightsIn(LanguageCode.en) != null
									&& bulletin.getAvActivityHighlightsIn(LanguageCode.en)
											.equals(LanguageCode.en.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getAvActivityCommentIn(LanguageCode.de) != null
									&& bulletin.getAvActivityCommentIn(LanguageCode.de)
											.equals(LanguageCode.de.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getAvActivityCommentIn(LanguageCode.it) != null
									&& bulletin.getAvActivityCommentIn(LanguageCode.it)
											.equals(LanguageCode.it.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getAvActivityCommentIn(LanguageCode.en) != null
									&& bulletin.getAvActivityCommentIn(LanguageCode.en)
											.equals(LanguageCode.en.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getSnowpackStructureCommentIn(LanguageCode.de) != null
									&& bulletin.getSnowpackStructureCommentIn(LanguageCode.de)
											.equals(LanguageCode.de.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getSnowpackStructureCommentIn(LanguageCode.it) != null
									&& bulletin.getSnowpackStructureCommentIn(LanguageCode.it)
											.equals(LanguageCode.it.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getSnowpackStructureCommentIn(LanguageCode.en) != null
									&& bulletin.getSnowpackStructureCommentIn(LanguageCode.en)
											.equals(LanguageCode.en.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getTendencyCommentIn(LanguageCode.de) != null
									&& bulletin.getTendencyCommentIn(LanguageCode.de)
											.equals(LanguageCode.de.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getTendencyCommentIn(LanguageCode.it) != null
									&& bulletin.getTendencyCommentIn(LanguageCode.it)
											.equals(LanguageCode.it.getBundleString("cop.incomplete-translation")))
							|| (bulletin.getTendencyCommentIn(LanguageCode.en) != null
									&& bulletin.getTendencyCommentIn(LanguageCode.en)
											.equals(LanguageCode.en.getBundleString("cop.incomplete-translation")))) {
						incompleteTranslation = true;
					}
				}
			}

			if (definedRegions.size() < AlbinaUtil.getRegionCount(region))
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
			if (incompleteTranslation)
				json.put("incompleteTranslation");

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
		List<BulletinLock> hits = new ArrayList<BulletinLock>();
		for (BulletinLock bulletinLock : bulletinLocks) {
			if (Objects.equals(bulletinLock.getSessionId(), sessionId))
				hits.add(bulletinLock);
		}
		for (BulletinLock bulletinLock : hits) {
			bulletinLocks.remove(bulletinLock);
			bulletinLock.setLock(false);
			AvalancheBulletinEndpoint.broadcast(bulletinLock);
		}
	}
}
