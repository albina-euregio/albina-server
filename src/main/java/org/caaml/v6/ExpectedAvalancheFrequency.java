// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Expected frequency of lowest snowpack stability, according to the EAWS definition. Three
 * stage scale (few, some, many).
 */
@Serdeable
public enum ExpectedAvalancheFrequency {
    few, many, none, some;

    public static ExpectedAvalancheFrequency forValue(String value) {
		return switch (value) {
			case "few" -> few;
			case "many" -> many;
			case "none" -> none;
			case "some" -> some;
			default -> throw new IllegalArgumentException("Cannot deserialize ExpectedAvalancheFrequency");
		};
	}
}
