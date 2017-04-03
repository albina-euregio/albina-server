package eu.albina.model.enumerations;

public enum PrecipitationIntensity {
	light, moderate, heavy;

	public static PrecipitationIntensity fromString(String text) {
		if (text != null) {
			for (PrecipitationIntensity type : PrecipitationIntensity.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
