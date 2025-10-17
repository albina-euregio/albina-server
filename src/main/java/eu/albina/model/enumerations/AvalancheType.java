// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum AvalancheType {
	slab, loose, glide;

	public static AvalancheType fromString(String text) {
		if (text != null) {
			return Arrays.stream(AvalancheType.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlv6String() {
        return switch (this) {
            case slab -> "slab";
            case loose -> "loose";
            case glide -> "glide";
		};
		}
}
