/*******************************************************************************
 * Copyright (C) 2024 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.model.enumerations;

import java.util.Arrays;

public enum GlidingSnowActivity {
	low, medium, high;

	public static GlidingSnowActivity fromString(String text) {
		if (text != null) {
			return Arrays.stream(GlidingSnowActivity.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlv6String() {
		switch (this) {
		case low:
			return "low";
		case medium:
			return "medium";
		case high:
			return "high";

		default:
			return null;
		}
	}
}
