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

public enum HazardSiteDistribution {
	single, some, many, many_most, moderately_steep;

	public static HazardSiteDistribution fromString(String text) {
		if (text != null) {
			for (HazardSiteDistribution type : HazardSiteDistribution.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public String toCaamlString() {
		switch (this) {
		case single:
			return "single";
		case some:
			return "some";
		case many:
			return "many";
		case many_most:
			return "many_most";
		case moderately_steep:
			return "moderately_steep";

		default:
			return null;
		}
	}
}
