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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The enum contains the ISO 639-1 codes for available languages.
 *
 * @author Norbert Lanzanasto
 *
 */
public enum LanguageCode {
	de(Locale.GERMAN), it(Locale.ITALIAN), en(Locale.ENGLISH), fr(Locale.FRENCH), es(new Locale("es")), ca(
			new Locale("ca")), oc(new Locale("oc"));

	private final Locale locale;

	LanguageCode(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}

	public ResourceBundle getBundle(String name) {
		return ResourceBundle.getBundle(name, locale);
	}

	public static LanguageCode fromString(String text) {
		if (text != null) {
			for (LanguageCode type : LanguageCode.values()) {
				if (text.equalsIgnoreCase(type.toString()))
					return type;
			}
		}
		return null;
	}
}
