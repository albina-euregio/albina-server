// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.exception.AlbinaException;
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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Path("/bulletins/status")
@Tag(name = "bulletins/status")
public class AvalancheBulletinStatusService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinStatusService.class);

	static class Status {
		public final Instant date;
		public final BulletinStatus status;
		public final AvalancheReport report;

		public Status(Instant date, BulletinStatus status, AvalancheReport report) {
			this.date = date;
			this.status = status;
			this.report = report;
		}
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

			List<Status> result = status.entrySet().stream()
				.map(entry -> new Status(entry.getKey(), entry.getValue(), null))
				.collect(Collectors.toList());

			return Response.ok(result, MediaType.APPLICATION_JSON).build();
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

			List<Status> result = AvalancheReportController.getInstance()
				.getInternalStatus(startDate, endDate, region).entrySet().stream()
				.map(entry -> new Status(entry.getKey(), entry.getValue(), null))
				.collect(Collectors.toList());

			return Response.ok(result, MediaType.APPLICATION_JSON).build();
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

		Region region = RegionController.getInstance().getRegion(regionId);
		List<Status> result = AvalancheReportController.getInstance()
			.getPublicationStatus(startDate, endDate, region)
			.entrySet().stream()
			.map(entry -> new Status(entry.getKey(), null, entry.getValue()))
			.collect(Collectors.toList());

		return Response.ok(result, MediaType.APPLICATION_JSON).build();
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
			Status result = new Status(entry.getKey(), null, entry.getValue());
			return Response.ok(result, MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading status", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}
}
