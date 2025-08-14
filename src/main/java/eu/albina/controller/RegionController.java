// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.RegionLock;
import eu.albina.rest.websocket.RegionEndpoint;
import eu.albina.util.HibernateUtil;

/**
 * Controller for regions.
 *
 * @author Norbert Lanzanasto
 *
 */
public class RegionController {

	private static final Logger logger = LoggerFactory.getLogger(RegionController.class);

	private static RegionController instance = null;
	private final List<RegionLock> regionLocks = new ArrayList<>();

	/**
	 * Private constructor.
	 */
	private RegionController() {
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
	 * Return {@code true} if the region with {@code id} exists.
	 *
	 * @param id
	 *            the id of the desired region
	 * @return {@code true} if the region with {@code id} exists
	 */
	public boolean regionExists(String id) {
		return HibernateUtil.getInstance().run(entityManager -> entityManager.find(Region.class, id) != null);
	}

	/**
	 * Save a {@code region} to the database.
	 *
	 * @param region the region to be saved
	 */
	public void createRegion(Region region) {
		HibernateUtil.getInstance().runTransaction(entityManager -> entityManager.persist(region), () -> null);
	}

	public void updateRegion(Region region) {
		HibernateUtil.getInstance().runTransaction(entityManager -> entityManager.merge(region));
	}

	private List<Region> getActiveRegions() {
		return HibernateUtil.getInstance().run(this::getActiveRegions);
	}

	private List<Region> getActiveRegions(EntityManager entityManager) {
		return entityManager.createQuery("from Region as r", Region.class).getResultList();
	}

	public Region getRegion(String regionId) {
		return HibernateUtil.getInstance().run(entityManager -> {
			Region region = entityManager.find(Region.class, regionId);
			if (region == null) {
				throw new HibernateException("No region with ID: " + regionId);
			}
			return region;
		});
    }

	public Region getRegionOrThrowAlbinaException(String regionId) throws AlbinaException {
		if (regionId == null) {
			throw new AlbinaException("No region defined!");
		}
		try {
			Region region = getRegion(regionId);
			if (region == null) {
				throw new AlbinaException("No region with id " + regionId + " found!");
			}
			return region;
		} catch (HibernateException e) {
			throw new AlbinaException("No region with id " + regionId + " found!");
		}
	}

	public List<Region> getRegions() {
		return HibernateUtil.getInstance().run(this::getRegions);
	}

	public List<Region> getRegions(EntityManager entityManager) {
		return getActiveRegions(entityManager).stream().filter(region -> !region.getServerInstance().isExternalServer()).collect(Collectors.toList());
	}

	public List<Region> getPublishBulletinRegions() {
		return getActiveRegions().stream().filter(region -> !region.getServerInstance().isExternalServer() && region.isPublishBulletins()).collect(Collectors.toList());
	}

	public List<Region> getPublishBlogRegions() {
		return getActiveRegions().stream().filter(region -> !region.getServerInstance().isExternalServer() && region.isPublishBlogs()).collect(Collectors.toList());
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
		List<RegionLock> hits = regionLocks.stream()
			.filter(regionLock -> Objects.equals(regionLock.getSessionId(), sessionId))
			.collect(Collectors.toList());
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
		return regionLocks.stream()
			.filter(regionLock -> regionLock.getRegion().equals(region))
			.map(regionLock -> regionLock.getDate().toInstant())
			.collect(Collectors.toList());
	}

	public List<Region> getRegionsOrBulletinRegions(List<String> regionIds) {
		if (regionIds.isEmpty()) {
			return getPublishBulletinRegions();
		}

		return regionIds.stream().map(regionId -> {
			try {
				return getRegion(regionId);
			} catch (HibernateException e) {
				logger.warn("No region with ID: " + regionId);
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

}
