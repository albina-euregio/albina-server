// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * This class holds all information about a server instance.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "server_instances")
public class ServerInstance implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "NAME", length = 191)
	private String name;

	@Column(name = "API_URL", length = 191)
	private String apiUrl;

	@Column(name = "USER_NAME", length = 191)
	private String userName;

	@Column(name = "PASSWORD", length = 191)
	private String password;

	@Column(name = "EXTERNAL_SERVER")
	private boolean externalServer;

	@Column(name = "PUBLISH_AT_5PM")
	private boolean publishAt5PM;

	@Column(name = "PUBLISH_AT_8PM")
	private boolean publishAt8AM;

	@Column(name = "PDF_DIRECTORY", length = 191)
	private String pdfDirectory;

	@Column(name = "HTML_DIRECTORY",  length = 191)
	private String htmlDirectory;

	@Column(name = "MAPS_PATH",  length = 191)
	private String mapsPath;

	@Column(name = "MEDIA_PATH",  length = 191)
	private String mediaPath;

	@Column(name = "MAP_PRODUCTION_URL", length = 191)
	private String mapProductionUrl;

	@Column(name = "SERVER_IMAGES_URL", length = 191)
	private String serverImagesUrl;

	@Column(name = "DANGER_LEVEL_ELEVATION_DEPENDENCY")
	private boolean dangerLevelElevationDependency;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public ServerInstance() {
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

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
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

	public boolean isDangerLevelElevationDependency() {
		return dangerLevelElevationDependency;
	}

	public void setDangerLevelElevationDependency(boolean dangerLevelElevationDependency) {
		this.dangerLevelElevationDependency = dangerLevelElevationDependency;
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
