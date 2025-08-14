// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum StrategicMindset {
	assessment, stepping_out, status_quo, stepping_back, entrenchment, free_ride, high_alert, spring_diurnal;

	public static StrategicMindset fromString(String text) {
		if (text != null) {
			return Arrays.stream(StrategicMindset.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}
}
