// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum EventName {
	bulletinUpdate, chatEvent, lockBulletin, unlockBulletin, lockRegion, unlockRegion, login, logout;

	public static EventName fromString(String text) {
		if (text != null) {
			return Arrays.stream(EventName.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}
}
