// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import eu.albina.model.enumerations.DangerSourceVariantType;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.Collection;

@Serdeable
public record DangerSourceVariantsStatus(Instant date, boolean forecast, boolean analysis) {

	public static DangerSourceVariantsStatus of(Instant date, Collection<DangerSourceVariant> dangerSourceVariants) {
		return new DangerSourceVariantsStatus(
			date,
			dangerSourceVariants.stream().anyMatch(variant -> variant.getDangerSourceVariantType() == DangerSourceVariantType.forecast),
			dangerSourceVariants.stream().anyMatch(variant -> variant.getDangerSourceVariantType() == DangerSourceVariantType.analysis)
		);
	}
}
