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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.model.enumerations.LanguageCode;

@Audited
@Entity
@Table(name = "texts")
public class Texts extends AbstractPersistentObject implements AvalancheInformationObject {

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "TEXT_PARTS", joinColumns = @JoinColumn(name = "TEXTS_ID"))
	private Set<Text> texts;

	public Texts() {
		texts = new HashSet<Text>();
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
		for (Text text : texts) {
			if (text.getLanguage() == languageCode)
				return text.getText();
		}
		return null;
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
