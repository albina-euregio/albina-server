// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import com.google.common.collect.Range;
import eu.albina.model.DangerSourceVariant;
import eu.albina.model.DangerSourceVariantsStatus;
import eu.albina.model.Region;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Join(value = "dangerSource", type = Join.Type.FETCH)
//	@Join(value = "regions", type = Join.Type.FETCH)
//	@Join(value = "aspects", type = Join.Type.FETCH)
//	@Join(value = "weakLayerGrainShapes", type = Join.Type.FETCH)
//	@Join(value = "terrainTypes", type = Join.Type.FETCH)
public interface DangerSourceVariantRepository extends CrudRepository<DangerSourceVariant, String> {

	Logger logger = LoggerFactory.getLogger(DangerSourceVariantRepository.class);

	List<DangerSourceVariant> findByValidFrom(Instant validFrom);

	List<DangerSourceVariant> findByValidFromBetween(Instant startDate, Instant endDate);

	/**
	 * Creates a {@code variant} in the database.
	 *
	 * @param validFrom the date range the variant is valid from
	 * @param region    the active region of the user who is creating the variant
	 */
	default void createDangerSourceVariant(DangerSourceVariant newVariant, Instant validFrom,
										   Region region) {
		List<DangerSourceVariant> loadedVariants = findByValidFrom(validFrom);
		this.removeDuplicateRegions(newVariant, loadedVariants);
		// Variant has to be created
		newVariant.setId(null);
		save(newVariant);
		logger.info("Danger source variant {} for region {} created", newVariant.getId(), region.getId());
	}

	default void saveDangerSourceVariants(List<DangerSourceVariant> newVariants, Instant validFrom,
										  Region region) {
		List<DangerSourceVariant> loadedVariants = findByValidFrom(validFrom);
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
	 * @param validFrom the date range the variant is valid from
	 * @param region    the active region of the user who is updating the variant
	 */
	default void updateDangerSourceVariant(DangerSourceVariant updatedVariant, Instant validFrom,
										   Region region) {
		List<DangerSourceVariant> loadedVariants = findByValidFrom(validFrom);
		removeDuplicateRegions(updatedVariant, loadedVariants);
		update(updatedVariant);
		logger.info("Danger source variant {} for region {} updated", updatedVariant.getId(), region.getId());
	}

	/**
	 * Returns the status for a given time period and
	 * {@code region}.
	 *
	 * @param dateRange the date range the variants should be valid from
	 * @param region    the region of the variants
	 * @return the most recent variants for the given time period and regions
	 */
	default List<DangerSourceVariantsStatus> getDangerSourceVariantsStatus(Range<Instant> dateRange,
																		   Region region) {
		return findByValidFromBetween(dateRange.lowerEndpoint(), dateRange.upperEndpoint()).stream()
			.filter(variant -> variant.affectsRegion(region))
			.collect(Collectors.groupingBy(DangerSourceVariant::getValidFrom))
			.entrySet().stream()
			.map(entry -> DangerSourceVariantsStatus.of(entry.getKey(), entry.getValue()))
			.toList();
	}

	/**
	 * Returns the most recent variants for a given time period and
	 * {@code regions} and {@code dangerSource}.
	 *
	 * @param validFrom      the date range the variants should be valid from
	 * @param regions        the regions of the variants
	 * @param dangerSourceId the id of the danger source
	 * @return the most recent variants for the given time period and regions
	 */
	default List<DangerSourceVariant> getDangerSourceVariants(Instant validFrom, List<Region> regions,
															  String dangerSourceId) {
		return findByValidFrom(validFrom).stream()
			.filter(variant -> regions.stream().anyMatch(variant::affectsRegion))
			.filter(variant -> variant.getDangerSource().getId().equals(dangerSourceId))
			.toList();
	}

	private void removeDuplicateRegions(DangerSourceVariant updatedVariant, List<DangerSourceVariant> loadedVariants) {
		for (DangerSourceVariant loadedVariant : loadedVariants) {
			if (loadedVariant.getId().equals(updatedVariant.getId()) ||
				!loadedVariant.getDangerSource().getId().equals(updatedVariant.getDangerSource().getId()) ||
				!loadedVariant.getDangerSourceVariantType().equals(updatedVariant.getDangerSourceVariantType())) {
				continue;
			}
			loadedVariant.getRegions().removeAll(updatedVariant.getRegions());
			save(loadedVariant);
		}
	}
}
