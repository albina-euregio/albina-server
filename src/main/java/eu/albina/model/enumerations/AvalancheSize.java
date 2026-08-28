// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

public enum AvalancheSize {
	small, medium, large, very_large, extreme;

	public String toString(Locale locale) {
		return ResourceBundle.getBundle("i18n.caaml", locale, new XMLResourceBundleControl())
				.getString("avalancheSize." + name());
	}

	public static AvalancheSize fromInteger(int value) {
		return switch (value) {
			case 1 -> small;
			case 2 -> medium;
			case 3 -> large;
			case 4 -> very_large;
			case 5 -> extreme;
			default -> throw new IllegalArgumentException("Cannot convert AvalancheSize: " + value);
		};
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
