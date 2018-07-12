package eu.albina.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinVersionTuple;
import eu.albina.model.BulletinLock;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.EventName;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.AuthorizationUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;

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
				// bulletin already exists
				if (results.containsKey(bulletin.getId())) {
					AvalancheBulletin b = results.get(bulletin.getId());
					b.copy(bulletin);
					// bulletin has to be created
				} else {
					entityManager.persist(bulletin);
				}
			}

			for (AvalancheBulletin avalancheBulletin : results.values()) {
				// bulletin has to be removed
				if (avalancheBulletin.affectsRegion(region) && !ids.contains(avalancheBulletin.getId()))
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
		Element rootElement = AlbinaUtil.createObsCollectionHeaderCaaml(doc);

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
			dateTimeReport.appendChild(doc.createTextNode(publicationDate.toString(GlobalVariables.formatterDateTime)));
			metaData.appendChild(dateTimeReport);

			metaDataProperty.appendChild(metaData);
			rootElement.appendChild(metaDataProperty);

			Element observations = doc.createElement("observations");

			for (AvalancheBulletin bulletin : result.bulletins) {
				if (bulletin.getStatus(regions) == BulletinStatus.published
						|| bulletin.getStatus(regions) == BulletinStatus.republished) {
					for (Element element : bulletin.toCAAML(doc, language, startDate)) {
						observations.appendChild(element);
					}
				}
			}
			rootElement.appendChild(observations);
		}

		doc.appendChild(rootElement);

		return AlbinaUtil.convertDocToString(doc);
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

	public void deleteBulletin(String bulletinId, List<Role> roles) throws AlbinaException {
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
				if (!AuthorizationUtil.hasPermissionForRegion(roles, region))
					result.add(region);
			}
			avalancheBulletin.setSavedRegions(result);

			regions = avalancheBulletin.getSuggestedRegions();
			result = new HashSet<String>();
			for (String region : regions) {
				if (!AuthorizationUtil.hasPermissionForRegion(roles, region))
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
	public void submitBulletins(DateTime startDate, DateTime endDate, String region) throws AlbinaException {
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

				// publish all saved regions
				result = new HashSet<String>();
				for (String entry : bulletin.getSavedRegions())
					if (entry.startsWith(region))
						result.add(entry);
				for (String entry : result) {
					bulletin.getSavedRegions().remove(entry);
					bulletin.getPublishedRegions().add(entry);
				}

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

	public void publishBulletins(DateTime startDate, DateTime endDate, List<String> regions, DateTime publicationDate)
			throws AlbinaException {
		for (String region : regions)
			this.publishBulletins(startDate, endDate, region, publicationDate);
	}

	@SuppressWarnings("unchecked")
	public void publishBulletins(DateTime startDate, DateTime endDate, String region, DateTime publicationDate)
			throws AlbinaException {

		// TODO check if current status is submitted

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

			for (AvalancheBulletin bulletin : results) {
				// set publication date if no regions where published before
				if (AuthorizationUtil.getRegion(bulletin.getUser().getRoles()).startsWith(region))
					bulletin.setPublicationDate(publicationDate);
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

			List<String> definedRegions = new ArrayList<String>();
			for (AvalancheBulletin bulletin : results) {

				for (String entry : bulletin.getSavedRegions())
					if (entry.startsWith(region) && !definedRegions.contains(entry))
						definedRegions.add(entry);
				for (String entry : bulletin.getPublishedRegions())
					if (entry.startsWith(region) && !definedRegions.contains(entry))
						definedRegions.add(entry);

				if (!pendingSuggestions)
					for (String entry : bulletin.getSuggestedRegions())
						if (entry.startsWith(region))
							pendingSuggestions = true;

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

			if (definedRegions.size() > AlbinaUtil.getRegionCount(region))
				json.put("duplicateRegion");
			else if (definedRegions.size() < AlbinaUtil.getRegionCount(region))
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

	public void unlockBulletins(UUID sessionId) {
		List<BulletinLock> hits = new ArrayList<BulletinLock>();
		for (BulletinLock bulletinLock : bulletinLocks) {
			if (bulletinLock.getSessionId() == sessionId)
				hits.add(bulletinLock);
		}
		for (BulletinLock bulletinLock : hits) {
			bulletinLocks.remove(bulletinLock);
			JSONObject json = new JSONObject();
			json.put("bulletin", bulletinLock.getBulletin());
			json.put("date", bulletinLock.getDate().toString(GlobalVariables.formatterDateTime));
			SocketIOController.getInstance().sendEvent(EventName.unlockBulletin.toString(), json.toString());
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
