package org.avalanches.ais.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.avalanches.ais.model.enumerations.FractureType;
import org.avalanches.ais.model.enumerations.PropagationType;
import org.avalanches.ais.model.enumerations.StabilityTestType;
import org.json.JSONArray;
import org.json.JSONObject;

@Embeddable
public class StabilityTest implements AvalancheInformationObject {

	@Enumerated(EnumType.STRING)
	@Column(name = "STABILITY_TEST_TYPE")
	private StabilityTestType stabilityTestType;

	@Enumerated(EnumType.STRING)
	@Column(name = "PROPAGATION_TYPE")
	private PropagationType propagationType;

	@Column(name = "STEP")
	private int step;

	@Column(name = "HEIGHT")
	private double height;

	@Enumerated(EnumType.STRING)
	@Column(name = "FRACTURE_TYPE")
	private FractureType fractureType;

	@OneToOne
	@JoinColumn(name = "COMMENT_ID")
	private Texts comment;

	public StabilityTest() {
		step = -1;
		height = Double.NaN;
	}

	public StabilityTest(JSONObject json) {
		this();

		if (json.has("stabilityTestType") && !json.isNull("stabilityTestType"))
			stabilityTestType = StabilityTestType.valueOf(json.getString("stabilityTestType").toUpperCase());
		if (json.has("propagationType") && !json.isNull("propagationType"))
			propagationType = PropagationType.valueOf(json.getString("propagationType").toUpperCase());
		if (json.has("step") && !json.isNull("step"))
			step = json.getInt("step");
		if (json.has("height") && !json.isNull("height"))
			height = json.getDouble("height");
		if (json.has("fractureType") && !json.isNull("fractureType"))
			fractureType = FractureType.valueOf(json.getString("fractureType"));
		if (json.has("comment") && !json.isNull("comment"))
			comment = new Texts((JSONArray) json.getJSONArray("comment"));
	}

	public StabilityTestType getStabilityTestType() {
		return stabilityTestType;
	}

	public void setStabilityTestType(StabilityTestType stabilityTestType) {
		this.stabilityTestType = stabilityTestType;
	}

	public PropagationType getPropagationType() {
		return propagationType;
	}

	public void setPropagationType(PropagationType propagationType) {
		this.propagationType = propagationType;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public FractureType getFractureType() {
		return fractureType;
	}

	public void setFractureType(FractureType fractureType) {
		this.fractureType = fractureType;
	}

	public Texts getComment() {
		return comment;
	}

	public void setComment(Texts comment) {
		this.comment = comment;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (stabilityTestType != null)
			json.put("stabilityTestType", stabilityTestType.toString());
		if (propagationType != null)
			json.put("propagationType", propagationType.toString());
		if (step > -1)
			json.put("step", step);
		if (!Double.isNaN(height))
			json.put("height", height);
		if (fractureType != null)
			json.put("fractureType", fractureType.toString());
		if (comment != null) {
			json.put("comment", comment.toJSONArray());
		}

		return json;
	}

}
