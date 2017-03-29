package org.avalanches.albina.controller;

import java.util.List;

import org.avalanches.albina.exception.AlbinaException;
import org.avalanches.albina.model.Region;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for regions.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class RegionController extends AlbinaController {

	private static Logger logger = LoggerFactory.getLogger(RegionController.class);

	private static RegionController instance = null;

	private RegionController() {
	}

	public static RegionController getInstance() {
		if (instance == null) {
			instance = new RegionController();
		}
		return instance;
	}

	/**
	 * Retrieve a region from the database by ID.
	 * 
	 * @param regionId
	 *            The ID of the desired region.
	 * @return The region with the given ID.
	 * @throws AlbinaException
	 */
	public Region getRegion(String regionId) throws AlbinaException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Region region = session.get(Region.class, regionId);
			if (region == null) {
				transaction.rollback();
				throw new AlbinaException("No region with ID: " + regionId);
			}
			Hibernate.initialize(region.getSubregions());
			transaction.commit();
			return region;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Region> getRegions(String regionId) throws AlbinaException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(Region.class);

			if (regionId == null || regionId == "") {
				criteria.add(Restrictions.isNull("parentRegion"));
			} else {
				criteria.add(Restrictions.eq("parentRegion.id", regionId));
			}

			List<Region> regions = criteria.list();

			for (Region region : regions) {
				Hibernate.initialize(region.getSubregions());
			}

			transaction.commit();
			return regions;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}
}
