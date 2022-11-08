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

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;

@Path("/bulletins/status")
@Tag(name = "bulletins/status")
public class AvalancheBulletinStatusService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinStatusService.class);

	static class Status {
		public String date;
		public BulletinStatus status;
		public String report;
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "status", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Status.class))))
	public Response getStatus(@QueryParam("region") String regionId,
			@QueryParam("timezone") String timezone,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("startDate") String start,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("endDate") String end) {

		Instant startDate = DateControllerUtil.parseDateOrToday(start);
		Instant endDate = DateControllerUtil.parseDateOrToday(end);
		ZoneId zoneId = DateControllerUtil.parseTimezoneOrLocal(timezone);

		try {
			Map<Instant, BulletinStatus> status;
			if (regionId == null || regionId.isEmpty()) {
				status = AvalancheReportController.getInstance().getStatus(startDate, endDate,
						RegionController.getInstance().getPublishBulletinRegions());
			} else {
				status = AvalancheReportController.getInstance().getStatus(startDate, endDate, RegionController.getInstance().getRegion(regionId));
			}

			JSONArray jsonResult = new JSONArray();

			for (Entry<Instant, BulletinStatus> entry : status.entrySet()) {
				JSONObject json = new JSONObject();
				final ZonedDateTime dateTime = entry.getKey().atZone(zoneId);
				final String iso8601 = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
				json.put("date", iso8601);
				json.put("status", entry.getValue().toString());
				jsonResult.put(json);
			}

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading status", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/internal")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "status", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Status.class))))
	public Response getInternalStatus(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("startDate") String start,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("endDate") String end) {

		try {
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			Instant startDate = DateControllerUtil.parseDateOrToday(start);
			Instant endDate = DateControllerUtil.parseDateOrNull(end);

			Map<Instant, BulletinStatus> status = AvalancheReportController.getInstance().getInternalStatus(startDate,
					endDate, region);
			JSONArray jsonResult = new JSONArray();

			for (Entry<Instant, BulletinStatus> entry : status.entrySet()) {
				JSONObject json = new JSONObject();
				json.put("date", DateTimeFormatter.ISO_INSTANT.format(entry.getKey()));
				json.put("status", entry.getValue().toString());
				jsonResult.put(json);
			}

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading status", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		} catch (HibernateException e) {
			logger.warn("Error loading status for " + regionId);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/publications")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "status", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Status.class))))
	public Response getPublicationsStatus(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("startDate") String start,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("endDate") String end) {

		Instant startDate = DateControllerUtil.parseDateOrToday(start);
		Instant endDate = DateControllerUtil.parseDateOrNull(end);

		if (regionId == null || regionId.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		Map<Instant, AvalancheReport> status = AvalancheReportController.getInstance().getPublicationStatus(startDate,
				endDate, RegionController.getInstance().getRegion(regionId));
		JSONArray jsonResult = new JSONArray();

		for (Entry<Instant, AvalancheReport> entry : status.entrySet()) {
			JSONObject json = new JSONObject();
			json.put("date", DateTimeFormatter.ISO_INSTANT.format(entry.getKey()));
			json.put("report", entry.getValue().toJSON());
			jsonResult.put(json);
		}

		return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/publication")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "status", content = @Content(schema = @Schema(implementation = Status.class)))
	public Response getPublicationStatus(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date) {

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		Instant endDate = startDate;

		try {
			Map<Instant, AvalancheReport> status = AvalancheReportController.getInstance()
					.getPublicationStatus(startDate, endDate, RegionController.getInstance().getRegionOrThrowAlbinaException(regionId));

			if (status.size() > 1)
				logger.warn("More than one report found!");
			else if (status.isEmpty())
				throw new AlbinaException("No publication found!");

			Entry<Instant, AvalancheReport> entry = status.entrySet().iterator().next();
			JSONObject json = new JSONObject();
			json.put("date", DateTimeFormatter.ISO_INSTANT.format(entry.getKey()));
			json.put("report", entry.getValue().toJSON());

			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading status", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}
}
