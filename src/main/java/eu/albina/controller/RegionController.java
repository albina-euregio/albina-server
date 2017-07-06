package eu.albina.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.RegionLock;
import eu.albina.model.enumerations.EventName;
import eu.albina.util.GlobalVariables;
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
		Session session = HibernateUtil.getInstance().getSessionFactory().openSession();
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
		Session session = HibernateUtil.getInstance().getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			List<Region> regions = null;
			if (regionId == null || regionId == "")
				regions = session.createQuery(HibernateUtil.queryGetTopLevelRegions).list();
			else
				regions = session.createQuery(HibernateUtil.queryGetSubregions).setParameter("regionId", regionId)
						.list();
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

	public void lockRegion(RegionLock lock) throws AlbinaException {
		for (RegionLock regionLock : regionLocks) {
			if (regionLock.getDate().getMillis() == lock.getDate().getMillis()
					&& regionLock.getRegion().equals(lock.getRegion()))
				throw new AlbinaException("Region already locked!");
		}
		logger.info("[lock region] Region locked: " + lock.getDate().getMillis() + ", " + lock.getDate());
		regionLocks.add(lock);
	}

	public void unlockRegion(String region, DateTime date) throws AlbinaException {
		RegionLock hit = null;
		for (RegionLock regionLock : regionLocks)
			if ((regionLock.getDate().getMillis() == date.getMillis()) && (regionLock.getRegion().equals(region)))
				hit = regionLock;

		if (hit != null) {
			regionLocks.remove(hit);
			logger.info("[unlock region] Region unlocked: " + date.getMillis() + ", " + date);
		} else
			throw new AlbinaException("Region not locked!");
	}

	public void unlockRegions(UUID sessionId) {
		List<RegionLock> hits = new ArrayList<RegionLock>();
		for (RegionLock regionLock : regionLocks) {
			if (regionLock.getSessionId() == sessionId)
				hits.add(regionLock);
		}
		for (RegionLock regionLock : hits) {
			regionLocks.remove(regionLock);
			JSONObject json = new JSONObject();
			json.put("region", regionLock.getRegion());
			json.put("date", regionLock.getDate().toString(GlobalVariables.formatterDateTime));
			SocketIOController.sendEvent(EventName.unlockRegion.toString(), json.toString());
			logger.debug("[SocketIO] Region unlocked: " + regionLock.getRegion() + ", " + regionLock.getDate());
		}
	}

	public List<DateTime> getLockedRegions(String region) {
		List<DateTime> result = new ArrayList<DateTime>();
		for (RegionLock regionLock : regionLocks) {
			if (regionLock.getRegion() == region)
				result.add(regionLock.getDate());
		}
		return result;
	}
}
