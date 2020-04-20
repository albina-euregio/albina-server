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

public enum ArtificialAvalancheReleaseProbability {
	one, two, three, four;

	public static ArtificialAvalancheReleaseProbability fromString(String text) {
		if (text != null) {
			for (ArtificialAvalancheReleaseProbability type : ArtificialAvalancheReleaseProbability.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public String toCaamlString() {
		switch (this) {
		case one:
			return "one";
		case two:
			return "two";
		case three:
			return "three";
		case four:
			return "four";

		default:
			return null;
		}
	}
}
