package eu.albina.model.rapidmail.mailings;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
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