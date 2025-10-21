// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

public enum Tendency {
	decreasing, steady, increasing;

	public String toString(Locale locale) {
		return ResourceBundle.getBundle("i18n.Tendency", locale, new XMLResourceBundleControl()).getString(name());
	}

	public static String getCaamlString(Tendency tendency) {
		return switch (tendency) {
			case decreasing -> "decreasing";
			case steady -> "steady";
			case increasing -> "increasing";
		};
	}

	public String getSymbolPath(boolean grayscale) {
		if (grayscale) {
			return switch (this) {
				case increasing -> "tendency/tendency_increasing_black.png";
				case steady -> "tendency/tendency_steady_black.png";
				case decreasing -> "tendency/tendency_decreasing_black.png";
			};
		} else {
            return switch (this) {
                case increasing -> "tendency/tendency_increasing_blue.png";
                case steady -> "tendency/tendency_steady_blue.png";
                case decreasing -> "tendency/tendency_decreasing_blue.png";
			};
		}
	}
}
