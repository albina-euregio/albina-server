package org.avalanches.ais.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.json.JSONObject;

@Embeddable
public class RamMeasurement implements AvalancheInformationObject {

	@Column(name = "DROP_HEIGHT")
	private double dropHeight;

	@Column(name = "COUNT")
	private int count;

	@Column(name = "PENETRATION_DEPTH")
	private double penetrationDepth;

	public RamMeasurement() {
		dropHeight = Double.NaN;
		count = -1;
		penetrationDepth = Double.NaN;
	}

	public RamMeasurement(JSONObject json) {
		this();

		if (json.has("dropHeight") && !json.isNull("dropHeight"))
			dropHeight = json.getDouble("dropHeight");
		if (json.has("count") && !json.isNull("count"))
			count = json.getInt("count");
		if (json.has("penetrationDepth") && !json.isNull("penetrationDepth"))
			penetrationDepth = json.getDouble("penetrationDepth");
	}

	public double getHeightTop() {
		return dropHeight;
	}

	public void setDropHeight(double dropHeight) {
		this.dropHeight = dropHeight;
	}

	public double getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getPenetrationDepth() {
		return penetrationDepth;
	}

	public void setPenetrationDepth(double penetrationDepth) {
		this.penetrationDepth = penetrationDepth;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (!Double.isNaN(dropHeight))
			json.put("dropHeight", dropHeight);
		if (count > -1)
			json.put("count", count);
		if (!Double.isNaN(penetrationDepth))
			json.put("penetrationDepth", penetrationDepth);

		return json;
	}

}
