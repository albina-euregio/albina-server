package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.json.JSONObject;

import eu.albina.model.enumerations.AvalancheReleaseProbability;
import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.HazardSiteDistribution;
import eu.albina.model.enumerations.SpontaneousAvalancheReleaseProbability;

@Audited
@Entity
@Table(name = "AVALANCHE_BULLETIN_MATRIX_INFORMATION")
public class MatrixInformation extends AbstractPersistentObject implements AvalancheInformationObject {

	@OneToOne
	@PrimaryKeyJoinColumn
	private AvalancheBulletinElevationDescription avalancheBulletinElevationDescription;

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
	@Column(name = "SPONTANEOUS_AVALANCHE_RELEASE_PROBABILITY")
	private SpontaneousAvalancheReleaseProbability spontaneousAvalancheReleaseProbability;

	@Enumerated(EnumType.STRING)
	@Column(name = "SPONTANEOUS_HAZARD_SITE_DISTRIBUTION")
	private HazardSiteDistribution spontaneousHazardSiteDistribution;

	public MatrixInformation() {
	}

	public MatrixInformation(JSONObject json) {
		this();

		if (json.has("avalancheSize"))
			this.avalancheSize = AvalancheSize.fromString(json.getString("avalancheSize"));
		if (json.has("avalancheReleaseProbability"))
			this.avalancheReleaseProbability = AvalancheReleaseProbability
					.fromString(json.getString("avalancheReleaseProbability"));
		if (json.has("hazardSiteDistribution"))
			this.hazardSiteDistribution = HazardSiteDistribution.fromString(json.getString("hazardSiteDistribution"));
		if (json.has("spontaneousAvalancheReleaseProbability"))
			this.spontaneousAvalancheReleaseProbability = SpontaneousAvalancheReleaseProbability
					.fromString(json.getString("spontaneousAvalancheReleaseProbability"));
		if (json.has("spontaneousHazardSiteDistribution"))
			this.spontaneousHazardSiteDistribution = HazardSiteDistribution
					.fromString(json.getString("spontaneousHazardSiteDistribution"));
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (id != null)
			json.put("id", id);
		if (avalancheSize != null)
			json.put("avalancheSize", this.avalancheSize.toString());
		if (avalancheReleaseProbability != null)
			json.put("avalancheReleaseProbability", this.avalancheReleaseProbability.toString());
		if (hazardSiteDistribution != null)
			json.put("hazardSiteDistribution", this.hazardSiteDistribution.toString());
		if (spontaneousAvalancheReleaseProbability != null)
			json.put("spontaneousAvalancheReleaseProbability", this.spontaneousAvalancheReleaseProbability.toString());
		if (spontaneousHazardSiteDistribution != null)
			json.put("spontaneousHazardSiteDistribution", this.spontaneousHazardSiteDistribution.toString());

		return json;
	}

}
