// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;

import com.github.openjson.JSONObject;

import com.google.common.base.Strings;
import eu.albina.model.enumerations.LanguageCode;

import java.util.Comparator;

@Embeddable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Text implements AvalancheInformationObject, Comparable<Text> {

	private static final Comparator<Text> COMPARATOR = Comparator.comparing(Text::getLanguage, Comparator.nullsLast(Comparator.naturalOrder()));

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	private LanguageCode languageCode;

	@Lob
	@Column(name = "TEXT")
	private String text;

	public Text() {
	}

	public Text(JSONObject json) {
		this();

		if (json.has("languageCode") && !json.isNull("languageCode"))
			this.languageCode = LanguageCode.valueOf((json.getString("languageCode").toLowerCase()));
		if (json.has("text") && !json.isNull("text"))
			this.text = json.getString("text");
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
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (languageCode != null)
			json.put("languageCode", this.languageCode.toString());
		if (!Strings.isNullOrEmpty(text))
			json.put("text", this.text);

		return json;
	}

	@Override
	public int compareTo(Text o) {
		return COMPARATOR.compare(this, o);
	}
}
