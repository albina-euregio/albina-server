// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication.rapidmail.mailings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RapidMailMailingsResponseLinks {

	@JsonProperty("self")
	private RapidMailMailingsResponseHref self;

	@JsonProperty("first")
	private RapidMailMailingsResponseHref first;

	@JsonProperty("last")
	private RapidMailMailingsResponseHref last;

	public void setSelf(RapidMailMailingsResponseHref self) {
		this.self = self;
	}

	public RapidMailMailingsResponseHref getSelf() {
		return self;
	}

	public void setFirst(RapidMailMailingsResponseHref first) {
		this.first = first;
	}

	public RapidMailMailingsResponseHref getFirst() {
		return first;
	}

	public void setLast(RapidMailMailingsResponseHref last) {
		this.last = last;
	}

	public RapidMailMailingsResponseHref getLast() {
		return last;
	}

	@Override
	public String toString() {
		return "Links{" + "self = '" + self + '\'' + "first = '" + first + '\'' + "last = '" + last + '\'' + "}";
	}
}
