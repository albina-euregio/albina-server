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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
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

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import eu.albina.model.enumerations.Complexity;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "avalanche_bulletin_daytime_descriptions")
public class AvalancheBulletinDaytimeDescription extends AbstractPersistentObject
		implements AvalancheInformationObject {

	@OneToOne
	@PrimaryKeyJoinColumn
	private AvalancheBulletin avalancheBulletin;

	@Column(name = "HAS_ELEVATION_DEPENDENCY")
	private boolean hasElevationDependency;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING_ABOVE")
	private DangerRating dangerRatingAbove;

	@Column(name = "ELEVATION")
	private int elevation;

	@Column(name = "TREELINE")
	private boolean treeline;

	/** Information about the selected field in the EAWS matrix */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "artificialDangerRating", column = @Column(name = "ARTIFICIAL_DANGER_RATING_ABOVE")),
			@AttributeOverride(name = "artificialAvalancheSize", column = @Column(name = "ARTIFICIAL_AVALANCHE_SIZE_ABOVE")),
			@AttributeOverride(name = "artificialAvalancheReleaseProbability", column = @Column(name = "ARTIFICIAL_AVALANCHE_RELEASE_PROBABILITY_ABOVE")),
			@AttributeOverride(name = "artificialHazardSiteDistribution", column = @Column(name = "ARTIFICIAL_HAZARD_SITE_DISTRIBUTION_ABOVE")),
			@AttributeOverride(name = "naturalDangerRating", column = @Column(name = "NATURAL_DANGER_RATING_ABOVE")),
			@AttributeOverride(name = "naturalAvalancheReleaseProbability", column = @Column(name = "NATURAL_AVALANCHE_RELEASE_PROBABILITY_ABOVE")),
			@AttributeOverride(name = "naturalHazardSiteDistribution", column = @Column(name = "NATURAL_HAZARD_SITE_DISTRIBUTION_ABOVE")) })
	private MatrixInformation matrixInformationAbove;

	@Lob
	@Column(name = "TERRAIN_FEATURE_ABOVE_TEXTCAT")
	private String terrainFeatureAboveTextcat;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "TEXT_PARTS", joinColumns = @JoinColumn(name = "TEXTS_ID"))
	@Column(name = "TERRAIN_FEATURE_ABOVE")
	private Set<Text> terrainFeatureAbove;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING_BELOW")
	private DangerRating dangerRatingBelow;

	/** Information about the selected field in the EAWS matrix */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "artificialDangerRating", column = @Column(name = "ARTIFICIAL_DANGER_RATING_BELOW")),
			@AttributeOverride(name = "artificialAvalancheSize", column = @Column(name = "ARTIFICIAL_AVALANCHE_SIZE_BELOW")),
			@AttributeOverride(name = "artificialAvalancheReleaseProbability", column = @Column(name = "ARTIFICIAL_AVALANCHE_RELEASE_PROBABILITY_BELOW")),
			@AttributeOverride(name = "artificialHazardSiteDistribution", column = @Column(name = "ARTIFICIAL_HAZARD_SITE_DISTRIBUTION_BELOW")),
			@AttributeOverride(name = "naturalDangerRating", column = @Column(name = "NATURAL_DANGER_RATING_BELOW")),
			@AttributeOverride(name = "naturalAvalancheReleaseProbability", column = @Column(name = "NATURAL_AVALANCHE_RELEASE_PROBABILITY_BELOW")),
			@AttributeOverride(name = "naturalHazardSiteDistribution", column = @Column(name = "NATURAL_HAZARD_SITE_DISTRIBUTION_BELOW")) })
	private MatrixInformation matrixInformationBelow;

	@Lob
	@Column(name = "TERRAIN_FEATURE_BELOW_TEXTCAT")
	private String terrainFeatureBelowTextcat;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "TEXT_PARTS", joinColumns = @JoinColumn(name = "TEXTS_ID"))
	@Column(name = "TERRAIN_FEATURE_BELOW")
	private Set<Text> terrainFeatureBelow;

	@Enumerated(EnumType.STRING)
	@Column(name = "COMPLEXITY")
	private Complexity complexity;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_SITUATION_1_ID")
	private AvalancheSituation avalancheSituation1;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_SITUATION_2_ID")
	private AvalancheSituation avalancheSituation2;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_SITUATION_3_ID")
	private AvalancheSituation avalancheSituation3;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_SITUATION_4_ID")
	private AvalancheSituation avalancheSituation4;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_SITUATION_5_ID")
	private AvalancheSituation avalancheSituation5;

	public AvalancheBulletinDaytimeDescription() {
		this.terrainFeatureAbove = new HashSet<Text>();
		this.terrainFeatureBelow = new HashSet<Text>();
	}

	public AvalancheBulletinDaytimeDescription(JSONObject json) {
		this();

		if (json.has("id"))
			this.id = json.getString("id");
		if (json.has("hasElevationDependency"))
			this.hasElevationDependency = json.getBoolean("hasElevationDependency");
		if (json.has("elevation"))
			this.elevation = json.getInt("elevation");
		if (json.has("treeline"))
			this.treeline = json.getBoolean("treeline");
		if (json.has("dangerRatingAbove"))
			this.dangerRatingAbove = DangerRating.valueOf(json.getString("dangerRatingAbove").toLowerCase());
		if (json.has("matrixInformationAbove"))
			this.matrixInformationAbove = new MatrixInformation(json.getJSONObject("matrixInformationAbove"));
		if (json.has("terrainFeatureAboveTextcat"))
			this.terrainFeatureAboveTextcat = json.getString("terrainFeatureAboveTextcat");
		if (json.has("terrainFeatureAbove"))
			for (Object entry : json.getJSONArray("terrainFeatureAbove"))
				terrainFeatureAbove.add(new Text((JSONObject) entry));
		if (json.has("dangerRatingBelow"))
			this.dangerRatingBelow = DangerRating.valueOf(json.getString("dangerRatingBelow").toLowerCase());
		if (json.has("matrixInformationBelow"))
			this.matrixInformationBelow = new MatrixInformation(json.getJSONObject("matrixInformationBelow"));
		if (json.has("terrainFeatureBelowTextcat"))
			this.terrainFeatureBelowTextcat = json.getString("terrainFeatureBelowTextcat");
		if (json.has("terrainFeatureBelow"))
			for (Object entry : json.getJSONArray("terrainFeatureBelow"))
				terrainFeatureBelow.add(new Text((JSONObject) entry));
		if (json.has("complexity"))
			this.complexity = Complexity.valueOf(json.getString("complexity").toLowerCase());
		if (json.has("avalancheSituation1"))
			this.avalancheSituation1 = new eu.albina.model.AvalancheSituation(
					json.getJSONObject("avalancheSituation1"));
		if (json.has("avalancheSituation2"))
			this.avalancheSituation2 = new eu.albina.model.AvalancheSituation(
					json.getJSONObject("avalancheSituation2"));
		if (json.has("avalancheSituation3"))
			this.avalancheSituation3 = new eu.albina.model.AvalancheSituation(
					json.getJSONObject("avalancheSituation3"));
		if (json.has("avalancheSituation4"))
			this.avalancheSituation4 = new eu.albina.model.AvalancheSituation(
					json.getJSONObject("avalancheSituation4"));
		if (json.has("avalancheSituation5"))
			this.avalancheSituation5 = new eu.albina.model.AvalancheSituation(
					json.getJSONObject("avalancheSituation5"));
	}

	public boolean isHasElevationDependency() {
		return hasElevationDependency;
	}

	public void setHasElevationDependency(boolean hasElevationDependency) {
		this.hasElevationDependency = hasElevationDependency;
	}

	public int getElevation() {
		return elevation;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public boolean getTreeline() {
		return treeline;
	}

	public void setTreeline(boolean treeline) {
		this.treeline = treeline;
	}

	public DangerRating getDangerRatingAbove() {
		return dangerRatingAbove;
	}

	public void setDangerRatingAbove(DangerRating dangerRatingAbove) {
		this.dangerRatingAbove = dangerRatingAbove;
	}

	public MatrixInformation getMatrixInformationAbove() {
		return matrixInformationAbove;
	}

	public void setMatrixInformationAbove(MatrixInformation matrixInformationAbove) {
		this.matrixInformationAbove = matrixInformationAbove;
	}

	public String getTerrainFeatureAboveTextcat() {
		return terrainFeatureAboveTextcat;
	}

	public void setTerrainFeatureAboveTextcat(String terrainFeatureTextcat) {
		this.terrainFeatureAboveTextcat = terrainFeatureTextcat;
	}

	public Set<Text> getTerrainFeatureAbove() {
		return terrainFeatureAbove;
	}

	public String getTerrainFeatureAbove(LanguageCode languageCode) {
		return terrainFeatureAbove.stream().filter(text -> text.getLanguage() == languageCode).findFirst().map(Text::getText).orElse(null);
	}

	public void setTerrainFeatureAbove(Set<Text> terrainFeature) {
		this.terrainFeatureAbove = terrainFeature;
	}

	public void addTerrainFeatureAbove(Text terrainFeature) {
		this.terrainFeatureAbove.add(terrainFeature);
	}

	public DangerRating getDangerRatingBelow() {
		return dangerRatingBelow;
	}

	public void setDangerRatingBelow(DangerRating dangerRatingBelow) {
		this.dangerRatingBelow = dangerRatingBelow;
	}

	public MatrixInformation getMatrixInformationBelow() {
		return matrixInformationBelow;
	}

	public void setMatrixInformationBelow(MatrixInformation matrixInformationBelow) {
		this.matrixInformationBelow = matrixInformationBelow;
	}

	public String getTerrainFeatureBelowTextcat() {
		return terrainFeatureBelowTextcat;
	}

	public void setTerrainFeatureBelowTextcat(String terrainFeatureTextcat) {
		this.terrainFeatureBelowTextcat = terrainFeatureTextcat;
	}

	public Set<Text> getTerrainFeatureBelow() {
		return terrainFeatureBelow;
	}

	public String getTerrainFeatureBelow(LanguageCode languageCode) {
		return terrainFeatureBelow.stream().filter(text -> text.getLanguage() == languageCode).findFirst().map(Text::getText).orElse(null);
	}

	public void setTerrainFeatureBelow(Set<Text> terrainFeature) {
		this.terrainFeatureBelow = terrainFeature;
	}

	public void addTerrainFeatureBelow(Text terrainFeature) {
		this.terrainFeatureBelow.add(terrainFeature);
	}

	public Complexity getComplexity() {
		return complexity;
	}

	public void setComplexity(Complexity complexity) {
		this.complexity = complexity;
	}

	public AvalancheSituation getAvalancheSituation1() {
		return avalancheSituation1;
	}

	public void setAvalancheSituation1(AvalancheSituation avalancheSituation1) {
		this.avalancheSituation1 = avalancheSituation1;
	}

	public AvalancheSituation getAvalancheSituation2() {
		return avalancheSituation2;
	}

	public void setAvalancheSituation2(AvalancheSituation avalancheSituation2) {
		this.avalancheSituation2 = avalancheSituation2;
	}

	public AvalancheSituation getAvalancheSituation3() {
		return avalancheSituation3;
	}

	public void setAvalancheSituation3(AvalancheSituation avalancheSituation3) {
		this.avalancheSituation3 = avalancheSituation3;
	}

	public AvalancheSituation getAvalancheSituation4() {
		return avalancheSituation4;
	}

	public void setAvalancheSituation4(AvalancheSituation avalancheSituation4) {
		this.avalancheSituation4 = avalancheSituation4;
	}

	public AvalancheSituation getAvalancheSituation5() {
		return avalancheSituation5;
	}

	public void setAvalancheSituation5(AvalancheSituation avalancheSituation5) {
		this.avalancheSituation5 = avalancheSituation5;
	}

	public List<AvalancheSituation> getAvalancheSituations() {
		return Arrays.asList(avalancheSituation1, avalancheSituation2, avalancheSituation3, avalancheSituation4,
				avalancheSituation5);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		if (id != null)
			json.put("id", id);
		json.put("hasElevationDependency", hasElevationDependency);
		if (hasElevationDependency) {
			if (treeline) {
				json.put("treeline", treeline);
			} else {
				json.put("elevation", elevation);
			}
		}
		if (dangerRatingAbove != null)
			json.put("dangerRatingAbove", this.dangerRatingAbove.toString());
		if (matrixInformationAbove != null)
			json.put("matrixInformationAbove", matrixInformationAbove.toJSON());
		if (terrainFeatureAboveTextcat != null && terrainFeatureAboveTextcat != "")
			json.put("terrainFeatureAboveTextcat", terrainFeatureAboveTextcat);
		if (terrainFeatureAbove != null && !terrainFeatureAbove.isEmpty()) {
			JSONArray arrayAbove = new JSONArray();
			for (Text text : terrainFeatureAbove) {
				arrayAbove.put(text.toJSON());
			}
			json.put("terrainFeatureAbove", arrayAbove);
		}
		if (dangerRatingBelow != null)
			json.put("dangerRatingBelow", this.dangerRatingBelow.toString());
		if (matrixInformationBelow != null)
			json.put("matrixInformationBelow", matrixInformationBelow.toJSON());
		if (terrainFeatureBelowTextcat != null && terrainFeatureBelowTextcat != "")
			json.put("terrainFeatureBelowTextcat", terrainFeatureBelowTextcat);
		if (terrainFeatureBelow != null && !terrainFeatureBelow.isEmpty()) {
			JSONArray arrayBelow = new JSONArray();
			for (Text text : terrainFeatureBelow) {
				arrayBelow.put(text.toJSON());
			}
			json.put("terrainFeatureBelow", arrayBelow);
		}
		if (complexity != null)
			json.put("complexity", this.complexity.toString());
		if (avalancheSituation1 != null)
			json.put("avalancheSituation1", avalancheSituation1.toJSON());
		if (avalancheSituation2 != null)
			json.put("avalancheSituation2", avalancheSituation2.toJSON());
		if (avalancheSituation3 != null)
			json.put("avalancheSituation3", avalancheSituation3.toJSON());
		if (avalancheSituation4 != null)
			json.put("avalancheSituation4", avalancheSituation4.toJSON());
		if (avalancheSituation5 != null)
			json.put("avalancheSituation5", avalancheSituation5.toJSON());
		return json;
	}

	public JSONObject toSmallJSON() {
		JSONObject json = new JSONObject();
		if (id != null)
			json.put("id", id);
		json.put("hasElevationDependency", hasElevationDependency);
		if (hasElevationDependency) {
			if (treeline) {
				json.put("treeline", treeline);
			} else {
				json.put("elevation", elevation);
			}
		}
		if (dangerRatingAbove != null)
			json.put("dangerRatingAbove", this.dangerRatingAbove.toString());
		if (terrainFeatureAboveTextcat != null && terrainFeatureAboveTextcat != "")
			json.put("terrainFeatureAboveTextcat", terrainFeatureAboveTextcat);
		if (terrainFeatureAbove != null && !terrainFeatureAbove.isEmpty()) {
			JSONArray arrayAbove = new JSONArray();
			for (Text text : terrainFeatureAbove) {
				arrayAbove.put(text.toJSON());
			}
			json.put("terrainFeatureAbove", arrayAbove);
		}
		if (dangerRatingBelow != null)
			json.put("dangerRatingBelow", this.dangerRatingBelow.toString());
		if (terrainFeatureBelowTextcat != null && terrainFeatureBelowTextcat != "")
			json.put("terrainFeatureBelowTextcat", terrainFeatureBelowTextcat);
		if (terrainFeatureBelow != null && !terrainFeatureBelow.isEmpty()) {
			JSONArray arrayBelow = new JSONArray();
			for (Text text : terrainFeatureBelow) {
				arrayBelow.put(text.toJSON());
			}
			json.put("terrainFeatureBelow", arrayBelow);
		}
		if (complexity != null)
			json.put("complexity", this.complexity.toString());
		if (avalancheSituation1 != null)
			json.put("avalancheSituation1", avalancheSituation1.toJSON());
		if (avalancheSituation2 != null)
			json.put("avalancheSituation2", avalancheSituation2.toJSON());
		if (avalancheSituation3 != null)
			json.put("avalancheSituation3", avalancheSituation3.toJSON());
		if (avalancheSituation4 != null)
			json.put("avalancheSituation4", avalancheSituation4.toJSON());
		if (avalancheSituation5 != null)
			json.put("avalancheSituation5", avalancheSituation5.toJSON());
		return json;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!AvalancheBulletinDaytimeDescription.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final AvalancheBulletinDaytimeDescription other = (AvalancheBulletinDaytimeDescription) obj;

		if (this.hasElevationDependency != other.hasElevationDependency)
			return false;
		if (this.elevation != other.elevation)
			return false;
		if (this.treeline != other.treeline)
			return false;
		if ((this.dangerRatingAbove == null) ? (other.dangerRatingAbove != null)
				: !this.dangerRatingAbove.equals(other.dangerRatingAbove))
			return false;
		if ((this.matrixInformationAbove == null) ? (other.matrixInformationAbove != null)
				: !this.matrixInformationAbove.equals(other.matrixInformationAbove))
			return false;
		if ((this.terrainFeatureAboveTextcat == null) ? (other.terrainFeatureAboveTextcat != null)
				: !this.terrainFeatureAboveTextcat.equals(other.terrainFeatureAboveTextcat))
			return false;
		if ((this.dangerRatingBelow == null) ? (other.dangerRatingBelow != null)
				: !this.dangerRatingBelow.equals(other.dangerRatingBelow))
			return false;
		if ((this.matrixInformationBelow == null) ? (other.matrixInformationBelow != null)
				: !this.matrixInformationBelow.equals(other.matrixInformationBelow))
			return false;
		if ((this.terrainFeatureBelowTextcat == null) ? (other.terrainFeatureBelowTextcat != null)
				: !this.terrainFeatureBelowTextcat.equals(other.terrainFeatureBelowTextcat))
			return false;
		if ((this.complexity == null) ? (other.complexity != null) : !this.complexity.equals(other.complexity))
			return false;
		if ((this.avalancheSituation1 == null) ? (other.avalancheSituation1 != null)
				: !this.avalancheSituation1.equals(other.avalancheSituation1))
			return false;
		if ((this.avalancheSituation2 == null) ? (other.avalancheSituation2 != null)
				: !this.avalancheSituation2.equals(other.avalancheSituation2))
			return false;
		if ((this.avalancheSituation3 == null) ? (other.avalancheSituation3 != null)
				: !this.avalancheSituation3.equals(other.avalancheSituation3))
			return false;
		if ((this.avalancheSituation4 == null) ? (other.avalancheSituation4 != null)
				: !this.avalancheSituation4.equals(other.avalancheSituation4))
			return false;
		if ((this.avalancheSituation5 == null) ? (other.avalancheSituation5 != null)
				: !this.avalancheSituation5.equals(other.avalancheSituation5))
			return false;

		return true;
	}
}
