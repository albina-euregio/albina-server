// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

import eu.albina.model.enumerations.LanguageCode;

@Serdeable
public record Texts(Set<Text> texts) {

	@JsonValue
	@Override
	public Set<Text> texts() {
		return texts;
	}

	public String getText(LanguageCode languageCode) {
		return texts.stream().filter(text -> text.languageCode() == languageCode).findFirst().map(Text::text).orElse(null);
	}

}
