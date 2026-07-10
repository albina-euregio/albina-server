// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum TendencyType {
    decreasing, increasing, steady;

    public static TendencyType forValue(String value) {
		return switch (value) {
			case "decreasing" -> decreasing;
			case "increasing" -> increasing;
			case "steady" -> steady;
			default -> throw new IllegalArgumentException("Cannot deserialize TendencyType");
		};
	}
}
