// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

public enum AvalancheSize {
	small, medium, large, very_large, extreme;

	public int toInteger() {
		return switch (this) {
			case small -> 1;
			case medium -> 2;
			case large -> 3;
			case very_large -> 4;
			case extreme -> 5;
		};
	}
}
