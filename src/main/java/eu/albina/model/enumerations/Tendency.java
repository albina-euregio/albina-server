/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
					return "images/tendency/tendency_increasing_black.png";
				case steady:
					return "images/tendency/tendency_steady_black.png";
				case decreasing:
					return "images/tendency/tendency_decreasing_black.png";
				default:
					return null;
			}
		} else {
			switch (this) {
				case increasing:
					return "images/tendency/tendency_increasing_blue.png";
				case steady:
					return "images/tendency/tendency_steady_blue.png";
				case decreasing:
					return "images/tendency/tendency_decreasing_blue.png";
				default:
					return null;
			}
		}
	}
}
