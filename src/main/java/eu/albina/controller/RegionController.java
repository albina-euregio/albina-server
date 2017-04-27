package eu.albina.controller;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.util.HibernateUtil;

/**
 * Controller for regions.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class RegionController {

	// private static Logger logger =
	// LoggerFactory.getLogger(RegionController.class);

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
		Session session = HibernateUtil.getSessionFactory().openSession();
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

	public List<Region> getRegions() throws AlbinaException {
		return getRegions(null);
	}

	@SuppressWarnings("unchecked")
	public List<Region> getRegions(String regionId) throws AlbinaException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(Region.class, "region");

			if (regionId == null || regionId == "") {
				criteria.add(Restrictions.isNull("parentRegion"));
			} else {
				criteria.createAlias("region.parentRegion", "parentRegion");
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
