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
