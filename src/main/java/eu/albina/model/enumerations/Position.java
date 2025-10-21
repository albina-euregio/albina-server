// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

public enum Position {
	topleft, topright, bottomleft, bottomright;

	public String toString() {
        return switch (this) {
            case topleft -> "topleft";
            case topright -> "topright";
            case bottomleft -> "bottomleft";
            case bottomright -> "bottomright";
		};
	}
}
