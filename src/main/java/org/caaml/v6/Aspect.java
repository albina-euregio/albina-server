// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * An aspect can be defined as a set of aspects. The aspects are the expositions as in a
 * eight part (45°) segments. The allowed aspects are the four main cardinal directions and
 * the four intercardinal directions.
 */
@Serdeable
public enum Aspect {
    E, N, NE, NW, @JsonProperty("n/a") N_A, S, SE, SW, W;

	/** The aspects encoded as one bit per aspect, clockwise from north, naming the pictogram. */
	public static int bitmask(List<Aspect> aspects) {
		return aspects.stream().mapToInt(Aspect::bitmask).reduce(0b00000000, (a, b) -> a | b);
	}

	public int bitmask() {
		return switch (this) {
			case N -> 0b10000000;
			case NE -> 0b01000000;
			case E -> 0b00100000;
			case SE -> 0b00010000;
			case S -> 0b00001000;
			case SW -> 0b00000100;
			case W -> 0b00000010;
			case NW -> 0b00000001;
			case N_A -> 0b00000000;
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
