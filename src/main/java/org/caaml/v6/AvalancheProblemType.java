// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Expected avalanche problem, according to the EAWS avalanche problem definition.
 */
@Serdeable
public enum AvalancheProblemType {
    CORNICES, FAVOURABLE_SITUATION, GLIDING_SNOW, NEW_SNOW, NO_DISTINCT_AVALANCHE_PROBLEM, PERSISTENT_WEAK_LAYERS, WET_SNOW, WIND_SLAB;

	@JsonValue
	@Override
	public String toString() {
        return switch (this) {
            case CORNICES -> "cornices";
            case FAVOURABLE_SITUATION -> "favourable_situation";
            case GLIDING_SNOW -> "gliding_snow";
            case NEW_SNOW -> "new_snow";
            case NO_DISTINCT_AVALANCHE_PROBLEM -> "no_distinct_avalanche_problem";
            case PERSISTENT_WEAK_LAYERS -> "persistent_weak_layers";
            case WET_SNOW -> "wet_snow";
            case WIND_SLAB -> "wind_slab";
        };
    }

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
