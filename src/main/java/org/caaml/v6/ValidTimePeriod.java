package org.caaml.v6;

/**
 * Valid time period can be used to limit the validity of an element to an erlier or later
 * period. It can be used to distinguish danger ratings or avalanche problems.
 */
public enum ValidTimePeriod {
    ALL_DAY, EARLIER, LATER;

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
