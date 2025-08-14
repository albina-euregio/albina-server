// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
 */
@JsonPropertyOrder({"highlights", "comment"})
public class Texts {
    private String comment;
    private String highlights;

	public Texts() {
	}

	public Texts(String highlights, String comment) {
		this.comment = comment;
		this.highlights = highlights;
	}

	public String getComment() { return comment; }
    public void setComment(String value) { this.comment = value; }

    public String getHighlights() { return highlights; }
    public void setHighlights(String value) { this.highlights = value; }
}
