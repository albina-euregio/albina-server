// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "avalanche_bulletin_photos")
@Serdeable
public class AvalancheBulletinPhoto extends AbstractPersistentObject {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AVALANCHE_BULLETIN_ID")
	@JsonIgnore
	private AvalancheBulletin avalancheBulletin;

	@Column(name = "URL", length = 191)
	private String url;

	@Column(name = "COPYRIGHT", length = 191)
	private String copyright;

	@Column(name = "DATE")
	private LocalDate date;

	@Column(name = "MICROREGION_ID", length = 191)
	private String microRegionId;

	@Column(name = "LOCATION_NAME", length = 191)
	private String locationName;

	@Column(name = "LATITUDE")
	private Double latitude;

	@Column(name = "LONGITUDE")
	private Double longitude;

	public AvalancheBulletinPhoto() {
	}

	public AvalancheBulletinPhoto(AvalancheBulletinPhoto source) {
		setUrl(source.getUrl());
		setCopyright(source.getCopyright());
		setDate(source.getDate());
		setMicroRegionId(source.getMicroRegionId());
		setLocationName(source.getLocationName());
		setLatitude(source.getLatitude());
		setLongitude(source.getLongitude());
	}

	public AvalancheBulletin getAvalancheBulletin() {
		return avalancheBulletin;
	}

	public void setAvalancheBulletin(AvalancheBulletin avalancheBulletin) {
		this.avalancheBulletin = avalancheBulletin;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getMicroRegionId() {
		return microRegionId;
	}

	public void setMicroRegionId(String microRegionId) {
		this.microRegionId = microRegionId;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

}
