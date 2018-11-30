package eu.albina.model.enumerations;

public enum ArtificialAvalancheReleaseProbability {
	one, two, three, four;

	public static ArtificialAvalancheReleaseProbability fromString(String text) {
		if (text != null) {
			for (ArtificialAvalancheReleaseProbability type : ArtificialAvalancheReleaseProbability.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
