package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.json.JSONObject;

@Embeddable
public class GrainSizes implements AvalancheInformationObject {

	@Column(name = "MIN")
	private double min;

	@Column(name = "MAX")
	private double max;

	public GrainSizes() {
		min = Double.NaN;
		max = Double.NaN;
	}

	public GrainSizes(JSONObject json) {
		this();

		if (json.has("min"))
			min = json.getDouble("min");
		if (json.has("max"))
			max = json.getDouble("max");
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (!Double.isNaN(min))
			json.put("min", min);
		if (!Double.isNaN(max))
			json.put("max", max);

		return json;
	}

}
