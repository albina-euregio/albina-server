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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import eu.albina.model.DangerSourceVariant;
import eu.albina.model.DangerSourceVariantsStatus;
import eu.albina.model.Region;
import eu.albina.model.enumerations.DangerSourceVariantType;
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
	 * Retrieve a variant from the database by {@code variantID}.
	 *
	 * @param variantId
	 *            The ID of the desired variant.
	 * @return The variant with the given ID.
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
	public synchronized void createDangerSourceVariant(DangerSourceVariant newVariant, Instant startDate, Instant endDate,
			Region region) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<DangerSourceVariant> loadedVariants = entityManager.createQuery(HibernateUtil.queryGetDangerSourceVariants, DangerSourceVariant.class)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

			this.removeDuplicateRegions(newVariant, entityManager, loadedVariants);

			// Variant has to be created
			newVariant.setId(null);
			entityManager.merge(newVariant);

			logger.info("Danger source variant {} for region {} created", newVariant.getId(), region.getId());

			return null;
		});
	}

	/**
	 * Deletes a {@code variant} from the database.
	 *
	 * @param startDate
	 *            the start date the variant is valid from
	 * @param endDate
	 *            the end date the variant is valid until
	 * @param region
	 *            the active region of the user who is deleting the variant
	 * @return a list of all variants for this day
	 */
    public synchronized void deleteDangerSourceVariant(String variantId) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			DangerSourceVariant variant = entityManager.find(DangerSourceVariant.class, variantId);
			entityManager.remove(variant);
			return null;
		});
	}

	public void saveDangerSourceVariants(List<DangerSourceVariant> newVariants, Instant startDate, Instant endDate, Region region) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<DangerSourceVariant> loadedVariants = entityManager.createQuery(HibernateUtil.queryGetDangerSourceVariants, DangerSourceVariant.class)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();
			Map<String, DangerSourceVariant> originalVariants = new HashMap<String, DangerSourceVariant>();

			for (DangerSourceVariant loadedVariant : loadedVariants)
				originalVariants.put(loadedVariant.getId(), loadedVariant);

			List<String> ids = new ArrayList<String>();
			for (DangerSourceVariant newVariant : newVariants) {

				ids.add(newVariant.getId());

				if (!originalVariants.containsKey(newVariant.getId())) {
					newVariant.setId(null);
				}
				entityManager.merge(newVariant);
			}

			// Delete obsolete variants
			for (DangerSourceVariant variant : originalVariants.values()) {

				// variant has to be removed
				if (variant.affectsRegion(region) && !ids.contains(variant.getId())
						&& variant.getOwnerRegion().startsWith(region.getId())) {
					entityManager.remove(variant);
				}
			}

			return null;
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
	public void updateDangerSourceVariant(DangerSourceVariant updatedVariant, Instant startDate, Instant endDate,
			Region region) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<DangerSourceVariant> loadedVariants = entityManager.createQuery(HibernateUtil.queryGetDangerSourceVariants, DangerSourceVariant.class)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();
			removeDuplicateRegions(updatedVariant, entityManager, loadedVariants);
			entityManager.merge(updatedVariant);

			logger.info("Danger source variant {} for region {} updated", updatedVariant.getId(), region.getId());

			return null;
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
	 * Returns the status for a given time period and
	 * {@code region}.
	 *
	 * @param startDate
	 *            the start date the variants should be valid from
	 * @param endDate
	 *            the end date the variants should be valid until
	 * @param region
	 *            the region of the variants
	 * @return the most recent variants for the given time period and regions
	 */
	public List<DangerSourceVariantsStatus> getDangerSourceVariantsStatus(Range<Instant> startDate, Range<Instant> endDate, Region region) {
		List<DangerSourceVariantsStatus> result = new ArrayList<DangerSourceVariantsStatus>();
		Instant date = startDate.lowerEndpoint();
		while (date.isBefore(endDate.lowerEndpoint()) || date.equals(endDate.lowerEndpoint())) {
			result.add(this.getDangerSourceVariantsStatusForDay(date, date.plus(1, ChronoUnit.DAYS), region));
			date = date.plus(1, ChronoUnit.DAYS);
		}
		return result;
	}

	public DangerSourceVariantsStatus getDangerSourceVariantsStatusForDay(Instant startDate, Instant endDate, Region region) {
		DangerSourceVariantsStatus status = new DangerSourceVariantsStatus();
		status.date = startDate;
		ArrayList<Region> regions = new ArrayList<Region>();
		regions.add(region);
		List<DangerSourceVariant> dangerSourceVariants = this.getDangerSourceVariants(startDate, endDate, regions);
		status.forecast = dangerSourceVariants.stream().anyMatch(variant -> variant.getDangerSourceVariantType() == DangerSourceVariantType.forecast);
		status.analysis = dangerSourceVariants.stream().anyMatch(variant -> variant.getDangerSourceVariantType() == DangerSourceVariantType.analysis);
		return status;
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
			.createQuery(HibernateUtil.queryGetDangerSourceVariantsForTimePeriod, DangerSourceVariant.class)
			.setParameter("startDate", startDate)
			.setParameter("endDate", endDate)
			.getResultList();
		for (DangerSourceVariant variant : variants) {
			initializeDangerSourceVariant(variant);
		}
		return variants;
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

	private void removeDuplicateRegions(DangerSourceVariant updatedVariant, EntityManager entityManager,
			List<DangerSourceVariant> loadedVariants) {
		for (DangerSourceVariant loadedVariant : loadedVariants) {
			if (
				!loadedVariant.getId().equals(updatedVariant.getId()) &&
				loadedVariant.getDangerSource().getId().equals(updatedVariant.getDangerSource().getId()) &&
				loadedVariant.getDangerSourceVariantType().equals(updatedVariant.getDangerSourceVariantType()))
			{
				// check micro-regions to prevent duplicates
				for (String microRegion : updatedVariant.getRegions()) {
					loadedVariant.getRegions().remove(microRegion);
				}
				entityManager.merge(loadedVariant);
			}
		}
	}
}
