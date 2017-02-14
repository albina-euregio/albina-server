package org.avalanches.ais.model.enumerations;

public enum Aspect {
	N, NE, E, SE, S, SW, W, NW;

	public static Aspect fromString(String text) {
		if (text != null) {
			for (Aspect type : Aspect.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
