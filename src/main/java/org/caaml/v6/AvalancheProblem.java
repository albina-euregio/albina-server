// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Defines an avalanche problem, its time, aspect, and elevation constraints. A textual
 * detail about the affected terrain can be given in the comment field. Also, details about
 * the expected avalanche size, snowpack stability and its frequency can be defined. The
 * implied danger rating value is optional.
 */
@JsonPropertyOrder({"problemType", "avalancheType", "elevation", "aspect", "validTimePeriod", "snowpackStability", "frequency", "avalancheSize", "dangerRatingValue", "comment", "metaData", "customData"})
public class AvalancheProblem {
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "aspect")
    private List<Aspect> aspects;
    private Integer avalancheSize;
    private String comment;
    private Object customData;
    private DangerRatingValue dangerRatingValue;
    private ElevationBoundaryOrBand elevation;
    private ExpectedAvalancheFrequency frequency;
    private MetaData metaData;
    private AvalancheProblemType problemType;
    private AvalancheTypeType avalancheType;
    private ExpectedSnowpackStability snowpackStability;
    private ValidTimePeriod validTimePeriod;

    public List<Aspect> getAspects() { return aspects; }
    public void setAspects(List<Aspect> value) { this.aspects = value; }

    public Integer getAvalancheSize() { return avalancheSize; }
    public void setAvalancheSize(Integer value) { this.avalancheSize = value; }

    public String getComment() { return comment; }
    public void setComment(String value) { this.comment = value; }

    public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public DangerRatingValue getDangerRatingValue() { return dangerRatingValue; }
    public void setDangerRatingValue(DangerRatingValue value) { this.dangerRatingValue = value; }

    public ElevationBoundaryOrBand getElevation() { return elevation; }
    public void setElevation(ElevationBoundaryOrBand value) { this.elevation = value; }

    public ExpectedAvalancheFrequency getFrequency() { return frequency; }
    public void setFrequency(ExpectedAvalancheFrequency value) { this.frequency = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public AvalancheProblemType getProblemType() { return problemType; }
    public void setProblemType(AvalancheProblemType value) { this.problemType = value; }

    public AvalancheTypeType getAvalancheType() { return avalancheType; }
    public void setAvalancheType(AvalancheTypeType value) { this.avalancheType = value; }

    public ExpectedSnowpackStability getSnowpackStability() { return snowpackStability; }
    public void setSnowpackStability(ExpectedSnowpackStability value) { this.snowpackStability = value; }

    public ValidTimePeriod getValidTimePeriod() { return validTimePeriod; }
    public void setValidTimePeriod(ValidTimePeriod value) { this.validTimePeriod = value; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AvalancheProblem that = (AvalancheProblem) o;
		return Objects.equals(aspects, that.aspects) && Objects.equals(avalancheSize, that.avalancheSize) && Objects.equals(elevation, that.elevation) && frequency == that.frequency && problemType == that.problemType && avalancheType == that.avalancheType && snowpackStability == that.snowpackStability && Objects.equals(comment, that.comment) && validTimePeriod == that.validTimePeriod;
	}

	@Override
	public int hashCode() {
		return Objects.hash(aspects, avalancheSize, elevation, frequency, problemType, avalancheType, snowpackStability, comment, validTimePeriod);
	}
}
