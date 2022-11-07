/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
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
import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

public enum AvalancheProblem {
	new_snow, wind_slab, persistent_weak_layers, wet_snow, gliding_snow, favourable_situation, cornices, no_distinct_problem;

	public String toString(Locale locale) {

		return ResourceBundle.getBundle("i18n.AvalancheProblem", locale, new XMLResourceBundleControl())
				.getString(name());
	}

	public static AvalancheProblem fromString(String text) {
		if (text != null) {
			return Arrays.stream(AvalancheProblem.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String toCaamlv6String() {
		switch (this) {
		case new_snow:
			return "new_snow";
		case wind_slab:
			return "wind_slab";
		case persistent_weak_layers:
			return "persistent_weak_layers";
		case wet_snow:
			return "wet_snow";
		case gliding_snow:
			return "gliding_snow";
		case favourable_situation:
			return "favourable_situation";
		case cornices:
			return "cornices";
		case no_distinct_problem:
			return "no_distinct_problem";

		default:
			return null;
		}
	}

	public String toStringId() {
		switch (this) {
		case new_snow:
			return "new_snow";
		case wind_slab:
			return "wind_slab";
		case persistent_weak_layers:
			return "persistent_weak_layers";
		case wet_snow:
			return "wet_snow";
		case gliding_snow:
			return "gliding_snow";
		case favourable_situation:
			return "favourable_situation";
		case cornices:
			return "cornices";
		case no_distinct_problem:
			return "no_distinct_problem";
		default:
			return "";
		}
	}

	public String getSymbolPath(boolean grayscale) {
		if (grayscale)
			return "avalanche_problems/grey/" + toStringId() + ".png";
		else
			return "avalanche_problems/color/" + toStringId() + ".png";
	}

}
