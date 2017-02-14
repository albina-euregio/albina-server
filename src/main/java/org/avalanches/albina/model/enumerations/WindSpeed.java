package org.avalanches.albina.model.enumerations;

public enum WindSpeed {
	calm, gentle, moderate, strong, gale, storm;

	public static WindSpeed fromString(String text) {
		if (text != null) {
			for (WindSpeed type : WindSpeed.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
