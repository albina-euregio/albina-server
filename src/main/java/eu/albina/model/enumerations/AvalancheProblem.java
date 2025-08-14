// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

public enum AvalancheProblem {
	new_snow, wind_slab, persistent_weak_layers, wet_snow, gliding_snow, favourable_situation, cornices, no_distinct_avalanche_problem;

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
		case no_distinct_avalanche_problem:
			return "no_distinct_avalanche_problem";

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
		case no_distinct_avalanche_problem:
			return "no_distinct_avalanche_problem";
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
