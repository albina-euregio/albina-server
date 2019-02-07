package eu.albina.model.messengerpeople;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
public class MessengerPeopleUserDataCustomfields {

	@JsonProperty("language")
	private String language;

	@JsonProperty("regionConfiguration")
	private String region;

	@JsonProperty("format")
	private String format;

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getRegion() {
		return region;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public String toString() {
		return "Customfields{" + "language = '" + language + '\'' + ",regionConfiguration = '" + region + '\''
				+ ",format = '" + format + '\'' + "}";
	}

	public MessengerPeopleUserDataCustomfields language(String language) {
		this.language = language;
		return this;
	}

	public MessengerPeopleUserDataCustomfields region(String region) {
		this.region = region;
		return this;
	}

	public MessengerPeopleUserDataCustomfields format(String format) {
		this.format = format;
		return this;
	}
}
