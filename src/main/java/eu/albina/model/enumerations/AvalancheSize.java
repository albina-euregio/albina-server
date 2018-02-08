package eu.albina.model.enumerations;

public enum AvalancheSize {
	small, medium, large, very_large, extreme;

	public static AvalancheSize fromString(String text) {
		if (text != null) {
			for (AvalancheSize type : AvalancheSize.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
