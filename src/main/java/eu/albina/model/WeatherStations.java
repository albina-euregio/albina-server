package eu.albina.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

public interface WeatherStations {
	/**
	 * A GeoJSON FeatureCollection of weather stations
	 */
	record FeatureCollection(
		List<Feature> features,
		Object properties,
		Type type
	) {
		enum Type {
			FeatureCollection
		}
	}

	/**
	 * A GeoJSON Feature corresponding to one weather station
	 */
	record Feature(
		Geometry geometry,
		@Schema(description = "The ID/UUID of the station")
		String id,
		Properties properties,
		Type type
	) {
		enum Type {
			Feature
		}
	}

	record Geometry(
		@Schema(description = "Longitude, latitude and (optionally) altitude")
		List<Double> coordinates,
		Type type
	) {
		enum Type {
			Point
		}
	}

	/**
	 * The properties of a weather station including measured values
	 */
	record Properties(
		@Schema(description = "Altitude above sea level (alternatively specify 3rd component in coordinates)")
		Double altitude,
		String dataProviderID,
		@Schema(description = "Data URLs for this station (typically SMET format is used, and three URLs are provided, short term, winter season, all winter seasons)")
		List<String> dataURLs,
		@Schema(description = "ISO 8601 timestamp")
		Instant date,
		@Schema(description = "Wind direction (optionally average over the last 3h) in °")
		Double DW,
		@Schema(description = "Snow height in m")
		Double HS,
		@Schema(description = "Difference in snow height over the last 24h in m")
		Double HSD_24,
		@Schema(description = "Difference in snow height over the last 48h in m")
		Double HSD_48,
		@Schema(description = "Difference in snow height over the last 6h in m")
		Double HSD_6,
		@Schema(description = "Difference in snow height over the last 72h in m")
		Double HSD_72,
		@Schema(description = "Incoming Long Wave Radiation in W/m²")
		Double ILWR,
		@Schema(description = "Incoming Short Wave Radiation in W/m²")
		Double ISWR,
		@Schema(description = "EAWS micro region ID, see https://gitlab.com/eaws/eaws-regions")
		String microRegionID,
		@Schema(description = "Station name")
		String name,
		@Schema(description = "Outgoing Long Wave Radiation in W/m²")
		Double OLWR,
		@Schema(description = "Station operator")
		String operator,
		@Schema(description = "License under which data is provided")
		String operatorLicense,
		@Schema(description = "Link to license", format = "uri")
		String operatorLicenseLink,
		@Schema(description = "Link to website of station operator", format = "uri")
		String operatorLink,
		@Schema(description = "Air pressure in Pa")
		Double P,
		@Schema(description = "For legacy PNG plots: name of plot which includes this station")
		String plot,
		@Schema(description = "Precipitation summed over the last 24h in mm")
		Double PSUM_24,
		@Schema(description = "Precipitation summed over the last 48h in mm")
		Double PSUM_48,
		@Schema(description = "Precipitation summed over the last 6h in mm")
		Double PSUM_6,
		@Schema(description = "Precipitation summed over the last 72h in mm")
		Double PSUM_72,
		@Schema(description = "Relative humidity between 0 and 1")
		Double RH,
		@Schema(description = "Reflected Short Wave Radiation in W/m²")
		Double RSWR,
		@Schema(description = "Station short name (such as ISEE2) consisting of [A-Za-z0-9] only", pattern = "^[A-Za-z0-9]+$")
		String shortName,
		@Schema(description = "Observation start year")
		String startYear,
		@Schema(description = "A few sentences describing the station characteristics/locality/history/...")
		String stationCharacteristics,
		Map<String, Statistics> statistics,
		@Schema(description = "Air temperature in Kelvin")
		Double TA,
		@Schema(description = "Max. air temperature over the last 24h in Kelvin")
		Double TA_MAX,
		@Schema(description = "Min. air temperature over the last 24h in Kelvin")
		Double TA_MIN,
		@Schema(description = "Dew point temperature in Kelvin")
		Double TD,
		@Schema(description = "Temperature Snow Surface in Kelvin")
		Double TSS,
		@Schema(description = "Wind velocity (optionally as average over the last 3h) in m/s")
		Double VW,
		@Schema(description = "Max. wind velocity (optionally max over the last 3h) in m/s")
		Double VW_MAX
	) {
	}

	record Statistics(
		Double average,
		Double count,
		Double delta,
		Double max,
		Double median,
		Double min,
		Double sum,
		@Schema(description = "Unit of the measured values", allowableValues = {
			"K", "℃", "m", "cm", "mm", "1", "%", "°", "m/s", "km/h", "hPa", "Pa", "W/m²"
		})
		String unit
	) {
	}

}
