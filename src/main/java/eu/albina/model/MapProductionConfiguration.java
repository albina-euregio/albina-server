/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.github.openjson.JSONObject;

@Embeddable
public class MapProductionConfiguration implements AvalancheInformationObject {

	@Enumerated(EnumType.STRING)
	@Column(name = "RASTER_FILE_PATH")
	private String rasterFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "COUNTRY_SHAPE_FILE_PATH")
	private String countryShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "PROVINCES_SHAPE_FILE_PATH")
	private String provincesShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "MICRO_REGIONS_SHAPE_FILE_PATH")
	private String microRegionsShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "RIVERS_SHAPE_FILE_PATH")
	private String riversShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "LAKES_SHAPE_FILE_PATH")
	private String lakesShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "CITIES_SHAPE_FILE_PATH")
	private String citiesShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "PEAKS_SHAPE_FILE_PATH")
	private String peaksShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "NAMES_P_SHAPE_FILE_PATH")
	private String namesPShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "NAMES_L_SHAPE_FILE_PATH")
	private String namesLShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "REGION_SHAPE_FILE_PATH")
	private String regionShapeFilePath;

	@Enumerated(EnumType.STRING)
	@Column(name = "PP_SHAPE_FILE_PATH")
	private String ppShapeFilePath;

	public MapProductionConfiguration() {
	}

	public MapProductionConfiguration(JSONObject json) {
		this();

		if (json.has("rasterFilePath"))
			this.rasterFilePath = json.getString("rasterFilePath");
		if (json.has("countryShapeFilePath"))
			this.countryShapeFilePath = json.getString("countryShapeFilePath");
		if (json.has("provincesShapeFilePath"))
			this.provincesShapeFilePath = json.getString("provincesShapeFilePath");
		if (json.has("microRegionsShapeFilePath"))
			this.microRegionsShapeFilePath = json.getString("microRegionsShapeFilePath");
		if (json.has("riversShapeFilePath"))
			this.riversShapeFilePath = json.getString("riversShapeFilePath");
		if (json.has("lakesShapeFilePath"))
			this.lakesShapeFilePath = json.getString("lakesShapeFilePath");
		if (json.has("citiesShapeFilePath"))
			this.citiesShapeFilePath = json.getString("citiesShapeFilePath");
		if (json.has("peaksShapeFilePath"))
			this.peaksShapeFilePath = json.getString("peaksShapeFilePath");
		if (json.has("namesPShapeFilePath"))
			this.namesPShapeFilePath = json.getString("namesPShapeFilePath");
		if (json.has("namesLShapeFilePath"))
			this.namesLShapeFilePath = json.getString("namesLShapeFilePath");
		if (json.has("regionShapeFilePath"))
			this.regionShapeFilePath = json.getString("regionShapeFilePath");
		if (json.has("ppShapeFilePath"))
			this.ppShapeFilePath = json.getString("ppShapeFilePath");
	}

	public String getRasterFilePath() {
		return rasterFilePath;
	}

	public void setRasterFilePath(String rasterFilePath) {
		this.rasterFilePath = rasterFilePath;
	}

	public String getCountryShapeFilePath() {
		return countryShapeFilePath;
	}

	public void setCountryShapeFilePath(String countryShapeFilePath) {
		this.countryShapeFilePath = countryShapeFilePath;
	}

	public String getProvincesShapeFilePath() {
		return provincesShapeFilePath;
	}

	public void setProvincesShapeFilePath(String provincesShapeFilePath) {
		this.provincesShapeFilePath = provincesShapeFilePath;
	}

	public String getMicroRegionsShapeFilePath() {
		return microRegionsShapeFilePath;
	}

	public void setMicroRegionsShapeFilePath(String microRegionsShapeFilePath) {
		this.microRegionsShapeFilePath = microRegionsShapeFilePath;
	}

	public String getRiversShapeFilePath() {
		return riversShapeFilePath;
	}

	public void setRiversShapeFilePath(String riversShapeFilePath) {
		this.riversShapeFilePath = riversShapeFilePath;
	}

	public String getLakesShapeFilePath() {
		return lakesShapeFilePath;
	}

	public void setLakesShapeFilePath(String lakesShapeFilePath) {
		this.lakesShapeFilePath = lakesShapeFilePath;
	}

	public String getCitiesShapeFilePath() {
		return citiesShapeFilePath;
	}

	public void setCitiesShapeFilePath(String citiesShapeFilePath) {
		this.citiesShapeFilePath = citiesShapeFilePath;
	}

	public String getPeaksShapeFilePath() {
		return peaksShapeFilePath;
	}

	public void setPeaksShapeFilePath(String peaksShapeFilePath) {
		this.peaksShapeFilePath = peaksShapeFilePath;
	}

	public String getNamesPShapeFilePath() {
		return namesPShapeFilePath;
	}

	public void setNamesPShapeFilePath(String namesPShapeFilePath) {
		this.namesPShapeFilePath = namesPShapeFilePath;
	}

	public String getNamesLShapeFilePath() {
		return namesLShapeFilePath;
	}

	public void setNamesLShapeFilePath(String namesLShapeFilePath) {
		this.namesLShapeFilePath = namesLShapeFilePath;
	}

	public String getRegionShapeFilePath() {
		return regionShapeFilePath;
	}

	public void setRegionShapeFilePath(String regionShapeFilePath) {
		this.regionShapeFilePath = regionShapeFilePath;
	}

	public String getPpShapeFilePath() {
		return ppShapeFilePath;
	}

	public void setPpShapeFilePath(String ppShapeFilePath) {
		this.ppShapeFilePath = ppShapeFilePath;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("rasterFilePath", this.rasterFilePath);
		json.put("countryShapeFilePath", this.countryShapeFilePath);
		json.put("provincesShapeFilePath", this.provincesShapeFilePath);
		json.put("microRegionsShapeFilePath", this.microRegionsShapeFilePath);
		json.put("riversShapeFilePath", this.riversShapeFilePath);
		json.put("lakesShapeFilePath", this.lakesShapeFilePath);
		json.put("citiesShapeFilePath", this.citiesShapeFilePath);
		json.put("peaksShapeFilePath", this.peaksShapeFilePath);
		json.put("namesPShapeFilePath", this.namesPShapeFilePath);
		json.put("namesLShapeFilePath", this.namesLShapeFilePath);
		json.put("regionShapeFilePath", this.regionShapeFilePath);
		json.put("ppShapeFilePath", this.ppShapeFilePath);

		return json;
	}
}
