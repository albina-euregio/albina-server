package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Type;
import org.json.JSONObject;

import eu.albina.model.enumerations.Quality;
import eu.albina.util.GlobalVariables;

@Embeddable
public class DateTime implements AvalancheInformationObject {

	@Column(name = "DATETIME")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime dateTime;

	@Column(name = "QUALITY")
	@Enumerated(EnumType.STRING)
	private Quality quality;

	public DateTime() {
	}

	public DateTime(JSONObject json) {
		if (json.has("date") && !json.isNull("date"))
			dateTime = new org.joda.time.DateTime(json.getString("date"));
		if (json.has("quality") && !json.isNull("quality"))
			quality = Quality.valueOf(json.getString("quality").toLowerCase());
	}

	public org.joda.time.DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(org.joda.time.DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Quality getQuality() {
		return quality;
	}

	public void setQuality(Quality quality) {
		this.quality = quality;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (dateTime != null)
			json.put("date", dateTime.toString(GlobalVariables.formatterDateTime));
		if (quality != null)
			json.put("quality", quality.toString());

		return json;
	}

}
