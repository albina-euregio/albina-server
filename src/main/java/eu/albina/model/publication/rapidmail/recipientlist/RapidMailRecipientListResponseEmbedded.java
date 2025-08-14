// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication.rapidmail.recipientlist;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RapidMailRecipientListResponseEmbedded {

	@JsonProperty("recipientlists")
	private List<RapidMailRecipientListResponseItem> recipientlists;

	public void setRecipientlists(List<RapidMailRecipientListResponseItem> recipientlists) {
		this.recipientlists = recipientlists;
	}

	public List<RapidMailRecipientListResponseItem> getRecipientlists() {
		return recipientlists;
	}

	@Override
	public String toString() {
		return "Embedded{" + "recipientlists = '" + recipientlists + '\'' + "}";
	}
}
