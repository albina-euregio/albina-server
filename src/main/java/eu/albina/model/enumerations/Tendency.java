// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

public enum Tendency {
	decreasing, steady, increasing;

	public String toString(Locale locale) {
		return ResourceBundle.getBundle("i18n.Tendency", locale, new XMLResourceBundleControl()).getString(name());
	}

	public static Tendency fromString(String text) {
		if (text != null) {
			return Arrays.stream(Tendency.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public static String getCaamlString(Tendency tendency) {
		switch (tendency) {
		case decreasing:
			return "decreasing";
		case steady:
			return "steady";
		case increasing:
			return "increasing";

		default:
			return "n/a";
		}
	}

	public String getSymbolPath(boolean grayscale) {
		if (grayscale) {
			switch (this) {
				case increasing:
					return "tendency/tendency_increasing_black.png";
				case steady:
					return "tendency/tendency_steady_black.png";
				case decreasing:
					return "tendency/tendency_decreasing_black.png";
				default:
					return null;
			}
		} else {
			switch (this) {
				case increasing:
					return "tendency/tendency_increasing_blue.png";
				case steady:
					return "tendency/tendency_steady_blue.png";
				case decreasing:
					return "tendency/tendency_decreasing_blue.png";
				default:
					return null;
			}
		}
	}
}
