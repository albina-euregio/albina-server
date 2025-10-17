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
		return switch (complexity) {
			case easy -> "easy";
			case challenging -> "challenging";
			case complex -> "complex";
		};
	}

	public static String getString(Complexity complexity) {
        return switch (complexity) {
            case easy -> "easy";
            case challenging -> "challenging";
            case complex -> "complex";
		};
	}
}
