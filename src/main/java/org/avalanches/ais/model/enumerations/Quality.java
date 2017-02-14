package org.avalanches.ais.model.enumerations;

public enum Quality {
	measured, estimated;

	public static Quality fromString(String text) {
		if (text != null) {
			for (Quality type : Quality.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
