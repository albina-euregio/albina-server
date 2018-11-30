package eu.albina.model.enumerations;

public enum Aspect {
	N, NE, E, SE, S, SW, W, NW;

	public static Aspect fromString(String text) {
		if (text != null) {
			for (Aspect type : Aspect.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public String toCaamlString() {
		return "AspectRange_" + this.toString();
	}

	public String toString() {
		switch (this) {
		case N:
			return "n";
		case NE:
			return "ne";
		case E:
			return "e";
		case SE:
			return "se";
		case S:
			return "s";
		case SW:
			return "sw";
		case W:
			return "w";
		case NW:
			return "nw";

		default:
			return null;
		}
	}

	public String toUpperCaseString() {
		switch (this) {
		case N:
			return "N";
		case NE:
			return "NE";
		case E:
			return "E";
		case SE:
			return "SE";
		case S:
			return "S";
		case SW:
			return "SW";
		case W:
			return "W";
		case NW:
			return "NW";

		default:
			return null;
		}
	}
}
