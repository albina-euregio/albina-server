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

import java.time.Instant;
import com.github.openjson.JSONObject;
import eu.albina.model.enumerations.BulletinStatus;

public class BulletinUpdate {

	private String region;
	private Instant date;
	private BulletinStatus status;

	public BulletinUpdate(String region, Instant date, BulletinStatus status) {
		this.region = region;
		this.date = date;
		this.status = status;
	}

	public BulletinUpdate(JSONObject json) {
		if (json.has("region"))
			this.region = json.getString("region");

		if (json.has("date"))
			this.date = Instant.parse(json.getString("date"));

		if (json.has("status"))
			this.status = BulletinStatus.fromString(json.getString("status"));
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public Instant getDate() {
		return date;
	}

	public void setDate(Instant date) {
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
			json.put("date", date.toString());
		if (status != null)
			json.put("status", status.toString());

		return json;
	}
}
