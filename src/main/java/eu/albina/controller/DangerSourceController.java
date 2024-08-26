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
import java.util.List;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.DangerSource;
import eu.albina.util.HibernateUtil;
import jakarta.persistence.EntityManager;

/**
 * Controller for danger sources.
 *
 * @author Norbert Lanzanasto
 *
 */
public class DangerSourceController {

	private static Logger logger = LoggerFactory.getLogger(DangerSourceController.class);

	private static DangerSourceController instance = null;

	/**
	 * Private constructor.
	 */
	private DangerSourceController() {
	}

	/**
	 * Returns the {@code DangerSourceController} object associated with the
	 * current Java application.
	 *
	 * @return the {@code DangerSourceController} object associated with the
	 *         current Java application.
	 */
	public static DangerSourceController getInstance() {
		if (instance == null) {
			instance = new DangerSourceController();
		}
		return instance;
	}

	/**
	 * Retrieve a danger source from the database by {@code dangerSourceID}.
	 *
	 * @param dangerSourceId
	 *            The ID of the desired danger source.
	 * @return The danger source with the given ID.
	 */
	public DangerSource getDangerSource(String dangerSourceId) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
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
	public List<DangerSource> getDangerSources(Instant startDate, Instant endDate) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<DangerSource> dangerSources = entityManager.createQuery(HibernateUtil.queryGetDangerSources, DangerSource.class).setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();
			return dangerSources;
		});
	}

	public List<DangerSource> getAllDangerSources() {
		return HibernateUtil.getInstance().runTransaction(entityManager -> getAllDangerSources(entityManager));
	}

	private List<DangerSource> getAllDangerSources(EntityManager entityManager) {
		final List<DangerSource> dangerSources = entityManager
			.createQuery(HibernateUtil.queryGetDangerSources, DangerSource.class)
			.getResultList();
		return dangerSources;
	}
}
