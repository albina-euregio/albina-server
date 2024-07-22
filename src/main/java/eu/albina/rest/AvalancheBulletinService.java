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
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import eu.albina.controller.ServerInstanceController;
import eu.albina.model.ServerInstance;
import eu.albina.caaml.Caaml;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.base.MoreObjects;

import eu.albina.caaml.CaamlVersion;
import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.jobs.ChangeJob;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.BulletinLock;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.GlobalVariables;
import eu.albina.map.MapUtil;
import eu.albina.util.PdfUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@Path("/bulletins")
@Tag(name = "bulletins")
@OpenAPIDefinition(info = @Info(
	title = "albina-server",
	version = "0.0",
	description = "Server component to compose and publish multilingual avalanche bulletins",
	license = @License(name = "GNU General Public License v3.0", url = "https://gitlab.com/albina-euregio/albina-server/-/blob/master/LICENSE"),
	contact = @Contact(name = "avalanche.report", url = "https://avalanche.report/", email = "info@avalanche.report")
), servers = {@Server(url = "/albina/api")})
public class AvalancheBulletinService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/edit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))))
	@Operation(summary = "Get bulletins for date")
	public Response getJSONBulletins(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("regions") List<String> regionIds) {
		logger.debug("GET JSON bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

		if (regionIds.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		List<Region> regions = new ArrayList<Region>();
		regionIds.forEach(regionId -> {
			try {
				Region region = RegionController.getInstance().getRegion(regionId);
				regions.add(region);
			} catch (HibernateException e) {
				logger.warn("No region with ID: " + regionId);
			}
		});

		List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance().getBulletins(startDate, endDate,
				regions);
		JSONArray jsonResult = new JSONArray();
		if (bulletins != null) {
			Collections.sort(bulletins);
			for (AvalancheBulletin bulletin : bulletins) {
				jsonResult.put(bulletin.toJSON());
			}
		}
		return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Operation(deprecated = true)
	public Response getPublishedXMLBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("region") String regionId,
		@QueryParam("lang") LanguageCode language,
		@QueryParam("version") CaamlVersion version) {
		List<String> regionIds = regionId != null ? Collections.singletonList(regionId) : Collections.emptyList();
		return getPublishedCaamlBulletins(date, regionIds, language, version);
	}

	@GET
	@Path("/caaml")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Operation(summary = "Get published bulletins for date as CAAML XML")
	public Response getPublishedCaamlBulletins(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("regions") List<String> regionIds,
			@QueryParam("lang") LanguageCode language,
			@QueryParam("version") CaamlVersion version) {
		logger.debug("GET published XML bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		List<Region> regions = RegionController.getInstance().getRegionsOrBulletinRegions(regionIds);

		try {
			AvalancheReport avalancheReport = AvalancheReport.of(
				AvalancheReportController.getInstance().getPublishedBulletins(startDate, regions), null,
				ServerInstanceController.getInstance().getLocalServerInstance());
			String caaml = Caaml.createCaaml(avalancheReport, MoreObjects.firstNonNull(language, LanguageCode.en), MoreObjects.firstNonNull(version, CaamlVersion.V5));
			if (caaml != null) {
				final String type = version != CaamlVersion.V6_JSON ? MediaType.APPLICATION_XML : MediaType.APPLICATION_JSON;
				return Response.ok(caaml, type).build();
			} else {
				logger.debug("No bulletins with status published.");
				return Response.noContent().build();
			}
		} catch (RuntimeException e) {
			logger.warn("Error loading bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_XML).build();
		}
	}
	@GET
	@Path("/caaml/json")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get published bulletins for date as CAAML JSON")
	public Response getPublishedCaamlJsonBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("regions") List<String> regionIds,
		@QueryParam("lang") LanguageCode language,
		@QueryParam("version") CaamlVersion version) {
		return getPublishedCaamlBulletins(date, regionIds, language, MoreObjects.firstNonNull(version, CaamlVersion.V6_JSON));
	}

	static class LatestBulletin {
		public Instant date;
	}

	@GET
	@Path("/latest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "latest", content = @Content(schema = @Schema(implementation = LatestBulletin.class)))
	@Operation(summary = "Get latest bulletin date")
	public Response getLatest() {
		logger.debug("GET latest date");
		try {
			LatestBulletin json = new LatestBulletin();
			json.date = AvalancheReportController.getInstance().getLatestDate();

			return Response.ok(json, MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading latest date", e);
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toString()).build();
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))))
	@Operation(deprecated = true)
	public Response getPublishedJSONBulletins0(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("regions") List<String> regionIds, @QueryParam("lang") LanguageCode language) {
		return getPublishedJSONBulletins(date, regionIds, language);
	}

	@GET
	@Path("/json")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))))
	@Operation(summary = "Get published bulletins for date")
	public Response getPublishedJSONBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("regions") List<String> regionIds, @QueryParam("lang") LanguageCode language) {
		logger.debug("GET published JSON bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		List<Region> regions = RegionController.getInstance().getRegionsOrBulletinRegions(regionIds);

		JSONArray jsonResult = new JSONArray();
		for (AvalancheBulletin bulletin : AvalancheReportController.getInstance().getPublishedBulletins(startDate, regions))
			jsonResult.put(bulletin.toSmallJSON());
		return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
	}

	static class Highest {
		public DangerRating dangerRating;
	}

	@GET
	@Path("/highest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "latest", content = @Content(schema = @Schema(implementation = Highest.class)))
	@Operation(summary = "Get highest danger rating")
	public Response getHighestDangerRating(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("regions") List<String> regionIds) {
		logger.debug("GET highest danger rating");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);

		if (regionIds.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		List<Region> regions = new ArrayList<Region>();
		regionIds.forEach(regionId -> regions.add(RegionController.getInstance().getRegion(regionId)));

		try {
			DangerRating highestDangerRating = AvalancheBulletinController.getInstance()
					.getHighestDangerRating(startDate, regions);

			Highest jsonResult = new Highest();
			jsonResult.dangerRating = highestDangerRating;
			return Response.ok(jsonResult, MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading highest danger rating", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
    @Path("/preview")
    @Produces("application/pdf")
	@Operation(summary = "Get bulletin preview as PDF")
    public Response getPreviewPdf(@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date, @QueryParam("region") String regionId, @QueryParam("lang") LanguageCode language) {

		logger.debug("GET PDF preview [{}, {}]", date, regionId);

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			AvalancheReport report = AvalancheReportController.getInstance().getInternalReport(startDate, region);
			List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
			if (report != null	&& report.getJsonString() != null) {
				JSONArray jsonArray = new JSONArray(report.getJsonString());
				for (Object object : jsonArray) {
					if (object instanceof JSONObject) {
						AvalancheBulletin bulletin = new AvalancheBulletin((JSONObject) object, UserController.getInstance()::getUser);
						if (bulletin.affectsRegionWithoutSuggestions(region)) {
							bulletins.add(bulletin);
						}
					}
				}
				Collections.sort(bulletins);

				ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
				serverInstance.setMapsPath(GlobalVariables.getTmpPdfDirectory());
				serverInstance.setPdfDirectory(GlobalVariables.getTmpPdfDirectory());
				AvalancheReport avalancheReport = AvalancheReport.of(bulletins, region, serverInstance);
				avalancheReport.setStatus(BulletinStatus.draft); // preview

				MapUtil.createMapyrusMaps(avalancheReport);

				final java.nio.file.Path pdf = new PdfUtil(avalancheReport, language, false).createPdf();
				File file = pdf.toFile();

				return Response.ok(file).header(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + pdf.getFileName() + "\"").header(HttpHeaders.CONTENT_TYPE, "application/pdf").build();
			} else {
				return Response.noContent().build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error creating PDFs", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		} catch (Exception e) {
			logger.warn("Error creating PDFs", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{bulletinId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletin", content = @Content(schema = @Schema(implementation = AvalancheBulletin.class)))
	@Operation(summary = "Get bulletin by ID")
	public Response getJSONBulletin(@PathParam("bulletinId") String bulletinId) {
		logger.debug("GET JSON bulletin: {}", bulletinId);

		try {
			AvalancheBulletin bulletin = AvalancheBulletinController.getInstance().getBulletin(bulletinId);
			if (bulletin == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("message", "Bulletin not found for ID: " + bulletinId);
				return Response.status(Response.Status.NOT_FOUND).entity(jsonObject.toString()).build();
			}
			String json = bulletin.toJSON().toString();
			return Response.ok(json, MediaType.APPLICATION_JSON).build();
		} catch (HibernateException e) {
			logger.warn("Error loading bulletin", e);
			return Response.status(400).type(MediaType.TEXT_PLAIN).entity(e.toString().toString()).build();
		}
	}

	@POST
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{bulletinId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update bulletin")
	public Response updateJSONBulletin(
			@PathParam("bulletinId") String bulletinId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Parameter(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin[].class))) String bulletinString,
			@QueryParam("region") String regionId,
			@Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletin");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				JSONObject bulletinJson = new JSONObject(bulletinString);
				AvalancheBulletin bulletin = new AvalancheBulletin(bulletinJson, UserController.getInstance()::getUser);

				AvalancheBulletinController.getInstance().updateBulletin(bulletin, startDate, endDate, region, user);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getJSONBulletins(date, regionIDs);
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@PUT
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create bulletin")
	public Response createJSONBulletin(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Parameter(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin[].class))) String bulletinString,
			@QueryParam("region") String regionId,
			@Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletin");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				JSONObject bulletinJson = new JSONObject(bulletinString);
				AvalancheBulletin bulletin = new AvalancheBulletin(bulletinJson, UserController.getInstance()::getUser);

				Map<String, AvalancheBulletin> avalancheBulletins = AvalancheBulletinController.getInstance()
						.createBulletin(bulletin, startDate, endDate, region);

				AvalancheReportController.getInstance().saveReport(avalancheBulletins, startDate, region, user);

				// save report for super regions
				for (Region superRegion : region.getSuperRegions()) {
					AvalancheReportController.getInstance().saveReport(avalancheBulletins, startDate, superRegion, user);
				}
			} else
				throw new AlbinaException("User is not authorized for this region!");

			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getJSONBulletins(date, regionIDs);
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@DELETE
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{bulletinId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Delete bulletin")
	public Response deleteJSONBulletin(
			@PathParam("bulletinId") String bulletinId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("region") String regionId,
			@Context SecurityContext securityContext) {
		logger.debug("DELETE JSON bulletin");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				AvalancheBulletinController.getInstance().deleteBulletin(bulletinId, startDate, endDate, region, user);

			} else
				throw new AlbinaException("User is not authorized for this region!");

			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getJSONBulletins(date, regionIDs);
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@POST
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create bulletins")
	public Response createJSONBulletins(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Parameter(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin[].class))) String bulletinsString,
			@QueryParam("region") String regionId,
			@Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletins");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				JSONArray bulletinsJson = new JSONArray(bulletinsString);
				List<AvalancheBulletin> bulletins = IntStream.range(0, bulletinsJson.length())
					.mapToObj(bulletinsJson::getJSONObject)
					.map(bulletinJson -> new AvalancheBulletin(bulletinJson, UserController.getInstance()::getUser))
					.collect(Collectors.toList());

				AvalancheBulletinController.getInstance().saveBulletins(bulletins, startDate, endDate, region, user);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getJSONBulletins(date, regionIDs);
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@POST
	@Secured({ Role.FORECASTER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/change")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Change bulletins")
	public Response changeBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@Parameter(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))) String bulletinsString,
		@QueryParam("region") String regionId,
		@Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletins change");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				BulletinStatus status = AvalancheReportController.getInstance().getInternalStatusForDay(startDate, region);

				if ((status != BulletinStatus.submitted) && (status != BulletinStatus.resubmitted)) {
					JSONArray bulletinsJson = new JSONArray(bulletinsString);
					List<AvalancheBulletin> bulletins = IntStream.range(0, bulletinsJson.length())
						.mapToObj(bulletinsJson::getJSONObject)
						.map(bulletinJson -> new AvalancheBulletin(bulletinJson, UserController.getInstance()::getUser))
						.collect(Collectors.toList());

					AvalancheBulletinController.getInstance().saveBulletins(bulletins, startDate, endDate, region, user);

					// eu.albina.model.AvalancheReport.timestamp has second precision due to MySQL's datatype datetime
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}

					List<AvalancheBulletin> allBulletins = AvalancheBulletinController.getInstance().submitBulletins(startDate,
							endDate, region, user);
					List<AvalancheBulletin> regionBulletins = allBulletins.stream()
						.filter(bulletin -> bulletin.affectsRegion(region))
						.collect(Collectors.toList());
					AvalancheReportController.getInstance().submitReport(regionBulletins, startDate, region, user);
					// submit report for super regions
					for (Region superRegion : region.getSuperRegions()) {
						List<AvalancheBulletin> superRegionBulletins = allBulletins.stream()
							.filter(bulletin -> bulletin.affectsRegion(superRegion))
							.collect(Collectors.toList());
						AvalancheReportController.getInstance().submitReport(superRegionBulletins, startDate, superRegion, user);
					}

					// eu.albina.model.AvalancheReport.timestamp has second precision due to MySQL's datatype datetime
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}

				List<Region> regions = Stream.concat(
					Stream.of(region),
					region.getSuperRegions().stream().filter(Region::isPublishBulletins)
				).distinct().collect(Collectors.toList());

				new Thread(() -> {
					new ChangeJob() {
						@Override
						protected Instant getStartDate(Clock clock) {
							return startDate;
						}

						@Override
						protected List<Region> getRegions() {
							return regions;
						}
					}.execute(null);
				}, "changeBulletins").start();

				return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@POST
	@Secured({ Role.FORECASTER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/submit")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Submit bulletins")
	public Response submitBulletins(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST submit bulletins");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (regionId != null && user.hasPermissionForRegion(regionId)) {
				List<AvalancheBulletin> allBulletins = AvalancheBulletinController.getInstance().submitBulletins(startDate,
						endDate, region, user);

				List<AvalancheBulletin> regionBulletins = allBulletins.stream()
					.filter(bulletin -> bulletin.affectsRegion(region))
					.collect(Collectors.toList());

				AvalancheReportController.getInstance().submitReport(regionBulletins, startDate, region, user);

				// submit report for super regions
				for (Region superRegion : region.getSuperRegions()) {
					List<AvalancheBulletin> superRegionBulletins = allBulletins.stream()
						.filter(bulletin -> bulletin.affectsRegion(superRegion))
						.collect(Collectors.toList());

					AvalancheReportController.getInstance().submitReport(superRegionBulletins, startDate, superRegion, user);
				}

				return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error submitting bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Check bulletins")
	public Response checkBulletins(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("GET check bulletins");

		try {
			if (regionId == null)
				throw new AlbinaException("No region defined!");

			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				JSONArray result = AvalancheBulletinController.getInstance().checkBulletins(startDate, endDate, region);
				return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/locked")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLockedBulletins(@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date, @Context SecurityContext securityContext) {
		logger.debug("GET JSON locked bulletins");

		try {
			JSONArray json = new JSONArray();
			for (BulletinLock bulletinLock : AvalancheBulletinController.getInstance().getLockedBulletins(DateControllerUtil.parseDateOrThrow(date)))
				json.put(bulletinLock);
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletin locks", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}
}
