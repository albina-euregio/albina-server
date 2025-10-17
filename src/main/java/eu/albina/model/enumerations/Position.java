// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum Position {
	topleft, topright, bottomleft, bottomright;

	public static Position fromString(String text) {
		if (text != null) {
			return Arrays.stream(Position.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toString() {
        return switch (this) {
            case topleft -> "topleft";
            case topright -> "topright";
            case bottomleft -> "bottomleft";
            case bottomright -> "bottomright";
		};
	}
}
