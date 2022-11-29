package org.caaml.v6;

import java.util.List;
import java.util.Objects;

/**
 * Defines an avalanche problem, its time, aspect, and elevation constraints. A textual
 * detail about the affected terrain can be given in the terrainFeature field. Also, details
 * about the expected avalanche size, snowpack stability and its frequency can be defined.
 */
public class AvalancheProblem {
    private List<Aspect> aspects;
    private Integer avalancheSize;
    private CustomData[] customData;
    private ElevationBoundaryOrBand elevation;
    private ExpectedAvalancheFrequency frequency;
    private MetaData metaData;
    private AvalancheProblemType problemType;
    private ExpectedSnowpackStability snowpackStability;
    private String terrainFeature;
    private ValidTimePeriod validTimePeriod;

    public List<Aspect> getAspects() { return aspects; }
    public void setAspects(List<Aspect> value) { this.aspects = value; }

    public Integer getAvalancheSize() { return avalancheSize; }
    public void setAvalancheSize(Integer value) { this.avalancheSize = value; }

    public CustomData[] getCustomData() { return customData; }
    public void setCustomData(CustomData[] value) { this.customData = value; }

    public ElevationBoundaryOrBand getElevation() { return elevation; }
    public void setElevation(ElevationBoundaryOrBand value) { this.elevation = value; }

    public ExpectedAvalancheFrequency getFrequency() { return frequency; }
    public void setFrequency(ExpectedAvalancheFrequency value) { this.frequency = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public AvalancheProblemType getProblemType() { return problemType; }
    public void setProblemType(AvalancheProblemType value) { this.problemType = value; }

    public ExpectedSnowpackStability getSnowpackStability() { return snowpackStability; }
    public void setSnowpackStability(ExpectedSnowpackStability value) { this.snowpackStability = value; }

    public String getTerrainFeature() { return terrainFeature; }
    public void setTerrainFeature(String value) { this.terrainFeature = value; }

    public ValidTimePeriod getValidTimePeriod() { return validTimePeriod; }
    public void setValidTimePeriod(ValidTimePeriod value) { this.validTimePeriod = value; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AvalancheProblem that = (AvalancheProblem) o;
		return Objects.equals(aspects, that.aspects) && Objects.equals(avalancheSize, that.avalancheSize) && Objects.equals(elevation, that.elevation) && frequency == that.frequency && problemType == that.problemType && snowpackStability == that.snowpackStability && Objects.equals(terrainFeature, that.terrainFeature) && validTimePeriod == that.validTimePeriod;
	}

	@Override
	public int hashCode() {
		return Objects.hash(aspects, avalancheSize, elevation, frequency, problemType, snowpackStability, terrainFeature, validTimePeriod);
	}

}
