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
		switch (this) {
			case small:
				return 1;
			case medium:
				return 2;
			case large:
				return 3;
			case very_large:
				return 4;
			case extreme:
				return 5;
			default:
				throw new IllegalStateException();
		}
	}
}
