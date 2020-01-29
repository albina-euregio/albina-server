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

import org.joda.time.DateTime;
import org.json.JSONObject;

import eu.albina.util.GlobalVariables;

public class BulletinLock {

	private String sessionId;
	private boolean lock;
	private String bulletin;
	private DateTime date;

	public BulletinLock(String sessionId, String bulletin, DateTime date, boolean lock) {
		this.sessionId = sessionId;
		this.bulletin = bulletin;
		this.date = date;
		this.lock = lock;
	}

	public BulletinLock(JSONObject json) {
		if (json.has("sessionId"))
			this.sessionId = json.getString("sessionId");

		if (json.has("bulletin"))
			this.bulletin = json.getString("bulletin");

		if (json.has("date"))
			this.date = new org.joda.time.DateTime(json.getString("date"));

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
		if (bulletin != null)
			json.put("bulletin", bulletin);
		if (date != null)
			json.put("date", date.toString(GlobalVariables.formatterDateTime));
		json.put("lock", lock);

		return json;
	}
}
