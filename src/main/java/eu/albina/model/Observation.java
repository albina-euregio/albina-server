/*******************************************************************************
 * Copyright (C) 2021 albina-euregio
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

import com.google.common.base.MoreObjects;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.EventType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "observations")
public class Observation {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "EVENT_TYPE")
	@Enumerated(EnumType.ORDINAL)
	private EventType eventType;

	@Column(name = "EVENT_DATE")
	private OffsetDateTime eventDate;

	@Column(name = "REPORT_DATE")
	private OffsetDateTime reportDate;

	@Column(name = "AUTHOR_NAME")
	private String authorName;

	@Column(name = "LOCATION_NAME")
	private String locationName;

	@Column(name = "LATITUDE")
	private Double latitude;

	@Column(name = "LONGITUDE")
	private Double longitude;

	@Column(name = "ELEVATION")
	private Double elevation;

	@Enumerated(EnumType.STRING)
	@Column(name = "ASPECT")
	private Aspect aspect;

	@Column(name = "REGION_ID")
	private String region;

	@Column(name = "CONTENT")
	private String content;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public OffsetDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(OffsetDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public OffsetDateTime getReportDate() {
		return reportDate;
	}

	public void setReportDate(OffsetDateTime reportDate) {
		this.reportDate = reportDate;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
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

	public Double getElevation() {
		return elevation;
	}

	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}

	public Aspect getAspect() {
		return aspect;
	}

	public void setAspect(Aspect aspect) {
		this.aspect = aspect;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("eventType", eventType)
			.add("eventDate", eventDate)
			.add("reportDate", reportDate)
			.add("authorName", authorName)
			.add("locationName", locationName)
			.add("latitude", latitude)
			.add("longitude", longitude)
			.add("elevation", elevation)
			.add("aspect", aspect)
			.add("region", region)
			.add("content", content)
			.toString();
	}
}
