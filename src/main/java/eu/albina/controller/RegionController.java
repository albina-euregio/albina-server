/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.RegionLock;
import eu.albina.model.Regions;
import eu.albina.rest.websocket.RegionEndpoint;
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
	private final List<RegionLock> regionLocks;

	/**
	 * Private constructor.
	 */
	private RegionController() {
		regionLocks = new ArrayList<RegionLock>();
	}

	/**
	 * Returns the {@code RegionController} object associated with the current Java
	 * application.
	 *
	 * @return the {@code RegionController} object associated with the current Java
	 *         application.
	 */
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
	 *            the ID of the desired region
	 * @return the region with the given ID.
	 * @throws AlbinaException
	 *             if the {@code Region} object could not be initialized
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

	/**
	 * Return all top-level regions (no parent region is available).
	 *
	 * @return all top-level regions
	 * @throws AlbinaException
	 *             if the {@code Region} objects could not be initialized
	 */
	@Nonnull
	public Regions getRegions() throws AlbinaException {
		return getRegions(null);
	}

	/**
	 * Return all sub-regions that have {@code regionId} as parent.
	 *
	 * @param regionId
	 *            the id of the parent region of all desired regions
	 * @return all sub-regions that have {@code regionId} as parent
	 * @throws AlbinaException
	 *             if the {@code Region} objects could not be initialized
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public Regions getRegions(String regionId) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			List<Region> regions = null;
			if (regionId == null || regionId.isEmpty())
				regions = entityManager.createQuery(HibernateUtil.queryGetTopLevelRegions).getResultList();
			else
				regions = entityManager.createQuery(HibernateUtil.queryGetSubregions).setParameter("regionId", regionId)
						.getResultList();
			for (Region region : regions) {
				Hibernate.initialize(region.getSubregions());
			}
			transaction.commit();
			return new Regions(regions);
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Lock a specific region due to current modification.
	 *
	 * @param lock
	 *            the bulletin lock
	 * @throws AlbinaException
	 *             if the region was already locked
	 */
	public void lockRegion(RegionLock lock) throws AlbinaException {
		for (RegionLock regionLock : regionLocks) {
			if (regionLock.getDate().toInstant().toEpochMilli() == lock.getDate().toInstant().toEpochMilli()
					&& regionLock.getRegion().equals(lock.getRegion()))
				throw new AlbinaException("Region already locked!");
		}
		regionLocks.add(lock);
	}

	/**
	 * Unlock a specific region.
	 *
	 * @param lock
	 *            the bulletin lock
	 * @throws AlbinaException
	 *             if the region was not locked
	 */
	public void unlockRegion(RegionLock lock) throws AlbinaException {
		RegionLock hit = null;
		for (RegionLock regionLock : regionLocks)
			if ((regionLock.getDate().toInstant().toEpochMilli() == lock.getDate().toInstant().toEpochMilli())
					&& (regionLock.getRegion().equals(lock.getRegion())))
				hit = regionLock;

		if (hit != null)
			regionLocks.remove(hit);
		else
			throw new AlbinaException("Region not locked!");
	}

	/**
	 * Unlock all regions locked by a specific {@code sessionId}.
	 *
	 * @param sessionId
	 *            the session id
	 */
	public void unlockRegions(String sessionId) {
		List<RegionLock> hits = new ArrayList<RegionLock>();
		for (RegionLock regionLock : regionLocks) {
			if (regionLock.getSessionId() == sessionId)
				hits.add(regionLock);
		}
		for (RegionLock regionLock : hits) {
			regionLocks.remove(regionLock);
			regionLock.setLock(false);
			RegionEndpoint.broadcast(regionLock);
		}
	}

	/**
	 * Return all dates that are locked for {@code region}.
	 *
	 * @param region
	 *            the region of interest
	 * @return all dates that are locked for {@code region}
	 */
	public List<Instant> getLockedRegions(String region) {
		List<Instant> result = new ArrayList<Instant>();
		for (RegionLock regionLock : regionLocks) {
			if (regionLock.getRegion().equals(region))
				result.add(regionLock.getDate().toInstant());
		}
		return result;
	}
}
