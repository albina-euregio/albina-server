/*******************************************************************************
 * Copyright (C) 2024 Norbert Lanzanasto
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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.io.Resources;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheType;
import eu.albina.model.enumerations.Characteristic;
import eu.albina.model.enumerations.CreationProcess;
import eu.albina.model.enumerations.Crust;
import eu.albina.model.enumerations.DangerSign;
import eu.albina.model.enumerations.DangerSourceVariantStatus;
import eu.albina.model.enumerations.DangerSourceVariantType;
import eu.albina.model.enumerations.Distribution;
import eu.albina.model.enumerations.GlidingSnowActivity;
import eu.albina.model.enumerations.GrainShape;
import eu.albina.model.enumerations.HandHardness;
import eu.albina.model.enumerations.Recognizability;
import eu.albina.model.enumerations.SnowpackPosition;
import eu.albina.model.enumerations.Tendency;
import eu.albina.model.enumerations.TerrainType;
import eu.albina.model.enumerations.Thickness;
import eu.albina.model.enumerations.Wetness;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * This class holds all information about one danger source variant.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "danger_source_variants")
public class DangerSourceVariant extends AbstractPersistentObject
		implements Comparable<DangerSourceVariant> {

	@Column(name = "ORIGINAL_DANGER_SOURCE_VARIANT_ID")
	private String originalDangerSourceVariantId;

	@Column(name = "FORECAST_DANGER_SOURCE_VARIANT_ID")
	private String forecastDangerSourceVariantId;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.EAGER)
	@JoinColumn(name = "DANGER_SOURCE_ID")
	private DangerSource dangerSource;

	@Column(name = "CREATION_DATE")
	private Instant creationDate;

	@Column(name = "UPDATE_DATE")
	private Instant updateDate;

	/** Validity of the danger source variant. */
	@Column(name = "VALID_FROM")
	private Instant validFrom;
	@Column(name = "VALID_UNTIL")
	private Instant validUntil;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_SOURCE_VARIANT_STATUS")
	private DangerSourceVariantStatus dangerSourceVariantStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_SOURCE_VARIANT_TYPE")
	private DangerSourceVariantType dangerSourceVariantType;

	@Column(name = "OWNER_REGION")
	private String ownerRegion;

	/** The regions where the danger source variant is present. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "danger_source_variant_regions", joinColumns = @JoinColumn(name = "DANGER_SOURCE_VARIANT_ID"))
	@Column(name = "REGION_ID")
	private Set<String> regions;

	@Column(name = "HAS_DAYTIME_DEPENDENCY")
	private Boolean hasDaytimeDependency;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_TYPE")
	private AvalancheType avalancheType;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "danger_source_variant_aspects", joinColumns = @JoinColumn(name = "DANGER_SOURCE_VARIANT_ID"))
	@Column(name = "ASPECT")
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	@Fetch(FetchMode.JOIN)
	private Set<Aspect> aspects;

	@Column(name = "ELEVATION_HIGH")
	private Integer elevationHigh;

	@Column(name = "TREELINE_HIGH")
	private Boolean treelineHigh;

	@Column(name = "ELEVATION_LOW")
	private Integer elevationLow;

	@Column(name = "TREELINE_LOW")
	private Boolean treelineLow;

	@Column(name = "DANGER_INCREASE_WITH_ELEVATION")
	private Boolean dangerIncreaseWithElevation;

	@Enumerated(EnumType.STRING)
	@Column(name = "HIGHEST_DANGER_ASPECT")
	private eu.albina.model.enumerations.Aspect highestDangerAspect;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_PEAK")
	private eu.albina.model.enumerations.Daytime dangerPeak;

	@Enumerated(EnumType.STRING)
	@Column(name = "SLOPE_GRADIENT")
	private eu.albina.model.enumerations.SlopeGradient slopeGradient;

	@Column(name = "RUNOUT_INTO_GREEN")
	private Boolean runoutIntoGreen;

	@Column(name = "PENETRATE_DEEP_LAYERS")
	private Boolean penetrateDeepLayers;

	@Enumerated(EnumType.STRING)
	@Column(name = "NATURAL_RELEASE")
	private eu.albina.model.enumerations.Probability naturalRelease;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "danger_source_variant_danger_signs", joinColumns = @JoinColumn(name = "DANGER_SOURCE_VARIANT_ID"))
	@Column(name = "DANGER_SIGN")
	@Fetch(FetchMode.JOIN)
	private Set<DangerSign> dangerSigns;

	/** Information about the selected field in the EAWS matrix */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "dangerRating", column = @Column(name = "DANGER_RATING")),
			@AttributeOverride(name = "avalancheSize", column = @Column(name = "AVALANCHE_SIZE")),
			@AttributeOverride(name = "snowpackStability", column = @Column(name = "SNOWPACK_STABILITY")),
			@AttributeOverride(name = "frequency", column = @Column(name = "FREQUENCY")) })
	private EawsMatrixInformation eawsMatrixInformation;

	@Lob
	@Column(name = "COMMENT")
	private String comment;

	@Lob
	@Column(name = "TEXTCAT")
	private String textcat;

	// TODO add uncertainties

	/** --------------------- */
	/** GLIDE SNOW AVALANCHES */
	/** --------------------- */
	@Enumerated(EnumType.STRING)
	@Column(name = "GLIDING_SNOW_ACTIVITY")
	private GlidingSnowActivity glidingSnowActivity;

	@Column(name = "GLIDING_SNOW_ACTIVITY_VALUE")
	private int glidingSnowActivityValue;

	@Column(name = "SNOW_HEIGHT_UPPER_LIMIT")
	private int snowHeightUpperLimit;

	@Column(name = "SNOW_HEIGHT_LOWER_LIMIT")
	private int snowHeightLowerLimit;

	@Column(name = "SNOW_HEIGHT_AVERAGE")
	private int snowHeightAverage;

	@Column(name = "ZERO_DEGREE_ISOTHERM")
	private Boolean zeroDegreeIsotherm;

	/** --------------- */
	/** SLAB AVALANCHES */
	/** --------------- */
	@Enumerated(EnumType.STRING)
	@Column(name = "SLAB_GRAIN_SHAPE")
	private GrainShape slabGrainShape;

	@Column(name = "SLAB_THICKNESS_UPPER_LIMIT")
	private int slabThicknessUpperLimit;

	@Column(name = "SLAB_THICKNESS_LOWER_LIMIT")
	private int slabThicknessLowerLimit;

	@Enumerated(EnumType.STRING)
	@Column(name = "SLAB_HAND_HARDNESS_UPPER_LIMIT")
	private HandHardness slabHandHardnessUpperLimit;

	@Enumerated(EnumType.STRING)
	@Column(name = "SLAB_HAND_HARDNESS_LOWER_LIMIT")
	private HandHardness slabHandHardnessLowerLimit;

	@Enumerated(EnumType.STRING)
	@Column(name = "SLAB_HARDNESS_PROFILE")
	private Tendency slabHardnessProfile;

	@Enumerated(EnumType.STRING)
	@Column(name = "SLAB_ENERGY_TRANSFER_POTENTIAL")
	private Characteristic slabEnergyTransferPotential;

	@Enumerated(EnumType.STRING)
	@Column(name = "SLAB_DISTRIBUTION")
	private Distribution slabDistribution;

	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_GRAIN_SHAPE")
	private GrainShape weakLayerGrainShape;

	@Column(name = "WEAK_LAYER_GRAIN_SIZE_UPPER_LIMIT")
	private double weakLayerGrainSizeUpperLimit;

	@Column(name = "WEAK_LAYER_GRAIN_SIZE_LOWER_LIMIT")
	private double weakLayerGrainSizeLowerLimit;

	@Column(name = "WEAK_LAYER_PERSISTENT")
	private Boolean weakLayerPersistent;

	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_THICKNESS")
	private Thickness weakLayerThickness;

	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_STRENGTH")
	private Characteristic weakLayerStrength;

	@Column(name = "WEAK_LAYER_WET")
	private Boolean weakLayerWet;

	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_CRUST_ABOVE")
	private Crust weakLayerCrustAbove;

	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_CRUST_BELOW")
	private Crust weakLayerCrustBelow;

	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_POSITION")
	private SnowpackPosition weakLayerPosition;

	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_CREATION")
	private CreationProcess weakLayerCreation;

	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_DISTRIBUTION")
	private Distribution weakLayerDistribution;

	@Enumerated(EnumType.STRING)
	@Column(name = "DANGER_SPOT_RECOGNIZABILITY")
	private Recognizability dangerSpotRecognizability;

	@Column(name = "REMOTE_TRIGGERING")
	private Boolean remoteTriggering;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "danger_source_variant_terrain_types", joinColumns = @JoinColumn(name = "DANGER_SOURCE_VARIANT_ID"))
	@Column(name = "TERRAIN_TYPE")
	@Fetch(FetchMode.JOIN)
	private Set<TerrainType> terrainTypes;

	/** --------------------- */
	/** LOOSE SNOW AVALANCHES */
	/** --------------------- */
	@Enumerated(EnumType.STRING)
	@Column(name = "LOOSE_SNOW_GRAIN_SHAPE")
	private GrainShape looseSnowGrainShape;

	@Enumerated(EnumType.STRING)
	@Column(name = "LOOSE_SNOW_MOISTURE")
	private Wetness looseSnowMoisture;

	/**
	 * Standard constructor for a danger source.
	 */
	public DangerSourceVariant() {
		regions = new LinkedHashSet<>();
		aspects = new LinkedHashSet<>();
		dangerSigns = new LinkedHashSet<>();
		terrainTypes = new LinkedHashSet<>();
	}

	public DangerSourceVariant(JSONObject jsonObject) {
		this.originalDangerSourceVariantId = jsonObject.optString("originalDangerSourceVariantId");
		this.forecastDangerSourceVariantId = jsonObject.optString("forecastDangerSourceVariantId");
		this.creationDate = Instant.parse(jsonObject.optString("creationDate"));
		this.updateDate = Instant.parse(jsonObject.optString("updateDate"));
		this.validFrom = Instant.parse(jsonObject.optString("validFrom"));
		this.validUntil = Instant.parse(jsonObject.optString("validUntil"));
		this.dangerSourceVariantStatus = DangerSourceVariantStatus
				.valueOf(jsonObject.optString("dangerSourceVariantStatus"));
		this.dangerSourceVariantType = DangerSourceVariantType.valueOf(jsonObject.optString("dangerSourceVariantType"));
		this.ownerRegion = jsonObject.optString("ownerRegion");
		this.hasDaytimeDependency = jsonObject.optBoolean("hasDaytimeDependency");
		this.avalancheType = AvalancheType.valueOf(jsonObject.optString("avalancheType"));
		this.elevationHigh = jsonObject.optInt("elevationHigh");
		this.treelineHigh = jsonObject.optBoolean("treelineHigh");
		this.elevationLow = jsonObject.optInt("elevationLow");
		this.treelineLow = jsonObject.optBoolean("treelineLow");
		this.dangerIncreaseWithElevation = jsonObject.optBoolean("dangerIncreaseWithElevation");
		this.highestDangerAspect = Aspect.valueOf(jsonObject.optString("highestDangerAspect"));
		this.dangerPeak = eu.albina.model.enumerations.Daytime.valueOf(jsonObject.optString("dangerPeak"));
		this.slopeGradient = eu.albina.model.enumerations.SlopeGradient.valueOf(jsonObject.optString("slopeGradient"));
		this.runoutIntoGreen = jsonObject.optBoolean("runoutIntoGreen");
		this.penetrateDeepLayers = jsonObject.optBoolean("penetrateDeepLayers");
		this.naturalRelease = eu.albina.model.enumerations.Probability.valueOf(jsonObject.optString("naturalRelease"));
		this.comment = jsonObject.optString("comment");
		this.textcat = jsonObject.optString("textcat");
		this.glidingSnowActivity = GlidingSnowActivity.valueOf(jsonObject.optString("glidingSnowActivity"));
		this.glidingSnowActivityValue = jsonObject.optInt("glidingSnowActivityValue");
		this.snowHeightUpperLimit = jsonObject.optInt("snowHeightUpperLimit");
		this.snowHeightLowerLimit = jsonObject.optInt("snowHeightLowerLimit");
		this.snowHeightAverage = jsonObject.optInt("snowHeightAverage");
		this.zeroDegreeIsotherm = jsonObject.optBoolean("zeroDegreeIsotherm");
		this.slabGrainShape = GrainShape.valueOf(jsonObject.optString("slabGrainShape"));
		this.slabThicknessUpperLimit = jsonObject.optInt("slabThicknessUpperLimit");
		this.slabThicknessLowerLimit = jsonObject.optInt("slabThicknessLowerLimit");
		this.slabHandHardnessUpperLimit = HandHardness.valueOf(jsonObject.optString("slabHandHardnessUpperLimit"));
		this.slabHandHardnessLowerLimit = HandHardness.valueOf(jsonObject.optString("slabHandHardnessLowerLimit"));
		this.slabHardnessProfile = Tendency.valueOf(jsonObject.optString("slabHardnessProfile"));
		this.slabEnergyTransferPotential = Characteristic.valueOf(jsonObject.optString("slabEnergyTransferPotential"));
		this.slabDistribution = Distribution.valueOf(jsonObject.optString("slabDistribution"));
		this.weakLayerGrainShape = GrainShape.valueOf(jsonObject.optString("weakLayerGrainShape"));
		this.weakLayerGrainSizeUpperLimit = jsonObject.optDouble("weakLayerGrainSizeUpperLimit");
		this.weakLayerGrainSizeLowerLimit = jsonObject.optDouble("weakLayerGrainSizeLowerLimit");
		this.weakLayerPersistent = jsonObject.optBoolean("weakLayerPersistent");
		this.weakLayerThickness = Thickness.valueOf(jsonObject.optString("weakLayerThickness"));
		this.weakLayerStrength = Characteristic.valueOf(jsonObject.optString("weakLayerStrength"));
		this.weakLayerWet = jsonObject.optBoolean("weakLayerWet");
		this.weakLayerCrustAbove = Crust.valueOf(jsonObject.optString("weakLayerCrustAbove"));
		this.weakLayerCrustBelow = Crust.valueOf(jsonObject.optString("weakLayerCrustBelow"));
		this.weakLayerPosition = SnowpackPosition.valueOf(jsonObject.optString("weakLayerPosition"));
		this.weakLayerCreation = CreationProcess.valueOf(jsonObject.optString("weakLayerCreation"));
		this.weakLayerDistribution = Distribution.valueOf(jsonObject.optString("weakLayerDistribution"));
		this.dangerSpotRecognizability = Recognizability.valueOf(jsonObject.optString("dangerSpotRecognizability"));
		this.remoteTriggering = jsonObject.optBoolean("remoteTriggering");
		this.looseSnowGrainShape = GrainShape.valueOf(jsonObject.optString("looseSnowGrainShape"));
		this.looseSnowMoisture = Wetness.valueOf(jsonObject.optString("looseSnowMoisture"));

		JSONArray regionsArray = jsonObject.optJSONArray("regions");
		this.regions = new LinkedHashSet<>();
		if (regionsArray != null) {
			for (int i = 0; i < regionsArray.length(); i++) {
				this.regions.add(regionsArray.getString(i));
			}
		}

		JSONArray aspectsArray = jsonObject.optJSONArray("aspects");
		this.aspects = new LinkedHashSet<>();
		if (aspectsArray != null) {
			for (int i = 0; i < aspectsArray.length(); i++) {
				this.aspects.add(Aspect.valueOf(aspectsArray.getString(i)));
			}
		}

		JSONArray dangerSignsArray = jsonObject.optJSONArray("dangerSigns");
		this.dangerSigns = new LinkedHashSet<>();
		if (dangerSignsArray != null) {
			for (int i = 0; i < dangerSignsArray.length(); i++) {
				this.dangerSigns.add(DangerSign.valueOf(dangerSignsArray.getString(i)));
			}
		}

		JSONArray terrainTypesArray = jsonObject.optJSONArray("terrainTypes");
		this.terrainTypes = new LinkedHashSet<>();
		if (terrainTypesArray != null) {
			for (int i = 0; i < terrainTypesArray.length(); i++) {
				this.terrainTypes.add(TerrainType.valueOf(terrainTypesArray.getString(i)));
			}
		}

		JSONObject eawsMatrixInformation = jsonObject.optJSONObject("eawsMatrixInformation");
		if (eawsMatrixInformation != null) {
			this.eawsMatrixInformation = new EawsMatrixInformation(eawsMatrixInformation);
		}
	}

	public String getOriginalDangerSourceVariantId() {
		return this.originalDangerSourceVariantId;
	}

	public void setOriginalDangerSourceVariantId(String dangerSourceVariantId) {
		this.originalDangerSourceVariantId = dangerSourceVariantId;
	}

	public String getForecastDangerSourceVariantId() {
		return this.forecastDangerSourceVariantId;
	}

	public void setForecastDangerSourceVariantId(String dangerSourceVariantId) {
		this.forecastDangerSourceVariantId = dangerSourceVariantId;
	}

	public DangerSource getDangerSource() {
		return this.dangerSource;
	}

	public void setDangerSource(DangerSource dangerSource) {
		this.dangerSource = dangerSource;
	}

	public Instant getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}

	public Instant getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Instant updateDate) {
		this.updateDate = updateDate;
	}

	public Instant getValidFrom() {
		return this.validFrom;
	}

	public void setValidFrom(Instant validFrom) {
		this.validFrom = validFrom;
	}

	public Instant getValidUntil() {
		return this.validUntil;
	}

	public void setValidUntil(Instant validUntil) {
		this.validUntil = validUntil;
	}

	public DangerSourceVariantStatus getDangerSourceVariantStatus() {
		return this.dangerSourceVariantStatus;
	}

	public void setDangerSourceVariantStatus(DangerSourceVariantStatus status) {
		this.dangerSourceVariantStatus = status;
	}

	public DangerSourceVariantType getDangerSourceVariantType() {
		return this.dangerSourceVariantType;
	}

	public void setDangerSourceVariantType(DangerSourceVariantType type) {
		this.dangerSourceVariantType = type;
	}

	public String getOwnerRegion() {
		return this.ownerRegion;
	}

	public void setOwnerRegion(String ownerRegion) {
		this.ownerRegion = ownerRegion;
	}

	public Set<String> getRegions() {
		return this.regions;
	}

	public void setRegions(Set<String> regions) {
		this.regions = regions;
	}

	public Boolean isHasDaytimeDependency() {
		return this.hasDaytimeDependency;
	}

	public Boolean getHasDaytimeDependency() {
		return this.hasDaytimeDependency;
	}

	public void setHasDaytimeDependency(Boolean hasDaytimeDependency) {
		this.hasDaytimeDependency = hasDaytimeDependency;
	}

	public eu.albina.model.enumerations.AvalancheType getAvalancheType() {
		return this.avalancheType;
	}

	public void setAvalancheType(eu.albina.model.enumerations.AvalancheType avalancheType) {
		this.avalancheType = avalancheType;
	}

	public Set<Aspect> getAspects() {
		return this.aspects;
	}

	public void setAspects(Set<Aspect> aspects) {
		this.aspects = aspects;
	}

	public Integer getElevationHigh() {
		return this.elevationHigh;
	}

	public void setElevationHigh(Integer elevationHigh) {
		this.elevationHigh = elevationHigh;
	}

	public Boolean isTreelineHigh() {
		return this.treelineHigh;
	}

	public Boolean getTreelineHigh() {
		return this.treelineHigh;
	}

	public void setTreelineHigh(Boolean treelineHigh) {
		this.treelineHigh = treelineHigh;
	}

	public Integer getElevationLow() {
		return this.elevationLow;
	}

	public void setElevationLow(Integer elevationLow) {
		this.elevationLow = elevationLow;
	}

	public Boolean isTreelineLow() {
		return this.treelineLow;
	}

	public Boolean getTreelineLow() {
		return this.treelineLow;
	}

	public void setTreelineLow(Boolean treelineLow) {
		this.treelineLow = treelineLow;
	}

	public Boolean isDangerIncreaseWithElevation() {
		return this.dangerIncreaseWithElevation;
	}

	public Boolean getDangerIncreaseWithElevation() {
		return this.dangerIncreaseWithElevation;
	}

	public void setDangerIncreaseWithElevation(Boolean dangerIncreaseWithElevation) {
		this.dangerIncreaseWithElevation = dangerIncreaseWithElevation;
	}

	public eu.albina.model.enumerations.Aspect getHighestDangerAspect() {
		return this.highestDangerAspect;
	}

	public void setHighestDangerAspect(eu.albina.model.enumerations.Aspect highestDangerAspect) {
		this.highestDangerAspect = highestDangerAspect;
	}

	public eu.albina.model.enumerations.Daytime getDangerPeak() {
		return this.dangerPeak;
	}

	public void setDangerPeak(eu.albina.model.enumerations.Daytime dangerPeak) {
		this.dangerPeak = dangerPeak;
	}

	public eu.albina.model.enumerations.SlopeGradient getSlopeGradient() {
		return this.slopeGradient;
	}

	public void setSlopeGradient(eu.albina.model.enumerations.SlopeGradient slopeGradient) {
		this.slopeGradient = slopeGradient;
	}

	public Boolean isRunoutIntoGreen() {
		return this.runoutIntoGreen;
	}

	public Boolean getRunoutIntoGreen() {
		return this.runoutIntoGreen;
	}

	public void setRunoutIntoGreen(Boolean runoutIntoGreen) {
		this.runoutIntoGreen = runoutIntoGreen;
	}

	public Boolean isPenetrateDeepLayers() {
		return this.penetrateDeepLayers;
	}

	public Boolean getPenetrateDeepLayers() {
		return this.penetrateDeepLayers;
	}

	public void setPenetrateDeepLayers(Boolean penetrateDeepLayers) {
		this.penetrateDeepLayers = penetrateDeepLayers;
	}

	public eu.albina.model.enumerations.Probability getNaturalRelease() {
		return this.naturalRelease;
	}

	public void setNaturalRelease(eu.albina.model.enumerations.Probability naturalRelease) {
		this.naturalRelease = naturalRelease;
	}

	public Set<DangerSign> getDangerSigns() {
		return this.dangerSigns;
	}

	public void setDangerSigns(Set<DangerSign> dangerSigns) {
		this.dangerSigns = dangerSigns;
	}

	public EawsMatrixInformation getEawsMatrixInformation() {
		return this.eawsMatrixInformation;
	}

	public void setEawsMatrixInformation(EawsMatrixInformation eawsMatrixInformation) {
		this.eawsMatrixInformation = eawsMatrixInformation;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getTextcat() {
		return this.textcat;
	}

	public void setTextcat(String textcat) {
		this.textcat = textcat;
	}

	public GlidingSnowActivity getGlidingSnowActivity() {
		return this.glidingSnowActivity;
	}

	public void setGlidingSnowActivity(GlidingSnowActivity glidingSnowActivity) {
		this.glidingSnowActivity = glidingSnowActivity;
	}

	public int getGlidingSnowActivityValue() {
		return this.glidingSnowActivityValue;
	}

	public void setGlidingSnowActivityValue(int glidingSnowActivityValue) {
		this.glidingSnowActivityValue = glidingSnowActivityValue;
	}

	public int getSnowHeightUpperLimit() {
		return this.snowHeightUpperLimit;
	}

	public void setSnowHeightUpperLimit(int snowHeightUpperLimit) {
		this.snowHeightUpperLimit = snowHeightUpperLimit;
	}

	public int getSnowHeightLowerLimit() {
		return this.snowHeightLowerLimit;
	}

	public void setSnowHeightLowerLimit(int snowHeightLowerLimit) {
		this.snowHeightLowerLimit = snowHeightLowerLimit;
	}

	public int getSnowHeightAverage() {
		return this.snowHeightAverage;
	}

	public void setSnowHeightAverage(int snowHeightAverage) {
		this.snowHeightAverage = snowHeightAverage;
	}

	public Boolean isZeroDegreeIsotherm() {
		return this.zeroDegreeIsotherm;
	}

	public Boolean getZeroDegreeIsotherm() {
		return this.zeroDegreeIsotherm;
	}

	public void setZeroDegreeIsotherm(Boolean zeroDegreeIsotherm) {
		this.zeroDegreeIsotherm = zeroDegreeIsotherm;
	}

	public GrainShape getSlabGrainShape() {
		return this.slabGrainShape;
	}

	public void setSlabGrainShape(GrainShape slabGrainShape) {
		this.slabGrainShape = slabGrainShape;
	}

	public int getSlabThicknessUpperLimit() {
		return this.slabThicknessUpperLimit;
	}

	public void setSlabThicknessUpperLimit(int slabThicknessUpperLimit) {
		this.slabThicknessUpperLimit = slabThicknessUpperLimit;
	}

	public int getSlabThicknessLowerLimit() {
		return this.slabThicknessLowerLimit;
	}

	public void setSlabThicknessLowerLimit(int slabThicknessLowerLimit) {
		this.slabThicknessLowerLimit = slabThicknessLowerLimit;
	}

	public HandHardness getSlabHandHardnessUpperLimit() {
		return this.slabHandHardnessUpperLimit;
	}

	public void setSlabHandHardnessUpperLimit(HandHardness slabHandHardnessUpperLimit) {
		this.slabHandHardnessUpperLimit = slabHandHardnessUpperLimit;
	}

	public HandHardness getSlabHandHardnessLowerLimit() {
		return this.slabHandHardnessLowerLimit;
	}

	public void setSlabHandHardnessLowerLimit(HandHardness slabHandHardnessLowerLimit) {
		this.slabHandHardnessLowerLimit = slabHandHardnessLowerLimit;
	}

	public Tendency getSlabHardnessProfile() {
		return this.slabHardnessProfile;
	}

	public void setSlabHardnessProfile(Tendency slabHardnessProfile) {
		this.slabHardnessProfile = slabHardnessProfile;
	}

	public Characteristic getSlabEnergyTransferPotential() {
		return this.slabEnergyTransferPotential;
	}

	public void setSlabEnergyTransferPotential(Characteristic slabEnergyTransferPotential) {
		this.slabEnergyTransferPotential = slabEnergyTransferPotential;
	}

	public Distribution getSlabDistribution() {
		return this.slabDistribution;
	}

	public void setSlabDistribution(Distribution slabDistribution) {
		this.slabDistribution = slabDistribution;
	}

	public GrainShape getWeakLayerGrainShape() {
		return this.weakLayerGrainShape;
	}

	public void setWeakLayerGrainShape(GrainShape weakLayerGrainShape) {
		this.weakLayerGrainShape = weakLayerGrainShape;
	}

	public double getWeakLayerGrainSizeUpperLimit() {
		return this.weakLayerGrainSizeUpperLimit;
	}

	public void setWeakLayerGrainSizeUpperLimit(double weakLayerGrainSizeUpperLimit) {
		this.weakLayerGrainSizeUpperLimit = weakLayerGrainSizeUpperLimit;
	}

	public double getWeakLayerGrainSizeLowerLimit() {
		return this.weakLayerGrainSizeLowerLimit;
	}

	public void setWeakLayerGrainSizeLowerLimit(double weakLayerGrainSizeLowerLimit) {
		this.weakLayerGrainSizeLowerLimit = weakLayerGrainSizeLowerLimit;
	}

	public Boolean isWeakLayerPersistent() {
		return this.weakLayerPersistent;
	}

	public Boolean getWeakLayerPersistent() {
		return this.weakLayerPersistent;
	}

	public void setWeakLayerPersistent(Boolean weakLayerPersistent) {
		this.weakLayerPersistent = weakLayerPersistent;
	}

	public Thickness getWeakLayerThickness() {
		return this.weakLayerThickness;
	}

	public void setWeakLayerThickness(Thickness weakLayerThickness) {
		this.weakLayerThickness = weakLayerThickness;
	}

	public Characteristic getWeakLayerStrength() {
		return this.weakLayerStrength;
	}

	public void setWeakLayerStrength(Characteristic weakLayerStrength) {
		this.weakLayerStrength = weakLayerStrength;
	}

	public Boolean isWeakLayerWet() {
		return this.weakLayerWet;
	}

	public Boolean getWeakLayerWet() {
		return this.weakLayerWet;
	}

	public void setWeakLayerWet(Boolean weakLayerWet) {
		this.weakLayerWet = weakLayerWet;
	}

	public Crust isWeakLayerCrustAbove() {
		return this.weakLayerCrustAbove;
	}

	public Crust getWeakLayerCrustAbove() {
		return this.weakLayerCrustAbove;
	}

	public void setWeakLayerCrustAbove(Crust weakLayerCrustAbove) {
		this.weakLayerCrustAbove = weakLayerCrustAbove;
	}

	public Crust isWeakLayerCrustBelow() {
		return this.weakLayerCrustBelow;
	}

	public Crust getWeakLayerCrustBelow() {
		return this.weakLayerCrustBelow;
	}

	public void setWeakLayerCrustBelow(Crust weakLayerCrustBelow) {
		this.weakLayerCrustBelow = weakLayerCrustBelow;
	}

	public SnowpackPosition getWeakLayerPosition() {
		return this.weakLayerPosition;
	}

	public void setWeakLayerPosition(SnowpackPosition weakLayerPosition) {
		this.weakLayerPosition = weakLayerPosition;
	}

	public CreationProcess getWeakLayerCreation() {
		return this.weakLayerCreation;
	}

	public void setWeakLayerCreation(CreationProcess weakLayerCreation) {
		this.weakLayerCreation = weakLayerCreation;
	}

	public Distribution getWeakLayerDistribution() {
		return this.weakLayerDistribution;
	}

	public void setWeakLayerDistribution(Distribution weakLayerDistribution) {
		this.weakLayerDistribution = weakLayerDistribution;
	}

	public Recognizability getDangerSpotRecognizability() {
		return this.dangerSpotRecognizability;
	}

	public void setDangerSpotRecognizability(Recognizability dangerSpotRecognizability) {
		this.dangerSpotRecognizability = dangerSpotRecognizability;
	}

	public Boolean isRemoteTriggering() {
		return this.remoteTriggering;
	}

	public Boolean getRemoteTriggering() {
		return this.remoteTriggering;
	}

	public void setRemoteTriggering(Boolean remoteTriggering) {
		this.remoteTriggering = remoteTriggering;
	}

	public Set<TerrainType> getTerrainTypes() {
		return this.terrainTypes;
	}

	public void setTerrainTypes(Set<TerrainType> terrainTypes) {
		this.terrainTypes = terrainTypes;
	}

	public GrainShape getLooseSnowGrainShape() {
		return this.looseSnowGrainShape;
	}

	public void setLooseSnowGrainShape(GrainShape looseSnowGrainShape) {
		this.looseSnowGrainShape = looseSnowGrainShape;
	}

	public Wetness getLooseSnowMoisture() {
		return this.looseSnowMoisture;
	}

	public void setLooseSnowMoisture(Wetness looseSnowMoisture) {
		this.looseSnowMoisture = looseSnowMoisture;
	}

	public Boolean affectsRegion(Region region) {
		return getRegions().stream().anyMatch(region::affects);
	}

	public eu.albina.model.enumerations.AvalancheProblem getAvalancheProblem() {
		switch (this.avalancheType) {
			case slab:
				switch (this.slabGrainShape) {
					case PP:
					case DF:
					case RG:
					case FC:
						if (this.weakLayerPersistent) {
							switch (this.dangerSpotRecognizability) {
								case very_easy:
								case easy:
									return eu.albina.model.enumerations.AvalancheProblem.wind_slab;
								case hard:
								case very_hard:
									return eu.albina.model.enumerations.AvalancheProblem.persistent_weak_layers;
								default:
									return null;
							}
						} else {
							switch (this.dangerSpotRecognizability) {
								case very_easy:
								case easy:
									return eu.albina.model.enumerations.AvalancheProblem.wind_slab;
								case hard:
								case very_hard:
									return eu.albina.model.enumerations.AvalancheProblem.new_snow;
								default:
									return null;
							}
						}
					case MF:
						return eu.albina.model.enumerations.AvalancheProblem.wet_snow;
					case MFcr:
						if (this.weakLayerPersistent) {
							return eu.albina.model.enumerations.AvalancheProblem.persistent_weak_layers;
						} else {
							return eu.albina.model.enumerations.AvalancheProblem.wet_snow;
						}
					default:
						return null;
				}
			case loose:
				switch (this.looseSnowGrainShape) {
					case PP:
					case DF:
						return eu.albina.model.enumerations.AvalancheProblem.new_snow;
					case MF:
						return eu.albina.model.enumerations.AvalancheProblem.wet_snow;
					case FC:
					case DH:
					case SH:
						return null;
					default:
						return null;
				}
			case glide:
				return eu.albina.model.enumerations.AvalancheProblem.gliding_snow;
			default:
				return null;
		}
	}

	public void copy(DangerSourceVariant dangerSourceVariant) {
		// TODO implement
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!DangerSourceVariant.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final DangerSourceVariant other = (DangerSourceVariant) obj;

		// TODO implement

		return true;
	}

	/**
	 * Sort {@code DangerSourceVariant} by danger rating (descending), snowpack
	 * stability, and avalanche size.
	 */
	@Override
	public int compareTo(DangerSourceVariant other) {
		return this.getEawsMatrixInformation().compareTo(other.getEawsMatrixInformation());
	}

	public static DangerSourceVariant readDangerSourceVariant(final URL resource) throws IOException {
		final String validDangerSourceVariantFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		return new DangerSourceVariant(new JSONObject(validDangerSourceVariantFromResource));
	}

	public static List<DangerSourceVariant> readDangerSourceVariants(final URL resource) throws IOException {
		final String validDangerSourceVariantStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		final JSONArray array = new JSONArray(validDangerSourceVariantStringFromResource);
		return IntStream.range(0, array.length()).mapToObj(array::getJSONObject).map(u -> new DangerSourceVariant(u))
				.collect(Collectors.toList());
	}

}
