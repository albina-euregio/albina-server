/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.model;

import java.util.LinkedHashSet;
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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.Direction;
import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "avalanche_situation")
public class AvalancheSituation extends AbstractPersistentObject implements AvalancheInformationObject {

	@OneToOne
	@PrimaryKeyJoinColumn
	private AvalancheBulletinDaytimeDescription avalancheBulletinDaytimeDescription;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_SITUATION")
	private eu.albina.model.enumerations.AvalancheSituation avalancheSituation;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "avalanche_situation_aspects", joinColumns = @JoinColumn(name = "AVALANCHE_SITUATION_ID", referencedColumnName = "ID"))
	@Column(name = "ASPECT")
	@Fetch(FetchMode.JOIN)
	private Set<Aspect> aspects;

	@Column(name = "ELEVATION_HIGH")
	private int elevationHigh;

	@Column(name = "TREELINE_HIGH")
	private boolean treelineHigh;

	@Column(name = "ELEVATION_LOW")
	private int elevationLow;

	@Column(name = "TREELINE_LOW")
	private boolean treelineLow;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING_DIRECTION")
	private Direction dangerRatingDirection;

	/** Information about the selected field in the EAWS matrix */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "artificialDangerRating", column = @Column(name = "ARTIFICIAL_DANGER_RATING")),
			@AttributeOverride(name = "artificialAvalancheSize", column = @Column(name = "ARTIFICIAL_AVALANCHE_SIZE")),
			@AttributeOverride(name = "artificialAvalancheReleaseProbability", column = @Column(name = "ARTIFICIAL_AVALANCHE_RELEASE_PROBABILITY")),
			@AttributeOverride(name = "artificialHazardSiteDistribution", column = @Column(name = "ARTIFICIAL_HAZARD_SITE_DISTRIBUTION")),
			@AttributeOverride(name = "naturalDangerRating", column = @Column(name = "NATURAL_DANGER_RATING")),
			@AttributeOverride(name = "naturalAvalancheReleaseProbability", column = @Column(name = "NATURAL_AVALANCHE_RELEASE_PROBABILITY")),
			@AttributeOverride(name = "naturalHazardSiteDistribution", column = @Column(name = "NATURAL_HAZARD_SITE_DISTRIBUTION")) })
	private MatrixInformation matrixInformation;

	@Lob
	@Column(name = "TERRAIN_FEATURE_TEXTCAT")
	private String terrainFeatureTextcat;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "TEXT_PARTS", joinColumns = @JoinColumn(name = "TEXTS_ID"))
	@Column(name = "TERRAIN_FEATURE")
	private Set<Text> terrainFeature;

	public AvalancheSituation() {
		this.aspects = new LinkedHashSet<Aspect>();
		this.terrainFeature = new LinkedHashSet<Text>();
	}

	public AvalancheSituation(JSONObject json) {
		this();

		if (json.has("avalancheSituation")) {
			if (json.getString("avalancheSituation").equals("weak_persistent_layer"))
				this.avalancheSituation = eu.albina.model.enumerations.AvalancheSituation.persistent_weak_layers;
			else
				this.avalancheSituation = eu.albina.model.enumerations.AvalancheSituation
						.valueOf(json.getString("avalancheSituation").toLowerCase());
		}
		if (json.has("aspects")) {
			JSONArray aspects = json.getJSONArray("aspects");
			for (Object entry : aspects) {
				this.aspects.add(Aspect.valueOf(((String) entry).toUpperCase()));
			}
		}
		if (json.has("elevationHigh"))
			this.elevationHigh = json.getInt("elevationHigh");
		if (json.has("treelineHigh"))
			this.treelineHigh = json.getBoolean("treelineHigh");
		if (json.has("elevationLow"))
			this.elevationLow = json.getInt("elevationLow");
		if (json.has("treelineLow"))
			this.treelineLow = json.getBoolean("treelineLow");
		if (json.has("dangerRatingDirection"))
			this.dangerRatingDirection = Direction.valueOf(json.getString("dangerRatingDirection").toLowerCase());
		if (json.has("matrixInformation"))
			this.matrixInformation = new MatrixInformation(json.getJSONObject("matrixInformation"));
		if (json.has("terrainFeatureTextcat"))
			this.terrainFeatureTextcat = json.getString("terrainFeatureTextcat");
		if (json.has("terrainFeature"))
			for (Object entry : json.getJSONArray("terrainFeature"))
				terrainFeature.add(new Text((JSONObject) entry));
	}

	public eu.albina.model.enumerations.AvalancheSituation getAvalancheSituation() {
		return avalancheSituation;
	}

	public void setAvalancheSituation(eu.albina.model.enumerations.AvalancheSituation avalancheSituation) {
		if (avalancheSituation == eu.albina.model.enumerations.AvalancheSituation.favourable_situation) {
			this.treelineHigh = false;
			this.treelineLow = false;
			this.elevationHigh = -1;
			this.elevationLow = -1;
		}
		this.avalancheSituation = avalancheSituation;
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
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (avalancheSituation != null)
			json.put("avalancheSituation", this.avalancheSituation.toString());
		if (aspects != null && aspects.size() > 0) {
			JSONArray aspects = new JSONArray();
			for (Aspect aspect : this.aspects) {
				aspects.put(aspect.toUpperCaseString());
			}
			json.put("aspects", aspects);
		}

		if (treelineHigh)
			json.put("treelineHigh", treelineHigh);
		else if (elevationHigh > 0)
			json.put("elevationHigh", elevationHigh);
		if (treelineLow)
			json.put("treelineLow", treelineLow);
		else if (elevationLow > 0)
			json.put("elevationLow", elevationLow);
		if (dangerRatingDirection != null)
			json.put("dangerRatingDirection", this.dangerRatingDirection.toString());
		if (matrixInformation != null)
			json.put("matrixInformation", matrixInformation.toJSON());

		if (terrainFeatureTextcat != null && terrainFeatureTextcat != "")
			json.put("terrainFeatureTextcat", terrainFeatureTextcat);
		if (terrainFeature != null && !terrainFeature.isEmpty()) {
			JSONArray array = new JSONArray();
			for (Text text : terrainFeature) {
				array.put(text.toJSON());
			}
			json.put("terrainFeature", array);
		}

		return json;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!AvalancheSituation.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final AvalancheSituation other = (AvalancheSituation) obj;

		if (this.avalancheSituation != other.avalancheSituation)
			return false;
		if (!this.aspects.containsAll(other.getAspects()) || !other.getAspects().containsAll(this.aspects))
			return false;
		if (this.elevationHigh != other.elevationHigh)
			return false;
		if (this.elevationLow != other.elevationLow)
			return false;
		if ((this.dangerRatingDirection == null) ? (other.dangerRatingDirection != null)
				: !this.dangerRatingDirection.equals(other.dangerRatingDirection))
			return false;
		if ((this.matrixInformation == null) ? (other.matrixInformation != null)
				: !this.matrixInformation.equals(other.matrixInformation))
			return false;
		if ((this.terrainFeatureTextcat == null) ? (other.terrainFeatureTextcat != null)
				: !this.terrainFeatureTextcat.equals(other.terrainFeatureTextcat))
			return false;

		return true;
	}
}
