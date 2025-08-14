// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum Complexity {
	easy, challenging, complex;

	public static Complexity fromString(String text) {
		if (text != null) {
			return Arrays.stream(Complexity.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public static String getCAAMLString(Complexity complexity) {
		switch (complexity) {
		case easy:
			return "easy";
		case challenging:
			return "challenging";
		case complex:
			return "complex";

		default:
			return "n/a";
		}
	}

	public static String getString(Complexity complexity) {
		switch (complexity) {
		case easy:
			return "easy";
		case challenging:
			return "challenging";
		case complex:
			return "complex";

		default:
			return "n/a";
		}
	}
}
