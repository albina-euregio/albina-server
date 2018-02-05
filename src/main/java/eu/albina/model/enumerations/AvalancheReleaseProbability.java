package eu.albina.model.enumerations;

public enum AvalancheReleaseProbability {
	one, two, three, four;

	public static AvalancheReleaseProbability fromString(String text) {
		if (text != null) {
			for (AvalancheReleaseProbability type : AvalancheReleaseProbability.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
