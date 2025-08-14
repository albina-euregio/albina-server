// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication.rapidmail.recipients.get;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.albina.model.publication.rapidmail.recipientlist.RapidMailRecipientListResponseHref;

public class GetRecipientsResponseLinks {

	@JsonProperty("self")
	private RapidMailRecipientListResponseHref self;

	@JsonProperty("first")
	private RapidMailRecipientListResponseHref first;

	@JsonProperty("last")
	private RapidMailRecipientListResponseHref last;

	public void setSelf(RapidMailRecipientListResponseHref self) {
		this.self = self;
	}

	public RapidMailRecipientListResponseHref getSelf() {
		return self;
	}

	public void setFirst(RapidMailRecipientListResponseHref first) {
		this.first = first;
	}

	public RapidMailRecipientListResponseHref getFirst() {
		return first;
	}

	public void setLast(RapidMailRecipientListResponseHref last) {
		this.last = last;
	}

	public RapidMailRecipientListResponseHref getLast() {
		return last;
	}

	@Override
	public String toString() {
		return "Links{" + "self = '" + self + '\'' + "first = '" + first + '\'' + "last = '" + last + '\'' + "}";
	}
}
