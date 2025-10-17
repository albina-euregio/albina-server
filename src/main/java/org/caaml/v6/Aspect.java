// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

/**
 * An aspect can be defined as a set of aspects. The aspects are the expositions as in a
 * eight part (45°) segments. The allowed aspects are the four main cardinal directions and
 * the four intercardinal directions.
 */
@Serdeable
public enum Aspect {
    E, N, NE, NW, N_A, S, SE, SW, W;

	@JsonValue
	@Override
	public String toString() {
        return switch (this) {
            case E -> "E";
            case N -> "N";
            case NE -> "NE";
            case NW -> "NW";
            case N_A -> "n/a";
            case S -> "S";
            case SE -> "SE";
            case SW -> "SW";
            case W -> "W";
        };
    }

    public static Aspect forValue(String value) {
		return switch (value) {
			case "E" -> E;
			case "N" -> N;
			case "NE" -> NE;
			case "NW" -> NW;
			case "n/a" -> N_A;
			case "S" -> S;
			case "SE" -> SE;
			case "SW" -> SW;
			case "W" -> W;
			default -> throw new IllegalArgumentException("Cannot deserialize Aspect");
		};
	}
}
