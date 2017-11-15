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

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.BulletinLock;
import eu.albina.model.Region;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.EventName;
import eu.albina.model.enumerations.Role;
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
			Hibernate.initialize(bulletin.getAvActivityComment());
			Hibernate.initialize(bulletin.getAvActivityHighlights());
			Hibernate.initialize(bulletin.getSnowpackStructureComment());
			Hibernate.initialize(bulletin.getSnowpackStructureHighlights());
			Hibernate.initialize(bulletin.getSynopsisComment());
			Hibernate.initialize(bulletin.getSynopsisHighlights());
			Hibernate.initialize(bulletin.getTravelAdvisoryComment());
			Hibernate.initialize(bulletin.getTravelAdvisoryHighlights());
			if (bulletin.getAbove() != null)
				Hibernate.initialize(bulletin.getAbove().getAspects());
			if (bulletin.getBelow() != null)
				Hibernate.initialize(bulletin.getBelow().getAspects());
			Hibernate.initialize(bulletin.getSuggestedRegions());
			Hibernate.initialize(bulletin.getSavedRegions());
			Hibernate.initialize(bulletin.getPublishedRegions());
			Hibernate.initialize(bulletin.getUser());
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
				if (results.containsKey(bulletin.getId()))
					results.get(bulletin.getId()).copy(bulletin);
				// bulletin has to be created
				else
					entityManager.persist(bulletin);
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
			for (AvalancheBulletin bulletin : bulletins)
				for (String region : regions)
					if (bulletin.affectsRegion(region))
						results.add(bulletin);
			for (AvalancheBulletin bulletin : results) {
				Hibernate.initialize(bulletin.getAvActivityComment());
				Hibernate.initialize(bulletin.getAvActivityHighlights());
				Hibernate.initialize(bulletin.getSnowpackStructureComment());
				Hibernate.initialize(bulletin.getSnowpackStructureHighlights());
				Hibernate.initialize(bulletin.getSynopsisComment());
				Hibernate.initialize(bulletin.getSynopsisHighlights());
				Hibernate.initialize(bulletin.getTravelAdvisoryComment());
				Hibernate.initialize(bulletin.getTravelAdvisoryHighlights());
				if (bulletin.getAbove() != null)
					Hibernate.initialize(bulletin.getAbove().getAspects());
				if (bulletin.getBelow() != null)
					Hibernate.initialize(bulletin.getBelow().getAspects());
				Hibernate.initialize(bulletin.getSuggestedRegions());
				Hibernate.initialize(bulletin.getSavedRegions());
				Hibernate.initialize(bulletin.getPublishedRegions());
				Hibernate.initialize(bulletin.getUser());
			}
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
				if (bulletin.getCreatorRegion().startsWith(region))
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

				if (bulletin.getAbove().getDangerRating() == DangerRating.missing
						|| (bulletin.getBelow() != null && bulletin.getElevation() > 0
								&& bulletin.getBelow().getDangerRating() == DangerRating.missing)) {
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
