// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Snowpack stability, according to the EAWS definition. Four stage scale (very poor, poor,
 * fair, good).
 */
@Serdeable
public enum ExpectedSnowpackStability {
    FAIR, GOOD, POOR, VERY_POOR;

	@JsonValue
	@Override
    public String toString() {
        return switch (this) {
            case FAIR -> "fair";
            case GOOD -> "good";
            case POOR -> "poor";
            case VERY_POOR -> "very_poor";
        };
    }

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
