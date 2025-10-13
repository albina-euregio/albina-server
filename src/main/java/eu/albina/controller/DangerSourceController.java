// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.time.Instant;
import java.util.List;

import jakarta.inject.Singleton;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.DangerSource;
import eu.albina.util.HibernateUtil;

/**
 * Controller for danger sources.
 *
 * @author Norbert Lanzanasto
 *
 */
@Singleton
public class DangerSourceController {

	private static final Logger logger = LoggerFactory.getLogger(DangerSourceController.class);

	/**
	 * Retrieve a danger source from the database by {@code dangerSourceID}.
	 *
	 * @param dangerSourceId
	 *            The ID of the desired danger source.
	 * @return The danger source with the given ID.
	 */
	public DangerSource getDangerSource(String dangerSourceId) {
		return HibernateUtil.getInstance().run(entityManager -> {
			DangerSource dangerSource = entityManager.find(DangerSource.class, dangerSourceId);
			if (dangerSource == null) {
				throw new HibernateException("No danger source with ID: " + dangerSourceId);
			}
			return dangerSource;
		});
	}

	/**
	 * Creates a {@code danger source} in the database.
	 *
	 * @return a map of all danger source ids
	 */
	public synchronized List<DangerSource> createDangerSource(DangerSource newDangerSource) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {

			newDangerSource.setId(null);
			entityManager.persist(newDangerSource);
			List<DangerSource> loadedDangerSources = entityManager.createQuery(HibernateUtil.queryGetDangerSources, DangerSource.class).getResultList();

			logger.info("Danger source {} created", newDangerSource.getId());

			return loadedDangerSources;
		});
	}

	/**
	 * Update a {@code danger source} in the database.
	 *
	 * @return a map of all danger sources ids
	 */
	public void updateDangerSource(DangerSource updatedDangerSource) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.merge(updatedDangerSource);
			logger.info("Danger source {} updated", updatedDangerSource.getId());
			return null;
		});
	}

	/**
	 * Returns {@code danger sources}.
	 *
	 * @return danger sources
	 */
	public List<DangerSource> getDangerSourcesForRegion(Instant startDate, Instant endDate, String region) {
		return HibernateUtil.getInstance().run(entityManager -> entityManager.createQuery(HibernateUtil.queryGetDangerSourcesForRegion, DangerSource.class).setParameter("startDate", startDate).setParameter("endDate", endDate).setParameter("region", region).getResultList());
	}

	public List<DangerSource> getAllDangerSources() {
		return HibernateUtil.getInstance().run(entityManager -> entityManager.createQuery(HibernateUtil.queryGetDangerSources, DangerSource.class).getResultList());
	}

}
