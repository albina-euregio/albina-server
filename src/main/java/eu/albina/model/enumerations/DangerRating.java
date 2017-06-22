package eu.albina.model.enumerations;

public enum DangerRating {
	no_rating, low, moderate, considerable, high, very_high;

	public static DangerRating fromString(String text) {
		if (text != null) {
			for (DangerRating type : DangerRating.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
