// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.RegionController;
import eu.albina.controller.StatisticsController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/statistics")
@Tag(name = "statistics")
public class StatisticsService {

	private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

	@Context
	UriInfo uri;

	@Get
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces("text/csv")
	@Operation(summary = "Get bulletin statistics")
	public HttpResponse<?> getBulletinCsv(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String startDate,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String endDate,
		@QueryValue("lang") LanguageCode language, @QueryValue("extended") boolean extended,
		@QueryValue("duplicate") boolean duplicate,
		@QueryValue("regions") List<String> regionIds,
		@QueryValue("obsoleteMatrix") boolean obsoleteMatrix) {
		logger.debug("GET CSV bulletins");

		Instant start = null;
		Instant end = null;

		if (startDate != null)
			start = OffsetDateTime.parse(startDate).toInstant();
		else
			return HttpResponse.badRequest();
		if (endDate != null)
			end = OffsetDateTime.parse(endDate).toInstant();
		else
			return HttpResponse.badRequest();

		List<Region> regions = new ArrayList<Region>();
		if (regionIds != null && !regionIds.isEmpty()) {
			for (String regionId : regionIds) {
				regions.add(RegionController.getInstance().getRegion(regionId));
			}
		} else {
			regions = RegionController.getInstance().getPublishBulletinRegions();
		}

		String statistics = StatisticsController.getInstance().getDangerRatingStatistics(start, end, language, regions, extended,
				duplicate, obsoleteMatrix);

		String filename = String.format("statistic_%s_%s%s%s%s_%s",
			OffsetDateTime.parse(startDate).toLocalDate(),
			OffsetDateTime.parse(endDate).toLocalDate(),
			duplicate || extended ? "_" : "",
			duplicate ? "d" : "",
			extended ? "e" : "",
			language.toString());

		try {
			File tmpFile = File.createTempFile(filename, ".csv");
			FileWriter writer = new FileWriter(tmpFile);
			writer.write(statistics);
			writer.close();

			return HttpResponse.ok(tmpFile).header(HttpHeaders.CONTENT_DISPOSITION,
			"attachment; filename=\"" + filename + ".csv\"").header(HttpHeaders.CONTENT_TYPE, "text/csv");
		} catch (IOException e) {
			logger.warn("Error creating bulletin statistics", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Get("/danger-sources")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces("text/csv")
	@Operation(summary = "Get danger source statistics")
	public HttpResponse<?> getDangerSourceCsv(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String startDate,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String endDate) {
		logger.debug("GET CSV danger sources");

		Instant start = null;
		Instant end = null;

		if (startDate != null)
			start = OffsetDateTime.parse(startDate).toInstant();
		else
			return HttpResponse.badRequest();
		if (endDate != null)
			end = OffsetDateTime.parse(endDate).toInstant();
		else
			return HttpResponse.badRequest();

		String statistics = StatisticsController.getInstance().getDangerSourceStatistics(start, end);

		String filename = String.format("danger_source_statistic_%s_%s",
			OffsetDateTime.parse(startDate).toLocalDate(),
			OffsetDateTime.parse(endDate).toLocalDate());

		try {
			File tmpFile = File.createTempFile(filename, ".csv");
			FileWriter writer = new FileWriter(tmpFile);
			writer.write(statistics);
			writer.close();

			return HttpResponse.ok(tmpFile).header(HttpHeaders.CONTENT_DISPOSITION,
			"attachment; filename=\"" + filename + ".csv\"").header(HttpHeaders.CONTENT_TYPE, "text/csv");
		} catch (IOException e) {
			logger.warn("Error creating danger source statistics", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Post("/vr")
	@Operation(summary = "Save VR statistics")
	public HttpResponse<?> saveMediaFile(
		@Header String authorization,
		InputStream inputStream
	) throws IOException {
		String token = System.getenv("ALBINA_VR_STATISTICS_TOKEN");
		if (token == null || token.isEmpty() || !token.equals(authorization)) {
			return HttpResponse.status(HttpStatus.FORBIDDEN);
		}
		String directory = System.getenv("ALBINA_VR_STATISTICS_DIRECTORY");
		Path file = Path.of(directory).resolve(UUID.randomUUID() + ".json");
		try (OutputStream outputStream = Files.newOutputStream(file)) {
			inputStream.transferTo(outputStream);
		}
		return HttpResponse.noContent();
	}
}
