// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Expected frequency of lowest snowpack stability, according to the EAWS definition. Three
 * stage scale (few, some, many).
 */
@Serdeable
public enum ExpectedAvalancheFrequency {
    @JsonProperty("few") FEW,
    @JsonProperty("many") MANY,
    @JsonProperty("none") NONE,
    @JsonProperty("some") SOME;

    public static ExpectedAvalancheFrequency forValue(String value) {
		return switch (value) {
			case "few" -> FEW;
			case "many" -> MANY;
			case "none" -> NONE;
			case "some" -> SOME;
			default -> throw new IllegalArgumentException("Cannot deserialize ExpectedAvalancheFrequency");
		};
	}
}
