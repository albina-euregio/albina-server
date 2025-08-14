// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum HazardSiteDistribution {
	single, some, many, many_most, moderately_steep;

	public static HazardSiteDistribution fromString(String text) {
		if (text != null) {
			return Arrays.stream(HazardSiteDistribution.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlv6String() {
		switch (this) {
		case single:
			return "1";
		case some:
			return "2";
		case many:
			return "3";
		case many_most:
			return "4";
		case moderately_steep:
			return "5";

		default:
			return null;
		}
	}
}
