/*******************************************************************************
 * Copyright (C) 2024 albina
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.google.common.base.MoreObjects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "generic_observations")
public class GenericObservation {
	@Id
	@Column(name = "SOURCE", nullable = false, length = 191)
	private String source;

	@Id
	@Column(name = "ID", nullable = false, length = 191)
	private String id;

	@Column(name = "OBS_TYPE", nullable = false)
	private String obsType;

	@Column(name = "EXTERNAL_URL")
	private String externalUrl;

	@Column(name = "STABILITY")
	private String stability;

	@Column(name = "ASPECTS")
	private String aspects;

	@Column(name = "AUTHOR_NAME", length = 191)
	private String authorName;

	@Column(name = "OBS_CONTENT")
	private String obsContent;

	@Column(name = "OBS_DATA")
	private String obsData;

	@Column(name = "ELEVATION")
	private Double elevation;

	@Column(name = "ELEVATION_LOWER_BOUND")
	private Double elevationLowerBound;

	@Column(name = "ELEVATION_UPPER_BOUND")
	private Double elevationUpperBound;

	@Column(name = "EVENT_DATE")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime eventDate;

	@Column(name = "LATITUDE")
	private Double latitude;

	@Column(name = "LOCATION_NAME", length = 191)
	private String locationName;

	@Column(name = "LONGITUDE")
	private Double longitude;

	@Column(name = "REGION_ID", length = 191)
	private String regionId;

	@Column(name = "REPORT_DATE")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime reportDate;

	@Column(name = "AVALANCHE_PROBLEMS")
	private String avalancheProblems;

	@Column(name = "DANGER_PATTERNS")
	private String dangerPatterns;

	@Column(name = "IMPORTANT_OBSERVATION")
	private String importantObservation;

	@Column(name = "EXTRA_DIALOG_ROWS")
	private String extraDialogRows;

	@Column(name = "EXTERNAL_IMG")
	private String externalImg;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getObsType() {
		return obsType;
	}

	public void setObsType(String obsType) {
		this.obsType = obsType;
	}

	public String getExternalUrl() {
		return externalUrl;
	}

	public void setExternalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
	}

	public String getStability() {
		return stability;
	}

	public void setStability(String stability) {
		this.stability = stability;
	}

	public String getAspects() {
		return aspects;
	}

	public void setAspects(String aspects) {
		this.aspects = aspects;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getObsContent() {
		return obsContent;
	}

	public void setObsContent(String obsContent) {
		this.obsContent = obsContent;
	}

	public String getObsData() {
		return obsData;
	}

	public void setObsData(String obsData) {
		this.obsData = obsData;
	}

	public Double getElevation() {
		return elevation;
	}

	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}

	public Double getElevationLowerBound() {
		return elevationLowerBound;
	}

	public void setElevationLowerBound(Double elevationLowerBound) {
		this.elevationLowerBound = elevationLowerBound;
	}

	public Double getElevationUpperBound() {
		return elevationUpperBound;
	}

	public void setElevationUpperBound(Double elevationUpperBound) {
		this.elevationUpperBound = elevationUpperBound;
	}

	public LocalDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public LocalDateTime getReportDate() {
		return reportDate;
	}

	public void setReportDate(LocalDateTime reportDate) {
		this.reportDate = reportDate;
	}

	public String getAvalancheProblems() {
		return avalancheProblems;
	}

	public void setAvalancheProblems(String avalancheProblems) {
		this.avalancheProblems = avalancheProblems;
	}

	public String getDangerPatterns() {
		return dangerPatterns;
	}

	public void setDangerPatterns(String dangerPatterns) {
		this.dangerPatterns = dangerPatterns;
	}

	public String getImportantObservation() {
		return importantObservation;
	}

	public void setImportantObservation(String importantObservation) {
		this.importantObservation = importantObservation;
	}

	public String getExtraDialogRows() {
		return extraDialogRows;
	}

	public void setExtraDialogRows(String extraDialogRows) {
		this.extraDialogRows = extraDialogRows;
	}

	public String getExternalImg() {
		return externalImg;
	}

	public void setExternalImg(String externalImg) {
		this.externalImg = externalImg;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericObservation that = (GenericObservation) o;
		return Objects.equals(source, that.source) && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, id);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("source", source)
			.add("id", id)
			.add("obsType", obsType)
			.add("externalUrl", externalUrl)
			.add("stability", stability)
			.add("aspects", aspects)
			.add("authorName", authorName)
			.add("obsContent", obsContent)
			.add("obsData", obsData)
			.add("elevation", elevation)
			.add("elevationLowerBound", elevationLowerBound)
			.add("elevationUpperBound", elevationUpperBound)
			.add("eventDate", eventDate)
			.add("latitude", latitude)
			.add("locationName", locationName)
			.add("longitude", longitude)
			.add("regionId", regionId)
			.add("reportDate", reportDate)
			.add("avalancheProblems", avalancheProblems)
			.add("dangerPatterns", dangerPatterns)
			.add("importantObservation", importantObservation)
			.add("extraDialogRows", extraDialogRows)
			.add("externalImg", externalImg)
			.toString();
	}
}
