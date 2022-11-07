package org.caaml.v6;

/**
 * Tendency element for a detailed description of the expected avalanche situation tendency
 * after the bulletin's period of validity.
 *
 * Describes the expected tendency of the development of the avalanche situation for a
 * defined time period.
 */
public class AvalancheSituationTendency {
    private String comment;
    private Object customData;
    private String highlights;
	// FIXME highlights, comment -> Texts
    private MetaData metaData;
    private TendencyType tendencyType;
    private ValidTime validTime;

	public AvalancheSituationTendency() {
	}

	public AvalancheSituationTendency(String highlights, TendencyType tendencyType) {
		this.highlights = highlights;
		this.tendencyType = tendencyType;
	}

	public String getComment() { return comment; }
    public void setComment(String value) { this.comment = value; }

    public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public String getHighlights() { return highlights; }
    public void setHighlights(String value) { this.highlights = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public TendencyType getTendencyType() { return tendencyType; }
    public void setTendencyType(TendencyType value) { this.tendencyType = value; }

    public ValidTime getValidTime() { return validTime; }
    public void setValidTime(ValidTime value) { this.validTime = value; }
}
