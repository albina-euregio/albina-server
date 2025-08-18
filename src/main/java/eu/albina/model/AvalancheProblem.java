// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheType;
import eu.albina.model.enumerations.Direction;
import eu.albina.model.enumerations.LanguageCode;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "avalanche_problems")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvalancheProblem extends AbstractPersistentObject {

	@OneToOne
	@PrimaryKeyJoinColumn
	private AvalancheBulletinDaytimeDescription avalancheBulletinDaytimeDescription;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_TYPE", length = 191)
	private eu.albina.model.enumerations.AvalancheType avalancheType;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_PROBLEM", length = 191)
	private eu.albina.model.enumerations.AvalancheProblem avalancheProblem;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_problem_aspects", joinColumns = @JoinColumn(name = "AVALANCHE_PROBLEM_ID", referencedColumnName = "ID"))
	@Column(name = "ASPECT")
	@Fetch(FetchMode.JOIN)
	@JsonDeserialize(as = LinkedHashSet.class)
	private Set<Aspect> aspects;

	@Column(name = "ELEVATION_HIGH")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int elevationHigh;

	@Column(name = "TREELINE_HIGH")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private boolean treelineHigh;

	@Column(name = "ELEVATION_LOW")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int elevationLow;

	@Column(name = "TREELINE_LOW")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private boolean treelineLow;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING_DIRECTION", length = 191)
	private Direction dangerRatingDirection;

	/** Information about the selected field in the EAWS matrix */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "artificialDangerRating", column = @Column(name = "ARTIFICIAL_DANGER_RATING", length = 191)),
			@AttributeOverride(name = "artificialAvalancheSize", column = @Column(name = "ARTIFICIAL_AVALANCHE_SIZE", length = 191)),
			@AttributeOverride(name = "artificialAvalancheReleaseProbability", column = @Column(name = "ARTIFICIAL_AVALANCHE_RELEASE_PROBABILITY", length = 191)),
			@AttributeOverride(name = "artificialHazardSiteDistribution", column = @Column(name = "ARTIFICIAL_HAZARD_SITE_DISTRIBUTION", length = 191)),
			@AttributeOverride(name = "naturalDangerRating", column = @Column(name = "NATURAL_DANGER_RATING", length = 191)),
			@AttributeOverride(name = "naturalAvalancheReleaseProbability", column = @Column(name = "NATURAL_AVALANCHE_RELEASE_PROBABILITY", length = 191)),
			@AttributeOverride(name = "naturalHazardSiteDistribution", column = @Column(name = "NATURAL_HAZARD_SITE_DISTRIBUTION", length = 191)) })
	private MatrixInformation matrixInformation;

	/** Information about the selected field in the EAWS matrix */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "dangerRating", column = @Column(name = "DANGER_RATING", length = 191)),
			@AttributeOverride(name = "avalancheSize", column = @Column(name = "AVALANCHE_SIZE", length = 191)),
			@AttributeOverride(name = "snowpackStability", column = @Column(name = "SNOWPACK_STABILITY", length = 191)),
			@AttributeOverride(name = "frequency", column = @Column(name = "FREQUENCY", length = 191)) })
	private EawsMatrixInformation eawsMatrixInformation;

	@Lob
	@Column(name = "TERRAIN_FEATURE_TEXTCAT")
	private String terrainFeatureTextcat;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "text_parts", joinColumns = @JoinColumn(name = "TEXTS_ID"))
	@Column(name = "TERRAIN_FEATURE")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private Set<Text> terrainFeature;

	public AvalancheProblem() {
		this.aspects = new LinkedHashSet<Aspect>();
		this.terrainFeature = new TreeSet<>(); // sort texts by language to allow caching of API calls
	}

	public AvalancheType getAvalancheType() {
		return avalancheType;
	}

	public void setAvalancheType(AvalancheType avalancheType) {
		this.avalancheType = avalancheType;
	}

	public eu.albina.model.enumerations.AvalancheProblem getAvalancheProblem() {
		return avalancheProblem;
	}

	public void setAvalancheProblem(eu.albina.model.enumerations.AvalancheProblem avalancheProblem) {
		if (avalancheProblem == eu.albina.model.enumerations.AvalancheProblem.favourable_situation) {
			this.treelineHigh = false;
			this.treelineLow = false;
			this.elevationHigh = -1;
			this.elevationLow = -1;
		}
		this.avalancheProblem = avalancheProblem;
	}

	public Set<Aspect> getAspects() {
		return aspects;
	}

	public void setAspects(Set<Aspect> aspects) {
		this.aspects = aspects;
	}

	public int getElevationHigh() {
		return elevationHigh;
	}

	public void setElevationHigh(int elevationHigh) {
		this.elevationHigh = elevationHigh;
	}

	public boolean getTreelineHigh() {
		return treelineHigh;
	}

	public void setTreelineHigh(boolean treelineHigh) {
		this.treelineHigh = treelineHigh;
	}

	public int getElevationLow() {
		return elevationLow;
	}

	public void setElevationLow(int elevationLow) {
		this.elevationLow = elevationLow;
	}

	public boolean getTreelineLow() {
		return treelineLow;
	}

	public void setTreelineLow(boolean treelineLow) {
		this.treelineLow = treelineLow;
	}

	public Direction getDangerRatingDirection() {
		return dangerRatingDirection;
	}

	public void setDangerRatingDirection(Direction dangerRatingDirection) {
		this.dangerRatingDirection = dangerRatingDirection;
	}

	public MatrixInformation getMatrixInformation() {
		return matrixInformation;
	}

	public void setMatrixInformation(MatrixInformation matrixInformation) {
		this.matrixInformation = matrixInformation;
	}

	public EawsMatrixInformation getEawsMatrixInformation() {
		return eawsMatrixInformation;
	}

	public void setEawsMatrixInformation(EawsMatrixInformation eawsMatrixInformation) {
		this.eawsMatrixInformation = eawsMatrixInformation;
	}

	public String getTerrainFeatureTextcat() {
		return terrainFeatureTextcat;
	}

	public void setTerrainFeatureTextcat(String terrainFeatureTextcat) {
		this.terrainFeatureTextcat = terrainFeatureTextcat;
	}

	public Set<Text> getTerrainFeature() {
		return terrainFeature;
	}

	public String getTerrainFeature(LanguageCode languageCode) {
		return terrainFeature.stream().filter(text -> text.getLanguage() == languageCode).findFirst().map(Text::getText).orElse(null);
	}

	public void setTerrainFeature(Set<Text> terrainFeature) {
		this.terrainFeature = terrainFeature;
	}

	public void addTerrainFeature(Text terrainFeature) {
		this.terrainFeature.add(terrainFeature);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!AvalancheProblem.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final AvalancheProblem other = (AvalancheProblem) obj;

		if (this.avalancheType != other.avalancheType)
			return false;
		if (this.avalancheProblem != other.avalancheProblem)
			return false;
		if (!this.aspects.containsAll(other.getAspects()) || !other.getAspects().containsAll(this.aspects))
			return false;
		if (this.elevationHigh != other.elevationHigh)
			return false;
		if (this.elevationLow != other.elevationLow)
			return false;
		if (!Objects.equals(this.dangerRatingDirection, other.dangerRatingDirection))
			return false;
		if (!Objects.equals(this.matrixInformation, other.matrixInformation))
			return false;
		if (!Objects.equals(this.eawsMatrixInformation, other.eawsMatrixInformation))
			return false;
		if (!Objects.equals(this.terrainFeatureTextcat, other.terrainFeatureTextcat))
			return false;

		return true;
	}
}
