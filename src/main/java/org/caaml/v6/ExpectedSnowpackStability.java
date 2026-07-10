// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Snowpack stability, according to the EAWS definition. Four stage scale (very poor, poor,
 * fair, good).
 */
@Serdeable
public enum ExpectedSnowpackStability {
    fair, good, poor, very_poor;

    public static ExpectedSnowpackStability forValue(String value) {
		return switch (value) {
			case "fair" -> fair;
			case "good" -> good;
			case "poor" -> poor;
			case "very_poor" -> very_poor;
			default -> throw new IllegalArgumentException("Cannot deserialize ExpectedSnowpackStability");
		};
	}
}
