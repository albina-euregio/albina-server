package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Expected avalanche problem, according to the EAWS avalanche problem definition.
 */
public enum AvalancheProblemType {
    CORNICES, FAVOURABLE_SITUATION, GLIDING_SNOW, NEW_SNOW, NO_DISTINCT_AVALANCHE_PROBLEM, PERSISTENT_WEAK_LAYERS, WET_SNOW, WIND_SLAB;

	@JsonValue
	@Override
	public String toString() {
        switch (this) {
            case CORNICES: return "cornices";
            case FAVOURABLE_SITUATION: return "favourable_situation";
            case GLIDING_SNOW: return "gliding_snow";
            case NEW_SNOW: return "new_snow";
            case NO_DISTINCT_AVALANCHE_PROBLEM: return "no_distinct_avalanche_problem";
            case PERSISTENT_WEAK_LAYERS: return "persistent_weak_layers";
            case WET_SNOW: return "wet_snow";
            case WIND_SLAB: return "wind_slab";
        }
        throw new IllegalStateException();
    }

    public static AvalancheProblemType forValue(String value) {
        if (value.equals("cornices")) return CORNICES;
        if (value.equals("favourable_situation")) return FAVOURABLE_SITUATION;
        if (value.equals("gliding_snow")) return GLIDING_SNOW;
        if (value.equals("new_snow")) return NEW_SNOW;
        if (value.equals("no_distinct_avalanche_problem")) return NO_DISTINCT_AVALANCHE_PROBLEM;
        if (value.equals("persistent_weak_layers")) return PERSISTENT_WEAK_LAYERS;
        if (value.equals("wet_snow")) return WET_SNOW;
        if (value.equals("wind_slab")) return WIND_SLAB;
        throw new IllegalArgumentException("Cannot deserialize AvalancheProblemType");
    }
}
