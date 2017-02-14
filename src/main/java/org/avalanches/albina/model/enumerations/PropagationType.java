package org.avalanches.albina.model.enumerations;

public enum PropagationType {
	N, P;

	public static PropagationType fromString(String text) {
		if (text != null) {
			for (PropagationType type : PropagationType.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
