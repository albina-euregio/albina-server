package org.caaml.v6;

/**
 * Elevation describes either an elevation range below a certain bound (only upperBound is
 * set to a value) or above a certain bound (only lowerBound is set to a value). If both
 * values are set to a value, an elevation band is defined by this property. The value uses
 * a numeric value, not more detailed than 100m resolution. Additionally to the numeric
 * values also 'treeline' is allowed.
 */
public class ElevationBounderyOrBand {
	// FIXME type Boundery -> Boundary
    private String lowerBound;
    private String upperBound;

	public ElevationBounderyOrBand() {
	}

	public ElevationBounderyOrBand(String lowerBound, String upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public String getLowerBound() { return lowerBound; }
    public void setLowerBound(String value) { this.lowerBound = value; }

    public String getUpperBound() { return upperBound; }
    public void setUpperBound(String value) { this.upperBound = value; }
}
