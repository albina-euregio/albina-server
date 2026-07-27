package eu.albina.rest;

import java.net.URI;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import eu.albina.model.WeatherStations;

@Controller("/weather-stations")
@Tag(name = "weather-stations")
public class WeatherStationsService {

	@Value("${albina.conf.weatherStationsUrl}")
	URI weatherStationsUrl;

	@Get
	@Secured(SecurityRule.IS_ANONYMOUS)
	@ApiResponse(responseCode = "307", description = "Redirect to the latest weather stations GeoJSON", content = @Content(schema = @Schema(implementation = WeatherStations.FeatureCollection.class)))
	public HttpResponse<?> getWeatherStations() {
		return HttpResponse.temporaryRedirect(weatherStationsUrl);
	}
}
