package eu.albina.model;

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

import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheProblem;

@Entity
@Table(name = "AVALANCHE_PROBLEM_DESCRIPTIONS")
public class AvalancheBulletinElevationDescription extends AbstractPersistentObject implements AvalancheInformationObject {

	@Column(name = "DANGER_RATING")
	private int dangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_PROBLEM")
	private AvalancheProblem avalancheProblem;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "AVALANCHE_PROBLEM_DESCRIPTION_ASPECTS", joinColumns = @JoinColumn(name = "AVALANCHE_PROBLEM_DESCRIPTION_ID"))
	@Column(name = "ASPECT")
	@Enumerated(EnumType.STRING)
	private Set<Aspect> aspects;

	public AvalancheBulletinElevationDescription() {
		this.aspects = new HashSet<Aspect>();
	}

	public AvalancheBulletinElevationDescription(JSONObject json) {
		this();

		this.dangerRating = json.getInt("dangerRating");
		this.avalancheProblem = AvalancheProblem.valueOf(json.getString("avalancheProblem").toLowerCase());
		JSONArray aspects = json.getJSONArray("aspects");
		for (Object entry : aspects) {
			this.aspects.add(Aspect.valueOf(((String) entry).toUpperCase()));
		}
	}

	public int getDangerRating() {
		return dangerRating;
	}

	public void setDangerRating(int dangerRating) {
		this.dangerRating = dangerRating;
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

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("dangerRating", this.dangerRating);
		json.put("avalancheProblem", this.avalancheProblem.toString());

		if (aspects != null && aspects.size() > 0) {
			JSONArray aspects = new JSONArray();
			for (Aspect aspect : this.aspects) {
				aspects.put(aspect.toString());
			}
			json.put("aspects", aspects);
		}

		return json;
	}

}
