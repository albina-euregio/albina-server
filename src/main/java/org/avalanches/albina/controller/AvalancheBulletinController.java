package org.avalanches.albina.controller;

import java.io.Serializable;
import java.util.List;

import org.avalanches.albina.exception.AvalancheInformationSystemException;
import org.avalanches.albina.model.AvalancheBulletin;
import org.avalanches.albina.model.enumerations.CountryCode;
import org.avalanches.albina.util.GlobalVariables;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

/**
 * Controller for snow profiles.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class AvalancheBulletinController extends AvalancheInformationSystemController {

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
	 * @throws AvalancheInformationSystemException
	 */
	public AvalancheBulletin getBulletin(String bulletinId) throws AvalancheInformationSystemException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			AvalancheBulletin bulletin = session.get(AvalancheBulletin.class, bulletinId);
			if (bulletin == null) {
				transaction.rollback();
				throw new AvalancheInformationSystemException("No bulletin with ID: " + bulletinId);
			}
			Hibernate.initialize(bulletin.getActivityComment());
			Hibernate.initialize(bulletin.getActivityHighlight());
			Hibernate.initialize(bulletin.getAvalancheSituationComment());
			Hibernate.initialize(bulletin.getAvalancheSituationHighlight());
			Hibernate.initialize(bulletin.getDaytimeBulletins());
			Hibernate.initialize(bulletin.getSnowpackStructureComment());
			Hibernate.initialize(bulletin.getSnowpackStructureHighlight());
			Hibernate.initialize(bulletin.getSynopsisComment());
			Hibernate.initialize(bulletin.getSynopsisHighlight());
			Hibernate.initialize(bulletin.getTendencyComment());
			Hibernate.initialize(bulletin.getTravelAdvisoryComment());
			Hibernate.initialize(bulletin.getTravelAdvisoryHighlight());
			transaction.commit();
			return bulletin;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AvalancheInformationSystemException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public Serializable saveBulletin(AvalancheBulletin bulletin) throws AvalancheInformationSystemException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Serializable bulletinId = session.save(bulletin);
			transaction.commit();
			return bulletinId;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AvalancheInformationSystemException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public void updateBulletin(AvalancheBulletin bulletin) throws AvalancheInformationSystemException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.update(bulletin);
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AvalancheInformationSystemException(he.getMessage());
		} finally {
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<AvalancheBulletin> getBulletin(int page, CountryCode country, String region, DateTime startDate,
			DateTime endDate) throws AvalancheInformationSystemException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(AvalancheBulletin.class)
					.setFirstResult((page - 1) * GlobalVariables.paginationCount)
					.setFetchSize(GlobalVariables.paginationCount);

			if (country != null) {
				criteria.add(Restrictions.eq("country", country));
			}
			if (region != null && !region.isEmpty()) {
				criteria.add(Restrictions.eq("region", region).ignoreCase());
			}
			if (startDate != null) {
				criteria.add(Restrictions.ge("validFrom", startDate));
			}
			if (endDate != null) {
				criteria.add(Restrictions.le("validUntil", endDate));
			}

			List<AvalancheBulletin> bulletins = criteria.list();

			for (AvalancheBulletin bulletin : bulletins) {
				Hibernate.initialize(bulletin.getActivityComment());
				Hibernate.initialize(bulletin.getActivityHighlight());
				Hibernate.initialize(bulletin.getAvalancheSituationComment());
				Hibernate.initialize(bulletin.getAvalancheSituationHighlight());
				Hibernate.initialize(bulletin.getDaytimeBulletins());
				Hibernate.initialize(bulletin.getSnowpackStructureComment());
				Hibernate.initialize(bulletin.getSnowpackStructureHighlight());
				Hibernate.initialize(bulletin.getSynopsisComment());
				Hibernate.initialize(bulletin.getSynopsisHighlight());
				Hibernate.initialize(bulletin.getTendencyComment());
				Hibernate.initialize(bulletin.getTravelAdvisoryComment());
				Hibernate.initialize(bulletin.getTravelAdvisoryHighlight());
			}

			transaction.commit();
			return bulletins;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AvalancheInformationSystemException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public void deleteBulletin(String bulletinId) throws AvalancheInformationSystemException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			AvalancheBulletin avalancheBulletin = session.get(AvalancheBulletin.class, bulletinId);
			if (avalancheBulletin == null) {
				transaction.rollback();
				throw new AvalancheInformationSystemException("No bulletin with ID: " + bulletinId);
			}
			session.delete(avalancheBulletin);
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AvalancheInformationSystemException(he.getMessage());
		} finally {
			session.close();
		}
	}

}
