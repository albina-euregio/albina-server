package eu.albina.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.Role;
import eu.albina.util.AuthorizationUtil;
import eu.albina.util.HibernateUtil;

/**
 * Controller for snow profiles.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class AvalancheBulletinController {

	// private static Logger logger =
	// LoggerFactory.getLogger(AvalancheBulletinController.class);

	private static AvalancheBulletinController instance = null;

	private AvalancheBulletinController() {
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
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			AvalancheBulletin bulletin = session.get(AvalancheBulletin.class, bulletinId);
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
			session.close();
		}
	}

	// TODO check if already published
	public Serializable saveBulletin(AvalancheBulletin bulletin) throws AlbinaException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Serializable bulletinId = session.save(bulletin);
			transaction.commit();
			return bulletinId;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	// TODO check if already published
	public void updateBulletin(AvalancheBulletin bulletin) throws AlbinaException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.update(bulletin);
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<AvalancheBulletin> getBulletins(DateTime startDate, DateTime endDate, List<String> regions)
			throws AlbinaException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			List<AvalancheBulletin> bulletins = session.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).list();

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
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public BulletinStatus getStatus(DateTime startDate, DateTime endDate, String region) throws AlbinaException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			List<AvalancheBulletin> bulletins = session.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).list();

			List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();

			// select bulletins within the region
			for (AvalancheBulletin bulletin : bulletins)
				if (bulletin.affectsRegion(region))
					results.add(bulletin);

			// get status of bulletins
			// TODO this relies on publishing for whole region (e.g. IT-32-BZ)
			BulletinStatus result = BulletinStatus.missing;
			for (AvalancheBulletin bulletin : results) {
				if (bulletin.getStatus(region).compareTo(result) < 0)
					result = bulletin.getStatus();
			}

			transaction.commit();
			return result;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public void deleteBulletin(String bulletinId, Role role) throws AlbinaException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			AvalancheBulletin avalancheBulletin = session.get(AvalancheBulletin.class, bulletinId);
			if (avalancheBulletin == null) {
				transaction.rollback();
				throw new AlbinaException("No bulletin with ID: " + bulletinId);
			} else if (avalancheBulletin.getStatus(AuthorizationUtil.getRegion(role)) == BulletinStatus.published) {
				transaction.rollback();
				throw new AlbinaException("Bulletin already published!");
			}

			Set<String> regions = avalancheBulletin.getSavedRegions();
			Set<String> result = new HashSet<String>();
			for (String region : regions) {
				if (!AuthorizationUtil.hasPermissionForRegion(role, region))
					result.add(region);
			}
			avalancheBulletin.setSavedRegions(regions);

			regions = avalancheBulletin.getSuggestedRegions();
			result = new HashSet<String>();
			for (String region : regions) {
				if (!AuthorizationUtil.hasPermissionForRegion(role, region))
					result.add(region);
			}
			avalancheBulletin.setSuggestedRegions(regions);

			if (avalancheBulletin.getSavedRegions().isEmpty() && avalancheBulletin.getPublishedRegions().isEmpty())
				session.delete(avalancheBulletin);
			else {
				session.update(avalancheBulletin);
			}

			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void publishBulletins(DateTime startDate, DateTime endDate, String region) throws AlbinaException {

		// TODO publish only in regions the user has the right, save
		// recommendations for neighbours

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			List<AvalancheBulletin> bulletins = session.createQuery(HibernateUtil.queryGetBulletins)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).list();

			List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();

			// select bulletins within the region
			for (AvalancheBulletin bulletin : bulletins)
				if (bulletin.affectsRegion(region))
					results.add(bulletin);

			Set<String> result = new HashSet<String>();
			for (AvalancheBulletin bulletin : results) {
				result = new HashSet<String>();
				for (String entry : bulletin.getSavedRegions())
					if (entry.startsWith(region))
						result.add(entry);
				for (String entry : result) {
					bulletin.getSavedRegions().remove(entry);
					bulletin.getPublishedRegions().add(entry);
				}

				result = new HashSet<String>();
				for (String entry : bulletin.getSuggestedRegions())
					if (entry.startsWith(region))
						result.add(entry);
				for (String entry : result) {
					bulletin.getSuggestedRegions().remove(entry);
					bulletin.getPublishedRegions().add(entry);
				}

				bulletin.setStatus(BulletinStatus.published);
				session.update(bulletin);
			}

			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

}
