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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.base.MoreObjects;

import eu.albina.caaml.CaamlVersion;
import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.RegionController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.AlbinaUtil;
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
))
public class AvalancheBulletinService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@Path("/edit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletins", content = @Content(schema = @Schema(implementation = AvalancheBulletin[].class)))
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
		regionIds.stream().forEach(regionId -> {
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
	public Response getPublishedXMLBulletins(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("region") String regionId, @QueryParam("lang") LanguageCode language,
			@QueryParam("version") CaamlVersion version) {
		logger.debug("GET published XML bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);

		if (regionId == null || regionId.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		try {
			Region region = RegionController.getInstance().getRegion(regionId);
			AvalancheBulletinController.getInstance();
			ArrayList<AvalancheBulletin> result = AvalancheReportController.getInstance().getPublishedBulletins(startDate,
				Collections.singletonList(region));
			AvalancheReport avalancheReport = AvalancheReport.of(result, region, ServerInstanceController.getInstance().getLocalServerInstance());
			String caaml = Caaml.createCaaml(avalancheReport, language, MoreObjects.firstNonNull(version, CaamlVersion.V5));
			if (caaml != null) {
				return Response.ok(caaml, MediaType.APPLICATION_XML).build();
			} else {
				logger.debug("No bulletins with status published.");
				return Response.noContent().build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins", e);
			try {
				return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toXML()).build();
			} catch (Exception ex) {
				return Response.status(400).type(MediaType.APPLICATION_XML).build();
			}
		} catch (RuntimeException e) {
			logger.warn("Error loading bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_XML).build();
		}
	}

	@GET
	@Path("/latest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLatest() {
		logger.debug("GET latest date");
		try {
			Instant date = AvalancheReportController.getInstance().getLatestDate();

			JSONObject json = new JSONObject();
			json.put("date", date.toString());

			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading latest date", e);
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toString()).build();
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublishedJSONBulletins(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("regions") List<String> regionIds, @QueryParam("lang") LanguageCode language) {
		logger.debug("GET published JSON bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);

		List<Region> regions = new ArrayList<Region>();

		if (regionIds.isEmpty()) {
			regions = RegionController.getInstance().getPublishBulletinRegions();
		} else {
			for (String regionId : regionIds) {
				try {
					Region region = RegionController.getInstance().getRegion(regionId);
					regions.add(region);
				} catch (HibernateException e) {
					logger.warn("No region with ID: " + regionId);
				}
			}
		}

		try {
			JSONArray jsonResult = AvalancheBulletinController.getInstance().getPublishedBulletinsJson(startDate,
					regions);

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Path("/highest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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
		regionIds.stream().forEach(regionId -> regions.add(RegionController.getInstance().getRegion(regionId)));

		try {
			DangerRating highestDangerRating = AvalancheBulletinController.getInstance()
					.getHighestDangerRating(startDate, regions);

			JSONObject jsonResult = new JSONObject();
			jsonResult.put("dangerRating", highestDangerRating);
			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading highest danger rating", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Path("/status")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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
	@Path("/status/internal")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInternalStatus(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("startDate") String start,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("endDate") String end) {

		if (regionId == null || regionId.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		try {
			Region region = RegionController.getInstance().getRegion(regionId);
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
	@Path("/status/publications")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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
	@Path("/status/publication")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublicationStatus(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date) {

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		Instant endDate = startDate;

		if (regionId == null || regionId.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		try {
			Map<Instant, AvalancheReport> status = AvalancheReportController.getInstance()
					.getPublicationStatus(startDate, endDate, RegionController.getInstance().getRegion(regionId));

			if (status.size() > 1)
				logger.warn("More than one report found!");
			else if (status.isEmpty())
				throw new AlbinaException("No publication found!");

			Map.Entry<Instant, AvalancheReport> entry = status.entrySet().iterator().next();
			JSONObject json = new JSONObject();
			json.put("date", DateTimeFormatter.ISO_INSTANT.format(entry.getKey()));
			json.put("report", entry.getValue().toJSON());

			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading status", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN })
    @Path("/preview")
    @Produces("application/pdf")
    public Response getPreviewPdf(@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date, @QueryParam("region") String regionId, @QueryParam("lang") LanguageCode language) {

		logger.debug("GET PDF preview [{}, {}]", date, regionId);

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Region region = RegionController.getInstance().getRegion(regionId);
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
	@Path("/{bulletinId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletin", content = @Content(schema = @Schema(implementation = AvalancheBulletin.class)))
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJSONBulletins(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			String bulletinsString, @QueryParam("region") String regionId, @Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletins");

		if (regionId == null || regionId.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			Region region = RegionController.getInstance().getRegion(regionId);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			JSONArray bulletinsJson = new JSONArray(bulletinsString);
			List<AvalancheBulletin> bulletins = IntStream.range(0, bulletinsJson.length())
				.mapToObj(bulletinsJson::getJSONObject)
				.map(bulletinJson -> new AvalancheBulletin(bulletinJson, UserController.getInstance()::getUser))
				.collect(Collectors.toList());

			Map<String, AvalancheBulletin> avalancheBulletins = AvalancheBulletinController.getInstance()
					.saveBulletins(bulletins, startDate, endDate, region, null);

			AvalancheReportController.getInstance().saveReport(avalancheBulletins, startDate, region, user);

			JSONObject jsonObject = new JSONObject();
			return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
					.entity(jsonObject.toString()).build();
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@POST
	@Secured({ Role.FORECASTER })
	@Path("/change")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response changeBulletins(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			String bulletinsString, @QueryParam("region") String regionId, @Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletins change");

		if (regionId == null || regionId.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			Region region = RegionController.getInstance().getRegion(regionId);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			JSONArray bulletinsJson = new JSONArray(bulletinsString);
			List<AvalancheBulletin> bulletins = IntStream.range(0, bulletinsJson.length())
				.mapToObj(bulletinsJson::getJSONObject)
				.map(bulletinJson -> new AvalancheBulletin(bulletinJson, UserController.getInstance()::getUser))
				.collect(Collectors.toList());
			// TODO validate

			Instant publicationTime = AlbinaUtil.getInstantNowNoNanos();
			AvalancheBulletinController.getInstance().saveBulletins(bulletins, startDate, endDate, region,
				publicationTime);
			AvalancheBulletinController.getInstance().submitBulletins(startDate, endDate, region, user);
			List<AvalancheBulletin> allBulletins = AvalancheBulletinController.getInstance().publishBulletins(startDate,
					endDate, region, publicationTime, user);

			// select bulletins within the region
			List<AvalancheBulletin> publishedBulletins = allBulletins.stream()
				.filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region))
				.collect(Collectors.toList());

			PublicationController.getInstance().startChangeThread(allBulletins, publishedBulletins, startDate, region,
					user);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@POST
	@Secured({ Role.FORECASTER })
	@Path("/submit")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response submitBulletins(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST submit bulletins");

		if (regionId == null || regionId.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			Region region = RegionController.getInstance().getRegion(regionId);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (regionId != null && user.hasPermissionForRegion(regionId)) {
				List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance().submitBulletins(startDate,
						endDate, region, user);
				AvalancheReportController.getInstance().submitReport(bulletins, startDate, region, user);

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
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkBulletins(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("GET check bulletins");

		try {
			if (regionId == null)
				throw new AlbinaException("No region defined!");

			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			Region region = RegionController.getInstance().getRegion(regionId);
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
}
