// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Snowpack stability, according to the EAWS definition. Four stage scale (very poor, poor,
 * fair, good).
 */
@Serdeable
public enum ExpectedSnowpackStability {
    @JsonProperty("fair") FAIR,
    @JsonProperty("good") GOOD,
    @JsonProperty("poor") POOR,
    @JsonProperty("very_poor") VERY_POOR;

    public static ExpectedSnowpackStability forValue(String value) {
		return switch (value) {
			case "fair" -> FAIR;
			case "good" -> GOOD;
			case "poor" -> POOR;
			case "very_poor" -> VERY_POOR;
			default -> throw new IllegalArgumentException("Cannot deserialize ExpectedSnowpackStability");
		};
	}
}
