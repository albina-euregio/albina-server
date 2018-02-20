package eu.albina.model.enumerations;

public enum Tendency {
	decreasing, steady, increasing;

	public static Tendency fromString(String text) {
		if (text != null) {
			for (Tendency type : Tendency.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
