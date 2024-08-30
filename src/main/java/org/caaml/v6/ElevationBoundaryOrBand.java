package org.caaml.v6;

import java.util.Objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Elevation describes either an elevation range below a certain bound (only upperBound is
 * set to a value) or above a certain bound (only lowerBound is set to a value). If both
 * values are set to a value, an elevation band is defined by this property. The value uses
 * a numeric value, not more detailed than 100m resolution. Additionally to the numeric
 * values also 'treeline' is allowed.
 */
public class ElevationBoundaryOrBand {
    private String lowerBound;
    private String upperBound;
	@JacksonXmlProperty(isAttribute = true)
	private String uom = "m";

	public ElevationBoundaryOrBand() {
	}

	public ElevationBoundaryOrBand(String lowerBound, String upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public String getLowerBound() { return lowerBound; }
    public void setLowerBound(String value) { this.lowerBound = value; }

    public String getUpperBound() { return upperBound; }
    public void setUpperBound(String value) { this.upperBound = value; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ElevationBoundaryOrBand that = (ElevationBoundaryOrBand) o;
		return Objects.equals(lowerBound, that.lowerBound) && Objects.equals(upperBound, that.upperBound);
	}

	@Override
	public int hashCode() {
		return Objects.hash(lowerBound, upperBound);
	}
}
