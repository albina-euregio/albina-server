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

	public static String getCAAMLv6String(DangerPattern dangerPattern) {
		switch (dangerPattern) {
		case dp1:
			return "dp1";
		case dp2:
			return "dp2";
		case dp3:
			return "dp3";
		case dp4:
			return "dp4";
		case dp5:
			return "dp5";
		case dp6:
			return "dp6";
		case dp7:
			return "dp7";
		case dp8:
			return "dp8";
		case dp9:
			return "dp9";
		case dp10:
			return "dp10";

		default:
			return null;
		}
	}
}
