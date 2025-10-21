// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Texts element with highlight and comment for the avalanche activity.
 *
 * Texts contains a highlight and a comment string, where highlights could also be described
 * as a kind of headline for the longer comment. For text-formatting the HTML-Tags <br/> for
 * a new line, (<ul>,<ul/>) and (<li>,<li/>) for lists, (<h1>,<h1/>) to (<h6>,<h6/>) for
 * headings and (<b>,</b>) for a bold text are allowed.
 *
 * Texts element with highlight and comment for details on the snowpack structure.
 *
 * Texts element with highlight and comment for travel advisory.
 *
 * Texts element with highlight and comment for weather forecast information.
 *
 * Texts element with highlight and comment for weather review information.
 *
 * Describes the expected tendency of the development of the avalanche situation for a
 * defined time period.
 */
@JsonPropertyOrder({"highlights", "comment", "tendencyType", "validTime", "metaData", "customData"})
@Serdeable
public class Tendency {
    private String comment;
    private String highlights;
    private Object customData;
    private MetaData metaData;
    private TendencyType tendencyType;
    private ValidTime validTime;

	public Tendency() {
	}

	public Tendency(String highlights, TendencyType tendencyType, ValidTime validTime) {
		this.highlights = highlights;
		this.tendencyType = tendencyType;
		this.validTime = validTime;
	}

	public String getComment() { return comment; }
    public void setComment(String value) { this.comment = value; }

    public String getHighlights() { return highlights; }
    public void setHighlights(String value) { this.highlights = value; }

    public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public TendencyType getTendencyType() { return tendencyType; }
    public void setTendencyType(TendencyType value) { this.tendencyType = value; }

    public ValidTime getValidTime() { return validTime; }
    public void setValidTime(ValidTime value) { this.validTime = value; }
}
