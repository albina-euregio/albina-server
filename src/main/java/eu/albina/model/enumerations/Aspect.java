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

public enum Aspect {
	N, NE, E, SE, S, SW, W, NW;

	public static Aspect fromString(String text) {
		if (text != null) {
			for (Aspect type : Aspect.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public String toCaamlString() {
		return "AspectRange_" + this.toString();
	}

	public String toString() {
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

	public String toUpperCaseString() {
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
}
