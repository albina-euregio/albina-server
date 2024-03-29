package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Expected frequency of lowest snowpack stability, according to the EAWS definition. Three
 * stage scale (few, some, many).
 */
public enum ExpectedAvalancheFrequency {
    FEW, MANY, NONE, SOME;

	@JsonValue
	@Override
	public String toString() {
        switch (this) {
            case FEW: return "few";
            case MANY: return "many";
            case NONE: return "none";
            case SOME: return "some";
        }
        return null;
    }

    public static ExpectedAvalancheFrequency forValue(String value) {
        if (value.equals("few")) return FEW;
        if (value.equals("many")) return MANY;
        if (value.equals("none")) return NONE;
        if (value.equals("some")) return SOME;
        throw new IllegalArgumentException("Cannot deserialize ExpectedAvalancheFrequency");
    }
}
