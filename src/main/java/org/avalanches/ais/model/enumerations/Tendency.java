package org.avalanches.ais.model.enumerations;

public enum Tendency {
	increasing, decreasing, no_change;

	public static Tendency fromString(String text) {
		if (text != null) {
			for (Tendency type : Tendency.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
