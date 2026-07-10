// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Expected avalanche problem, according to the EAWS avalanche problem definition.
 */
@Serdeable
public enum AvalancheProblemType {
    cornices, favourable_situation, gliding_snow, new_snow, no_distinct_avalanche_problem, persistent_weak_layers, wet_snow, wind_slab;

    public static AvalancheProblemType forValue(String value) {
		return switch (value) {
			case "cornices" -> cornices;
			case "favourable_situation" -> favourable_situation;
			case "gliding_snow" -> gliding_snow;
			case "new_snow" -> new_snow;
			case "no_distinct_avalanche_problem" -> no_distinct_avalanche_problem;
			case "persistent_weak_layers" -> persistent_weak_layers;
			case "wet_snow" -> wet_snow;
			case "wind_slab" -> wind_slab;
			default -> throw new IllegalArgumentException("Cannot deserialize AvalancheProblemType");
		};
	}
}
