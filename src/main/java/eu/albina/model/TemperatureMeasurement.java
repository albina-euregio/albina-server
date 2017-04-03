package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.json.JSONObject;

@Embeddable
public class TemperatureMeasurement implements AvalancheInformationObject {

	@Column(name = "HEIGHT")
	private double height;

	@Column(name = "TEMPERATURE")
	private double temperature;

	public TemperatureMeasurement() {
		height = Double.NaN;
		temperature = Double.NaN;
	}

	public TemperatureMeasurement(JSONObject json) {
		this();

		if (json.has("height") && !json.isNull("height"))
			height = json.getDouble("height");
		if (json.has("temperature") && !json.isNull("temperature"))
			temperature = json.getDouble("temperature");
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (!Double.isNaN(height))
			json.put("height", height);
		if (!Double.isNaN(temperature))
			json.put("temperature", temperature);

		return json;
	}

}
