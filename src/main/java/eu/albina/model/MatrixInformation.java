package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.json.JSONObject;

import eu.albina.model.enumerations.AvalancheReleaseProbability;
import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.HazardSiteDistribution;
import eu.albina.model.enumerations.SpontaneousAvalancheReleaseProbability;

@Embeddable
public class MatrixInformation implements AvalancheInformationObject {

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING")
	private DangerRating dangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_SIZE")
	private AvalancheSize avalancheSize;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_RELEASE_PROBABILITY")
	private AvalancheReleaseProbability avalancheReleaseProbability;

	@Enumerated(EnumType.STRING)
	@Column(name = "HARZARD_SITE_DISTRIBUTION")
	private HazardSiteDistribution hazardSiteDistribution;

	@Enumerated(EnumType.STRING)
	@Column(name = "SPONTANEOUS_DANGER_RATING")
	private DangerRating spontaneousDangerRating;

	@Enumerated(EnumType.STRING)
	@Column(name = "SPONTANEOUS_AVALANCHE_RELEASE_PROBABILITY")
	private SpontaneousAvalancheReleaseProbability spontaneousAvalancheReleaseProbability;

	@Enumerated(EnumType.STRING)
	@Column(name = "SPONTANEOUS_HAZARD_SITE_DISTRIBUTION")
	private HazardSiteDistribution spontaneousHazardSiteDistribution;

	public MatrixInformation() {
	}

	public MatrixInformation(JSONObject json) {
		this();

		if (json.has("dangerRating"))
			this.dangerRating = DangerRating.fromString(json.getString("dangerRating"));
		if (json.has("avalancheSize"))
			this.avalancheSize = AvalancheSize.fromString(json.getString("avalancheSize"));
		if (json.has("avalancheReleaseProbability"))
			this.avalancheReleaseProbability = AvalancheReleaseProbability
					.fromString(json.getString("avalancheReleaseProbability"));
		if (json.has("hazardSiteDistribution"))
			this.hazardSiteDistribution = HazardSiteDistribution.fromString(json.getString("hazardSiteDistribution"));
		if (json.has("spontaneousDangerRating"))
			this.spontaneousDangerRating = DangerRating.fromString(json.getString("spontaneousDangerRating"));
		if (json.has("spontaneousAvalancheReleaseProbability"))
			this.spontaneousAvalancheReleaseProbability = SpontaneousAvalancheReleaseProbability
					.fromString(json.getString("spontaneousAvalancheReleaseProbability"));
		if (json.has("spontaneousHazardSiteDistribution"))
			this.spontaneousHazardSiteDistribution = HazardSiteDistribution
					.fromString(json.getString("spontaneousHazardSiteDistribution"));
	}

	public DangerRating getDangerRating() {
		return dangerRating;
	}

	public void setDangerRating(DangerRating dangerRating) {
		this.dangerRating = dangerRating;
	}

	public AvalancheSize getAvalancheSize() {
		return avalancheSize;
	}

	public void setAvalancheSize(AvalancheSize avalancheSize) {
		this.avalancheSize = avalancheSize;
	}

	public AvalancheReleaseProbability getAvalancheReleaseProbability() {
		return avalancheReleaseProbability;
	}

	public void setAvalancheReleaseProbability(AvalancheReleaseProbability avalancheReleaseProbability) {
		this.avalancheReleaseProbability = avalancheReleaseProbability;
	}

	public HazardSiteDistribution getHazardSiteDistribution() {
		return hazardSiteDistribution;
	}

	public void setHazardSiteDistribution(HazardSiteDistribution hazardSiteDistribution) {
		this.hazardSiteDistribution = hazardSiteDistribution;
	}

	public DangerRating getSpontaneousDangerRating() {
		return spontaneousDangerRating;
	}

	public void setSpontaneousDangerRating(DangerRating spontaneousDangerRating) {
		this.spontaneousDangerRating = spontaneousDangerRating;
	}

	public SpontaneousAvalancheReleaseProbability getSpontaneousAvalancheReleaseProbability() {
		return spontaneousAvalancheReleaseProbability;
	}

	public void setSpontaneousAvalancheReleaseProbability(
			SpontaneousAvalancheReleaseProbability spontaneousAvalancheReleaseProbability) {
		this.spontaneousAvalancheReleaseProbability = spontaneousAvalancheReleaseProbability;
	}

	public HazardSiteDistribution getSpontaneousHazardSiteDistribution() {
		return spontaneousHazardSiteDistribution;
	}

	public void setSpontaneousHazardSiteDistribution(HazardSiteDistribution spontaneousHazardSiteDistribution) {
		this.spontaneousHazardSiteDistribution = spontaneousHazardSiteDistribution;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (dangerRating != null)
			json.put("dangerRating", this.dangerRating.toString());
		if (avalancheSize != null)
			json.put("avalancheSize", this.avalancheSize.toString());
		if (avalancheReleaseProbability != null)
			json.put("avalancheReleaseProbability", this.avalancheReleaseProbability.toString());
		if (hazardSiteDistribution != null)
			json.put("hazardSiteDistribution", this.hazardSiteDistribution.toString());
		if (spontaneousDangerRating != null)
			json.put("spontaneousDangerRating", this.spontaneousDangerRating.toString());
		if (spontaneousAvalancheReleaseProbability != null)
			json.put("spontaneousAvalancheReleaseProbability", this.spontaneousAvalancheReleaseProbability.toString());
		if (spontaneousHazardSiteDistribution != null)
			json.put("spontaneousHazardSiteDistribution", this.spontaneousHazardSiteDistribution.toString());

		return json;
	}

}
