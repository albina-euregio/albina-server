// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum DangerSourceVariantType {
	forecast, analysis;

	public static DangerSourceVariantType fromString(String text) {
		if (text != null) {
			return Arrays.stream(DangerSourceVariantType.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}
}
