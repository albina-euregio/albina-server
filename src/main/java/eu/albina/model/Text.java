// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;

import eu.albina.model.enumerations.LanguageCode;

import java.util.Comparator;

@Embeddable
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Text implements Comparable<Text> {

	private static final Comparator<Text> COMPARATOR = Comparator.comparing(Text::getLanguage, Comparator.nullsLast(Comparator.naturalOrder()));

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	private LanguageCode languageCode;

	@Lob
	@Column(name = "TEXT")
	private String text;

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
