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
package eu.albina.model.publication.rapidmail.mailings;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Generated("com.robohorse.robopojogenerator")
public class PostMailingsRequestDestination {

	@JsonProperty("action")
	private String action;

	@JsonProperty("id")
	private Integer id;

	@JsonProperty("type")
	private String type;

	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "DestinationsItem{" + "action = '" + action + '\'' + ",id = '" + id + '\'' + ",type = '" + type + '\''
				+ "}";
	}

	public PostMailingsRequestDestination action(String action) {
		this.action = action;
		return this;
	}

	public PostMailingsRequestDestination id(Integer id) {
		this.id = id;
		return this;
	}

	public PostMailingsRequestDestination type(String type) {
		this.type = type;
		return this;
	}
}
