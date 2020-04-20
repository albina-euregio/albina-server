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

public enum AvalancheSize {
	small, medium, large, very_large, extreme;

	public static AvalancheSize fromString(String text) {
		if (text != null) {
			for (AvalancheSize type : AvalancheSize.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public String toCaamlString() {
		switch (this) {
		case small:
			return "1";
		case medium:
			return "2";
		case large:
			return "3";
		case very_large:
			return "4";
		case extreme:
			return "5";

		default:
			return null;
		}
	}
}
