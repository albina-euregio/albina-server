// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.albina.controller.RegionRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.security.annotation.Secured;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@Inject
	StatisticsController statisticsController;

	@Inject
	RegionRepository regionRepository;

	@Get
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.TEXT_CSV)
	@Operation(summary = "Get bulletin statistics")
	public StreamedFile getBulletinCsv(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String startDate,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String endDate,
		@QueryValue("lang") LanguageCode language,
		@QueryValue(value = "extended", defaultValue = "false") boolean extended,
		@QueryValue(value = "duplicate", defaultValue = "false") boolean duplicate,
		@QueryValue("regions") List<String> regionIds,
		@QueryValue(value = "obsoleteMatrix", defaultValue = "false") boolean obsoleteMatrix) {

		try {
			Instant start = DateControllerUtil.parseDateOrThrow(startDate);
			Instant end = DateControllerUtil.parseDateOrThrow(endDate);

			List<Region> regions = new ArrayList<>();
			if (regionIds != null && !regionIds.isEmpty()) {
				for (String regionId : regionIds) {
					regions.add(regionRepository.findById(regionId).orElseThrow());
				}
			} else {
				regions = regionRepository.getPublishBulletinRegions();
			}

			String statistics = statisticsController.getDangerRatingStatistics(start, end, language, regions, extended,
				duplicate, obsoleteMatrix);

			String filename = String.format("statistic_%s_%s%s%s%s_%s.csv",
				OffsetDateTime.parse(startDate).toLocalDate(),
				OffsetDateTime.parse(endDate).toLocalDate(),
				duplicate || extended ? "_" : "",
				duplicate ? "d" : "",
				extended ? "e" : "",
				language.toString());

			ByteArrayInputStream inputStream = new ByteArrayInputStream(statistics.getBytes(StandardCharsets.UTF_8));
			return new StreamedFile(inputStream, MediaType.TEXT_CSV_TYPE).attach(filename + ".csv");
		} catch (Exception e) {
			logger.warn("Error creating bulletin statistics", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get("/danger-sources")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.TEXT_CSV)
	@Operation(summary = "Get danger source statistics")
	public StreamedFile getDangerSourceCsv(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String startDate,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String endDate) {

		try {
			Instant start = DateControllerUtil.parseDateOrThrow(startDate);
			Instant end = DateControllerUtil.parseDateOrThrow(endDate);

			String statistics = statisticsController.getDangerSourceStatistics(start, end);

			String filename = String.format("danger_source_statistic_%s_%s.csv",
				OffsetDateTime.parse(startDate).toLocalDate(),
				OffsetDateTime.parse(endDate).toLocalDate());

			ByteArrayInputStream inputStream = new ByteArrayInputStream(statistics.getBytes(StandardCharsets.UTF_8));
			return new StreamedFile(inputStream, MediaType.TEXT_CSV_TYPE).attach(filename);
		} catch (Exception e) {
			logger.warn("Error creating danger source statistics", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/vr")
	@Operation(summary = "Save VR statistics")
	public void saveMediaFile(
		@Header String authorization,
		InputStream inputStream
	) throws IOException {
		String token = System.getenv("ALBINA_VR_STATISTICS_TOKEN");
		if (token == null || token.isEmpty() || !token.equals(authorization)) {
			throw new HttpStatusException(HttpStatus.FORBIDDEN, "");
		}
		String directory = System.getenv("ALBINA_VR_STATISTICS_DIRECTORY");
		Path file = Path.of(directory).resolve(UUID.randomUUID() + ".json");
		try (OutputStream outputStream = Files.newOutputStream(file)) {
			inputStream.transferTo(outputStream);
		}
	}
}
