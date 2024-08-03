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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector.Characteristics;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.base.Strings;

import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.Characteristic;
import eu.albina.model.enumerations.CreationProcess;
import eu.albina.model.enumerations.DangerSign;
import eu.albina.model.enumerations.DangerSourceStatus;
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
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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
		implements AvalancheInformationObject, Comparable<DangerSourceVariant> {

	@Column(name = "DANGER_SOURCE_VARIANT_ID")
	private String dangerSourceVariantId;

	@Column(name = "CREATION_DATE")
	private ZonedDateTime creationDate;

	@Column(name = "UPDATE_DATE")
	private ZonedDateTime updateDate;

	/** Validity of the danger source */
	@Column(name = "VALID_FROM")
	private ZonedDateTime validFrom;
	@Column(name = "VALID_UNTIL")
	private ZonedDateTime validUntil;

	@Column(name = "STATUS")
	private DangerSourceStatus status;

	/** The regions the danger source is for. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "danger_source_regions", joinColumns = @JoinColumn(name = "DANGER_SOURCE_ID"))
	@Column(name = "REGION_ID")
	private Set<String> regions;

	@Column(name = "HAS_DAYTIME_DEPENDENCY")
	private boolean hasDaytimeDependency;

	@Enumerated(EnumType.STRING)
	@Column(name = "AVALANCHE_TYPE")
	private eu.albina.model.enumerations.AvalancheType avalancheType;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "danger_source_aspects", joinColumns = @JoinColumn(name = "DANGER_SOURCE_ID", referencedColumnName = "ID"))
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

	@Column(name = "DANGER_INCREASE_WITH_ELEVATION")
	private boolean dangerIncreaseWithElevation;

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
	private boolean runoutIntoGreen;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "NATURAL_RELEASE")
	private eu.albina.model.enumerations.Propability naturalRelease;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "danger_signs", joinColumns = @JoinColumn(name = "DANGER_SOURCE_ID", referencedColumnName = "ID"))
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

	/** --------------------- */
	/** GLIDE SNOW AVALANCHES */
	/** --------------------- */
	@Enumerated(EnumType.STRING)
	@Column(name = "GLIDING_SNOW_ACTIVITY")
	private GlidingSnowActivity glidingSnowActivity;

	@Column(name = "SNOW_HEIGHT_UPPER_LIMIT")
	private int snowHeightUpperLimit;

	@Column(name = "SNOW_HEIGHT_LOWER_LIMIT")
	private int snowHeightLowerLimit;

	@Column(name = "ZERO_DEGREE_ISOTHERM")
	private boolean zeroDegreeIsotherm;

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
	private Characteristics slabEneryTransferPotential;

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
	private boolean weakLayerPersistent;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_THICKNESS")
	private Thickness weakLayerThickness;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_STRENGTH")
	private Characteristic weakLayerStrength;
	
	@Column(name = "WEAK_LAYER_WET")
	private boolean weakLayerWet;
	
	@Column(name = "WEAK_LAYER_CRUST_ABOVE")
	private boolean weakLayerCrustAbove;
	
	@Column(name = "WEAK_LAYER_CRUST_BELOW")
	private boolean weakLayerCrustBelow;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "WEAK_LAYER_POSTION")
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
	private boolean remoteTriggering;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "terrain_types", joinColumns = @JoinColumn(name = "DANGER_SOURCE_ID", referencedColumnName = "ID"))
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
		// TODO create dangerSourceVariantId
		regions = new LinkedHashSet<>();
		aspects = new LinkedHashSet<>();
		dangerSigns = new LinkedHashSet<>();
		terrainTypes = new LinkedHashSet<>();
	}

	/**
	 * Custom constructor that creates a danger source variant object from JSON input.
	 *
	 * @param json
	 *            JSONObject holding information about a danger source variant.
	 */
	public DangerSourceVariant(JSONObject json, Function<String, User> userFunction) {
		this();

		if (json.has("id"))
			this.id = json.getString("id");

		if (json.has("validity")) {
			JSONObject validity = json.getJSONObject("validity");
			this.validFrom = ZonedDateTime.parse(validity.getString("from"));
			this.validUntil = ZonedDateTime.parse(validity.getString("until"));
		}

		if (json.has("regions")) {
			JSONArray regions = json.getJSONArray("regions");
			for (Object entry : regions) {
				this.regions.add((String) entry);
			}
		}

		if (json.has("hasDaytimeDependency"))
			this.hasDaytimeDependency = json.getBoolean("hasDaytimeDependency");
	}

	public String getDangerSourceVariantId() {
		return this.dangerSourceVariantId;
	}

	public void setDangerSourceVariantId(String dangerSourceVariantId) {
		this.dangerSourceVariantId = dangerSourceVariantId;
	}

	public ZonedDateTime getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(ZonedDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public ZonedDateTime getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(ZonedDateTime updateDate) {
		this.updateDate = updateDate;
	}

	public ZonedDateTime getValidFrom() {
		return this.validFrom;
	}

	public void setValidFrom(ZonedDateTime validFrom) {
		this.validFrom = validFrom;
	}

	public ZonedDateTime getValidUntil() {
		return this.validUntil;
	}

	public void setValidUntil(ZonedDateTime validUntil) {
		this.validUntil = validUntil;
	}

	public DangerSourceStatus getStatus() {
		return this.status;
	}

	public void setStatus(DangerSourceStatus status) {
		this.status = status;
	}

	public Set<String> getRegions() {
		return this.regions;
	}

	public void setRegions(Set<String> regions) {
		this.regions = regions;
	}

	public boolean isHasDaytimeDependency() {
		return this.hasDaytimeDependency;
	}

	public boolean getHasDaytimeDependency() {
		return this.hasDaytimeDependency;
	}

	public void setHasDaytimeDependency(boolean hasDaytimeDependency) {
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

	public int getElevationHigh() {
		return this.elevationHigh;
	}

	public void setElevationHigh(int elevationHigh) {
		this.elevationHigh = elevationHigh;
	}

	public boolean isTreelineHigh() {
		return this.treelineHigh;
	}

	public boolean getTreelineHigh() {
		return this.treelineHigh;
	}

	public void setTreelineHigh(boolean treelineHigh) {
		this.treelineHigh = treelineHigh;
	}

	public int getElevationLow() {
		return this.elevationLow;
	}

	public void setElevationLow(int elevationLow) {
		this.elevationLow = elevationLow;
	}

	public boolean isTreelineLow() {
		return this.treelineLow;
	}

	public boolean getTreelineLow() {
		return this.treelineLow;
	}

	public void setTreelineLow(boolean treelineLow) {
		this.treelineLow = treelineLow;
	}

	public boolean isDangerIncreaseWithElevation() {
		return this.dangerIncreaseWithElevation;
	}

	public boolean getDangerIncreaseWithElevation() {
		return this.dangerIncreaseWithElevation;
	}

	public void setDangerIncreaseWithElevation(boolean dangerIncreaseWithElevation) {
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

	public boolean isRunoutIntoGreen() {
		return this.runoutIntoGreen;
	}

	public boolean getRunoutIntoGreen() {
		return this.runoutIntoGreen;
	}

	public void setRunoutIntoGreen(boolean runoutIntoGreen) {
		this.runoutIntoGreen = runoutIntoGreen;
	}

	public eu.albina.model.enumerations.Propability getNaturalRelease() {
		return this.naturalRelease;
	}

	public void setNaturalRelease(eu.albina.model.enumerations.Propability naturalRelease) {
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

	public GlidingSnowActivity getGlidingSnowActivity() {
		return this.glidingSnowActivity;
	}

	public void setGlidingSnowActivity(GlidingSnowActivity glidingSnowActivity) {
		this.glidingSnowActivity = glidingSnowActivity;
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

	public boolean isZeroDegreeIsotherm() {
		return this.zeroDegreeIsotherm;
	}

	public boolean getZeroDegreeIsotherm() {
		return this.zeroDegreeIsotherm;
	}

	public void setZeroDegreeIsotherm(boolean zeroDegreeIsotherm) {
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

	public Characteristics getSlabEneryTransferPotential() {
		return this.slabEneryTransferPotential;
	}

	public void setSlabEneryTransferPotential(Characteristics slabEneryTransferPotential) {
		this.slabEneryTransferPotential = slabEneryTransferPotential;
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

	public boolean isWeakLayerPersistent() {
		return this.weakLayerPersistent;
	}

	public boolean getWeakLayerPersistent() {
		return this.weakLayerPersistent;
	}

	public void setWeakLayerPersistent(boolean weakLayerPersistent) {
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

	public boolean isWeakLayerWet() {
		return this.weakLayerWet;
	}

	public boolean getWeakLayerWet() {
		return this.weakLayerWet;
	}

	public void setWeakLayerWet(boolean weakLayerWet) {
		this.weakLayerWet = weakLayerWet;
	}

	public boolean isWeakLayerCrustAbove() {
		return this.weakLayerCrustAbove;
	}

	public boolean getWeakLayerCrustAbove() {
		return this.weakLayerCrustAbove;
	}

	public void setWeakLayerCrustAbove(boolean weakLayerCrustAbove) {
		this.weakLayerCrustAbove = weakLayerCrustAbove;
	}

	public boolean isWeakLayerCrustBelow() {
		return this.weakLayerCrustBelow;
	}

	public boolean getWeakLayerCrustBelow() {
		return this.weakLayerCrustBelow;
	}

	public void setWeakLayerCrustBelow(boolean weakLayerCrustBelow) {
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

	public boolean isRemoteTriggering() {
		return this.remoteTriggering;
	}

	public boolean getRemoteTriggering() {
		return this.remoteTriggering;
	}

	public void setRemoteTriggering(boolean remoteTriggering) {
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

	public static ZonedDateTime getValidityDate(ZonedDateTime validFrom) {
		ZonedDateTime date = validFrom;
		if (validFrom.getHour() > 12)
			date = date.plusDays(1);
		return date;
	}

	public ZonedDateTime getValidityDate() {
		return getValidityDate(validFrom);
	}

	public String getValidityDateString() {
		return getValidityDate().toLocalDate().toString();
	}

	@Override
	public JSONObject toJSON() {

		// TODO implement

		JSONObject json = new JSONObject();

		if (!Strings.isNullOrEmpty(id))
			json.put("id", id);

		JSONObject validity = new JSONObject();
		validity.put("from", DateTimeFormatter.ISO_INSTANT.format(validFrom));
		validity.put("until", DateTimeFormatter.ISO_INSTANT.format(validUntil));
		json.put("validity", validity);

		json.put("regions", regions);
		json.put("hasDaytimeDependency", hasDaytimeDependency);

		return json;
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
	 * Sort {@code DangerSourceVariant} by danger rating (descending), snowpack stability, and avalanche size.
	 *
	 */
	@Override
	public int compareTo(DangerSourceVariant other) {
		// TODO implement
		return 0;
	}

}
