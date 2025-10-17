// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

public enum DangerPattern {
	dp1, dp2, dp3, dp4, dp5, dp6, dp7, dp8, dp9, dp10;

	public String toString(Locale locale) {
		return ResourceBundle.getBundle("i18n.DangerPattern", locale, new XMLResourceBundleControl()).getString(name());
	}

	public static DangerPattern fromString(String text) {
		if (text != null) {
			return Arrays.stream(DangerPattern.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public static String getCAAMLv5String(DangerPattern dangerPattern) {
		return switch (dangerPattern) {
			case dp1 -> "DP1";
			case dp2 -> "DP2";
			case dp3 -> "DP3";
			case dp4 -> "DP4";
			case dp5 -> "DP5";
			case dp6 -> "DP6";
			case dp7 -> "DP7";
			case dp8 -> "DP8";
			case dp9 -> "DP9";
			case dp10 -> "DP10";
		};
	}

	public static String getCAAMLv6String(DangerPattern dangerPattern) {
        return switch (dangerPattern) {
            case dp1 -> "dp1";
            case dp2 -> "dp2";
            case dp3 -> "dp3";
            case dp4 -> "dp4";
            case dp5 -> "dp5";
            case dp6 -> "dp6";
            case dp7 -> "dp7";
            case dp8 -> "dp8";
            case dp9 -> "dp9";
            case dp10 -> "dp10";
		};
	}
}
