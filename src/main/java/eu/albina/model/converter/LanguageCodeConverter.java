package eu.albina.model.converter;

import eu.albina.model.enumerations.LanguageCode;
import jakarta.persistence.Converter;

@Converter
public class LanguageCodeConverter extends EnumSetToStringConverter<LanguageCode> {
	@Override
	protected Class<LanguageCode> getEnumClass() {
		return LanguageCode.class;
	}
}
