/*******************************************************************************
 * Copyright (C) 2019 Clesius srl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.model.rapidmail.recipients.get;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.albina.model.rapidmail.recipientlist.RapidMailRecipientListResponseHref;

@Generated("com.robohorse.robopojogenerator")
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
