// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum SnowpackStability {
	good, fair, poor, very_poor;

	public static SnowpackStability fromString(String text) {
		if (text != null) {
			return Arrays.stream(SnowpackStability.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlv6String() {
		switch (this) {
		case good:
			return "good";
		case fair:
			return "fair";
		case poor:
			return "poor";
		case very_poor:
			return "very_poor";

		default:
			return null;
		}
	}
}
