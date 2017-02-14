package org.avalanches.ais.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.avalanches.ais.model.enumerations.Aspect;
import org.avalanches.ais.model.enumerations.AvalancheProblem;
import org.avalanches.ais.model.enumerations.DangerZone;
import org.json.JSONArray;
import org.json.JSONObject;

@Entity
@Table(name = "AVALANCHE_PROBLEM_DESCRIPTIONS")
public class AvalancheProblemDescription extends AbstractPersistentObject implements AvalancheInformationObject {

	@Column(name = "ELEVATION_LIMIT")
	private Integer elevationLimit;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_ZONE")
	private DangerZone dangerZone;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_PROBLEM")
	private AvalancheProblem avalancheProblem;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "AVALANCHE_PROBLEM_DESCRIPTION_ASPECTS", joinColumns = @JoinColumn(name = "AVALANCHE_PROBLEM_DESCRIPTION_ID"))
	@Column(name = "ASPECT")
	@Enumerated(EnumType.STRING)
	private Set<Aspect> aspects;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "AVALANCHE_PROBLEM_DESCRIPTION_COMMENTS", joinColumns = @JoinColumn(name = "AVALANCHE_PROBLEM_DESCRIPTION_ID"))
	@Column(name = "COMMENT")
	private Set<Text> comment;

	public AvalancheProblemDescription() {
		this.aspects = new HashSet<Aspect>();
		this.comment = new HashSet<Text>();
	}

	public AvalancheProblemDescription(JSONObject json) {
		this();

		if (json.has("elevationLimit"))
			this.elevationLimit = json.getInt("elevationLimit");

		if (json.has("dangerZone"))
			this.dangerZone = DangerZone.valueOf(json.getString("dangerZone").toLowerCase());

		this.avalancheProblem = AvalancheProblem.valueOf(json.getString("avalancheProblem").toLowerCase());

		JSONArray aspects = json.getJSONArray("aspects");
		for (Object entry : aspects) {
			this.aspects.add(Aspect.valueOf(((String) entry).toUpperCase()));
		}

		if (json.has("comment")) {
			JSONArray comments = json.getJSONArray("comment");
			for (Object entry : comments) {
				this.comment.add(new Text((JSONObject) entry));
			}
		}
	}

	public int getElevationLimit() {
		return elevationLimit;
	}

	public void setElevationLimit(int elevationLimit) {
		this.elevationLimit = elevationLimit;
	}

	public DangerZone getDangerZone() {
		return dangerZone;
	}

	public void setDangerZone(DangerZone dangerZone) {
		this.dangerZone = dangerZone;
	}

	public AvalancheProblem getAvalancheProblem() {
		return avalancheProblem;
	}

	public void setAvalancheProblem(AvalancheProblem avalancheProblem) {
		this.avalancheProblem = avalancheProblem;
	}

	public Set<Aspect> getAspects() {
		return aspects;
	}

	public void setAspects(Set<Aspect> aspects) {
		this.aspects = aspects;
	}

	public Set<Text> getComment() {
		return comment;
	}

	public void setComment(Set<Text> comment) {
		this.comment = comment;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (elevationLimit != null)
			json.put("elevationLimit", elevationLimit);
		if (dangerZone != null)
			json.put("dangerZone", this.dangerZone.toString());
		json.put("avalancheProblem", this.avalancheProblem.toString());

		if (aspects != null && aspects.size() > 0) {
			JSONArray aspects = new JSONArray();
			for (Aspect aspect : this.aspects) {
				aspects.put(aspect.toString());
			}
			json.put("aspects", aspects);
		}

		if (comment != null && comment.size() > 0) {
			JSONArray comments = new JSONArray();
			for (Text comment : this.comment) {
				comments.put(comment.toJSON());
			}
			json.put("comment", comments);
		}

		return json;
	}

}
