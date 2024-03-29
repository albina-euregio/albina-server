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

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostMailingsRequestPostFile {

	@JsonProperty("description")
	private String description;

	@JsonProperty("type")
	private String type;

	@JsonProperty("content")
	private String content;

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "File{" + "description = '" + description + '\'' + "type = '" + type + '\'' + ",content = '" + content
				+ '\'' + "}";
	}

	public PostMailingsRequestPostFile type(String type) {
		this.type = type;
		return this;
	}

	public PostMailingsRequestPostFile content(String content) {
		this.content = content;
		return this;
	}

	public PostMailingsRequestPostFile description(String description) {
		this.description = description;
		return this;
	}
}
