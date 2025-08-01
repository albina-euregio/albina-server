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
import java.util.Objects;
import java.util.Set;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import eu.albina.model.enumerations.Complexity;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.Transient;

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
	@Column(name = "DANGER_RATING_ABOVE", length = 191)
	private DangerRating dangerRatingAbove;

	@Column(name = "ELEVATION")
	private int elevation;

	@Column(name = "TREELINE")
	private boolean treeline;

	@Lob
	@Column(name = "TERRAIN_FEATURE_ABOVE_TEXTCAT")
	private String terrainFeatureAboveTextcat;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "text_parts", joinColumns = @JoinColumn(name = "TEXTS_ID"))
	@Column(name = "TERRAIN_FEATURE_ABOVE")
	private Set<Text> terrainFeatureAbove;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_RATING_BELOW", length = 191)
	private DangerRating dangerRatingBelow;

	@Lob
	@Column(name = "TERRAIN_FEATURE_BELOW_TEXTCAT")
	private String terrainFeatureBelowTextcat;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "text_parts", joinColumns = @JoinColumn(name = "TEXTS_ID"))
	@Column(name = "TERRAIN_FEATURE_BELOW")
	private Set<Text> terrainFeatureBelow;

	@Enumerated(EnumType.STRING)
	@Column(name = "COMPLEXITY", length = 191)
	private Complexity complexity;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_1_ID")
	private AvalancheProblem avalancheProblem1;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_2_ID")
	private AvalancheProblem avalancheProblem2;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_3_ID")
	private AvalancheProblem avalancheProblem3;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_4_ID")
	private AvalancheProblem avalancheProblem4;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "AVALANCHE_PROBLEM_5_ID")
	private AvalancheProblem avalancheProblem5;

	@Transient
	private transient ServerInstance serverInstance;

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
		if (json.has("terrainFeatureAboveTextcat"))
			this.terrainFeatureAboveTextcat = json.getString("terrainFeatureAboveTextcat");
		if (json.has("terrainFeatureAbove"))
			for (Object entry : json.getJSONArray("terrainFeatureAbove"))
				terrainFeatureAbove.add(new Text((JSONObject) entry));
		if (json.has("dangerRatingBelow"))
			this.dangerRatingBelow = DangerRating.valueOf(json.getString("dangerRatingBelow").toLowerCase());
		if (json.has("terrainFeatureBelowTextcat"))
			this.terrainFeatureBelowTextcat = json.getString("terrainFeatureBelowTextcat");
		if (json.has("terrainFeatureBelow"))
			for (Object entry : json.getJSONArray("terrainFeatureBelow"))
				terrainFeatureBelow.add(new Text((JSONObject) entry));
		if (json.has("complexity"))
			this.complexity = Complexity.valueOf(json.getString("complexity").toLowerCase());
		if (json.has("avalancheProblem1"))
			this.avalancheProblem1 = new eu.albina.model.AvalancheProblem(
					json.getJSONObject("avalancheProblem1"));
		if (json.has("avalancheProblem2"))
			this.avalancheProblem2 = new eu.albina.model.AvalancheProblem(
					json.getJSONObject("avalancheProblem2"));
		if (json.has("avalancheProblem3"))
			this.avalancheProblem3 = new eu.albina.model.AvalancheProblem(
					json.getJSONObject("avalancheProblem3"));
		if (json.has("avalancheProblem4"))
			this.avalancheProblem4 = new eu.albina.model.AvalancheProblem(
					json.getJSONObject("avalancheProblem4"));
		if (json.has("avalancheProblem5"))
			this.avalancheProblem5 = new eu.albina.model.AvalancheProblem(
					json.getJSONObject("avalancheProblem5"));
	}

	public boolean isHasElevationDependency() {
		if (isDangerLevelElevationDependency()) {
			return hasElevationDependency;
		} else {
			return false;
		}
	}

	public void setHasElevationDependency(boolean hasElevationDependency) {
		this.hasElevationDependency = hasElevationDependency;
	}

	public int getElevation() {
		if (isDangerLevelElevationDependency()) {
			return elevation;
		} else {
			return 0;
		}
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public boolean getTreeline() {
		if (isDangerLevelElevationDependency()) {
			return treeline;
		} else {
			return false;
		}
	}

	public void setTreeline(boolean treeline) {
		this.treeline = treeline;
	}

	private DangerRating getDangerRatingAbove() {
		return dangerRatingAbove;
	}

	public void setDangerRatingAbove(DangerRating dangerRatingAbove) {
		this.dangerRatingAbove = dangerRatingAbove;
	}

	private String getTerrainFeatureAboveTextcat() {
		return terrainFeatureAboveTextcat;
	}

	public void setTerrainFeatureAboveTextcat(String terrainFeatureTextcat) {
		this.terrainFeatureAboveTextcat = terrainFeatureTextcat;
	}

	private Set<Text> getTerrainFeatureAbove() {
		return terrainFeatureAbove;
	}

	public String getTerrainFeatureAbove(LanguageCode languageCode) {
		return terrainFeature(true).stream().filter(text -> text.getLanguage() == languageCode).findFirst().map(Text::getText).orElse(null);
	}

	public void setTerrainFeatureAbove(Set<Text> terrainFeature) {
		this.terrainFeatureAbove = terrainFeature;
	}

	public void addTerrainFeatureAbove(Text terrainFeature) {
		this.terrainFeatureAbove.add(terrainFeature);
	}

	private DangerRating getDangerRatingBelow() {
		return dangerRatingBelow;
	}

	public void setDangerRatingBelow(DangerRating dangerRatingBelow) {
		this.dangerRatingBelow = dangerRatingBelow;
	}

	private String getTerrainFeatureBelowTextcat() {
		return terrainFeatureBelowTextcat;
	}

	public void setTerrainFeatureBelowTextcat(String terrainFeatureTextcat) {
		this.terrainFeatureBelowTextcat = terrainFeatureTextcat;
	}

	private Set<Text> getTerrainFeatureBelow() {
		return terrainFeatureBelow;
	}

	public String getTerrainFeatureBelow(LanguageCode languageCode) {
		return terrainFeature(false).stream().filter(text -> text.getLanguage() == languageCode).findFirst().map(Text::getText).orElse(null);
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

	public AvalancheProblem getAvalancheProblem1() {
		return avalancheProblem1;
	}

	public void setAvalancheProblem1(AvalancheProblem avalancheProblem1) {
		this.avalancheProblem1 = avalancheProblem1;
	}

	public AvalancheProblem getAvalancheProblem2() {
		return avalancheProblem2;
	}

	public void setAvalancheProblem2(AvalancheProblem avalancheProblem2) {
		this.avalancheProblem2 = avalancheProblem2;
	}

	public AvalancheProblem getAvalancheProblem3() {
		return avalancheProblem3;
	}

	public void setAvalancheProblem3(AvalancheProblem avalancheProblem3) {
		this.avalancheProblem3 = avalancheProblem3;
	}

	public AvalancheProblem getAvalancheProblem4() {
		return avalancheProblem4;
	}

	public void setAvalancheProblem4(AvalancheProblem avalancheProblem4) {
		this.avalancheProblem4 = avalancheProblem4;
	}

	public AvalancheProblem getAvalancheProblem5() {
		return avalancheProblem5;
	}

	public void setAvalancheProblem5(AvalancheProblem avalancheProblem5) {
		this.avalancheProblem5 = avalancheProblem5;
	}

	public List<AvalancheProblem> getAvalancheProblems() {
		return Arrays.asList(avalancheProblem1, avalancheProblem2, avalancheProblem3, avalancheProblem4,
				avalancheProblem5);
	}

	public DangerRating dangerRating(boolean above) {
		if (isDangerLevelElevationDependency()) {
			if (isHasElevationDependency() && !above) {
				return getDangerRatingBelow();
			} else {
				return getDangerRatingAbove();
			}
		} else {
			return highestDangerRating();
		}
	}

	private DangerRating highestDangerRating() {
		DangerRating highest = null;
		if (getDangerRatingAbove() != null) {
			highest = getDangerRatingAbove();
			if (getDangerRatingBelow() != null && getDangerRatingBelow().ordinal() > highest.ordinal()) {
				highest = getDangerRatingBelow();
			}
		} else if (getDangerRatingBelow() != null) {
			highest = getDangerRatingBelow();
		}
		return highest;
	}

	public Set<Text> terrainFeature(boolean above) {
		if (isDangerLevelElevationDependency()) {
			if (isHasElevationDependency() && !above) {
				return getTerrainFeatureBelow();
			} else {
				return getTerrainFeatureAbove();
			}
		} else {
			return highestDangerRatingTerrainFeature();
		}
	}

	private Set<Text> highestDangerRatingTerrainFeature() {
		Set<Text> terrainFeature = null;
		if (terrainFeatureAbove != null && !terrainFeatureAbove.isEmpty()) {
			terrainFeature = getTerrainFeatureAbove();
			if (terrainFeatureBelow != null  && !terrainFeatureBelow.isEmpty() && getDangerRatingBelow().ordinal() > getDangerRatingAbove().ordinal()) {
				terrainFeature = getTerrainFeatureBelow();
			}
		} else if (terrainFeatureBelow != null && !terrainFeatureBelow.isEmpty()) {
			terrainFeature = getTerrainFeatureBelow();
		}
		return terrainFeature;
	}

	public String terrainFeatureTextcat(boolean above) {
		if (isDangerLevelElevationDependency()) {
			if (isHasElevationDependency() && !above) {
				return getTerrainFeatureBelowTextcat();
			} else {
				return getTerrainFeatureAboveTextcat();
			}
		} else {
			return highestDangerRatingTerrainFeatureTextcat();
		}
	}

	private String highestDangerRatingTerrainFeatureTextcat() {
		String terrainFeatureTextcat = null;
		if (terrainFeatureAbove != null && !terrainFeatureAbove.isEmpty()) {
			terrainFeatureTextcat = getTerrainFeatureAboveTextcat();
			if (terrainFeatureBelow != null  && !terrainFeatureBelow.isEmpty() && getDangerRatingBelow().ordinal() > getDangerRatingAbove().ordinal()) {
				terrainFeatureTextcat = getTerrainFeatureBelowTextcat();
			}
		} else if (terrainFeatureBelow != null && !terrainFeatureBelow.isEmpty()) {
			terrainFeatureTextcat = getTerrainFeatureBelowTextcat();
		}
		return terrainFeatureTextcat;
	}

	boolean isDangerLevelElevationDependency() {
		return serverInstance == null || serverInstance.isDangerLevelElevationDependency();
	}

	ServerInstance serverInstance() {
		return serverInstance;
	}

	void serverInstance(ServerInstance serverInstance) {
		this.serverInstance = serverInstance;
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
		if (dangerRating(true) != null)
			json.put("dangerRatingAbove", this.dangerRating(true).toString());
		if (!com.google.common.base.Strings.isNullOrEmpty(terrainFeatureAboveTextcat))
			json.put("terrainFeatureAboveTextcat", terrainFeatureTextcat(true));
		if (terrainFeature(true) != null && !terrainFeature(true).isEmpty()) {
			JSONArray arrayAbove = new JSONArray();
			for (Text text : terrainFeature(true)) {
				arrayAbove.put(text.toJSON());
			}
			json.put("terrainFeatureAbove", arrayAbove);
		}
		if (dangerRating(false) != null)
			json.put("dangerRatingBelow", this.dangerRating(false).toString());
		if (!com.google.common.base.Strings.isNullOrEmpty(terrainFeatureTextcat(false)))
			json.put("terrainFeatureBelowTextcat", terrainFeatureTextcat(false));
		if (terrainFeature(false) != null && !terrainFeature(false).isEmpty()) {
			JSONArray arrayBelow = new JSONArray();
			for (Text text : terrainFeature(false)) {
				arrayBelow.put(text.toJSON());
			}
			json.put("terrainFeatureBelow", arrayBelow);
		}
		if (complexity != null)
			json.put("complexity", this.complexity.toString());
		if (avalancheProblem1 != null)
			json.put("avalancheProblem1", avalancheProblem1.toJSON());
		if (avalancheProblem2 != null)
			json.put("avalancheProblem2", avalancheProblem2.toJSON());
		if (avalancheProblem3 != null)
			json.put("avalancheProblem3", avalancheProblem3.toJSON());
		if (avalancheProblem4 != null)
			json.put("avalancheProblem4", avalancheProblem4.toJSON());
		if (avalancheProblem5 != null)
			json.put("avalancheProblem5", avalancheProblem5.toJSON());
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
		if (dangerRating(true) != null)
			json.put("dangerRatingAbove", this.dangerRating(true).toString());
		if (!com.google.common.base.Strings.isNullOrEmpty(terrainFeatureAboveTextcat))
			json.put("terrainFeatureAboveTextcat", terrainFeatureTextcat(true));
		if (terrainFeature(true) != null && !terrainFeature(true).isEmpty()) {
			JSONArray arrayAbove = new JSONArray();
			for (Text text : terrainFeature(true)) {
				arrayAbove.put(text.toJSON());
			}
			json.put("terrainFeatureAbove", arrayAbove);
		}
		if (dangerRating(false) != null)
			json.put("dangerRatingBelow", this.dangerRating(false).toString());
		if (!com.google.common.base.Strings.isNullOrEmpty(terrainFeatureTextcat(false)))
			json.put("terrainFeatureBelowTextcat", terrainFeatureTextcat(false));
		if (terrainFeature(false) != null && !terrainFeature(false).isEmpty()) {
			JSONArray arrayBelow = new JSONArray();
			for (Text text : terrainFeature(false)) {
				arrayBelow.put(text.toJSON());
			}
			json.put("terrainFeatureBelow", arrayBelow);
		}
		if (complexity != null)
			json.put("complexity", this.complexity.toString());
		if (avalancheProblem1 != null)
			json.put("avalancheProblem1", avalancheProblem1.toJSON());
		if (avalancheProblem2 != null)
			json.put("avalancheProblem2", avalancheProblem2.toJSON());
		if (avalancheProblem3 != null)
			json.put("avalancheProblem3", avalancheProblem3.toJSON());
		if (avalancheProblem4 != null)
			json.put("avalancheProblem4", avalancheProblem4.toJSON());
		if (avalancheProblem5 != null)
			json.put("avalancheProblem5", avalancheProblem5.toJSON());
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
		if (!Objects.equals(this.dangerRatingAbove, other.dangerRatingAbove))
			return false;
		if (!Objects.equals(this.terrainFeatureAboveTextcat, other.terrainFeatureAboveTextcat))
			return false;
		if (!Objects.equals(this.dangerRatingBelow, other.dangerRatingBelow))
			return false;
		if (!Objects.equals(this.terrainFeatureBelowTextcat, other.terrainFeatureBelowTextcat))
			return false;
		if (!Objects.equals(this.complexity, other.complexity))
			return false;
		if (!Objects.equals(this.avalancheProblem1, other.avalancheProblem1))
			return false;
		if (!Objects.equals(this.avalancheProblem2, other.avalancheProblem2))
			return false;
		if (!Objects.equals(this.avalancheProblem3, other.avalancheProblem3))
			return false;
		if (!Objects.equals(this.avalancheProblem4, other.avalancheProblem4))
			return false;
		if (!Objects.equals(this.avalancheProblem5, other.avalancheProblem5))
			return false;

		return true;
	}
}
