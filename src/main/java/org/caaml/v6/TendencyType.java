package org.caaml.v6;

public enum TendencyType {
    DECREASING, INCREASING, STEADY;

	@Override
	public String toString() {
        switch (this) {
            case DECREASING: return "decreasing";
            case INCREASING: return "increasing";
            case STEADY: return "steady";
        }
        return null;
    }

    public static TendencyType forValue(String value) {
        if (value.equals("decreasing")) return DECREASING;
        if (value.equals("increasing")) return INCREASING;
        if (value.equals("steady")) return STEADY;
        throw new IllegalArgumentException("Cannot deserialize TendencyType");
    }
}
