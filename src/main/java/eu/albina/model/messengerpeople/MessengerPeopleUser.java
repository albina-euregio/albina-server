package eu.albina.model.messengerpeople;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
public class MessengerPeopleUser {

	@JsonProperty("image")
	private String image;

	@JsonProperty("messenger")
	private String messenger;

	@JsonProperty("created")
	private String created;

	@JsonProperty("name")
	private String name;

	@JsonProperty("id")
	private String id;

	@JsonProperty("status")
	private String status;

	public void setImage(String image) {
		this.image = image;
	}

	public String getImage() {
		return image;
	}

	public void setMessenger(String messenger) {
		this.messenger = messenger;
	}

	public String getMessenger() {
		return messenger;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getCreated() {
		return created;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "Response{" + "image = '" + image + '\'' + ",messenger = '" + messenger + '\'' + ",created = '" + created
				+ '\'' + ",name = '" + name + '\'' + ",id = '" + id + '\'' + ",status = '" + status + '\'' + "}";
	}
}
