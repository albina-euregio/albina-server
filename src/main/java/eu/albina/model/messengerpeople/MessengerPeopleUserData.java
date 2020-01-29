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
public class MessengerPeopleUserData {

	@JsonProperty("agent")
	private String agent;

	@JsonProperty("name")
	private String name;

	@JsonProperty("customfields")
	private MessengerPeopleUserDataCustomfields customfields;

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getAgent() {
		return agent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setCustomfields(MessengerPeopleUserDataCustomfields customfields) {
		this.customfields = customfields;
	}

	public MessengerPeopleUserDataCustomfields getCustomfields() {
		return customfields;
	}

	@Override
	public String toString() {
		return "MessengerPeopleUserData{" + "agent = '" + agent + '\'' + ",name = '" + name + '\'' + ",customfields = '"
				+ customfields + '\'' + "}";
	}

	public MessengerPeopleUserData agent(String agent) {
		this.agent = agent;
		return this;
	}

	public MessengerPeopleUserData name(String name) {
		this.name = name;
		return this;
	}

	public MessengerPeopleUserData customfields(MessengerPeopleUserDataCustomfields customfields) {
		this.customfields = customfields;
		return this;
	}
}
