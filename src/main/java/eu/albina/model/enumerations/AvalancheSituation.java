package eu.albina.model.enumerations;

public enum AvalancheSituation {
	new_snow, wind_drifted_snow, weak_persistent_layer, wet_snow, gliding_snow, favourable_situation;

	public static AvalancheSituation fromString(String text) {
		if (text != null) {
			for (AvalancheSituation type : AvalancheSituation.values()) {
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
		case weak_persistent_layer:
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
