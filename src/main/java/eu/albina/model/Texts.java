// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "texts")
public class Texts extends AbstractPersistentObject implements AvalancheInformationObject {

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "text_parts", joinColumns = @JoinColumn(name = "TEXTS_ID"))
	@JsonValue
	private Set<Text> texts;

	public Texts() {
		texts = new TreeSet<>(); // sort texts by language to allow caching of API calls
	}

	@JsonCreator
	public Texts(Set<Text> texts) {
		this.texts = texts;
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
		for (Text text : new TreeSet<>(texts)) {
			array.put(text.toJSON());
		}
		return array;
	}

	@Override
	public JSONObject toJSON() {
		return null;
	}
}
