package org.avalanches.ais.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import org.json.JSONObject;

@Embeddable
public class Layer implements AvalancheInformationObject {

	@Column(name = "HEIGHT_TOP")
	private double heightTop;

	@Column(name = "HEIGHT_BOTTOM")
	private double heightBottom;

	@Column(name = "WETNESS")
	private int wetness;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "primary", column = @Column(name = "GRAIN_SHAPE_PRIMARY")),
			@AttributeOverride(name = "secondary", column = @Column(name = "GRAIN_SHAPE_SECONDARY")) })
	private GrainShapes grainShapes;

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "min", column = @Column(name = "GRAIN_SIZE_MIN")),
			@AttributeOverride(name = "max", column = @Column(name = "GRAIN_SIZE_MAX")) })
	private GrainSizes grainSizes;

	@Column(name = "HARDNESS")
	private int hardness;

	public Layer() {
		heightTop = Double.NaN;
		heightBottom = Double.NaN;
		wetness = -1;
		hardness = -1;
	}

	public Layer(JSONObject json) {
		this();

		if (json.has("heightTop") && !json.isNull("heightTop"))
			heightTop = json.getDouble("heightTop");
		if (json.has("heightBottom") && !json.isNull("heightBottom"))
			heightBottom = json.getDouble("heightBottom");
		if (json.has("wetness") && !json.isNull("wetness"))
			wetness = json.getInt("wetness");
		if (json.has("grainShapes") && !json.isNull("grainShapes"))
			grainShapes = new GrainShapes(json.getJSONObject("grainShapes"));
		if (json.has("grainSizes") && !json.isNull("grainSizes"))
			grainSizes = new GrainSizes(json.getJSONObject("grainSizes"));
		if (json.has("hardness") && !json.isNull("hardness"))
			hardness = json.getInt("hardness");
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

	public int getWetness() {
		return wetness;
	}

	public void setWetness(int wetness) {
		this.wetness = wetness;
	}

	public GrainShapes getGrainShapes() {
		return grainShapes;
	}

	public void setGrainShapes(GrainShapes grainShapes) {
		this.grainShapes = grainShapes;
	}

	public GrainSizes getGrainSizes() {
		return grainSizes;
	}

	public void setGrainSizes(GrainSizes grainSizes) {
		this.grainSizes = grainSizes;
	}

	public int getHardness() {
		return hardness;
	}

	public void setHardness(int hardness) {
		this.hardness = hardness;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (!Double.isNaN(heightTop))
			json.put("heightTop", heightTop);
		if (!Double.isNaN(heightBottom))
			json.put("heightBottom", heightBottom);
		if (wetness > -1)
			json.put("wetness", wetness);
		if (grainShapes != null)
			json.put("grainShapes", grainShapes.toJSON());
		if (grainSizes != null)
			json.put("grainSizes", grainSizes.toJSON());
		if (hardness > -1)
			json.put("hardness", hardness);

		return json;
	}

}
