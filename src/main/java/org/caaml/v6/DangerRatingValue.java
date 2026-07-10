// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Danger rating value, according to EAWS danger scale definition.
 */
@Serdeable
public enum DangerRatingValue {
    @JsonProperty("considerable") CONSIDERABLE,
    @JsonProperty("high") HIGH,
    @JsonProperty("low") LOW,
    @JsonProperty("moderate") MODERATE,
    @JsonProperty("no_rating") NO_RATING,
    @JsonProperty("no_snow") NO_SNOW,
    @JsonProperty("very_high") VERY_HIGH;

    public static DangerRatingValue forValue(String value) {
		return switch (value) {
			case "considerable" -> CONSIDERABLE;
			case "high" -> HIGH;
			case "low" -> LOW;
			case "moderate" -> MODERATE;
			case "no_rating" -> NO_RATING;
			case "no_snow" -> NO_SNOW;
			case "very_high" -> VERY_HIGH;
			default -> throw new IllegalArgumentException("Cannot deserialize DangerRatingValue");
		};
	}
}
