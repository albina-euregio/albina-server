package eu.albina.model.enumerations;

public enum AvalancheProblem {
	new_snow, wind_drifted_snow, old_snow, wet_snow, gliding_snow, favourable_situation;

	public static AvalancheProblem fromString(String text) {
		if (text != null) {
			for (AvalancheProblem type : AvalancheProblem.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public String toCaamlString() {
		switch (this) {
		case new_snow:
			return "new snow";
		case wind_drifted_snow:
			return "drifting snow";
		case old_snow:
			return "old snow";
		case wet_snow:
			return "wet snow";
		case gliding_snow:
			return "gliding snow";
		case favourable_situation:
			return "favourable situation";

		default:
			return null;
		}
	}
}
