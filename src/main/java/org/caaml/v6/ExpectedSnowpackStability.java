package org.caaml.v6;

/**
 * Snowpack stability, according to the EAWS definition. Four stage scale (very poor, poor,
 * fair, good).
 */
public enum ExpectedSnowpackStability {
    FAIR, GOOD, POOR, VERY_POOR;

	@Override
    public String toString() {
        switch (this) {
            case FAIR: return "fair";
            case GOOD: return "good";
            case POOR: return "poor";
            case VERY_POOR: return "very_poor";
        }
        return null;
    }

    public static ExpectedSnowpackStability forValue(String value) {
        if (value.equals("fair")) return FAIR;
        if (value.equals("good")) return GOOD;
        if (value.equals("poor")) return POOR;
        if (value.equals("very_poor")) return VERY_POOR;
        throw new IllegalArgumentException("Cannot deserialize ExpectedSnowpackStability");
    }
}
