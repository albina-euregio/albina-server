/*******************************************************************************
 * Copyright (C) 2022 Norbert Lanzanasto
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
package eu.albina.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * This class holds all information about a server instance.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "server_instances")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ServerInstance.class)
public class ServerInstance implements AvalancheInformationObject {

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "API_URL")
	private String apiUrl;

	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "PASSWORD")
	private String password;

	@OneToMany(mappedBy = "serverInstance")
	private List<Region> regions;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public ServerInstance() {
	}

	public ServerInstance(JSONObject json) {
		this();
		if (json.has("id") && !json.isNull("id"))
			this.id = json.getString("id");
		if (json.has("name") && !json.isNull("name"))
			this.name = json.getString("name");
		if (json.has("apiUrl") && !json.isNull("apiUrl"))
			this.apiUrl = json.getString("apiUrl");
		if (json.has("userName") && !json.isNull("userName"))
			this.userName = json.getString("userName");
		if (json.has("password") && !json.isNull("password"))
			this.password = json.getString("password");
		if (json.has("regions")) {
			JSONArray regions = json.getJSONArray("regions");
			for (Object region : regions) {
				this.regions.add(new Region((JSONObject) region));
			}
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName= userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Region> getRegions() {
		return regions;
	}

	public void setRegions(List<Region> regions) {
		this.regions = regions;
	}

	public void addRegion(Region region) {
		if (!this.regions.contains(region))
			this.regions.add(region);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		json.put("id", getId());
		json.put("name", getName());
		json.put("apiUrl", getApiUrl());
		json.put("userName", getUserName());
		json.put("password", getPassword());
		if (regions != null && regions.size() > 0) {
			JSONArray jsonRegions = new JSONArray();
			for (Region region : regions) {
				jsonRegions.put(region.toJSON());
			}
			json.put("regions", jsonRegions);
		}

		return json;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ServerInstance server = (ServerInstance) o;
		return Objects.equals(id, server.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
