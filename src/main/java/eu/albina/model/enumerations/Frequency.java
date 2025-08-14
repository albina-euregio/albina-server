// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum Frequency {
	none, few, some, many;

	public static Frequency fromString(String text) {
		if (text != null) {
			return Arrays.stream(Frequency.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlv6String() {
		switch (this) {
		case none:
			return "none";
		case few:
			return "few";
		case some:
			return "some";
		case many:
			return "many";

		default:
			return null;
		}
	}
}
