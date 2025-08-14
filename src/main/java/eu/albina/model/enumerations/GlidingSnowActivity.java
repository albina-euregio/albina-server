// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum GlidingSnowActivity {
	low, medium, high;

	public static GlidingSnowActivity fromString(String text) {
		if (text != null) {
			return Arrays.stream(GlidingSnowActivity.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlv6String() {
		switch (this) {
		case low:
			return "low";
		case medium:
			return "medium";
		case high:
			return "high";

		default:
			return null;
		}
	}
}
