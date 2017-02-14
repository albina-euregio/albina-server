package org.avalanches.albina.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.json.JSONObject;

@Embeddable
public class DensityMeasurement implements AvalancheInformationObject {

	@Column(name = "HEIGHT_TOP")
	private double heightTop;

	@Column(name = "HEIGHT_BOTTOM")
	private double heightBottom;

	@Column(name = "DENSITY")
	private double density;

	public DensityMeasurement() {
		heightTop = Double.NaN;
		heightBottom = Double.NaN;
		density = Double.NaN;
	}

	public DensityMeasurement(JSONObject json) {
		this();

		if (json.has("heightTop") && !json.isNull("heightTop"))
			heightTop = json.getDouble("heightTop");
		if (json.has("heightBottom") && !json.isNull("heightBottom"))
			heightBottom = json.getDouble("heightBottom");
		if (json.has("density") && !json.isNull("density"))
			density = json.getDouble("density");
	}

	public double getHeightTop() {
		return heightTop;
	}

	public void setHeightTop(double heightTop) {
		this.heightTop = heightTop;
	}

	public double getHeightBottom() {
		return heightBottom;
	}

	public void setHeightBottom(double heightBottom) {
		this.heightBottom = heightBottom;
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (!Double.isNaN(heightTop))
			json.put("heightTop", heightTop);
		if (!Double.isNaN(heightBottom))
			json.put("heightBottom", heightBottom);
		if (!Double.isNaN(density))
			json.put("density", density);

		return json;
	}

}
