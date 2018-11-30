package eu.albina.model.enumerations;

public enum NaturalAvalancheReleaseProbability {
	one, two, three, four;

	public static NaturalAvalancheReleaseProbability fromString(String text) {
		if (text != null) {
			for (NaturalAvalancheReleaseProbability type : NaturalAvalancheReleaseProbability.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
