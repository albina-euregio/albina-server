// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum TendencyType {
    DECREASING, INCREASING, STEADY;

	@JsonValue
	@Override
	public String toString() {
        return switch (this) {
            case DECREASING -> "decreasing";
            case INCREASING -> "increasing";
            case STEADY -> "steady";
        };
    }

    public static TendencyType forValue(String value) {
		return switch (value) {
			case "decreasing" -> DECREASING;
			case "increasing" -> INCREASING;
			case "steady" -> STEADY;
			default -> throw new IllegalArgumentException("Cannot deserialize TendencyType");
		};
	}
}
