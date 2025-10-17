// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

public enum AvalancheProblem {
	new_snow, wind_slab, persistent_weak_layers, wet_snow, gliding_snow, favourable_situation, cornices, no_distinct_avalanche_problem;

	public String toString(Locale locale) {

		return ResourceBundle.getBundle("i18n.AvalancheProblem", locale, new XMLResourceBundleControl())
				.getString(name());
	}

	public String toStringId() {
        return switch (this) {
            case new_snow -> "new_snow";
            case wind_slab -> "wind_slab";
            case persistent_weak_layers -> "persistent_weak_layers";
            case wet_snow -> "wet_snow";
            case gliding_snow -> "gliding_snow";
            case favourable_situation -> "favourable_situation";
            case cornices -> "cornices";
            case no_distinct_avalanche_problem -> "no_distinct_avalanche_problem";
		};
	}

	public String getSymbolPath(boolean grayscale) {
		if (grayscale)
			return "avalanche_problems/grey/" + toStringId() + ".png";
		else
			return "avalanche_problems/color/" + toStringId() + ".png";
	}

}
