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

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.openjson.JSONArray;
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

	private static Logger logger = LoggerFactory.getLogger(RegionController.class);

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
	 * Return {@code true} if the region with {@code id} exists.
	 *
	 * @param id
	 *            the id of the desired region
	 * @return {@code true} if the region with {@code id} exists
	 */
	public boolean regionExists(String id) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			Region region = entityManager.find(Region.class, id);
			return region != null;
		});
	}

	/**
	 * Save a {@code region} to the database.
	 *
	 * @param region
	 *            the region to be saved
	 * @return the id of the saved region
	 */
	public Serializable createRegion(Region region) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(region);
			return region.getId();
		});
	}

	public Region updateRegion(Region region) throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.merge(region);
			return region;
		});
	}

	private List<Region> getActiveRegions() throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(this::getActiveRegions);
	}

	private List<Region> getActiveRegions(EntityManager entityManager) {
		return entityManager.createQuery(HibernateUtil.queryGetRegions, Region.class).getResultList();
	}

	public Region getRegion(String regionId) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
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
		return HibernateUtil.getInstance().runTransaction(this::getRegions);
	}

	public List<Region> getRegions(EntityManager entityManager) {
		return RegionController.getInstance().getActiveRegions(entityManager).stream().filter(region -> !region.getServerInstance().isExternalServer()).collect(Collectors.toList());
	}

	public List<Region> getPublishBulletinRegions() {
		try {
			List<Region> result = RegionController.getInstance().getActiveRegions().stream().filter(region -> !region.getServerInstance().isExternalServer() && region.isPublishBulletins()).collect(Collectors.toList());
			return result;
		} catch (AlbinaException ae) {
			logger.warn("Active region ids could not be loaded!", ae);
			return new ArrayList<Region>();
		}
	}

	public List<Region> getPublishBlogRegions() {
		try {
			return RegionController.getInstance().getActiveRegions().stream().filter(region -> !region.getServerInstance().isExternalServer() && region.isPublishBlogs()).collect(Collectors.toList());
		} catch (AlbinaException ae) {
			logger.warn("Active regions could not be loaded!", ae);
			return new ArrayList<Region>();
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

	public JSONArray getRegionsJson() throws AlbinaException {
		List<String> regions = getActiveRegions().stream().filter(region -> !region.getServerInstance().isExternalServer()).map(Region::getId).collect(Collectors.toList());
		return new JSONArray(regions);
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
