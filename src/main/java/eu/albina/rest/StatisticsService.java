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
package eu.albina.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.RegionController;
import eu.albina.controller.StatisticsController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/statistics")
@Tag(name = "statistics")
public class StatisticsService {

	private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces("text/csv")
	@Operation(summary = "Get bulletin statistics")
	public Response getBulletinCsv(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("startDate") String startDate,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("endDate") String endDate,
			@QueryParam("lang") LanguageCode language, @QueryParam("extended") boolean extended,
			@QueryParam("duplicate") boolean duplicate,
			@QueryParam("regions") List<String> regionIds,
			@QueryParam("obsoleteMatrix") boolean obsoleteMatrix) {
		logger.debug("GET CSV bulletins");

		Instant start = null;
		Instant end = null;

		if (startDate != null)
			start = OffsetDateTime.parse(startDate).toInstant();
		else
			return Response.notAcceptable(null).build();
		if (endDate != null)
			end = OffsetDateTime.parse(endDate).toInstant();
		else
			return Response.notAcceptable(null).build();

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

			return Response.ok(tmpFile).header(HttpHeaders.CONTENT_DISPOSITION,
			"attachment; filename=\"" + filename + ".csv\"").header(HttpHeaders.CONTENT_TYPE, "text/csv").build();
		} catch (IOException e) {
			logger.warn("Error creating bulletin statistics", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@GET
	@Path("/danger-sources")
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces("text/csv")
	@Operation(summary = "Get danger source statistics")
	public Response getDangerSourceCsv(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("startDate") String startDate,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("endDate") String endDate) {
		logger.debug("GET CSV danger sources");

		Instant start = null;
		Instant end = null;

		if (startDate != null)
			start = OffsetDateTime.parse(startDate).toInstant();
		else
			return Response.notAcceptable(null).build();
		if (endDate != null)
			end = OffsetDateTime.parse(endDate).toInstant();
		else
			return Response.notAcceptable(null).build();

		String statistics = StatisticsController.getInstance().getDangerSourceStatistics(start, end);

		String filename = String.format("danger_source_statistic_%s_%s",
			OffsetDateTime.parse(startDate).toLocalDate(),
			OffsetDateTime.parse(endDate).toLocalDate());

		try {
			File tmpFile = File.createTempFile(filename, ".csv");
			FileWriter writer = new FileWriter(tmpFile);
			writer.write(statistics);
			writer.close();

			return Response.ok(tmpFile).header(HttpHeaders.CONTENT_DISPOSITION,
			"attachment; filename=\"" + filename + ".csv\"").header(HttpHeaders.CONTENT_TYPE, "text/csv").build();
		} catch (IOException e) {
			logger.warn("Error creating danger source statistics", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@POST
	@Path("/vr")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Save VR statistics")
	public Response saveMediaFile(
		@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
		InputStream inputStream
	) throws IOException {
		String token = System.getenv("ALBINA_VR_STATISTICS_TOKEN");
		if (token == null || token.isEmpty() || !token.equals(authorization)) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		String directory = System.getenv("ALBINA_VR_STATISTICS_DIRECTORY");
		java.nio.file.Path file = java.nio.file.Path.of(directory).resolve(UUID.randomUUID() + ".json");
		try (OutputStream outputStream = Files.newOutputStream(file)) {
			inputStream.transferTo(outputStream);
		}
		return Response.status(Response.Status.CREATED).build();
	}
}
