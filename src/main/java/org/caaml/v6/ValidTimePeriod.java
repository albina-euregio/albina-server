// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Valid time period can be used to limit the validity of an element to an earlier or later
 * period. It can be used to distinguish danger ratings or avalanche problems.
 */
public enum ValidTimePeriod {
    ALL_DAY, EARLIER, LATER;

	@JsonValue
	@Override
	public String toString() {
        switch (this) {
            case ALL_DAY: return "all_day";
            case EARLIER: return "earlier";
            case LATER: return "later";
        }
        return null;
    }

    public static ValidTimePeriod forValue(String value) {
        if (value.equals("all_day")) return ALL_DAY;
        if (value.equals("earlier")) return EARLIER;
        if (value.equals("later")) return LATER;
        throw new IllegalArgumentException("Cannot deserialize ValidTimePeriod");
    }
}
