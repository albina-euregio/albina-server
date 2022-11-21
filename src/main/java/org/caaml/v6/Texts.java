package org.caaml.v6;

/**
 * Texts element with highlight and comment for the avalanche activity.
 *
 * Texts contains a highlight and a comment string, where highlights could also be described
 * as a kind of headline for the longer comment. For text-formating only the HTML-Tags <br/>
 * for a new line and <b> followed by </b> for a bold text.
 *
 * Texts element with highlight and comment for details on the snowpack structure.
 *
 * Texts element with highlight and comment for travel advisory.
 *
 * Texts element with highlight and comment for weather forcast information.
 */
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
