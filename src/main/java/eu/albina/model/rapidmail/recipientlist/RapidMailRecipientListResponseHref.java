package eu.albina.model.rapidmail.recipientlist;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
public class RapidMailRecipientListResponseHref {

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