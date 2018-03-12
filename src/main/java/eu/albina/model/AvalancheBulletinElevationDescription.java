package eu.albina.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheSituation;
import eu.albina.model.enumerations.DangerRating;

@Audited
@Entity
@Table(name = "AVALANCHE_BULLETIN_ELEVATION_DESCRIPTIONS")
public class AvalancheBulletinElevationDescription extends AbstractPersistentObject
		implements AvalancheInformationObject {

	@OneToOne
	@PrimaryKeyJoinColumn
	private AvalancheBulletin avalancheBulletin;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING")
	private DangerRating dangerRating;

	/** Information about the location of the snow profile */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "dangerRating", column = @Column(name = "MATRIX_INFORMATION_DANGER_RATING")),
			@AttributeOverride(name = "avalancheSize", column = @Column(name = "MATRIX_INFORMATION_AVALANCHE_SIZE")),
			@AttributeOverride(name = "avalancheReleaseProbability", column = @Column(name = "MATRIX_INFORMATION_AVALANCHE_RELEASE_PROBABILITY")),
			@AttributeOverride(name = "hazardSiteDistribution", column = @Column(name = "MATRIX_INFORMATION_HAZARD_SITE_DISTRIBUTION")),
			@AttributeOverride(name = "spontaneousDangerRating", column = @Column(name = "MATRIX_INFORMATION_SPONTANEOUS_DANGER_RATING")),
			@AttributeOverride(name = "spontaneousAvalancheReleaseProbability", column = @Column(name = "MATRIX_INFORMATION_SPONTANEOUS_AVALANCHE_RELEASE_PROBABILITY")),
			@AttributeOverride(name = "spontaneousHazardSiteDistribution", column = @Column(name = "MATRIX_INFORMATION_SPONTANEOUS_HAZARD_SITE_DISTRIBUTION")) })
	private MatrixInformation matrixInformation;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_SITUATION")
	private AvalancheSituation avalancheSituation;

	@ElementCollection
	@CollectionTable(name = "AVALANCHE_BULLETIN_ELEVATION_DESCRIPTION_ASPECTS", joinColumns = @JoinColumn(name = "AVALANCHE_BULLETIN_ELEVATION_DESCRIPTION_ID", referencedColumnName = "ID"))
	@Column(name = "ASPECT")
	private Set<Aspect> aspects;

	public AvalancheBulletinElevationDescription() {
		this.aspects = new HashSet<Aspect>();
	}

	public AvalancheBulletinElevationDescription(JSONObject json) {
		this();

		if (json.has("id"))
			this.id = json.getString("id");
		if (json.has("dangerRating"))
			this.dangerRating = DangerRating.valueOf(json.getString("dangerRating").toLowerCase());
		if (json.has("matrixInformation"))
			this.matrixInformation = new MatrixInformation(json.getJSONObject("matrixInformation"));
		if (json.has("avalancheSituation"))
			this.avalancheSituation = AvalancheSituation.valueOf(json.getString("avalancheSituation").toLowerCase());
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

	public MatrixInformation getMatrixInformation() {
		return matrixInformation;
	}

	public void setMatrixInformation(MatrixInformation matrixInformation) {
		this.matrixInformation = matrixInformation;
	}

	public AvalancheSituation getAvalancheSituation() {
		return avalancheSituation;
	}

	public void setAvalancheSituation(AvalancheSituation avalancheSituation) {
		this.avalancheSituation = avalancheSituation;
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
		if (id != null)
			json.put("id", id);
		if (dangerRating != null)
			json.put("dangerRating", this.dangerRating.toString());
		if (matrixInformation != null)
			json.put("matrixInformation", matrixInformation.toJSON());
		if (avalancheSituation != null)
			json.put("avalancheSituation", this.avalancheSituation.toString());

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
