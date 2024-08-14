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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.DangerSourceVariant;
import eu.albina.model.Region;
import eu.albina.util.HibernateUtil;
import jakarta.persistence.EntityManager;

/**
 * Controller for danger sources variants.
 *
 * @author Norbert Lanzanasto
 *
 */
public class DangerSourceVariantController {

	private static Logger logger = LoggerFactory.getLogger(DangerSourceVariantController.class);

	private static DangerSourceVariantController instance = null;

	/**
	 * Private constructor.
	 */
	private DangerSourceVariantController() {
	}

	/**
	 * Returns the {@code DangerSourceController} object associated with the
	 * current Java application.
	 *
	 * @return the {@code DangerSourceController} object associated with the
	 *         current Java application.
	 */
	public static DangerSourceVariantController getInstance() {
		if (instance == null) {
			instance = new DangerSourceVariantController();
		}
		return instance;
	}

	/**
	 * Retrieve an avalanche bulletin from the database by {@code bulletinID}.
	 *
	 * @param variantId
	 *            The ID of the desired avalanche bulletin.
	 * @return The avalanche bulletin with the given ID.
	 */
	public DangerSourceVariant getDangerSourceVariant(String variantId) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			DangerSourceVariant variant = entityManager.find(DangerSourceVariant.class, variantId);
			if (variant == null) {
				throw new HibernateException("No danger source variant with ID: " + variantId);
			}
			initializeDangerSourceVariant(variant);
			return variant;
		});
	}

	/**
	 * Initialize all fields of the {@code danger source variant} to be able to access it after
	 * the DB transaction was closed.
	 *
	 * @param variant
	 *            the danger source variant that should be initialized
	 */
	private void initializeDangerSourceVariant(DangerSourceVariant variant) {
		Hibernate.initialize(variant.getDangerSource());
		Hibernate.initialize(variant.getAspects());
		Hibernate.initialize(variant.getRegions());
		Hibernate.initialize(variant.getDangerSigns());
		Hibernate.initialize(variant.getTerrainTypes());
	}

	/**
	 * Creates a {@code variant} in the database.
	 *
	 * @param startDate
	 *            the start date the variant is valid from
	 * @param endDate
	 *            the end date the variant is valid until
	 * @param region
	 *            the active region of the user who is creating the variant
	 * @return a map of all variant ids and variants for this day
	 */
	public synchronized Map<String, DangerSourceVariant> createDangerSourceVariant(DangerSourceVariant newVariant, Instant startDate, Instant endDate,
			Region region) {
		Map<String, DangerSourceVariant> resultVariants = new HashMap<String, DangerSourceVariant>();

		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<DangerSourceVariant> loadedVariants = entityManager.createQuery(HibernateUtil.queryGetDangerSourceVariants, DangerSourceVariant.class)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

			for (DangerSourceVariant loadedVariant : loadedVariants) {
				if (loadedVariant.getDangerSource().equals(newVariant.getDangerSource())) {
					// check micro-regions to prevent duplicates
					for (String microRegion : newVariant.getRegions()) {
						loadedVariant.getRegions().remove(microRegion);
					}
					entityManager.merge(loadedVariant);
					resultVariants.put(loadedVariant.getId(), loadedVariant);
				}
			}

			// Variant has to be created
			newVariant.setId(null);
			entityManager.persist(newVariant);
			resultVariants.put(newVariant.getId(), newVariant);

			for (DangerSourceVariant variant : resultVariants.values()) {
				initializeDangerSourceVariant(variant);
			}

			logger.info("Danger source variant {} for region {} created", newVariant.getId(), region.getId());

			return resultVariants;
		});
	}

	/**
	 * Update a {@code variant} in the database.
	 *
	 * @param startDate
	 *            the start date the variant is valid from
	 * @param endDate
	 *            the end date the variant is valid until
	 * @param region
	 *            the active region of the user who is updating the variant
	 * @return a map of all variant ids and variants for this day
	 */
	public Map<String, DangerSourceVariant> updateDangerSourceVariant(DangerSourceVariant updatedVariant, Instant startDate, Instant endDate,
			Region region) {
		Map<String, DangerSourceVariant> resultVariants = new HashMap<String, DangerSourceVariant>();

		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<DangerSourceVariant> loadedVariants = entityManager.createQuery(HibernateUtil.queryGetDangerSourceVariants, DangerSourceVariant.class)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

			for (DangerSourceVariant loadedVariant : loadedVariants) {
				if (!loadedVariant.getId().equals(updatedVariant.getId())) {
					// check micro-regions to prevent duplicates
					for (String microRegion : updatedVariant.getRegions()) {
						loadedVariant.getRegions().remove(microRegion);
					}
					entityManager.merge(loadedVariant);
					resultVariants.put(loadedVariant.getId(), loadedVariant);
				}
			}

			// Variant has to be updated
			entityManager.merge(updatedVariant);
			resultVariants.put(updatedVariant.getId(), updatedVariant);

			for (DangerSourceVariant variant : resultVariants.values())
				initializeDangerSourceVariant(variant);

			logger.info("Danger source variant {} for region {} updated", updatedVariant.getId(), region.getId());

			return resultVariants;
		});
	}

	/**
	 * Returns the most recent variants for a given time period and
	 * {@code regions}.
	 *
	 * @param startDate
	 *            the start date the variants should be valid from
	 * @param endDate
	 *            the end date the variants should be valid until
	 * @param regions
	 *            the regions of the variants
	 * @return the most recent variants for the given time period and regions
	 */
	public List<DangerSourceVariant> getDangerSourceVariants(Instant startDate, Instant endDate, List<Region> regions) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<DangerSourceVariant> variants = entityManager.createQuery(HibernateUtil.queryGetDangerSourceVariants, DangerSourceVariant.class)
				.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();
			List<DangerSourceVariant> results = variants.stream()
				.filter(variant -> regions.stream()
					.anyMatch(variant::affectsRegion))
				.collect(Collectors.toList());

			for (DangerSourceVariant variant : results)
				initializeDangerSourceVariant(variant);

			return results;
		});
	}

	/**
	 * Returns the most recent variants for a given time period and
	 * {@code regions} and {@code dangerSource}.
	 *
	 * @param startDate
	 *            the start date the variants should be valid from
	 * @param endDate
	 *            the end date the variants should be valid until
	 * @param regions
	 *            the regions of the variants
	 * @param dangerSourceId
	 *            the id of the danger source
	 * @return the most recent variants for the given time period and regions
	 */
	public List<DangerSourceVariant> getDangerSourceVariants(Instant startDate, Instant endDate, List<Region> regions, String dangerSourceId) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<DangerSourceVariant> variants = entityManager.createQuery(HibernateUtil.queryGetDangerSourceVariants, DangerSourceVariant.class)
				.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();
			List<DangerSourceVariant> results = variants.stream()
				.filter(variant -> regions.stream()
					.anyMatch(variant::affectsRegion))
				.filter(variant -> variant.getDangerSource().getId().equals(dangerSourceId))
				.collect(Collectors.toList());

			for (DangerSourceVariant variant : results)
				initializeDangerSourceVariant(variant);

			return results;
		});
	}

	public List<DangerSourceVariant> getAllDangerSourceVariants(Instant startDate, Instant endDate) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> getAllDangerSourceVariants(startDate, endDate, entityManager));
	}

	private List<DangerSourceVariant> getAllDangerSourceVariants(Instant startDate, Instant endDate, EntityManager entityManager) {
		final List<DangerSourceVariant> variants = entityManager
			.createQuery(HibernateUtil.queryGetDangerSourceVariants, DangerSourceVariant.class)
			.setParameter("startDate", startDate)
			.setParameter("endDate", endDate)
			.getResultList();
		for (DangerSourceVariant variant : variants) {
			initializeDangerSourceVariant(variant);
		}
		return variants;
	}
}
