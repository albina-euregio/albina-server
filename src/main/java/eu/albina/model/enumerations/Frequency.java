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
        return switch (this) {
            case none -> "none";
            case few -> "few";
            case some -> "some";
            case many -> "many";
		};
	}
}
