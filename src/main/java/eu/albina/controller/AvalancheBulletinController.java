package eu.albina.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.websocket.EncodeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinVersionTuple;
import eu.albina.model.BulletinLock;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.rest.AvalancheBulletinEndpoint;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.AuthorizationUtil;
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

	// private static Logger logger =
	// LoggerFactory.getLogger(AvalancheBulletinController.class);

	private static AvalancheBulletinController instance = null;
	private List<BulletinLock> bulletinLocks;

	private AvalancheBulletinController() {
		bulletinLocks = new ArrayList<BulletinLock>();
	}

	public static AvalancheBulletinController getInstance() {
		if (instance == null) {
			instance = new AvalancheBulletinController();
		}
		return instance;
	}

	/**
	 * Retrieve an avalanche bulletin from the database by ID.
	 * 
	 * @param bulletinId
	 *            The ID of the desired avalanche bulletin.
	 * @return The avalanche bulletin with the given ID.
	 * @throws AlbinaException
	 */
	public AvalancheBulletin getBulletin(String bulletinId) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			AvalancheBulletin bulletin = entityManager.find(AvalancheBulletin.class, bulletinId);
			if (bulletin == null) {
				transaction.rollback();
				throw new AlbinaException("No bulletin with ID: " + bulletinId);
			}
			initializeBulletin(bulletin);
			transaction.commit();
			return bulletin;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

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
			if (bulletin.getForenoon().getAvalancheSituation1() != null)
				Hibernate.initialize(bulletin.getForenoon().getAvalancheSituation1().getAspects());
			if (bulletin.getForenoon().getAvalancheSituation1() != null)
				Hibernate.initialize(bulletin.getForenoon().getAvalancheSituation2().getAspects());
		}
		if (bulletin.getAfternoon() != null) {
			if (bulletin.getAfternoon().getAvalancheSituation1() != null)
				Hibernate.initialize(bulletin.getAfternoon().getAvalancheSituation1().getAspects());
			if (bulletin.getAfternoon().getAvalancheSituation2() != null)
				Hibernate.initialize(bulletin.getAfternoon().getAvalancheSituation2().getAspects());
		}
		Hibernate.initialize(bulletin.getSuggestedRegions());
		Hibernate.initialize(bulletin.getSavedRegions());
		Hibernate.initialize(bulletin.getPublishedRegions());
		Hibernate.initialize(bulletin.getUser());
		Hibernate.initialize(bulletin.getAdditionalAuthors());
	}

	@SuppressWarnings("unchecked")
	public void saveBulletins(List<AvalancheBulletin> bulletins, DateTime startDate, DateTime endDate, String region,
			DateTime publicationDate) throws AlbinaException {

		if (checkBulletinsForDuplicateRegion(bulletins, region))
			throw new AlbinaException("duplicateRegion");

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			List<AvalancheBulletin> originalBulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();
			Map<String, AvalancheBulletin> results = new HashMap<String, AvalancheBulletin>();

			for (AvalancheBulletin bulletin : originalBulletins)
				results.put(bulletin.getId(), bulletin);

			List<String> ids = new ArrayList<String>();
			for (AvalancheBulletin bulletin : bulletins) {
				ids.add(bulletin.getId());

				if (publicationDate != null)
					bulletin.setPublicationDate(publicationDate);

				if (results.containsKey(bulletin.getId())) {
					// Bulletin already exists
					AvalancheBulletin b = results.get(bulletin.getId());
					if (results.get(bulletin.getId()).getOwnerRegion().startsWith(region)) {
						// Own bulletin - save the bulletin
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
					} else {
						// foreign bulletin
						for (String r : bulletin.getSavedRegions()) {
							if (r.startsWith(region)) {
								if (!b.getSavedRegions().contains(r))
									b.addSavedRegion(r);
							}
						}
						Set<String> tmpRegions = new HashSet<String>();
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
							if (r.startsWith(region)) {
								if (bulletin.getSavedRegions().contains(r))
									tmpRegions.add(r);
							}
						}
						for (String r : tmpRegions)
							b.getPublishedRegions().remove(r);
					}
					entityManager.merge(b);
				} else {
					// Bulletin has to be created
					bulletin.setId(null);
					entityManager.persist(bulletin);
				}
			}

			// Delete obsolete bulletins
			for (AvalancheBulletin avalancheBulletin : results.values()) {
				// bulletin has to be removed
				if (avalancheBulletin.affectsRegion(region) && !ids.contains(avalancheBulletin.getId())
						&& avalancheBulletin.getOwnerRegion().startsWith(region))
					entityManager.remove(avalancheBulletin);
			}

			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public Serializable saveBulletin(AvalancheBulletin bulletin) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.persist(bulletin);
			transaction.commit();
			return bulletin.getId();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public void updateBulletin(AvalancheBulletin bulletin) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			AvalancheBulletin originalBulletin = entityManager.find(AvalancheBulletin.class, bulletin.getId());
			originalBulletin.copy(bulletin);
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public String getPublishedBulletinsCaaml(DateTime startDate, DateTime endDate, List<String> regions,
			LanguageCode language) throws TransformerException, AlbinaException, ParserConfigurationException {
		AvalancheBulletinVersionTuple result = AvalancheReportController.getInstance().getPublishedBulletins(startDate,
				endDate, regions);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = XmlUtil.createObsCollectionHeaderCaaml(doc);

		// create meta data
		DateTime publicationDate = null;
		if (result.bulletins != null && !result.bulletins.isEmpty()) {
			for (AvalancheBulletin bulletin : result.bulletins) {
				if (bulletin.getStatus(regions) == BulletinStatus.published
						|| bulletin.getStatus(regions) == BulletinStatus.republished) {
					if (bulletin.getPublicationDate() != null) {
						if (publicationDate == null)
							publicationDate = bulletin.getPublicationDate();
						else {
							if (bulletin.getPublicationDate().isAfter(publicationDate))
								publicationDate = bulletin.getPublicationDate();
						}
					}
				}
			}

			Element metaDataProperty = doc.createElement("metaDataProperty");
			Element metaData = doc.createElement("MetaData");
			Element dateTimeReport = doc.createElement("dateTimeReport");
			dateTimeReport.appendChild(doc.createTextNode(
					publicationDate.withZone(DateTimeZone.UTC).toString(GlobalVariables.formatterDateTime)));
			metaData.appendChild(dateTimeReport);

			metaDataProperty.appendChild(metaData);
			rootElement.appendChild(metaDataProperty);

			Element observations = doc.createElement("observations");

			for (AvalancheBulletin bulletin : result.bulletins) {
				if (bulletin.getStatus(regions) == BulletinStatus.published
						|| bulletin.getStatus(regions) == BulletinStatus.republished) {
					for (Element element : bulletin.toCAAML(doc, language)) {
						observations.appendChild(element);
					}
				}
			}
			rootElement.appendChild(observations);
		}

		doc.appendChild(rootElement);

		return XmlUtil.convertDocToString(doc);
	}

	public String getAinevaBulletinsCaaml(DateTime startDate, DateTime endDate, List<String> regions,
			LanguageCode language) throws TransformerException, AlbinaException, ParserConfigurationException {
		List<AvalancheBulletin> bulletins = getBulletins(startDate, endDate, regions);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = XmlUtil.createObsCollectionHeaderCaaml(doc);

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
					for (Element element : bulletin.toCAAML(doc, language)) {
						observations.appendChild(element);
					}
				}
			}
			rootElement.appendChild(observations);
		}

		doc.appendChild(rootElement);

		return XmlUtil.convertDocToString(doc);
	}

	public Collection<AvalancheBulletin> getPublishedBulletinsJson(DateTime startDate, DateTime endDate,
			List<String> regions) throws AlbinaException {
		AvalancheBulletinVersionTuple result = AvalancheReportController.getInstance().getPublishedBulletins(startDate,
				endDate, regions);

		if (result != null)
			return result.bulletins;
		else
			throw new AlbinaException("Published bulletins could not be loaded!");
	}

	public DangerRating getHighestDangerRating(DateTime startDate, DateTime endDate, List<String> regions)
			throws AlbinaException {
		AvalancheBulletinVersionTuple result = AvalancheReportController.getInstance().getPublishedBulletins(startDate,
				endDate, regions);

		if (result != null) {
			DangerRating dangerRating = DangerRating.missing;
			for (AvalancheBulletin bulletin : result.bulletins) {
				if (bulletin.getHighestDangerRating().compareTo(dangerRating) <= 0)
					dangerRating = bulletin.getHighestDangerRating();
			}
			return dangerRating;
		} else
			throw new AlbinaException("Published bulletins could not be loaded!");
	}

	@SuppressWarnings("unchecked")
	public List<AvalancheBulletin> getBulletins(DateTime startDate, DateTime endDate, List<String> regions)
			throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();
			List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();
			for (AvalancheBulletin bulletin : bulletins) {
				for (String region : regions)
					if (bulletin.affectsRegion(region)) {
						results.add(bulletin);
						break;
					}
			}

			for (AvalancheBulletin bulletin : results)
				initializeBulletin(bulletin);

			transaction.commit();
			return results;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public void deleteBulletin(String bulletinId, User user) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			AvalancheBulletin avalancheBulletin = entityManager.find(AvalancheBulletin.class, bulletinId);
			if (avalancheBulletin == null) {
				transaction.rollback();
				throw new AlbinaException("No bulletin with ID: " + bulletinId);
			}

			Set<String> regions = avalancheBulletin.getSavedRegions();
			Set<String> result = new HashSet<String>();
			for (String region : regions) {
				if (!AuthorizationUtil.hasPermissionForRegion(user, region))
					result.add(region);
			}
			avalancheBulletin.setSavedRegions(result);

			regions = avalancheBulletin.getSuggestedRegions();
			result = new HashSet<String>();
			for (String region : regions) {
				if (!AuthorizationUtil.hasPermissionForRegion(user, region))
					result.add(region);
			}
			avalancheBulletin.setSuggestedRegions(result);

			if (avalancheBulletin.getSavedRegions().isEmpty() && avalancheBulletin.getPublishedRegions().isEmpty())
				entityManager.remove(avalancheBulletin);
			else {
				entityManager.merge(avalancheBulletin);
			}

			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void submitBulletins(DateTime startDate, DateTime endDate, String region, User user) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

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

			transaction.commit();

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public Map<String, AvalancheBulletin> publishBulletins(DateTime startDate, DateTime endDate, List<String> regions,
			DateTime publicationDate, User user) throws AlbinaException {
		Map<String, AvalancheBulletin> results = new HashMap<String, AvalancheBulletin>();

		for (String region : regions) {
			List<AvalancheBulletin> bulletins = this.publishBulletins(startDate, endDate, region, publicationDate,
					user);
			for (AvalancheBulletin avalancheBulletin : bulletins)
				results.put(avalancheBulletin.getId(), avalancheBulletin);
		}

		return results;
	}

	@SuppressWarnings("unchecked")
	public List<AvalancheBulletin> publishBulletins(DateTime startDate, DateTime endDate, String region,
			DateTime publicationDate, User user) throws AlbinaException {
		List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

			// select bulletins within the region
			for (AvalancheBulletin bulletin : bulletins)
				if (bulletin.affectsRegionWithoutSuggestions(region))
					results.add(bulletin);

			Set<String> result = new HashSet<String>();
			for (AvalancheBulletin bulletin : results) {

				// set author
				if (user.getEmail() != GlobalVariables.avalancheReportUsername) {
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

				bulletin.setPublicationDate(publicationDate);
				entityManager.merge(bulletin);
			}

			for (AvalancheBulletin avalancheBulletin : bulletins)
				initializeBulletin(avalancheBulletin);

			transaction.commit();

			return bulletins;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

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

	private Set<String> getOwnRegions(Set<String> regions, String region) {
		Set<String> result = new HashSet<String>();
		for (String entry : regions)
			if (entry.startsWith(region))
				result.add(entry);
		return result;
	}

	@SuppressWarnings("unchecked")
	public JSONArray checkBulletins(DateTime startDate, DateTime endDate, String region) throws AlbinaException {
		JSONArray json = new JSONArray();
		boolean missingAvActivityHighlights = false;
		boolean missingAvActivityComment = false;
		boolean missingSnowpackStructureHighlights = false;
		boolean missingSnowpackStructureComment = false;
		boolean pendingSuggestions = false;
		boolean missingDangerRating = false;

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			List<AvalancheBulletin> bulletins = entityManager.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

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
					/*
					 * if (missingSnowpackStructureHighlights ||
					 * bulletin.getSnowpackStructureHighlightsTextcat() == null ||
					 * bulletin.getSnowpackStructureHighlightsTextcat().isEmpty())
					 * missingSnowpackStructureHighlights = true;
					 */
					if (missingSnowpackStructureComment || bulletin.getSnowpackStructureCommentTextcat() == null
							|| bulletin.getSnowpackStructureCommentTextcat().isEmpty())
						missingSnowpackStructureComment = true;

					if (bulletin.getForenoon() == null
							|| bulletin.getForenoon().getDangerRatingAbove() == DangerRating.missing
							|| (bulletin.getForenoon() != null && bulletin.isHasElevationDependency()
									&& bulletin.getForenoon().getDangerRatingBelow() == DangerRating.missing)) {
						missingDangerRating = true;
					}

					if (missingDangerRating || (bulletin.isHasDaytimeDependency() && bulletin.getAfternoon() == null)
							|| (bulletin.isHasDaytimeDependency()
									&& bulletin.getAfternoon().getDangerRatingAbove() == DangerRating.missing)
							|| (bulletin.isHasDaytimeDependency() && bulletin.getAfternoon() != null
									&& bulletin.isHasElevationDependency()
									&& bulletin.getAfternoon().getDangerRatingBelow() == DangerRating.missing)) {
						missingDangerRating = true;
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

			transaction.commit();

			return json;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public void lockBulletin(BulletinLock lock) throws AlbinaException {
		for (BulletinLock bulletinLock : bulletinLocks) {
			if (bulletinLock.getDate().getMillis() == lock.getDate().getMillis()
					&& bulletinLock.getBulletin().equals(lock.getBulletin()))
				throw new AlbinaException("Bulletin already locked!");
		}
		bulletinLocks.add(lock);
	}

	public void unlockBulletin(BulletinLock lock) throws AlbinaException {
		unlockBulletin(lock.getBulletin(), lock.getDate());
	}

	public void unlockBulletin(String bulletin, DateTime date) throws AlbinaException {
		date = date.withTimeAtStartOfDay();

		BulletinLock hit = null;
		for (BulletinLock bulletinLock : bulletinLocks) {
			if (bulletinLock.getDate().getMillis() == date.getMillis() && bulletinLock.getBulletin().equals(bulletin))
				hit = bulletinLock;
		}

		if (hit != null)
			bulletinLocks.remove(hit);
		else
			throw new AlbinaException("Bulletin not locked!");
	}

	public void unlockBulletins(String sessionId) {
		List<BulletinLock> hits = new ArrayList<BulletinLock>();
		for (BulletinLock bulletinLock : bulletinLocks) {
			if (bulletinLock.getSessionId() == sessionId)
				hits.add(bulletinLock);
		}
		for (BulletinLock bulletinLock : hits) {
			bulletinLocks.remove(bulletinLock);
			bulletinLock.setLock(false);
			try {
				AvalancheBulletinEndpoint.broadcast(bulletinLock);
			} catch (IOException | EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	public List<DateTime> getLockedBulletins(String bulletin) {
		List<DateTime> result = new ArrayList<DateTime>();
		for (BulletinLock bulletinLock : bulletinLocks) {
			if (bulletinLock.getBulletin() == bulletin)
				result.add(bulletinLock.getDate());
		}
		return result;
	}
}
