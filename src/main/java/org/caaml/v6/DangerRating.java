package org.caaml.v6;

import java.util.Objects;

/**
 * Defines a danger rating, its elevation constraints and the valid time period. If
 * validTimePeriod or elevation are constrained for a rating, it is expected to define a
 * dangerRating for all the other cases.
 */
public class DangerRating {
    private Object customData;
    private ElevationBounderyOrBand elevation;
    private DangerRatingValue mainValue;
    private MetaData metaData;
    private ValidTimePeriod validTimePeriod;

    public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public ElevationBounderyOrBand getElevation() { return elevation; }
    public void setElevation(ElevationBounderyOrBand value) { this.elevation = value; }

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
