package eu.albina.controller;

import java.io.Serializable;
import java.util.ArrayList;
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
import eu.albina.model.Region;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.EventName;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.AuthorizationUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;
import eu.albina.util.MapUtil;

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

	public String getCaaml(DateTime startDate, DateTime endDate, List<String> regions, LanguageCode language)
			throws TransformerException, AlbinaException, ParserConfigurationException {
		AvalancheBulletinVersionTuple result = AvalancheReportController.getInstance().getPublishedBulletins(startDate,
				endDate, regions);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = AlbinaUtil.createObsCollectionHeaderCaaml(doc);

		// create meta data
		boolean hasDaytimeDependency = false;
		DateTime publicationDate = new DateTime();
		if (result.bulletins != null) {
			for (AvalancheBulletin bulletin : result.bulletins) {
				if (bulletin.getStatus(regions) == BulletinStatus.published) {
					if (bulletin.hasDaytimeDependency())
						hasDaytimeDependency = true;
					if (bulletin.getPublicationDate() != null
							&& bulletin.getPublicationDate().isBefore(publicationDate))
						publicationDate = bulletin.getPublicationDate();
				}
			}
		}

		Element metaDataProperty = doc.createElement("metaDataProperty");
		Element metaData = doc.createElement("MetaData");
		Element dateTimeReport = doc.createElement("dateTimeReport");
		dateTimeReport.appendChild(doc.createTextNode(publicationDate.toString(GlobalVariables.formatterDateTime)));
		metaData.appendChild(dateTimeReport);

		Element customData = doc.createElement("customData");
		Element daytimeDependency = doc.createElement("albina:daytimeDependency");
		if (hasDaytimeDependency) {
			daytimeDependency.appendChild(doc.createTextNode("true"));
			customData.appendChild(daytimeDependency);

			Element dangerRatingMapAM300 = doc.createElement("albina:DangerRatingMap");
			Element resolutionAM300 = doc.createElement("albina:resolution");
			resolutionAM300.appendChild(doc.createTextNode("300"));
			dangerRatingMapAM300.appendChild(resolutionAM300);
			Element filetypeAM300 = doc.createElement("albina:filetype");
			filetypeAM300.appendChild(doc.createTextNode(GlobalVariables.fileExtensionJpg));
			dangerRatingMapAM300.appendChild(filetypeAM300);
			Element daytimeAM300 = doc.createElement("albina:daytime");
			daytimeAM300.appendChild(doc.createTextNode(GlobalVariables.urlStringForenoon));
			dangerRatingMapAM300.appendChild(daytimeAM300);
			Element urlAM300 = doc.createElement("albina:url");
			urlAM300.appendChild(doc.createTextNode(MapUtil.createMapUrlOverview(startDate, result.version,
					GlobalVariables.urlStringForenoon, 300, GlobalVariables.fileExtensionJpg)));
			dangerRatingMapAM300.appendChild(urlAM300);
			customData.appendChild(dangerRatingMapAM300);

			Element dangerRatingMapPM300 = doc.createElement("albina:DangerRatingMap");
			Element resolutionPM300 = doc.createElement("albina:resolution");
			resolutionPM300.appendChild(doc.createTextNode("300"));
			dangerRatingMapPM300.appendChild(resolutionPM300);
			Element filetypePM300 = doc.createElement("albina:filetype");
			filetypePM300.appendChild(doc.createTextNode(GlobalVariables.fileExtensionJpg));
			dangerRatingMapPM300.appendChild(filetypePM300);
			Element daytimePM300 = doc.createElement("albina:daytime");
			daytimePM300.appendChild(doc.createTextNode(GlobalVariables.urlStringAfternoon));
			dangerRatingMapPM300.appendChild(daytimePM300);
			Element urlPM300 = doc.createElement("albina:url");
			urlPM300.appendChild(doc.createTextNode(MapUtil.createMapUrlOverview(startDate, result.version,
					GlobalVariables.urlStringAfternoon, 300, GlobalVariables.fileExtensionJpg)));
			dangerRatingMapPM300.appendChild(urlPM300);
			customData.appendChild(dangerRatingMapPM300);

			Element dangerRatingMapAM150 = doc.createElement("albina:DangerRatingMap");
			Element resolutionAM150 = doc.createElement("albina:resolution");
			resolutionAM150.appendChild(doc.createTextNode("150"));
			dangerRatingMapAM150.appendChild(resolutionAM150);
			Element filetypeAM150 = doc.createElement("albina:filetype");
			filetypeAM150.appendChild(doc.createTextNode(GlobalVariables.fileExtensionJpg));
			dangerRatingMapAM150.appendChild(filetypeAM150);
			Element daytimeAM150 = doc.createElement("albina:daytime");
			daytimeAM150.appendChild(doc.createTextNode(GlobalVariables.urlStringForenoon));
			dangerRatingMapAM150.appendChild(daytimeAM150);
			Element urlAM150 = doc.createElement("albina:url");
			urlAM150.appendChild(doc.createTextNode(MapUtil.createMapUrlOverview(startDate, result.version,
					GlobalVariables.urlStringForenoon, 150, GlobalVariables.fileExtensionJpg)));
			dangerRatingMapAM150.appendChild(urlAM150);
			customData.appendChild(dangerRatingMapAM150);

			Element dangerRatingMapPM150 = doc.createElement("albina:DangerRatingMap");
			Element resolutionPM150 = doc.createElement("albina:resolution");
			resolutionPM150.appendChild(doc.createTextNode("150"));
			dangerRatingMapPM150.appendChild(resolutionPM150);
			Element filetypePM150 = doc.createElement("albina:filetype");
			filetypePM150.appendChild(doc.createTextNode(GlobalVariables.fileExtensionJpg));
			dangerRatingMapPM150.appendChild(filetypePM150);
			Element daytimePM150 = doc.createElement("albina:daytime");
			daytimePM150.appendChild(doc.createTextNode(GlobalVariables.urlStringAfternoon));
			dangerRatingMapPM150.appendChild(daytimePM150);
			Element urlPM150 = doc.createElement("albina:url");
			urlPM150.appendChild(doc.createTextNode(MapUtil.createMapUrlOverview(startDate, result.version,
					GlobalVariables.urlStringAfternoon, 150, GlobalVariables.fileExtensionJpg)));
			dangerRatingMapPM150.appendChild(urlPM150);
			customData.appendChild(dangerRatingMapPM150);

		} else {
			daytimeDependency.appendChild(doc.createTextNode("false"));
			customData.appendChild(daytimeDependency);

			Element dangerRatingMapFullday300 = doc.createElement("albina:DangerRatingMap");
			Element resolutionFullday300 = doc.createElement("albina:resolution");
			resolutionFullday300.appendChild(doc.createTextNode("300"));
			dangerRatingMapFullday300.appendChild(resolutionFullday300);
			Element filetypeFullday300 = doc.createElement("albina:filetype");
			filetypeFullday300.appendChild(doc.createTextNode(GlobalVariables.fileExtensionJpg));
			dangerRatingMapFullday300.appendChild(filetypeFullday300);
			Element daytimeFullday300 = doc.createElement("albina:daytime");
			daytimeFullday300.appendChild(doc.createTextNode(GlobalVariables.urlStringFullday));
			dangerRatingMapFullday300.appendChild(daytimeFullday300);
			Element urlFullday300 = doc.createElement("albina:url");
			urlFullday300.appendChild(doc.createTextNode(MapUtil.createMapUrlOverview(startDate, result.version,
					GlobalVariables.urlStringFullday, 300, GlobalVariables.fileExtensionJpg)));
			dangerRatingMapFullday300.appendChild(urlFullday300);
			customData.appendChild(dangerRatingMapFullday300);

			Element dangerRatingMapFullday150 = doc.createElement("albina:DangerRatingMap");
			Element resolutionFullday150 = doc.createElement("albina:resolution");
			resolutionFullday150.appendChild(doc.createTextNode("150"));
			dangerRatingMapFullday150.appendChild(resolutionFullday150);
			Element filetypeFullday150 = doc.createElement("albina:filetype");
			filetypeFullday150.appendChild(doc.createTextNode(GlobalVariables.fileExtensionJpg));
			dangerRatingMapFullday150.appendChild(filetypeFullday150);
			Element daytimeFullday150 = doc.createElement("albina:daytime");
			daytimeFullday150.appendChild(doc.createTextNode(GlobalVariables.urlStringFullday));
			dangerRatingMapFullday150.appendChild(daytimeFullday150);
			Element urlFullday150 = doc.createElement("albina:url");
			urlFullday150.appendChild(doc.createTextNode(MapUtil.createMapUrlOverview(startDate, result.version,
					GlobalVariables.urlStringFullday, 150, GlobalVariables.fileExtensionJpg)));
			dangerRatingMapFullday150.appendChild(urlFullday150);
			customData.appendChild(dangerRatingMapFullday150);
		}
		metaData.appendChild(customData);

		metaDataProperty.appendChild(metaData);
		rootElement.appendChild(metaDataProperty);

		Element observations = doc.createElement("observations");

		boolean found = false;

		if (result.bulletins != null) {
			for (AvalancheBulletin bulletin : result.bulletins) {
				if (bulletin.getStatus(regions) == BulletinStatus.published) {
					for (Element element : bulletin.toCAAML(doc, language, startDate, result.version)) {
						observations.appendChild(element);
					}
					found = true;
				}
			}
		}
		rootElement.appendChild(observations);
		doc.appendChild(rootElement);

		if (found)
			return AlbinaUtil.convertDocToString(doc);
		else
			return null;
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

	public void deleteBulletin(String bulletinId, Role role) throws AlbinaException {
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
				if (!AuthorizationUtil.hasPermissionForRegion(role, region))
					result.add(region);
			}
			avalancheBulletin.setSavedRegions(result);

			regions = avalancheBulletin.getSuggestedRegions();
			result = new HashSet<String>();
			for (String region : regions) {
				if (!AuthorizationUtil.hasPermissionForRegion(role, region))
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
				if (AuthorizationUtil.getRegion(bulletin.getUser().getRole()).startsWith(region))
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

			List<Region> regions = RegionController.getInstance().getRegions(region);

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

				if (missingAvActivityHighlights || bulletin.getAvActivityHighlights() == null
						|| bulletin.getAvActivityHighlights().getTexts() == null
						|| bulletin.getAvActivityHighlights().getTexts().size() < 1)
					missingAvActivityHighlights = true;
				if (missingAvActivityComment || bulletin.getAvActivityComment() == null
						|| bulletin.getAvActivityComment().getTexts() == null
						|| bulletin.getAvActivityComment().getTexts().size() < 1)
					missingAvActivityComment = true;

				if (bulletin.getForenoon().getDangerRatingAbove() == DangerRating.missing
						|| (bulletin.getForenoon() != null && bulletin.getElevation() > 0
								&& bulletin.getForenoon().getDangerRatingBelow() == DangerRating.missing)) {
					missingDangerRating = true;
				}
			}

			if (definedRegions.size() > regions.size())
				json.put("duplicateRegion");
			else if (definedRegions.size() < regions.size())
				json.put("missingRegion");

			if (missingAvActivityHighlights)
				json.put("missingAvActivityHighlights");
			if (missingAvActivityComment)
				json.put("missingAvActivityComment");
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
