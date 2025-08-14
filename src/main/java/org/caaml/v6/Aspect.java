// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An aspect can be defined as a set of aspects. The aspects are the expositions as in a
 * eight part (45°) segments. The allowed aspects are the four main cardinal directions and
 * the four intercardinal directions.
 */
public enum Aspect {
    E, N, NE, NW, N_A, S, SE, SW, W;

	@JsonValue
	@Override
	public String toString() {
        switch (this) {
            case E: return "E";
            case N: return "N";
            case NE: return "NE";
            case NW: return "NW";
            case N_A: return "n/a";
            case S: return "S";
            case SE: return "SE";
            case SW: return "SW";
            case W: return "W";
        }
        return null;
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
