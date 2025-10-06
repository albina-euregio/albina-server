// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import io.micronaut.serde.annotation.Serdeable;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheProblemType;
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
import eu.albina.model.enumerations.Probability;
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
@Serdeable
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
	private Probability naturalRelease;

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

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "danger_source_variant_weak_layer_grain_shapes", joinColumns = @JoinColumn(name = "DANGER_SOURCE_VARIANT_ID"))
	@Column(name = "WEAK_LAYER_GRAIN_SHAPE")
	@Enumerated(EnumType.STRING)
	private Set<GrainShape> weakLayerGrainShapes;

	@Column(name = "WEAK_LAYER_GRAIN_SIZE_UPPER_LIMIT", columnDefinition = "double")
	private double weakLayerGrainSizeUpperLimit;

	@Column(name = "WEAK_LAYER_GRAIN_SIZE_LOWER_LIMIT", columnDefinition = "double")
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

	@Enumerated(EnumType.STRING)
	@Column(name = "REMOTE_TRIGGERING")
	private Probability remoteTriggering;

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

	public Set<GrainShape> getWeakLayerGrainShapes() {
		return this.weakLayerGrainShapes;
	}

	public void setWeakLayerGrainShapes(Set<GrainShape> weakLayerGrainShapes) {
		this.weakLayerGrainShapes = weakLayerGrainShapes;
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

	public Probability getRemoteTriggering() {
		return this.remoteTriggering;
	}

	public void setRemoteTriggering(Probability remoteTriggering) {
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

	public eu.albina.model.enumerations.AvalancheProblem deriveAvalancheProblem() {
		switch (this.avalancheType) {
			case slab:
				if (this.slabGrainShape == null) {
					return null;
				}
				switch (this.slabGrainShape) {
					case PP:
					case DF:
					case RG:
					case FC:
						if (Boolean.TRUE.equals(this.weakLayerPersistent)) {
							if (this.dangerSpotRecognizability == null) {
								return null;
							}
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
							if (this.dangerSpotRecognizability == null) {
								return null;
							}
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
						if (Boolean.TRUE.equals(this.weakLayerPersistent)) {
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

	public AvalancheProblemType deriveAvalancheProblemType() {
		switch (this.avalancheType) {
			case slab:
				if (this.slabGrainShape == null) {
					return null;
				}
				switch (this.slabGrainShape) {
					case PP:
					case DF:
					case RG:
					case FC:
						if (Boolean.TRUE.equals(this.weakLayerPersistent)) {
							if (this.dangerSpotRecognizability == null) {
								return null;
							}
							switch (this.dangerSpotRecognizability) {
								case very_easy:
								case easy:
									return AvalancheProblemType.wind_slab;
								case hard:
								case very_hard:
									if (this.getWeakLayerPosition() != null
											&& (this.getWeakLayerPosition() == SnowpackPosition.ground
													|| this.getWeakLayerPosition() == SnowpackPosition.lower)) {
										return AvalancheProblemType.deep_persistent_slab;
									} else {
										return AvalancheProblemType.persistent_slab;
									}
								default:
									return null;
							}
						} else {
							if (this.dangerSpotRecognizability == null) {
								return null;
							}
							switch (this.dangerSpotRecognizability) {
								case very_easy:
								case easy:
									return AvalancheProblemType.wind_slab;
								case hard:
								case very_hard:
									return AvalancheProblemType.storm_slab;
								default:
									return null;
							}
						}
					case MF:
						return AvalancheProblemType.wet_slab;
					case MFcr:
						if (Boolean.TRUE.equals(this.weakLayerPersistent)) {
							return AvalancheProblemType.persistent_slab;
						} else {
							return AvalancheProblemType.wet_slab;
						}
					default:
						return null;
				}
			case loose:
				switch (this.looseSnowGrainShape) {
					case PP:
					case DF:
					case FC:
					case DH:
					case SH:
						return AvalancheProblemType.dry_loose;
					case MF:
						return AvalancheProblemType.wet_loose;
					default:
						return null;
				}
			case glide:
				return AvalancheProblemType.glide_avalanche;
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

}
