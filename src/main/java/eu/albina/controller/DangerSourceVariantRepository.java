// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import com.google.common.collect.Range;
import eu.albina.model.DangerSourceVariant;
import eu.albina.model.DangerSourceVariantsStatus;
import eu.albina.model.Region;
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
public interface DangerSourceVariantRepository extends CrudRepository<DangerSourceVariant, String> {

	Logger logger = LoggerFactory.getLogger(DangerSourceVariantRepository.class);

	List<DangerSourceVariant> findByValidFrom(Instant validFrom);

	List<DangerSourceVariant> findByValidFromBetween(Instant startDate, Instant endDate);

	/**
	 * Creates or update a {@code variant} in the database.
	 *
	 * @param validFrom the date range the variant is valid from
	 * @param region    the active region of the user who is creating the variant
	 */
	default void saveDangerSourceVariant(DangerSourceVariant variant, Instant validFrom,
										 Region region) {

		// remove duplicate regions
		for (DangerSourceVariant loadedVariant : findByValidFrom(validFrom)) {
			if (loadedVariant.getId().equals(variant.getId()) ||
				!loadedVariant.getDangerSource().getId().equals(variant.getDangerSource().getId()) ||
				!loadedVariant.getDangerSourceVariantType().equals(variant.getDangerSourceVariantType())) {
				continue;
			}
			loadedVariant.getRegions().removeAll(variant.getRegions());
			save(loadedVariant);
		}

		if (variant.getId() == null) {
			save(variant);
		} else {
			update(variant);
		}
		logger.info("Danger source variant {} for region {} saved", variant.getId(), region.getId());
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

}
