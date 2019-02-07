package eu.albina.model.rapidmail.recipientlist;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
public class RapidMailRecipientListResponseLinks {

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