// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.util.Locale;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

public enum Frequency {
	none, few, some, many;

	public String toString(Locale locale) {
		return ResourceBundle.getBundle("i18n.caaml", locale, new XMLResourceBundleControl())
				.getString("frequency." + name());
	}
}
