package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Danger rating value, according to EAWS danger scale definition.
 */
public enum DangerRatingValue {
    CONSIDERABLE, HIGH, LOW, MODERATE, NO_RATING, NO_SNOW, VERY_HIGH;

	@JsonValue
	@Override
	public String toString() {
        switch (this) {
            case CONSIDERABLE: return "considerable";
            case HIGH: return "high";
            case LOW: return "low";
            case MODERATE: return "moderate";
            case NO_RATING: return "no_rating";
            case NO_SNOW: return "no_snow";
            case VERY_HIGH: return "very_high";
        }
        return null;
    }

    public static DangerRatingValue forValue(String value) {
        if (value.equals("considerable")) return CONSIDERABLE;
        if (value.equals("high")) return HIGH;
        if (value.equals("low")) return LOW;
        if (value.equals("moderate")) return MODERATE;
        if (value.equals("no_rating")) return NO_RATING;
        if (value.equals("no_snow")) return NO_SNOW;
        if (value.equals("very_high")) return VERY_HIGH;
        throw new IllegalArgumentException("Cannot deserialize DangerRatingValue");
    }
}
