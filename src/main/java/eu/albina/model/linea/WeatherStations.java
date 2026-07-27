package eu.albina.model.linea;

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
		Double DW,
		Double HS,
		Double HSD_24,
		Double HSD_48,
		Double HSD_6,
		Double HSD_72,
		Double ILWR,
		Double ISWR,
		@Schema(description = "EAWS micro region ID, see https://gitlab.com/eaws/eaws-regions")
		String microRegionID,
		@Schema(description = "Station name")
		String name,
		Double OLWR,
		@Schema(description = "Station operator")
		String operator,
		@Schema(description = "License under which data is provided")
		String operatorLicense,
		@Schema(description = "Link to license", format = "uri")
		String operatorLicenseLink,
		@Schema(description = "Link to website of station operator", format = "uri")
		String operatorLink,
		Double P,
		@Schema(description = "For legacy PNG plots: name of plot which includes this station")
		String plot,
		Double PSUM_24,
		Double PSUM_48,
		Double PSUM_6,
		Double PSUM_72,
		Double RH,
		Double RSWR,
		@Schema(description = "Station short name (such as ISEE2) consisting of [A-Za-z0-9] only", pattern = "^[A-Za-z0-9]+$")
		String shortName,
		@Schema(description = "Observation start year")
		String startYear,
		@Schema(description = "A few sentences describing the station characteristics/locality/history/...")
		String stationCharacteristics,
		Map<String, Statistics> statistics,
		Double TA,
		Double TA_MAX,
		Double TA_MIN,
		Double TD,
		Double TSS,
		Double VW,
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
