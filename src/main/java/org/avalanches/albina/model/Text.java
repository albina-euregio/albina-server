package org.avalanches.albina.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;

import org.avalanches.albina.model.enumerations.LanguageCode;
import org.json.JSONObject;

@Embeddable
public class Text implements AvalancheInformationObject {

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE")
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
		if (text != null && text != "")
			json.put("text", this.text);

		return json;
	}
}
