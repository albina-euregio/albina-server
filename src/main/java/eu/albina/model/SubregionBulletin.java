package eu.albina.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.json.JSONArray;
import org.json.JSONObject;

@Entity
@Table(name = "SUBREGION_BULLETINS")
public class SubregionBulletin extends AbstractPersistentObject implements AvalancheInformationObject {

	@Column(name = "ELEVATION")
	private int elevation;

	@Column(name = "SUBREGION")
	private String subregion;

	@Column(name = "DANGER_RATING_ABOVE")
	private int dangerRatingAbove;

	@Column(name = "DANGER_RATING_BELOW")
	private int dangerRatingBelow;

	/** The relevant danger patterns */
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "SUBREGION_BULLETIN_DANGER_PATTERNS", joinColumns = @JoinColumn(name = "SUBREGION_BULLETIN_ID"))
	@Column(name = "DANGER_PATTERN")
	private Set<Integer> dangerPatterns;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "SUBREGION_BULLETIN_AVALANCHE_PROBLEM_DESCRIPTIONS", joinColumns = @JoinColumn(name = "SUBREGION_BULLETIN_ID"), inverseJoinColumns = @JoinColumn(name = "AVALANCHE_PROBLEM_DESCRIPTION_ID"))
	private Set<AvalancheProblemDescription> avalancheProblemDescriptions;

	public SubregionBulletin() {
		this.dangerPatterns = new HashSet<Integer>();
		this.avalancheProblemDescriptions = new HashSet<AvalancheProblemDescription>();
	}

	public SubregionBulletin(JSONObject json) {
		this();
		this.elevation = json.getInt("elevation");
		this.subregion = json.getString("subregion");

		JSONObject dangerRatings = json.getJSONObject("dangerRatings");
		this.dangerRatingAbove = dangerRatings.getInt("above");
		this.dangerRatingBelow = dangerRatings.getInt("below");

		if (json.has("dangerPatterns")) {
			JSONArray dangerPatterns = json.getJSONArray("dangerPatterns");
			for (Object entry : dangerPatterns) {
				this.dangerPatterns.add((Integer) entry);
			}
		}

		JSONArray avalancheProblemDescriptions = json.getJSONArray("problemDescriptions");
		for (Object entry : avalancheProblemDescriptions) {
			this.avalancheProblemDescriptions.add(new AvalancheProblemDescription((JSONObject) entry));
		}
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public String getSubregion() {
		return subregion;
	}

	public void setSubregion(String subregion) {
		this.subregion = subregion;
	}

	public int getDangerRatingAbove() {
		return dangerRatingAbove;
	}

	public void setDangerRatingAbove(int dangerRatingAbove) {
		this.dangerRatingAbove = dangerRatingAbove;
	}

	public int getDangerRatingBelow() {
		return dangerRatingBelow;
	}

	public void setDangerRatingBelow(int dangerRatingBelow) {
		this.dangerRatingBelow = dangerRatingBelow;
	}

	public Set<Integer> getDangerPatterns() {
		return dangerPatterns;
	}

	public void setDangerPatterns(Set<Integer> dangerPatterns) {
		this.dangerPatterns = dangerPatterns;
	}

	public Set<AvalancheProblemDescription> getAvalancheProblemDescriptions() {
		return avalancheProblemDescriptions;
	}

	public void setAvalancheProblemDescriptions(Set<AvalancheProblemDescription> avalancheProblemDescriptions) {
		this.avalancheProblemDescriptions = avalancheProblemDescriptions;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("elevation", this.elevation);
		json.put("subregion", this.subregion);

		JSONObject dangerRatings = new JSONObject();
		dangerRatings.put("above", dangerRatingAbove);
		dangerRatings.put("below", dangerRatingBelow);

		json.put("dangerRatings", dangerRatings);

		JSONArray dangerPatterns = new JSONArray();
		for (Integer dangerPattern : this.dangerPatterns) {
			dangerPatterns.put(dangerPattern);
		}
		json.put("dangerPatterns", dangerPatterns);

		JSONArray avalancheProblemDescriptions = new JSONArray();
		for (AvalancheProblemDescription avalancheProblemDescription : this.avalancheProblemDescriptions) {
			avalancheProblemDescriptions.put(avalancheProblemDescription.toJSON());
		}
		json.put("problemDescriptions", avalancheProblemDescriptions);

		return json;
	}

}
