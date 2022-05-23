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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "texts")
public class Texts extends AbstractPersistentObject implements AvalancheInformationObject {

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "text_parts", joinColumns = @JoinColumn(name = "TEXTS_ID"))
	private Set<Text> texts;

	public Texts() {
		texts = new LinkedHashSet<>();
	}

	public Texts(JSONArray json) {
		this();
		for (Object entry : json) {
			texts.add(new Text((JSONObject) entry));
		}
	}

	public Set<Text> getTexts() {
		return texts;
	}

	public String getText(LanguageCode languageCode) {
		return texts.stream().filter(text -> text.getLanguage() == languageCode).findFirst().map(Text::getText).orElse(null);
	}

	public void setTexts(Set<Text> texts) {
		this.texts = texts;
	}

	public void addText(Text text) {
		this.texts.add(text);
	}

	public JSONArray toJSONArray() {
		JSONArray array = new JSONArray();
		for (Text text : texts) {
			array.put(text.toJSON());
		}
		return array;
	}

	@Override
	public JSONObject toJSON() {
		return null;
	}
}
