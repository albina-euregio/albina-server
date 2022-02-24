package eu.albina.model.enumerations;

import java.util.Arrays;

public enum Position {
	topleft, topright, bottomleft, bottomright;

	public static Position fromString(String text) {
		if (text != null) {
			return Arrays.stream(Position.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toString() {
		switch (this) {
		case topleft:
			return "topleft";
		case topright:
			return "topright";
		case bottomleft:
			return "bottomleft";
		case bottomright:
			return "bottomright";

		default:
			return null;
		}
	}
}
