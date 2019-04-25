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
import java.time.ZonedDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A Shipment.
 */
@Entity
@Table(name = "socialmedia_shipment")
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
// property = "id",scope = Shipment.class)
public class Shipment implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "DATE")
	private ZonedDateTime date;

	@Column(name = "NAME")
	private String name;

	@Column(name = "LANGUAGE")
	private String language;

	@Column(name = "ID_MP")
	private String idMp;

	@Column(name = "ID_RM")
	private String idRm;

	@Column(name = "ID_TW")
	private String idTw;

	@Lob
	@Column(name = "REQUEST")
	private String request;

	@Lob
	@Column(name = "RESPONSE")
	private String response;

	@ManyToOne
	@JsonIgnoreProperties({ "shipments", "messengerPeopleConfig", "twitterConfig", "rapidMailConfig", "channels" })
	@JoinColumn(name = "REGION_ID")
	private RegionConfiguration region;

	@ManyToOne
	@JsonIgnoreProperties("shipments")
	@JoinColumn(name = "PROVIDER_ID")
	private Provider provider;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public Shipment date(ZonedDateTime date) {
		this.date = date;
		return this;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public Shipment name(String name) {
		this.name = name;
		return this;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLanguage() {
		return language;
	}

	public Shipment language(String language) {
		this.language = language;
		return this;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getIdMp() {
		return idMp;
	}

	public Shipment idMp(String idMp) {
		this.idMp = idMp;
		return this;
	}

	public void setIdMp(String idMp) {
		this.idMp = idMp;
	}

	public String getIdRm() {
		return idRm;
	}

	public Shipment idRm(String idRm) {
		this.idRm = idRm;
		return this;
	}

	public void setIdRm(String idRm) {
		this.idRm = idRm;
	}

	public String getIdTw() {
		return idTw;
	}

	public Shipment idTw(String idTw) {
		this.idTw = idTw;
		return this;
	}

	public void setIdTw(String idTw) {
		this.idTw = idTw;
	}

	public String getRequest() {
		return request;
	}

	public Shipment request(String request) {
		this.request = request;
		return this;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponse() {
		return response;
	}

	public Shipment response(String response) {
		this.response = response;
		return this;
	}

	public RegionConfiguration getRegion() {
		return region;
	}

	public Shipment region(RegionConfiguration region) {
		this.region = region;
		return this;
	}

	public void setRegion(RegionConfiguration region) {
		this.region = region;
	}

	public Provider getProvider() {
		return provider;
	}

	public Shipment provider(Provider provider) {
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
		Shipment shipment = (Shipment) o;
		if (shipment.getId() == null || getId() == null) {
			return false;
		}
		return Objects.equals(getId(), shipment.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public String toString() {
		return "Shipment{" + "id=" + getId() + ", date='" + getDate() + "'" + ", name='" + getName() + "'"
				+ ", language='" + getLanguage() + "'" + ", idMp='" + getIdMp() + "'" + ", idRm='" + getIdRm() + "'"
				+ ", idTw='" + getIdTw() + "'" + ", request='" + getRequest() + "'" + ", response='" + getResponse()
				+ "'" + "}";
	}
}
