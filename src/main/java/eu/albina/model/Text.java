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
package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;

import org.json.JSONObject;

import eu.albina.model.enumerations.LanguageCode;

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
