package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.json.JSONObject;

@Embeddable
public class Geo implements AvalancheInformationObject {

	@Column(name = "LATITUDE")
	private double latitude;

	@Column(name = "LONGITUDE")
	private double longitude;

	public Geo() {
		latitude = Double.NaN;
		longitude = Double.NaN;
	}

	public Geo(JSONObject json) {
		this();

		if (json.has("latitude"))
			this.setLatitude(json.getDouble("latitude"));
		if (json.has("longitude"))
			this.setLongitude(json.getDouble("longitude"));
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (!Double.isNaN(latitude))
			json.put("latitude", this.latitude);
		if (!Double.isNaN(longitude))
			json.put("longitude", this.longitude);

		return json;
	}
}
