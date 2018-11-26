package eu.albina.model;

import org.joda.time.DateTime;
import org.json.JSONObject;

import eu.albina.util.GlobalVariables;

public class RegionLock {

	private String sessionId;
	private String username;
	private String region;
	private DateTime date;
	private boolean lock;

	public RegionLock(String username, String region, DateTime date, boolean lock) {
		this.username = username;
		this.region = region;
		this.date = date;
		this.lock = lock;
	}

	public RegionLock(JSONObject json) {
		if (json.has("sessionId"))
			this.sessionId = json.getString("sessionId");
		if (json.has("username"))
			this.username = json.getString("username");
		if (json.has("region"))
			this.region = json.getString("region");
		if (json.has("date"))
			this.date = new org.joda.time.DateTime(json.getString("date"));
		this.lock = json.getBoolean("lock");
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public boolean getLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (sessionId != null)
			json.put("sessionId", sessionId);
		if (username != null)
			json.put("username", username);
		if (region != null)
			json.put("region", region);
		if (date != null)
			json.put("date", date.toString(GlobalVariables.formatterDateTime));
		json.put("lock", lock);

		return json;
	}
}
