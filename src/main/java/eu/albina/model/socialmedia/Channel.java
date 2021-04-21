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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A Channel.
 */
@Entity
@Table(name = "socialmedia_channel")
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
// property = "id",scope = Channel.class)
public class Channel implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false, insertable = false)
	private Long id;

	@Column(name = "NAME", updatable = false, insertable = false)
	private String name;

	@ManyToOne
	@JsonIgnoreProperties("channels")
	@JoinColumn(name = "PROVIDER_ID", updatable = false, insertable = false)
	private Provider provider;

	@JsonIgnoreProperties({ "channels", "regionConfiguration", "region", "twitterConfig",
			"rapidMailConfig", "telegramConfig", "shipments" })
	@ManyToMany(mappedBy = "channels", fetch = FetchType.EAGER)
	private Set<RegionConfiguration> regions = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public Channel name(String name) {
		this.name = name;
		return this;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Provider getProvider() {
		return provider;
	}

	public Channel provider(Provider provider) {
		this.provider = provider;
		return this;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Set<RegionConfiguration> getRegions() {
		return regions;
	}

	public Channel regions(Set<RegionConfiguration> regions) {
		this.regions = regions;
		return this;
	}

	public Channel addRegion(RegionConfiguration region) {
		this.regions.add(region);
		region.getChannels().add(this);
		return this;
	}

	public Channel removeRegion(RegionConfiguration region) {
		this.regions.remove(region);
		region.getChannels().remove(this);
		return this;
	}

	public void setRegions(Set<RegionConfiguration> regions) {
		this.regions = regions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Channel channel = (Channel) o;
		if (channel.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), channel.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "Channel{" + "id=" + getId() + ", name='" + getName() + "'" + "}";
	}
}
