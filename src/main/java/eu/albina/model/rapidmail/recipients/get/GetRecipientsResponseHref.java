package eu.albina.model.rapidmail.recipients.get;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
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