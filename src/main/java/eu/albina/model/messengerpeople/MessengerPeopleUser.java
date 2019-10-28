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

	@JsonProperty("blocked")
	private String blocked;

	@JsonProperty("stopped")
	private String stopped;

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

	public void setBlocked(String blocked) {
		this.blocked = blocked;
	}

	public String getBlocked() {
		return blocked;
	}

	public void setStopped(String stopped) {
		this.stopped = stopped;
	}

	public String getStopped() {
		return stopped;
	}

	@Override
	public String toString() {
		return "Response{" + "image = '" + image + '\'' + ",messenger = '" + messenger + '\'' + ",created = '" + created
				+ '\'' + ",name = '" + name + '\'' + ",id = '" + id + '\'' + ",status = '" + status + '\''
				+ ",blocked = '" + blocked + '\'' + ",stopped = '" + stopped + '\'' + "}";
	}
}
