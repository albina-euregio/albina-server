package org.avalanches.albina.model.enumerations;

public enum PrecipitationType {
	none, snow, rain, graupel;

	public static PrecipitationType fromString(String text) {
		if (text != null) {
			for (PrecipitationType type : PrecipitationType.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
