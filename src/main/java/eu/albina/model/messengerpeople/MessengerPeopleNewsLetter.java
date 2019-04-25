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
public class MessengerPeopleNewsLetter {

	@JsonProperty("code")
	private int code;

	@JsonProperty("broadcast_id")
	private int broadcastId;

	@JsonProperty("reusable_attachment")
	private String reusableAttachment;

	public void setCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setBroadcastId(int broadcastId) {
		this.broadcastId = broadcastId;
	}

	public int getBroadcastId() {
		return broadcastId;
	}

	public String getReusableAttachment() {
		return reusableAttachment;
	}

	public void setReusableAttachment(String reusableAttachment) {
		this.reusableAttachment = reusableAttachment;
	}

	@Override
	public String toString() {
		return "NewsletterId{" + "code = '" + code + '\'' + ",broadcast_id = '" + broadcastId + '\''
				+ ",reusable_attachment = '" + reusableAttachment + '\'' + "}";
	}
}
