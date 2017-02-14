package org.avalanches.ais.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.avalanches.ais.model.enumerations.GrainShape;
import org.json.JSONObject;

@Embeddable
public class GrainShapes implements AvalancheInformationObject {

	@Column(name = "PRIMARY")
	@Enumerated(EnumType.STRING)
	private GrainShape primary;

	@Column(name = "SECONDARY")
	@Enumerated(EnumType.STRING)
	private GrainShape secondary;

	public GrainShapes() {
	}

	public GrainShapes(JSONObject json) {
		if (json.has("primary"))
			primary = GrainShape.valueOf(json.getString("primary"));
		if (json.has("secondary"))
			secondary = GrainShape.valueOf(json.getString("secondary"));
	}

	public GrainShape getPrimary() {
		return primary;
	}

	public void setPrimary(GrainShape primary) {
		this.primary = primary;
	}

	public GrainShape getSecondary() {
		return secondary;
	}

	public void setSecondary(GrainShape secondary) {
		this.secondary = secondary;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (primary != null)
			json.put("primary", primary.toString());
		if (secondary != null)
			json.put("secondary", secondary.toString());

		return json;
	}

}
