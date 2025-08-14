// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.Instant;
import com.github.openjson.JSONObject;

public class BulletinLock {

	private String sessionId;
	private boolean lock;
	private String bulletin;
	private String userEmail;
	private String userName;
	private Instant date;

	public BulletinLock(String sessionId, String bulletin, Instant date, boolean lock, String userEmail, String userName) {
		this.sessionId = sessionId;
		this.bulletin = bulletin;
		this.date = date;
		this.userEmail = userEmail;
		this.userName = userName;
		this.lock = lock;
	}

	public BulletinLock(JSONObject json) {
		if (json.has("sessionId"))
			this.sessionId = json.getString("sessionId");

		if (json.has("bulletin"))
			this.bulletin = json.getString("bulletin");

		if (json.has("date"))
			this.date = Instant.parse(json.getString("date"));

		if (json.has("userEmail"))
			this.userEmail = json.getString("userEmail");

		if (json.has("userName"))
			this.userName = json.getString("userName");

		if (json.has("lock"))
			this.lock = json.getBoolean("lock");
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getBulletin() {
		return bulletin;
	}

	public void setBulletin(String bulletin) {
		this.bulletin = bulletin;
	}

	public Instant getDate() {
		return date;
	}

	public void setDate(Instant date) {
		this.date = date;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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
		if (bulletin != null)
			json.put("bulletin", bulletin);
		if (date != null)
			json.put("date", date.toString());
		if (userEmail != null)
			json.put("userEmail", userEmail);
		if (userName != null)
			json.put("userName", userName);
		json.put("lock", lock);

		return json;
	}
}
