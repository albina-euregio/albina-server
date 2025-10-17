// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum ArtificialAvalancheReleaseProbability {
	one, two, three, four;

	public static ArtificialAvalancheReleaseProbability fromString(String text) {
		if (text != null) {
			return Arrays.stream(ArtificialAvalancheReleaseProbability.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
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
