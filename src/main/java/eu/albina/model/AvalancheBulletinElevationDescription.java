package eu.albina.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheProblem;
import eu.albina.model.enumerations.DangerRating;

@Entity
@Table(name = "AVALANCHE_BULLETIN_ELEVATION_DESCRIPTIONS")
public class AvalancheBulletinElevationDescription extends AbstractPersistentObject
		implements AvalancheInformationObject {

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING")
	private DangerRating dangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_PROBLEM")
	private AvalancheProblem avalancheProblem;

	@ElementCollection
	@CollectionTable(name = "AVALANCHE_BULLETIN_ELEVATION_DESCRIPTION_ASPECTS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ELEVATION_DESCRIPTION_ID", referencedColumnName = "ID"))
	@Column(name = "ASPECT")
	private List<Aspect> aspects;

	public AvalancheBulletinElevationDescription() {
		this.aspects = new ArrayList<Aspect>();
	}

	public AvalancheBulletinElevationDescription(JSONObject json) {
		this();

		if (json.has("dangerRating"))
			this.dangerRating = DangerRating.valueOf(json.getString("dangerRating").toLowerCase());
		if (json.has("avalancheProblem"))
			this.avalancheProblem = AvalancheProblem.valueOf(json.getString("avalancheProblem").toLowerCase());
		if (json.has("aspects")) {
			JSONArray aspects = json.getJSONArray("aspects");
			for (Object entry : aspects) {
				this.aspects.add(Aspect.valueOf(((String) entry).toUpperCase()));
			}
		}
	}

	public DangerRating getDangerRating() {
		return dangerRating;
	}

	public void setDangerRating(DangerRating dangerRating) {
		this.dangerRating = dangerRating;
	}

	public AvalancheProblem getAvalancheProblem() {
		return avalancheProblem;
	}

	public void setAvalancheProblem(AvalancheProblem avalancheProblem) {
		this.avalancheProblem = avalancheProblem;
	}

	public List<Aspect> getAspects() {
		return aspects;
	}

	public void setAspects(List<Aspect> aspects) {
		this.aspects = aspects;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (dangerRating != null)
			json.put("dangerRating", this.dangerRating.toString());
		if (avalancheProblem != null)
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
