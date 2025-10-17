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
        if (value.equals("E")) return E;
        if (value.equals("N")) return N;
        if (value.equals("NE")) return NE;
        if (value.equals("NW")) return NW;
        if (value.equals("n/a")) return N_A;
        if (value.equals("S")) return S;
        if (value.equals("SE")) return SE;
        if (value.equals("SW")) return SW;
        if (value.equals("W")) return W;
        throw new IllegalArgumentException("Cannot deserialize Aspect");
    }
}
