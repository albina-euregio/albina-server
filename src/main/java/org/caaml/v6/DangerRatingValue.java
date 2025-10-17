// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Danger rating value, according to EAWS danger scale definition.
 */
@Serdeable
public enum DangerRatingValue {
    CONSIDERABLE, HIGH, LOW, MODERATE, NO_RATING, NO_SNOW, VERY_HIGH;

	@JsonValue
	@Override
	public String toString() {
        return switch (this) {
            case CONSIDERABLE -> "considerable";
            case HIGH -> "high";
            case LOW -> "low";
            case MODERATE -> "moderate";
            case NO_RATING -> "no_rating";
            case NO_SNOW -> "no_snow";
            case VERY_HIGH -> "very_high";
        };
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
