// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication.rapidmail.recipients.get;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetRecipientsResponseHref {

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
		return "Self{" + "href = '" + href + '\'' + "}";
	}
}
