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
		return switch (this) {
			case low -> "#CCFF66";
			case moderate -> "#FFFF00";
			case considerable -> "#FF9900";
			case high -> "#FF0000";
			case very_high -> "#800000";
			default -> "#969696";
		};
	}

	public static String getCAAMLv6String(DangerRating dangerRating) {
		return switch (dangerRating) {
			case missing -> "no_rating";
			case no_rating -> "no_rating";
			case no_snow -> "no_snow";
			case low -> "low";
			case moderate -> "moderate";
			case considerable -> "considerable";
			case high -> "high";
			case very_high -> "very_high";
		};
	}

	public static String getCAAMLv5String(DangerRating dangerRating) {
		return switch (dangerRating) {
			case missing -> "n/a";
			case no_rating -> "n/a";
			case no_snow -> "n/a";
			case low -> "1";
			case moderate -> "2";
			case considerable -> "3";
			case high -> "4";
			case very_high -> "5";
		};
	}

	public static String getString(DangerRating dangerRating) {
		return switch (dangerRating) {
			case missing -> "0";
			case no_rating -> "0";
			case no_snow -> "0";
			case low -> "1";
			case moderate -> "2";
			case considerable -> "3";
			case high -> "4";
			case very_high -> "5";
		};
	}

	public static int getInt(DangerRating dangerRating) {
		return switch (dangerRating) {
			case missing -> 0;
			case no_rating -> 0;
			case no_snow -> 0;
			case low -> 1;
			case moderate -> 2;
			case considerable -> 3;
			case high -> 4;
			case very_high -> 5;
		};
	}

	public double getDouble() {
        return switch (this) {
            case missing -> .0;
            case no_rating -> .0;
            case no_snow -> .0;
            case low -> 1.0 / 1364;
            case moderate -> 1.0 / 1364 * 4.0;
            case considerable -> 1.0 / 1364 * 4.0 * 4.0;
            case high -> 1.0 / 1364 * 4.0 * 4.0 * 4.0;
            case very_high -> 1.0 / 1364 * 4.0 * 4.0 * 4.0 * 4.0;
		};
	}
}
