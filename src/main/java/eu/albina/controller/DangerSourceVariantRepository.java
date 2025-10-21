// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import com.google.common.collect.Range;
import eu.albina.model.DangerSourceVariant;
import eu.albina.model.DangerSourceVariantsStatus;
import eu.albina.model.Region;
import eu.albina.model.enumerations.DangerSourceVariantType;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface DangerSourceVariantRepository extends CrudRepository<DangerSourceVariant, String> {

	Logger logger = LoggerFactory.getLogger(DangerSourceVariantRepository.class);

	List<DangerSourceVariant> findByCreationDateBetween(Instant startDate, Instant endDate);

	List<DangerSourceVariant> findByCreationDateBetweenAndOwnerRegion(Instant startDate, Instant endDate, String region);


	/**
	 * Creates a {@code variant} in the database.
	 *
	 * @param startDate the start date the variant is valid from
	 * @param endDate   the end date the variant is valid until
	 * @param region    the active region of the user who is creating the variant
	 */
	default void createDangerSourceVariant(DangerSourceVariant newVariant, Instant startDate,
										   Instant endDate,
										   Region region) {
		List<DangerSourceVariant> loadedVariants = findByCreationDateBetween(startDate, endDate);
		this.removeDuplicateRegions(newVariant, loadedVariants);
		// Variant has to be created
		newVariant.setId(null);
		save(newVariant);
		logger.info("Danger source variant {} for region {} created", newVariant.getId(), region.getId());
	}

	default void saveDangerSourceVariants(List<DangerSourceVariant> newVariants, Instant startDate, Instant endDate,
										  Region region) {
		List<DangerSourceVariant> loadedVariants = findByCreationDateBetween(startDate, endDate);
		Map<String, DangerSourceVariant> originalVariants = new HashMap<>();

		for (DangerSourceVariant loadedVariant : loadedVariants)
			originalVariants.put(loadedVariant.getId(), loadedVariant);

		List<String> ids = new ArrayList<>();
		for (DangerSourceVariant newVariant : newVariants) {

			ids.add(newVariant.getId());

			if (!originalVariants.containsKey(newVariant.getId())) {
				newVariant.setId(null);
				save(newVariant);
			} else {
				update(newVariant);
			}
		}

		// Delete obsolete variants
		for (DangerSourceVariant variant : originalVariants.values()) {

			// variant has to be removed
			if (variant.affectsRegion(region) && !ids.contains(variant.getId())
				&& variant.getOwnerRegion().startsWith(region.getId())) {
				delete(variant);
			}
		}
	}

	/**
	 * Update a {@code variant} in the database.
	 *
	 * @param startDate the start date the variant is valid from
	 * @param endDate   the end date the variant is valid until
	 * @param region    the active region of the user who is updating the variant
	 */
	default void updateDangerSourceVariant(DangerSourceVariant updatedVariant, Instant startDate, Instant endDate,
										   Region region) {
		List<DangerSourceVariant> loadedVariants = findByCreationDateBetween(startDate, endDate);
		removeDuplicateRegions(updatedVariant, loadedVariants);
		update(updatedVariant);
		logger.info("Danger source variant {} for region {} updated", updatedVariant.getId(), region.getId());
	}

	/**
	 * Returns the most recent variants for a given time period and
	 * {@code regions}.
	 *
	 * @param startDate the start date the variants should be valid from
	 * @param endDate   the end date the variants should be valid until
	 * @param region    the region of the variants
	 * @return the most recent variants for the given time period and regions
	 */
	default List<DangerSourceVariant> getDangerSourceVariants(Instant startDate, Instant endDate, Region region) {
		return findByCreationDateBetween(startDate, endDate).stream()
			.filter(variant -> variant.affectsRegion(region))
			.toList();
	}

	/**
	 * Returns the status for a given time period and
	 * {@code region}.
	 *
	 * @param startDate the start date the variants should be valid from
	 * @param endDate   the end date the variants should be valid until
	 * @param region    the region of the variants
	 * @return the most recent variants for the given time period and regions
	 */
	default List<DangerSourceVariantsStatus> getDangerSourceVariantsStatus(Range<Instant> startDate,
																		   Range<Instant> endDate, Region region) {
		List<DangerSourceVariantsStatus> result = new ArrayList<>();
		Instant date = startDate.lowerEndpoint();
		while (date.isBefore(endDate.lowerEndpoint()) || date.equals(endDate.lowerEndpoint())) {
			result.add(this.getDangerSourceVariantsStatusForDay(date, date.plus(1, ChronoUnit.DAYS), region));
			date = date.plus(1, ChronoUnit.DAYS);
		}
		return result;
	}

	default DangerSourceVariantsStatus getDangerSourceVariantsStatusForDay(Instant startDate, Instant endDate,
																		   Region region) {
		List<DangerSourceVariant> dangerSourceVariants = this.getDangerSourceVariants(startDate, endDate, region);
		DangerSourceVariantsStatus status = new DangerSourceVariantsStatus();
		status.date = startDate;
		status.forecast = dangerSourceVariants.stream().anyMatch(variant -> variant.getDangerSourceVariantType() == DangerSourceVariantType.forecast);
		status.analysis = dangerSourceVariants.stream().anyMatch(variant -> variant.getDangerSourceVariantType() == DangerSourceVariantType.analysis);
		return status;
	}

	/**
	 * Returns the most recent variants for a given time period and
	 * {@code regions} and {@code dangerSource}.
	 *
	 * @param startDate      the start date the variants should be valid from
	 * @param endDate        the end date the variants should be valid until
	 * @param regions        the regions of the variants
	 * @param dangerSourceId the id of the danger source
	 * @return the most recent variants for the given time period and regions
	 */
	default List<DangerSourceVariant> getDangerSourceVariants(Instant startDate, Instant endDate, List<Region> regions,
															  String dangerSourceId) {
		return findByCreationDateBetween(startDate, endDate).stream()
			.filter(variant -> regions.stream().anyMatch(variant::affectsRegion))
			.filter(variant -> variant.getDangerSource().getId().equals(dangerSourceId))
			.toList();
	}

	private void removeDuplicateRegions(DangerSourceVariant updatedVariant, List<DangerSourceVariant> loadedVariants) {
		for (DangerSourceVariant loadedVariant : loadedVariants) {
			if (!loadedVariant.getId().equals(updatedVariant.getId()) &&
				loadedVariant.getDangerSource().getId().equals(updatedVariant.getDangerSource().getId()) &&
				loadedVariant.getDangerSourceVariantType().equals(updatedVariant.getDangerSourceVariantType())) {
				// check micro-regions to prevent duplicates
				for (String microRegion : updatedVariant.getRegions()) {
					loadedVariant.getRegions().remove(microRegion);
				}
				save(loadedVariant);
			}
		}
	}
}
