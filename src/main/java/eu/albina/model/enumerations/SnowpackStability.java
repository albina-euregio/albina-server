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
        return switch (this) {
            case good -> "good";
            case fair -> "fair";
            case poor -> "poor";
            case very_poor -> "very_poor";
		};
	}
}
