// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Defines a danger rating, its elevation constraints and the valid time period. If
 * validTimePeriod or elevation are constrained for a rating, it is expected to define a
 * dangerRating for all the other cases.
 */
@JsonPropertyOrder({"mainValue", "elevation", "aspect", "validTimePeriod", "metaData", "customData"})
@Serdeable
public class DangerRating {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "aspect")
    private List<Aspect> aspects;
    private Object customData;
    private ElevationBoundaryOrBand elevation;
    private DangerRatingValue mainValue;
    private MetaData metaData;
    private ValidTimePeriod validTimePeriod;

    public List<Aspect> getAspects() { return aspects; }
    public void setAspects(List<Aspect> value) { this.aspects = value; }

    public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public ElevationBoundaryOrBand getElevation() { return elevation; }
    public void setElevation(ElevationBoundaryOrBand value) { this.elevation = value; }

    public DangerRatingValue getMainValue() { return mainValue; }
    public void setMainValue(DangerRatingValue value) { this.mainValue = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public ValidTimePeriod getValidTimePeriod() { return validTimePeriod; }
    public void setValidTimePeriod(ValidTimePeriod value) { this.validTimePeriod = value; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DangerRating that = (DangerRating) o;
		return Objects.equals(elevation, that.elevation) && mainValue == that.mainValue && validTimePeriod == that.validTimePeriod;
	}

	@Override
	public int hashCode() {
		return Objects.hash(elevation, mainValue, validTimePeriod);
	}
}
