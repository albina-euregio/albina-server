// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.io.Serializable;
import java.util.Objects;

import io.micronaut.serde.annotation.Serdeable;
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
@Serdeable
public class ServerInstance implements Serializable {

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
