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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
public class ServerInstance implements AvalancheInformationObject, Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "API_URL")
	private String apiUrl;

	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "EXTERNAL_SERVER")
	private boolean externalServer;

	@OneToMany(mappedBy = "serverInstance", fetch = FetchType.EAGER)
	private List<Region> regions;

	@Column(name = "PUBLISH_AT_5PM")
	private boolean publishAt5PM;

	@Column(name = "PUBLISH_AT_8PM")
	private boolean publishAt8AM;

	@Column(name = "PDF_DIRECTORY")
	private String pdfDirectory;

	@Column(name = "HTML_DIRECTORY")
	private String htmlDirectory;

	@Column(name = "MAPS_PATH")
	private String mapsPath;

	@Column(name = "MAP_PRODUCTION_URL")
	private String mapProductionUrl;

	@Column(name = "SERVER_IMAGES_URL")
	private String serverImagesUrl;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public ServerInstance() {
	}

	public ServerInstance(JSONObject json, Function<String, Region> regionFunction) {
		this();
		if (json.has("id") && !json.isNull("id"))
			this.id = json.getLong("id");
		if (json.has("name") && !json.isNull("name"))
			this.name = json.getString("name");
		if (json.has("apiUrl") && !json.isNull("apiUrl"))
			this.apiUrl = json.getString("apiUrl");
		if (json.has("userName") && !json.isNull("userName"))
			this.userName = json.getString("userName");
		if (json.has("password") && !json.isNull("password"))
			this.password = json.getString("password");
		if (json.has("external") && !json.isNull("external"))
			this.externalServer = json.getBoolean("external");
		if (json.has("regions")) {
			JSONArray regions = json.getJSONArray("regions");
			for (Object region : regions) {
				this.regions.add(new Region((JSONObject) region, regionFunction));
			}
		}
		if (json.has("publishAt5PM") && !json.isNull("publishAt5PM"))
			this.publishAt5PM = json.getBoolean("publishAt5PM");
		if (json.has("publishAt8AM") && !json.isNull("publishAt8AM"))
			this.publishAt8AM = json.getBoolean("publishAt8AM");
		if (json.has("pdfDirectory") && !json.isNull("pdfDirectory"))
			this.pdfDirectory = json.getString("pdfDirectory");
		if (json.has("htmlDirectory") && !json.isNull("htmlDirectory"))
			this.htmlDirectory = json.getString("htmlDirectory");
		if (json.has("mapsPath") && !json.isNull("mapsPath"))
			this.mapsPath = json.getString("mapsPath");
		if (json.has("mapProductionUrl") && !json.isNull("mapProductionUrl"))
			this.mapProductionUrl = json.getString("mapProductionUrl");
		if (json.has("serverImagesUrl") && !json.isNull("serverImagesUrl"))
			this.serverImagesUrl = json.getString("serverImagesUrl");
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	public boolean isExternalServer() {
		return externalServer;
	}

	public void setExternalServer(boolean externalServer) {
		this.externalServer = externalServer;
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

	public boolean isPublishAt5PM() {
		return publishAt5PM;
	}

	public void setPublishAt5PM(boolean publishAt5PM) {
		this.publishAt5PM = publishAt5PM;
	}

	public boolean isPublishAt8AM() {
		return publishAt8AM;
	}

	public void setPublishAt8AM(boolean publishAt8AM) {
		this.publishAt8AM = publishAt8AM;
	}

	public String getPdfDirectory() {
		return pdfDirectory;
	}

	public void setPdfDirectory(String pdfDirectory) {
		this.pdfDirectory = pdfDirectory;
	}

	public String getHtmlDirectory() {
		return htmlDirectory;
	}

	public void setHtmlDirectory(String htmlDirectory) {
		this.htmlDirectory = htmlDirectory;
	}

	public String getMapsPath() {
		return mapsPath;
	}

	public void setMapsPath(String mapsPath) {
		this.mapsPath = mapsPath;
	}

	public String getMapProductionUrl() {
		return mapProductionUrl;
	}

	public void setMapProductionUrl(String mapProductionUrl) {
		this.mapProductionUrl = mapProductionUrl;
	}

	public String getServerImagesUrl() {
		return serverImagesUrl;
	}

	public void setServerImagesUrl(String serverImagesUrl) {
		this.serverImagesUrl = serverImagesUrl;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		json.put("id", getId());
		json.put("name", getName());
		json.put("apiUrl", getApiUrl());
		json.put("userName", getUserName());
		json.put("password", getPassword());
		json.put("externalServer", isExternalServer());
		if (regions != null && regions.size() > 0) {
			JSONArray jsonRegions = new JSONArray();
			for (Region region : regions) {
				jsonRegions.put(region.getId());
			}
			json.put("regions", jsonRegions);
		}
		json.put("publishAt5PM", isPublishAt5PM());
		json.put("publishAt8AM", isPublishAt8AM());
		json.put("pdfDirectory", getPdfDirectory());
		json.put("htmlDirectory", getHtmlDirectory());
		json.put("mapsPath", getMapsPath());
		json.put("mapProductionUrl", getMapProductionUrl());
		json.put("serverImagesUrl", getServerImagesUrl());
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
