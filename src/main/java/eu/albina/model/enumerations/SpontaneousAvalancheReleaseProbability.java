package eu.albina.model.enumerations;

public enum SpontaneousAvalancheReleaseProbability {
	one, two, three, four;

	public static SpontaneousAvalancheReleaseProbability fromString(String text) {
		if (text != null) {
			for (SpontaneousAvalancheReleaseProbability type : SpontaneousAvalancheReleaseProbability.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
