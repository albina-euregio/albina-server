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

public enum DangerRating {
	missing, no_snow, no_rating, low, moderate, considerable, high, very_high;

	public static DangerRating fromString(String text) {
		if (text != null) {
			for (DangerRating type : DangerRating.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}

	public static String getCAAMLString(DangerRating dangerRating) {
		switch (dangerRating) {
		case missing:
			return "n/a";
		case no_rating:
			return "n/a";
		case no_snow:
			return "n/a";
		case low:
			return "1";
		case moderate:
			return "2";
		case considerable:
			return "3";
		case high:
			return "4";
		case very_high:
			return "5";

		default:
			return "n/a";
		}
	}

	public static String getString(DangerRating dangerRating) {
		switch (dangerRating) {
		case missing:
			return "0";
		case no_rating:
			return "0";
		case no_snow:
			return "0";
		case low:
			return "1";
		case moderate:
			return "2";
		case considerable:
			return "3";
		case high:
			return "4";
		case very_high:
			return "5";

		default:
			return "0";
		}
	}

	public static int getInt(DangerRating dangerRating) {
		switch (dangerRating) {
		case missing:
			return 0;
		case no_rating:
			return 0;
		case no_snow:
			return 0;
		case low:
			return 1;
		case moderate:
			return 2;
		case considerable:
			return 3;
		case high:
			return 4;
		case very_high:
			return 5;

		default:
			return 0;
		}
	}
}
