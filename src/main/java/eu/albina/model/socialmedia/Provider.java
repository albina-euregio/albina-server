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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A Provider.
 */
@Entity
@Table(name = "socialmedia_provider")
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
// property = "id",scope = Provider.class)
public class Provider implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", updatable = false, insertable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "NAME", updatable = false, insertable = false)
	private String name;

	@JsonIgnore
	@OneToMany(mappedBy = "provider", fetch = FetchType.EAGER)
	private Set<Channel> channels = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "provider", fetch = FetchType.EAGER)
	private Set<RapidMailConfig> rapidMailConfigs = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public Provider name(String name) {
		this.name = name;
		return this;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Channel> getChannels() {
		return channels;
	}

	public Provider channels(Set<Channel> channels) {
		this.channels = channels;
		return this;
	}

	public Provider addChannel(Channel channel) {
		this.channels.add(channel);
		channel.setProvider(this);
		return this;
	}

	public Provider removeChannel(Channel channel) {
		this.channels.remove(channel);
		channel.setProvider(null);
		return this;
	}

	public void setChannels(Set<Channel> channels) {
		this.channels = channels;
	}

	public Set<RapidMailConfig> getRapidMailConfigs() {
		return rapidMailConfigs;
	}

	public Provider rapidMailConfigs(Set<RapidMailConfig> rapidMailConfigs) {
		this.rapidMailConfigs = rapidMailConfigs;
		return this;
	}

	public Provider addRapidMailConfig(RapidMailConfig rapidMailConfig) {
		this.rapidMailConfigs.add(rapidMailConfig);
		rapidMailConfig.setProvider(this);
		return this;
	}

	public Provider removeRapidMailConfig(RapidMailConfig rapidMailConfig) {
		this.rapidMailConfigs.remove(rapidMailConfig);
		rapidMailConfig.setProvider(null);
		return this;
	}

	public void setRapidMailConfigs(Set<RapidMailConfig> rapidMailConfigs) {
		this.rapidMailConfigs = rapidMailConfigs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Provider provider = (Provider) o;
		if (provider.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), provider.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "Provider{" + "id=" + getId() + ", name='" + getName() + "'" + "}";
	}
}
