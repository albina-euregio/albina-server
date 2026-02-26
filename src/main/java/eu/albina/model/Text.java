// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import eu.albina.model.enumerations.LanguageCode;

import java.util.Comparator;

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Text implements Comparable<Text> {

	private static final Comparator<Text> COMPARATOR = Comparator.comparing(Text::getLanguage, Comparator.nullsLast(Comparator.naturalOrder()));

	private LanguageCode languageCode;
	private String text;

	public Text() {
	}

	public Text(LanguageCode languageCode, String text) {
		this.languageCode = languageCode;
		this.text = text;
	}

	@JsonProperty("languageCode")
	public LanguageCode getLanguage() {
		return languageCode;
	}

	public void setLanguage(LanguageCode language) {
		this.languageCode = language;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int compareTo(Text o) {
		return COMPARATOR.compare(this, o);
	}
}
