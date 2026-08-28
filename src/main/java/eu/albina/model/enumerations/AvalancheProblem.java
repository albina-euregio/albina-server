// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.model.converter.EnumSetToStringConverter;
import eu.albina.util.DataURL;
import eu.albina.util.XMLResourceBundleControl;

public enum AvalancheProblem {
	new_snow, wind_slab, persistent_weak_layers, wet_snow, gliding_snow, favourable_situation, cornices, no_distinct_avalanche_problem;

	public String toString(Locale locale) {
		return ResourceBundle.getBundle("i18n.caaml", locale, new XMLResourceBundleControl())
				.getString("avalancheProblem." + name());
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
		return getSymbolPath(toStringId(), grayscale);
	}

	/** The pictogram of an avalanche problem of the CAAMLv6 model. */
	public static String getSymbolPath(org.caaml.v6.AvalancheProblemType problemType, boolean grayscale) {
		return getSymbolPath(problemType.name(), grayscale);
	}

	private static String getSymbolPath(String problemId, boolean grayscale) {
		if (grayscale)
			return "avalanche_problems/grey/" + problemId + ".png";
		else
			return "avalanche_problems/color/" + problemId + ".png";
	}

	public String getDataURL() {
		return getDataURL(toStringId());
	}

	/** The pictogram of an avalanche problem of the CAAMLv6 model. */
	public static String getDataURL(org.caaml.v6.AvalancheProblemType problemType) {
		return getDataURL(problemType.name());
	}

	private static String getDataURL(String problemId) {
		return DataURL.ofResource("images/avalanche_problems/color/" + problemId + ".webp");
	}

	@jakarta.persistence.Converter
	public static class Converter extends EnumSetToStringConverter<AvalancheProblem> {

		public static final String COLUMN_DEFINITION = "set('new_snow', 'wind_slab', 'persistent_weak_layers', 'wet_snow', 'gliding_snow', 'favourable_situation', 'cornices', 'no_distinct_avalanche_problem')";

		@Override
		protected Class<AvalancheProblem> getEnumClass() {
			return AvalancheProblem.class;
		}
	}
}
