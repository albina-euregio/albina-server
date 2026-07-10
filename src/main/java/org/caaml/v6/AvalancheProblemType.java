// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Expected avalanche problem, according to the EAWS avalanche problem definition.
 */
@Serdeable
public enum AvalancheProblemType {
    @JsonProperty("cornices") CORNICES,
    @JsonProperty("favourable_situation") FAVOURABLE_SITUATION,
    @JsonProperty("gliding_snow") GLIDING_SNOW,
    @JsonProperty("new_snow") NEW_SNOW,
    @JsonProperty("no_distinct_avalanche_problem") NO_DISTINCT_AVALANCHE_PROBLEM,
    @JsonProperty("persistent_weak_layers") PERSISTENT_WEAK_LAYERS,
    @JsonProperty("wet_snow") WET_SNOW,
    @JsonProperty("wind_slab") WIND_SLAB;

    public static AvalancheProblemType forValue(String value) {
		return switch (value) {
			case "cornices" -> CORNICES;
			case "favourable_situation" -> FAVOURABLE_SITUATION;
			case "gliding_snow" -> GLIDING_SNOW;
			case "new_snow" -> NEW_SNOW;
			case "no_distinct_avalanche_problem" -> NO_DISTINCT_AVALANCHE_PROBLEM;
			case "persistent_weak_layers" -> PERSISTENT_WEAK_LAYERS;
			case "wet_snow" -> WET_SNOW;
			case "wind_slab" -> WIND_SLAB;
			default -> throw new IllegalArgumentException("Cannot deserialize AvalancheProblemType");
		};
	}
}
