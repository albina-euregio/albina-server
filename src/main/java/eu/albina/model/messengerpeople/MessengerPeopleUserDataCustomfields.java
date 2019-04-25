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
