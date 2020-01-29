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

public enum DangerPattern {
	dp1, dp2, dp3, dp4, dp5, dp6, dp7, dp8, dp9, dp10;

	public static DangerPattern fromString(String text) {
		if (text != null) {
			for (DangerPattern type : DangerPattern.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public static String getCAAMLString(DangerPattern dangerPattern) {
		switch (dangerPattern) {
		case dp1:
			return "DP1";
		case dp2:
			return "DP2";
		case dp3:
			return "DP3";
		case dp4:
			return "DP4";
		case dp5:
			return "DP5";
		case dp6:
			return "DP6";
		case dp7:
			return "DP7";
		case dp8:
			return "DP8";
		case dp9:
			return "DP9";
		case dp10:
			return "DP10";

		default:
			return "missing";
		}
	}
}
