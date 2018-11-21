package eu.albina.model;

import org.joda.time.DateTime;
import org.json.JSONObject;

import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.util.GlobalVariables;

public class BulletinUpdate {

	private String region;
	private DateTime date;
	private BulletinStatus status;

	public BulletinUpdate(String region, DateTime date, BulletinStatus status) {
		this.region = region;
		this.date = date;
		this.status = status;
	}

	public BulletinUpdate(JSONObject json) {
		if (json.has("region"))
			this.region = json.getString("region");

		if (json.has("date"))
			this.date = new org.joda.time.DateTime(json.getString("date"));

		if (json.has("status"))
			this.status = BulletinStatus.fromString(json.getString("status"));
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public BulletinStatus getStatus() {
		return status;
	}

	public void setStatus(BulletinStatus status) {
		this.status = status;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (region != null)
			json.put("region", region);
		if (date != null)
			json.put("date", date.toString(GlobalVariables.formatterDateTime));
		if (status != null)
			json.put("status", status.toString());

		return json;
	}
}
