package eu.albina.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.websocket.EncodeException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.RegionLock;
import eu.albina.rest.RegionEndpoint;
import eu.albina.util.HibernateUtil;

/**
 * Controller for regions.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class RegionController {

	private static Logger logger = LoggerFactory.getLogger(RegionController.class);

	private static RegionController instance = null;
	private List<RegionLock> regionLocks;

	private RegionController() {
		regionLocks = new ArrayList<RegionLock>();
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
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			Region region = entityManager.find(Region.class, regionId);
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
			entityManager.close();
		}
	}

	public List<Region> getRegions() throws AlbinaException {
		return getRegions(null);
	}

	@SuppressWarnings("unchecked")
	public List<Region> getRegions(String regionId) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			List<Region> regions = null;
			if (regionId == null || regionId == "")
				regions = entityManager.createQuery(HibernateUtil.queryGetTopLevelRegions).getResultList();
			else
				regions = entityManager.createQuery(HibernateUtil.queryGetSubregions).setParameter("regionId", regionId)
						.getResultList();
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
			entityManager.close();
		}
	}

	public void lockRegion(RegionLock lock) throws AlbinaException {
		for (RegionLock regionLock : regionLocks) {
			if (regionLock.getDate().getMillis() == lock.getDate().getMillis()
					&& regionLock.getRegion().equals(lock.getRegion()))
				throw new AlbinaException("Region already locked!");
		}
		regionLocks.add(lock);
	}

	public void unlockRegion(RegionLock lock) throws AlbinaException {
		RegionLock hit = null;
		for (RegionLock regionLock : regionLocks)
			if ((regionLock.getDate().getMillis() == lock.getDate().getMillis())
					&& (regionLock.getRegion().equals(lock.getRegion())))
				hit = regionLock;

		if (hit != null)
			regionLocks.remove(hit);
		else
			throw new AlbinaException("Region not locked!");
	}

	public void unlockRegions(String sessionId) {
		List<RegionLock> hits = new ArrayList<RegionLock>();
		for (RegionLock regionLock : regionLocks) {
			if (regionLock.getSessionId() == sessionId)
				hits.add(regionLock);
		}
		for (RegionLock regionLock : hits) {
			regionLocks.remove(regionLock);
			regionLock.setLock(false);
			try {
				RegionEndpoint.broadcast(regionLock);
			} catch (IOException | EncodeException e) {
				logger.error("Error broadcasting region unlock: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public List<DateTime> getLockedRegions(String region) {
		List<DateTime> result = new ArrayList<DateTime>();
		for (RegionLock regionLock : regionLocks) {
			if (regionLock.getRegion().equals(region))
				result.add(regionLock.getDate());
		}
		return result;
	}
}
