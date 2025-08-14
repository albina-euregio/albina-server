// SPDX-License-Identifier: AGPL-3.0-or-later
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
