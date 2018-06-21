package eu.albina.model.enumerations;

public enum DangerRating {
	missing, no_rating, low, moderate, considerable, high, very_high;

	public static DangerRating fromString(String text) {
		if (text != null) {
			for (DangerRating type : DangerRating.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public static String getCAAMLString(DangerRating dangerRating) {
		switch (dangerRating) {
		case missing:
			return "missing";
		case no_rating:
			return "n/a";
		case low:
			return "1";
		case moderate:
			return "2";
		case considerable:
			return "3";
		case high:
			return "4";
		case very_high:
			return "5";

		default:
			return "missing";
		}
	}

	public static String getString(DangerRating dangerRating) {
		switch (dangerRating) {
		case missing:
			return "0";
		case no_rating:
			return "0";
		case low:
			return "1";
		case moderate:
			return "2";
		case considerable:
			return "3";
		case high:
			return "4";
		case very_high:
			return "5";

		default:
			return "0";
		}
	}
}
