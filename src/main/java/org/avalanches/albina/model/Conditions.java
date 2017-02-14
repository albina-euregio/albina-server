package org.avalanches.albina.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.avalanches.albina.model.enumerations.Aspect;
import org.avalanches.albina.model.enumerations.PrecipitationIntensity;
import org.avalanches.albina.model.enumerations.PrecipitationType;
import org.avalanches.albina.model.enumerations.WindSpeed;
import org.json.JSONObject;

@Embeddable
public class Conditions implements AvalancheInformationObject {

	@Column(name = "CONDITIONS_AIR_TEMPERATURE")
	private double airTemperature;

	@Column(name = "CONDITIONS_CLOUDINESS")
	private int cloudiness;

	@Column(name = "CONDITIONS_PRECIPITATION_TYPE")
	@Enumerated(EnumType.STRING)
	private PrecipitationType precipitationType;

	@Column(name = "CONDITIONS_PRECIPITATION_INTENSITY")
	@Enumerated(EnumType.STRING)
	private PrecipitationIntensity precipitationIntensity;

	@Column(name = "CONDITIONS_WIND_SPEED")
	@Enumerated(EnumType.STRING)
	private WindSpeed windSpeed;

	@Column(name = "CONDITIONS_WIND_DIRECTION")
	@Enumerated(EnumType.STRING)
	private Aspect windDirection;

	public Conditions() {
		airTemperature = Double.NaN;
		cloudiness = -1;
	}

	public Conditions(JSONObject json) {
		this();

		if (json.has("airTemperature") && !json.isNull("airTemperature"))
			airTemperature = json.getDouble("airTemperature");
		if (json.has("cloudiness") && !json.isNull("cloudiness"))
			cloudiness = json.getInt("cloudiness");
		if (json.has("precipitationType") && !json.isNull("precipitationType"))
			precipitationType = PrecipitationType.valueOf(json.getString("precipitationType").toLowerCase());
		if (json.has("precipitationIntensity") && !json.isNull("precipitationIntensity"))
			precipitationIntensity = PrecipitationIntensity
					.valueOf(json.getString("precipitationIntensity").toLowerCase());
		if (json.has("windSpeed") && !json.isNull("windSpeed"))
			windSpeed = WindSpeed.valueOf(json.getString("windSpeed").toLowerCase());
		if (json.has("windDirection") && !json.isNull("windDirection")) {
			// TODO just for testing
			// windDirection =
			// Aspect.valueOf(json.getString("windDirection").toUpperCase());
			String string = json.getString("windDirection");
			if (string.equals("SO"))
				string = "SE";
			else if (string.equals("O"))
				string = "E";
			else if (string.equals("NO"))
				string = "NE";
			windDirection = Aspect.valueOf(string.toUpperCase());
		}
	}

	public double getAirTemperature() {
		return airTemperature;
	}

	public void setAirTemperature(double airTemperature) {
		this.airTemperature = airTemperature;
	}

	public int getCloudiness() {
		return cloudiness;
	}

	public void setCloudiness(int cloudiness) {
		this.cloudiness = cloudiness;
	}

	public PrecipitationType getPrecipitationType() {
		return precipitationType;
	}

	public void setPrecipitationType(PrecipitationType precipitationType) {
		this.precipitationType = precipitationType;
	}

	public PrecipitationIntensity getPrecipitationIntensity() {
		return precipitationIntensity;
	}

	public void setPrecipitationIntensity(PrecipitationIntensity precipitationIntensity) {
		this.precipitationIntensity = precipitationIntensity;
	}

	public WindSpeed getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(WindSpeed windSpeed) {
		this.windSpeed = windSpeed;
	}

	public Aspect getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(Aspect windDirection) {
		this.windDirection = windDirection;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (!Double.isNaN(airTemperature))
			json.put("airTemperature", airTemperature);
		if (cloudiness > -1)
			json.put("cloudiness", cloudiness);
		if (precipitationType != null)
			json.put("precipitationType", precipitationType.toString());
		if (precipitationIntensity != null)
			json.put("precipitationIntensity", precipitationIntensity.toString());
		if (windSpeed != null)
			json.put("windSpeed", windSpeed.toString());
		if (windDirection != null)
			json.put("windDirection", windDirection.toString());

		return json;
	}

}
