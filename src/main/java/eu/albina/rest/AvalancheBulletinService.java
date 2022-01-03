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
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.base.MoreObjects;

import eu.albina.caaml.CaamlVersion;
import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.EmailUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.map.MapUtil;
import eu.albina.util.PdfUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path("/bulletins")
@Api(value = "/bulletins")
public class AvalancheBulletinService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@Path("/edit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJSONBulletins(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions) {
		logger.debug("GET JSON bulletins");

		Instant startDate = null;
		Instant endDate = null;

		if (date != null)
			startDate = ZonedDateTime.parse(date).toInstant();
		else
			startDate = AlbinaUtil.getInstantStartOfDay();

		endDate = startDate.plus(1, ChronoUnit.DAYS);

		if (regions.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

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
			@ApiParam(value = "Start date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions, @QueryParam("lang") LanguageCode language,
			@QueryParam("version") CaamlVersion version) {
		logger.debug("GET published XML bulletins");

		Instant startDate = null;

		if (regions.isEmpty()) {
			regions = GlobalVariables.getPublishRegions();
		}

		try {
			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				startDate = AlbinaUtil.getInstantStartOfDay();

			String caaml = AvalancheBulletinController.getInstance().getPublishedBulletinsCaaml(startDate, regions,
					language, MoreObjects.firstNonNull(version, CaamlVersion.V5));
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
		} catch (TransformerException | ParserConfigurationException e) {
			logger.warn("Error loading bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_XML).build();
		}
	}

	// TODO enable authentication
	@GET
	@Path("/aineva")
	// @Secured({ Role.ADMIN, Role.FORECASTER })
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAinevaXMLBulletins(
			@ApiParam(value = "Start date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions, @QueryParam("lang") LanguageCode language) {
		logger.debug("GET published XML bulletins");

		Instant startDate = null;
		Instant endDate = null;

		if (regions.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		try {
			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				startDate = AlbinaUtil.getInstantStartOfDay();
			endDate = startDate.plus(1, ChronoUnit.DAYS);

			String caaml = AvalancheBulletinController.getInstance().getAinevaBulletinsCaaml(startDate, endDate,
					regions, language);
			if (caaml != null) {
				return Response.ok(caaml, MediaType.APPLICATION_XML).build();
			} else {
				logger.debug("No bulletins found.");
				return Response.noContent().build();
			}
		} catch (TransformerException | ParserConfigurationException e) {
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
			@ApiParam(value = "Start date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions, @QueryParam("lang") LanguageCode language) {
		logger.debug("GET published JSON bulletins");

		Instant startDate = null;

		if (regions.isEmpty()) {
			regions = GlobalVariables.getPublishRegions();
		}

		try {
			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				startDate = AlbinaUtil.getInstantStartOfDay();

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
			@ApiParam(value = "Start date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions) {
		logger.debug("GET highest danger rating");

		Instant startDate = null;

		if (regions.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		try {
			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				startDate = AlbinaUtil.getInstantStartOfDay();

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
	public Response getStatus(@QueryParam("region") String region,
			@QueryParam("timezone") String timezone,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("startDate") String start,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("endDate") String end) {
		Instant startDate = null;
		Instant endDate = null;

		try {
			if (start != null)
				startDate = ZonedDateTime.parse(start).toInstant();
			else
				startDate = AlbinaUtil.getInstantStartOfDay();

			if (end != null)
				endDate = ZonedDateTime.parse(end).toInstant();

			ZoneId zoneId = AlbinaUtil.localZone();
			if (!Strings.isNullOrEmpty(timezone)) {
				zoneId = ZoneId.of(timezone);
			}

			Map<Instant, BulletinStatus> status;
			// if no region is defined, get status for EUREGIO
			if (region == null || region.isEmpty())
				status = AvalancheReportController.getInstance().getStatus(startDate, endDate,
						GlobalVariables.getPublishRegions());
			else
				status = AvalancheReportController.getInstance().getStatus(startDate, endDate, region);

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
	public Response getInternalStatus(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("startDate") String start,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("endDate") String end) {
		Instant startDate = null;
		Instant endDate = null;

		try {
			if (start != null)
				startDate = ZonedDateTime.parse(start).toInstant();
			else
				startDate = AlbinaUtil.getInstantStartOfDay();

			if (end != null)
				endDate = ZonedDateTime.parse(end).toInstant();

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
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@Path("/status/publications")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublicationsStatus(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("startDate") String start,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("endDate") String end) {
		Instant startDate = null;
		Instant endDate = null;

		if (start != null)
			startDate = ZonedDateTime.parse(start).toInstant();
		else
			startDate = AlbinaUtil.getInstantStartOfDay();

		if (end != null)
			endDate = ZonedDateTime.parse(end).toInstant();

		Map<Instant, AvalancheReport> status = AvalancheReportController.getInstance().getPublicationStatus(startDate,
				endDate, region);
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
	public Response getPublicationStatus(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date) {
		Instant startDate = null;
		Instant endDate = null;

		try {
			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				startDate = AlbinaUtil.getInstantStartOfDay();

			endDate = startDate;

			Map<Instant, AvalancheReport> status = AvalancheReportController.getInstance()
					.getPublicationStatus(startDate, endDate, region);

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
    public Response getPreviewPdf(@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date, @QueryParam("region") String region, @QueryParam("lang") LanguageCode language) {

		logger.debug("GET PDF preview [" + date + "]");

		try {
			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			Collection<AvalancheBulletin> result = AvalancheBulletinController.getInstance().getBulletins(startDate, startDate, GlobalVariables.getPublishRegions());
			List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
			for (AvalancheBulletin b : result) {
				if (b.affectsRegion(region))
					bulletins.add(b);
			}
			Collections.sort(bulletins);

			String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTimeString = AlbinaUtil.getZonedDateTimeNowNoNanos().format(GlobalVariables.formatterPublicationTime);
			java.nio.file.Path outputDirectory = Paths.get(GlobalVariables.getTmpMapsPath(), validityDateString, publicationTimeString);

			MapUtil.createMapyrusMaps(bulletins, true, outputDirectory);

			PdfUtil.getInstance().createPdf(bulletins, language, region, false, AlbinaUtil.hasDaytimeDependency(bulletins), validityDateString,
						publicationTimeString, true);

			String filename = validityDateString + "_" + language.toString() + ".pdf";

			File file = new File(GlobalVariables.getTmpPdfDirectory() + System.getProperty("file.separator")
			+ validityDateString + System.getProperty("file.separator") + publicationTimeString
			+ System.getProperty("file.separator") + filename);

			return Response.ok(file).header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + filename + "\"").header(HttpHeaders.CONTENT_TYPE, "application/pdf").build();
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
	public Response getJSONBulletin(@PathParam("bulletinId") String bulletinId) {
		logger.debug("GET JSON bulletin: " + bulletinId);

		try {
			AvalancheBulletin bulletin = AvalancheBulletinController.getInstance().getBulletin(bulletinId);
			if (bulletin == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("message", "Bulletin not found for ID: " + bulletinId);
				return Response.status(Response.Status.NOT_FOUND).entity(jsonObject.toString()).build();
			}
			String json = bulletin.toJSON().toString();
			return Response.ok(json, MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletin", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJSONBulletins(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			String bulletinsString, @QueryParam("region") String region, @Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletins");

		try {
			Instant startDate = null;
			Instant endDate = null;
			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");
			endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			JSONArray bulletinsJson = new JSONArray(bulletinsString);
			List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
			for (int i = 0; i < bulletinsJson.length(); i++) {
				JSONObject bulletinJson = bulletinsJson.getJSONObject(i);
				AvalancheBulletin bulletin = new AvalancheBulletin(bulletinJson);
				bulletins.add(bulletin);
			}

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
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			String bulletinsString, @QueryParam("region") String region, @Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletins change");

		try {
			Instant startDate = null;
			Instant endDate = null;
			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");
			endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			JSONArray bulletinsJson = new JSONArray(bulletinsString);
			List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
			for (int i = 0; i < bulletinsJson.length(); i++) {
				// TODO validate
				JSONObject bulletinJson = bulletinsJson.getJSONObject(i);
				AvalancheBulletin bulletin = new AvalancheBulletin(bulletinJson);
				if (bulletin.affectsRegion(region))
					bulletins.add(bulletin);
			}

			Instant publicationTime = AlbinaUtil.getInstantNowNoNanos();
			AvalancheBulletinController.getInstance().saveBulletins(bulletins, startDate, endDate, region,
				publicationTime);
			AvalancheBulletinController.getInstance().submitBulletins(startDate, endDate, region, user);
			List<AvalancheBulletin> allBulletins = AvalancheBulletinController.getInstance().publishBulletins(startDate,
					endDate, region, publicationTime, user);

			// select bulletins within the region
			List<AvalancheBulletin> publishedBulletins = new ArrayList<AvalancheBulletin>();
			for (AvalancheBulletin bulletin : allBulletins)
				if (bulletin.affectsRegionWithoutSuggestions(region))
					publishedBulletins.add(bulletin);

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
	public Response submitBulletins(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST submit bulletins");

		try {
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region != null && user.hasPermissionForRegion(region)) {
				Instant startDate = null;
				Instant endDate = null;

				if (date != null)
					startDate = ZonedDateTime.parse(date).toInstant();
				else
					throw new AlbinaException("No date!");
				endDate = startDate.plus(1, ChronoUnit.DAYS);

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

	/**
	 * Publish a major update to an already published bulletin (not at 5PM nor 8AM).
	 *
	 * @param region
	 *            The region to publish the bulletins for.
	 * @param date
	 *            The date to publish the bulletins for.
	 * @param securityContext
	 * @return
	 */
	@POST
	@Secured({ Role.FORECASTER })
	@Path("/publish")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response publishBulletins(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST publish bulletins");

		try {
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region != null && user.hasPermissionForRegion(region)) {
				Instant startDate = null;
				Instant endDate = null;

				if (date != null)
					startDate = ZonedDateTime.parse(date).toInstant();
				else
					throw new AlbinaException("No date!");
				endDate = startDate.plus(1, ChronoUnit.DAYS);

				Instant publicationDate = AlbinaUtil.getInstantNowNoNanos();

				List<AvalancheBulletin> allBulletins = AvalancheBulletinController.getInstance()
						.publishBulletins(startDate, endDate, region, publicationDate, user);

				// select bulletins within the region
				List<AvalancheBulletin> publishedBulletins = new ArrayList<AvalancheBulletin>();
				for (AvalancheBulletin bulletin : allBulletins)
					if (bulletin.affectsRegionWithoutSuggestions(region))
						publishedBulletins.add(bulletin);

				List<String> regions = new ArrayList<String>();
				regions.add(region);

				PublicationController.getInstance().startUpdateThread(allBulletins, regions, publishedBulletins,
						startDate, region, user, publicationDate);

				return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error publishing bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	/**
	 * Publish a major update to an already published bulletin (not at 5PM nor 8AM).
	 *
	 * @param date
	 *            The date to publish the bulletins for.
	 * @param securityContext
	 * @return
	 */
	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response publishAllBulletins(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST publish all bulletins");

		try {
			// REGION
			List<String> regions = GlobalVariables.getPublishRegions();

			if (!regions.isEmpty()) {
				try {
					User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

					Instant startDate = null;
					Instant endDate = null;

					if (date != null)
						startDate = ZonedDateTime.parse(date).toInstant();
					else
						throw new AlbinaException("No date!");
					endDate = startDate.plus(1, ChronoUnit.DAYS);

					Instant publicationDate = AlbinaUtil.getInstantNowNoNanos();

					// Set publication date
					Map<String, AvalancheBulletin> publishedBulletins = AvalancheBulletinController.getInstance()
							.publishBulletins(startDate, endDate, regions, publicationDate, user);

					if (publishedBulletins.values() != null && !publishedBulletins.values().isEmpty()) {
						List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
						for (AvalancheBulletin avalancheBulletin : publishedBulletins.values()) {
							if (avalancheBulletin.getPublishedRegions() != null
									&& !avalancheBulletin.getPublishedRegions().isEmpty())
								result.add(avalancheBulletin);
						}
						if (result != null && !result.isEmpty())
							PublicationController.getInstance().publishAutomatically(result);
					}

					List<String> avalancheReportIds = new ArrayList<String>();
					for (String region : regions) {
						String avalancheReportId = AvalancheReportController.getInstance()
								.publishReport(publishedBulletins.values(), startDate, region, user, publicationDate);
						avalancheReportIds.add(avalancheReportId);
					}
				} catch (AlbinaException e) {
					logger.error("Error publishing bulletins", e);
					throw new AlbinaException(e.getMessage());
				}
			} else {
				logger.info("No bulletins to publish.");
			}
			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error publishing bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/pdf")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPdf(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create PDF [" + date + "]");

		try {
			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			Collection<AvalancheBulletin> result = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, GlobalVariables.getPublishRegions());

			List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
			for (AvalancheBulletin b : result)
				bulletins.add(b);

			Collections.sort(bulletins);

			String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

			Thread createPdfThread = PublicationController.getInstance().createPdf(bulletins, validityDateString,
					publicationTimeString);
			createPdfThread.start();

			try {
				createPdfThread.join();
			} catch (InterruptedException e) {
				logger.error("PDF production interrupted", e);
			}

			// copy files
			AlbinaUtil.runUpdatePdfsScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestPdfsScript(validityDateString);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating PDFs", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/html")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createHtml(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create HTML [" + date + "]");

		try {
			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, GlobalVariables.getPublishRegions());

			Thread createSimpleHtmlThread = PublicationController.getInstance().createSimpleHtml(bulletins);
			createSimpleHtmlThread.start();

			try {
				createSimpleHtmlThread.join();
			} catch (InterruptedException e) {
				logger.error("HTML production interrupted", e);
			}

			// copy files
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestHtmlsScript(AlbinaUtil.getValidityDateString(bulletins));

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating HTMLs", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/staticwidget")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createStaticWidget(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create static widget [" + date + "]");

		try {
			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, GlobalVariables.getPublishRegions());

			String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

			Thread createStaticWidgetsThread = PublicationController.getInstance().createStaticWidgets(bulletins,
					validityDateString, publicationTimeString);
			createStaticWidgetsThread.start();

			try {
				createStaticWidgetsThread.join();
			} catch (InterruptedException e) {
				logger.error("Static widget production interrupted", e);
			}

			// copy files
			AlbinaUtil.runUpdateStaticWidgetsScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestStaticWidgetsScript(validityDateString);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating static widgets", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/map")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createMap(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create map [" + date + "]");

		try {
			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, GlobalVariables.getPublishRegions());

			String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

			try {
				PublicationController.getInstance().createMaps(bulletins, validityDateString, publicationTimeString);
			} catch (InterruptedException e) {
				logger.error("Map production interrupted", e);
			} catch (Exception e1) {
				logger.error("Error during map production", e1);
			}

			// copy files
			AlbinaUtil.runUpdateMapsScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestMapsScript(validityDateString);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating maps", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/caaml")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createCaaml(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create caaml [" + date + "]");

		try {
			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, GlobalVariables.getPublishRegions());

			String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

			PublicationController.getInstance().createCaaml(bulletins, validityDateString, publicationTimeString);

			// copy files
			AlbinaUtil.runUpdateXmlsScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestXmlsScript(validityDateString);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating CAAML", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createJson(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create json [" + date + "]");

		try {
			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, GlobalVariables.getPublishRegions());

			String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

			PublicationController.getInstance().createJson(bulletins, validityDateString, publicationTimeString);

			// copy files
			AlbinaUtil.runUpdateJsonScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestJsonScript(validityDateString);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating CAAML", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/email")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendEmail(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST send emails for " + region + " [" + date + "]");

		try {
			if (region == null)
				throw new AlbinaException("No region defined!");

			List<String> regions = new ArrayList<String>();
			regions.add(region);

			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, GlobalVariables.getPublishRegions());

			Thread sendEmailsThread = PublicationController.getInstance().sendEmails(bulletins, regions, false, false);
			sendEmailsThread.start();

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error sending emails", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/email/test")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendTestEmail(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST send test emails for " + region + " [" + date + "]");

		try {
			if (region == null)
				throw new AlbinaException("No region defined!");

			List<String> regions = new ArrayList<String>();
			regions.add(region);

			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, GlobalVariables.getPublishRegions());

			logger.debug("startDate: " + startDate.toString());
			logger.debug("#bulletins: " + bulletins.size());

			EmailUtil.getInstance().sendBulletinEmails(bulletins, regions, false, true);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error sending test emails", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		} catch (Exception e) {
			logger.warn("Error sending test emails", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/publish/telegram")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response triggerTelegramChannel(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST trigger telegram channel for " + region + " [" + date + "]");

		try {
			if (region == null)
				throw new AlbinaException("No region defined!");

			List<String> regions = new ArrayList<String>();
			regions.add(region);

			Instant startDate = null;

			if (date != null)
				startDate = ZonedDateTime.parse(date).toInstant();
			else
				throw new AlbinaException("No date!");

			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, GlobalVariables.getPublishRegions());

			Thread triggerTelegramChannelThread = PublicationController.getInstance().triggerTelegramChannel(bulletins,
					regions, false);
			triggerTelegramChannelThread.start();

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error triggering telegram channel", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkBulletins(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST publish bulletins");

		try {
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region != null && user.hasPermissionForRegion(region)) {
				Instant startDate = null;
				Instant endDate = null;

				if (date != null)
					startDate = ZonedDateTime.parse(date).toInstant();
				else
					throw new AlbinaException("No date!");
				endDate = startDate.plus(1, ChronoUnit.DAYS);

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
