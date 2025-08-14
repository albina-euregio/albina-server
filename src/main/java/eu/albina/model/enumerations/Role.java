// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum Role {
	SUPERADMIN, ADMIN, FORECASTER, FOREMAN, OBSERVER;

	public static Role fromString(String text) {
		if (text != null) {
			return Arrays.stream(Role.values()).filter(role -> text.equalsIgnoreCase(role.toString())).findFirst().orElse(null);
		}
		return null;
	}
}
