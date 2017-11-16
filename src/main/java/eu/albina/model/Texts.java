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
@Table(name = "TEXTS")
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
