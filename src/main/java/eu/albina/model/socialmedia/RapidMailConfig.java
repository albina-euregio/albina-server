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
package eu.albina.model.socialmedia;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A RapidMailConfig.
 */
@Entity
@Table(name = "socialmedia_rapid_mail_config")
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
// property = "id",scope = RapidMailConfig.class)
public class RapidMailConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "USERNAME")
	private String username;

	@Column(name = "PASSWORD")
	private String password;

	@ManyToOne
	@JoinColumn(name = "PROVIDER_ID")
	private Provider provider;

	@JsonIgnoreProperties(value = { "region", "messengerPeopleConfig", "twitterConfig", "rapidMailConfig",
			"telegramConfigs", "shipments", "channels" }, allowSetters = true)
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(unique = true, name = "REGION_CONFIGURATION_ID")
	private RegionConfiguration regionConfiguration;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public RapidMailConfig username(String username) {
		this.username = username;
		return this;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public RapidMailConfig password(String password) {
		this.password = password;
		return this;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public RegionConfiguration getRegionConfiguration() {
		return regionConfiguration;
	}

	public RapidMailConfig regionConfiguration(RegionConfiguration regionConfiguration) {
		this.regionConfiguration = regionConfiguration;
		return this;
	}

	public void setRegionConfiguration(RegionConfiguration regionConfiguration) {
		this.regionConfiguration = regionConfiguration;
	}

	public Provider getProvider() {
		return provider;
	}

	public RapidMailConfig provider(Provider provider) {
		this.provider = provider;
		return this;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RapidMailConfig rapidMailConfig = (RapidMailConfig) o;
		if (rapidMailConfig.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), rapidMailConfig.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "RapidMailConfig{" + "id=" + getId() + ", username='" + getUsername() + "'" + ", password='"
				+ getPassword() + "'" + "}";
	}
}
