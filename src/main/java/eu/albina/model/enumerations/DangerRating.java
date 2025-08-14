// SPDX-License-Identifier: AGPL-3.0-or-later
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
