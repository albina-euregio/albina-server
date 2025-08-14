// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import eu.albina.util.XMLResourceBundleControl;

/**
 * The enum contains the ISO 639-1 codes for available languages.
 *
 * @author Norbert Lanzanasto
 */
public enum LanguageCode {
	de(new Locale("de", "AT")),
	it(Locale.ITALIAN),
	en(new Locale("en", "IE")),
	fr(Locale.FRENCH),
	es(new Locale("es")),
	ca(new Locale("ca")),
	oc(new Locale("ca")) {
		private String replaceAranes(String date) {
			date = date.replaceAll("(lunes|dilluns|monday)", "deluns");
			date = date.replaceAll("(martes|dimarts|tuesday)", "dimars");
			date = date.replaceAll("(miércoles|dimecres|wednesday)", "dimèrcles");
			date = date.replaceAll("(jueves|dijous|thursday)", "dijaus");
			date = date.replaceAll("(viernes|divendres|monday)", "diuendres");
			date = date.replaceAll("(sábado|dissabte|saturday)", "dissabte");
			date = date.replaceAll("(domingo|diumenge|sunday)", "dimenge");
			date = date.replaceAll("(enero|gener|january)", "Gèr");
			date = date.replaceAll("(febrero|febrer|february)", "Heruèr");
			date = date.replaceAll("(marzo|març|march)", "Març");
			date = date.replaceAll("(abril|april)", "Abriu");
			date = date.replaceAll("(mayo|maig|may)", "Mai");
			date = date.replaceAll("(junio|juny|june)", "Junh");
			date = date.replaceAll("(julio|juliol|july)", "Junhsèga");
			date = date.replaceAll("(agosto|agost|august)", "Agost");
			date = date.replaceAll("(septiembre|setembre|september)", "Seteme");
			date = date.replaceAll("(octubre|october)", "Octubre");
			date = date.replaceAll("(noviembre|novembre|november)", "Noveme");
			date = date.replaceAll("(diciembre|desembre|december)", "Deseme");
			return date;
		}

		@Override
		public String getDateTime(ZonedDateTime date) {
			return replaceAranes(super.getDateTime(date));
		}

		@Override
		public String getDate(ZonedDateTime date) {
			return replaceAranes(super.getDate(date));
		}

		@Override
		public String getLongDate(ZonedDateTime date) {
			return replaceAranes(super.getLongDate(date));
		}
	};

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

	public String getBundleString(String key, Map<String, String> replacements) {
		String bundleString = getBundleString(key);
		for (Map.Entry<String, String> replacement : replacements.entrySet()) {
			bundleString = bundleString.replace("{" + replacement.getKey() + "}", replacement.getValue());
		}
		if (bundleString.contains("{") || bundleString.contains("}")) {
			throw new IllegalArgumentException("Missing replacements in: " + bundleString);
		}
		return bundleString;
	}

	public static LanguageCode fromString(String text) {
		if (text != null) {
			return Arrays.stream(LanguageCode.values()).filter(type -> text.equalsIgnoreCase(type.toString())).findFirst().orElse(null);
		}
		return null;
	}

	public String getRegionName(String regionId) {
		if ("".equals(regionId)) {
			return "";
		}
		return getBundle("micro-regions_names").getString(regionId);
	}

	public String getDateTime(ZonedDateTime date) {
		return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(getLocale()).format(date);
	}

	public String getDate(ZonedDateTime date) {
		return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale()).format(date);
	}

	public String getLongDate(ZonedDateTime date) {
		return DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(getLocale()).format(date);
	}

}
