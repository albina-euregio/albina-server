// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Expected frequency of lowest snowpack stability, according to the EAWS definition. Three
 * stage scale (few, some, many).
 */
@Serdeable
public enum ExpectedAvalancheFrequency {
    FEW, MANY, NONE, SOME;

	@JsonValue
	@Override
	public String toString() {
        return switch (this) {
            case FEW -> "few";
            case MANY -> "many";
            case NONE -> "none";
            case SOME -> "some";
        };
    }

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
