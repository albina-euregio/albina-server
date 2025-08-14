// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum DangerRatingModificator {
	minus, equal, plus;

	public static DangerRatingModificator fromString(String text) {
		if (text != null) {
			return Arrays.stream(DangerRatingModificator.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}
}
