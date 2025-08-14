// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication.rapidmail.mailings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RapidMailMailingsResponseHref {

	@JsonProperty("href")
	private String href;

	public void setHref(String href) {
		this.href = href;
	}

	public String getHref() {
		return href;
	}

	@Override
	public String toString() {
		return "{" + "href = '" + href + '\'' + "}";
	}
}
