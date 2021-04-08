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

public enum DangerRating {
	missing, no_snow, no_rating, low, moderate, considerable, high, very_high;

	public String toString(Locale locale, boolean useLong) {
		return ResourceBundle.getBundle("i18n.DangerRating", locale, new XMLResourceBundleControl())
				.getString(name() + (useLong ? ".long" : ""));
	}

	public static DangerRating fromString(String text) {
		if (text != null) {
			return Arrays.stream(DangerRating.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String getColor() {
		switch (this) {
		case low:
			return "#CCFF66";
		case moderate:
			return "#FFFF00";
		case considerable:
			return "#FF9900";
		case high:
			return "#FF0000";
		case very_high:
			return "#800000";
		default:
			return "#969696";
		}
	}

	public static String getCAAMLv6String(DangerRating dangerRating) {
		switch (dangerRating) {
		case missing:
			return "no_rating";
		case no_rating:
			return "no_rating";
		case no_snow:
			return "no_snow";
		case low:
			return "low";
		case moderate:
			return "moderate";
		case considerable:
			return "considerable";
		case high:
			return "high";
		case very_high:
			return "very_high";

		default:
			return "no_rating";
		}
	}

	public static String getCAAMLv5String(DangerRating dangerRating) {
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

	public double getDouble() {
		switch (this) {
		case missing:
			return .0;
		case no_rating:
			return .0;
		case no_snow:
			return .0;
		case low:
			return 1.0 / 1364;
		case moderate:
			return 1.0 / 1364 * 4.0;
		case considerable:
			return 1.0 / 1364 * 4.0 * 4.0;
		case high:
			return 1.0 / 1364 * 4.0 * 4.0 * 4.0;
		case very_high:
			return 1.0 / 1364 * 4.0 * 4.0 * 4.0 * 4.0;
		default:
			return .0;
		}
	}
}
