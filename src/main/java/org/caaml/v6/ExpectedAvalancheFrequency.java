package org.caaml.v6;

/**
 * Expected frequency of lowest snowpack stability, according to the EAWS definition. Three
 * stage scale (few, some, many).
 */
public enum ExpectedAvalancheFrequency {
    FEW, MANY, SOME;

	@Override
	public String toString() {
        switch (this) {
            case FEW: return "few";
            case MANY: return "many";
            case SOME: return "some";
        }
        return null;
    }

    public static ExpectedAvalancheFrequency forValue(String value) {
        if (value.equals("few")) return FEW;
        if (value.equals("many")) return MANY;
        if (value.equals("some")) return SOME;
        throw new IllegalArgumentException("Cannot deserialize ExpectedAvalancheFrequency");
    }
}
