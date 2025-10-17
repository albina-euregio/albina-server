// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum NaturalAvalancheReleaseProbability {
	one, two, three, four;

	public static NaturalAvalancheReleaseProbability fromString(String text) {
		if (text != null) {
			return Arrays.stream(NaturalAvalancheReleaseProbability.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlv6String() {
		return switch (this) {
			case one -> "1";
			case two -> "2";
			case three -> "3";
			case four -> "4";
		};
	}
}
