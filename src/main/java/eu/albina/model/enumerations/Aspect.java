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
import java.util.Set;

import eu.albina.util.XMLResourceBundleControl;

public enum Aspect {
	N, NE, E, SE, S, SW, W, NW;

	public String toString(Locale locale) {
		return ResourceBundle.getBundle("i18n.Aspect", locale, new XMLResourceBundleControl())
				.getString(name());
	}

	public static Aspect fromString(String text) {
		if (text != null) {
			return Arrays.stream(Aspect.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlString() {
		return "AspectRange_" + this.toLowerCaseString();
	}

	public String toString() {
		switch (this) {
		case N:
			return "N";
		case NE:
			return "NE";
		case E:
			return "E";
		case SE:
			return "SE";
		case S:
			return "S";
		case SW:
			return "SW";
		case W:
			return "W";
		case NW:
			return "NW";

		default:
			return null;
		}
	}

	public String toLowerCaseString() {
		switch (this) {
		case N:
			return "n";
		case NE:
			return "ne";
		case E:
			return "e";
		case SE:
			return "se";
		case S:
			return "s";
		case SW:
			return "sw";
		case W:
			return "w";
		case NW:
			return "nw";

		default:
			return null;
		}
	}

	public static String getSymbolPath(Set<Aspect> aspects, boolean grayscale) {
		if (aspects == null || aspects.isEmpty()) {
			return "aspects/color/empty.png";
		}
		int bitmask = aspects.stream().mapToInt(Aspect::bitmask).reduce(0b00000000, (a, b) -> a | b);
		if (grayscale)
			return "aspects/grey/" + Integer.valueOf(bitmask).toString() + ".png";
		else
			return "aspects/color/" + Integer.valueOf(bitmask).toString() + ".png";
	}

	private static int bitmask(Aspect aspect) {
		switch (aspect) {
			case N:
				return 0b10000000;
			case NE:
				return 0b01000000;
			case E:
				return 0b00100000;
			case SE:
				return 0b00010000;
			case S:
				return 0b00001000;
			case SW:
				return 0b00000100;
			case W:
				return 0b00000010;
			case NW:
				return 0b00000001;
			default:
				return 0;
		}
	}
}
