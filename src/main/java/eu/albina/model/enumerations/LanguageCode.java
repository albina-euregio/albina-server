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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import eu.albina.util.XMLResourceBundleControl;

/**
 * The enum contains the ISO 639-1 codes for available languages.
 *
 * @author Norbert Lanzanasto
 */
public enum LanguageCode {
	de(Locale.GERMAN), it(Locale.ITALIAN), en(Locale.ENGLISH), fr(Locale.FRENCH), es(new Locale("es")), ca(
			new Locale("ca")), oc(new Locale("oc"));

	// LANG
	public static Set<LanguageCode> ENABLED = Collections.unmodifiableSet(EnumSet.of(de, it, en, fr, es, ca, oc));

	// LANG
	public static Set<LanguageCode> SOCIAL_MEDIA = Collections.unmodifiableSet(EnumSet.of(de, it, en));

	private final Locale locale;

	LanguageCode(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}

	public ResourceBundle getBundle(String name) {
		return ResourceBundle.getBundle(name, locale, new XMLResourceBundleControl());
	}

	public String getBundleString(String key) {
		return getBundle("i18n.MessagesBundle").getString(key);
	}

	public static LanguageCode fromString(String text) {
		if (text != null) {
			return Arrays.stream(LanguageCode.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}
}
