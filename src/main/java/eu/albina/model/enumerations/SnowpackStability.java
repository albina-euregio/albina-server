// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

public enum SnowpackStability {
	good, fair, poor, very_poor;

	public String toString(Locale locale) {
		return ResourceBundle.getBundle("i18n.caaml", locale, new XMLResourceBundleControl())
				.getString("snowpackStability." + name());
	}
}
