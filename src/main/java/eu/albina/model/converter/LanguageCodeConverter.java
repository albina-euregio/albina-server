// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.converter;

import eu.albina.model.enumerations.LanguageCode;
import jakarta.persistence.Converter;

@Converter
public class LanguageCodeConverter extends EnumSetToStringConverter<LanguageCode> {

	public static final String COLUMN_DEFINITION = "set('de', 'it', 'en', 'fr', 'es', 'ca', 'oc')";

	@Override
	protected Class<LanguageCode> getEnumClass() {
		return LanguageCode.class;
	}
}
