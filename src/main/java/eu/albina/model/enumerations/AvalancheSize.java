// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum AvalancheSize {
	small, medium, large, very_large, extreme;

	public static AvalancheSize fromString(String text) {
		if (text != null) {
			return Arrays.stream(AvalancheSize.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlString() {
		return Integer.toString(toInteger());
	}

	public int toInteger() {
		return switch (this) {
			case small -> 1;
			case medium -> 2;
			case large -> 3;
			case very_large -> 4;
			case extreme -> 5;
		};
	}
}
