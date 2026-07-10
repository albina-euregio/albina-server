// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum TendencyType {
    @JsonProperty("decreasing") DECREASING,
    @JsonProperty("increasing") INCREASING,
    @JsonProperty("steady") STEADY;

    public static TendencyType forValue(String value) {
		return switch (value) {
			case "decreasing" -> DECREASING;
			case "increasing" -> INCREASING;
			case "steady" -> STEADY;
			default -> throw new IllegalArgumentException("Cannot deserialize TendencyType");
		};
	}
}
