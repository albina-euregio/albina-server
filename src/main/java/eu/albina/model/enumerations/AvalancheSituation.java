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

	public String toStringId() {
		switch (this) {
		case new_snow:
			return "new_snow";
		case wind_drifted_snow:
			return "wind_drifted_snow";
		case weak_persistent_layer:
			return "weak_persistent_layer";
		case wet_snow:
			return "wet_snow";
		case gliding_snow:
			return "gliding_snow";
		case favourable_situation:
			return "favourable_situation";
		default:
			return "";
		}
	}

	public String toString(LanguageCode lang) {
		switch (lang) {
		case de:
			switch (this) {
			case new_snow:
				return "Neuschnee";
			case wind_drifted_snow:
				return "Triebschnee";
			case weak_persistent_layer:
				return "Altschnee";
			case wet_snow:
				return "Nasschnee";
			case gliding_snow:
				return "Gleitschnee";
			case favourable_situation:
				return "Günstige Situation";
			default:
				return "";
			}
		case it:
			switch (this) {
			case new_snow:
				return "Neve fresca";
			case wind_drifted_snow:
				return "Neve ventata";
			case weak_persistent_layer:
				return "Strati deboli persistenti";
			case wet_snow:
				return "Neve bagnata";
			case gliding_snow:
				return "Valanghe di slittamento";
			case favourable_situation:
				return "Situazione favorevole";
			default:
				return "";
			}
		case en:
			switch (this) {
			case new_snow:
				return "New snow";
			case wind_drifted_snow:
				return "Wind-drifted snow";
			case weak_persistent_layer:
				return "Persistent weak layer";
			case wet_snow:
				return "Wet snow";
			case gliding_snow:
				return "Gliding snow";
			case favourable_situation:
				return "Favourable situation";
			default:
				return "";
			}

		default:
			switch (this) {
			case new_snow:
				return "New snow";
			case wind_drifted_snow:
				return "Wind-drifted snow";
			case weak_persistent_layer:
				return "Persistent weak layer";
			case wet_snow:
				return "Wet snow";
			case gliding_snow:
				return "Gliding snow";
			case favourable_situation:
				return "Favourable situation";
			default:
				return "";
			}
		}
	}
}
