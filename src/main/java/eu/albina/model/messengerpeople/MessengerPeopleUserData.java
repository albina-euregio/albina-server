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
