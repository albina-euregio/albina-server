package eu.albina.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.AuthorizationUtil;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path("/bulletins")
@Api(value = "/bulletins")
public class AvalancheBulletinService {

	private static Logger logger = LoggerFactory.getLogger(AvalancheBulletinService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.OBSERVER })
	@Path("/edit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJSONBulletins(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions) {
		logger.debug("GET JSON bulletins");

		DateTime startDate = null;
		DateTime endDate = null;

		if (date != null)
			startDate = DateTime.parse(date, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
		else
			startDate = (new DateTime().withTimeAtStartOfDay()).toDateTime(DateTimeZone.UTC);
		endDate = startDate.plusDays(1);

		if (regions.isEmpty()) {
			regions.add(GlobalVariables.codeTrentino);
			regions.add(GlobalVariables.codeSouthTyrol);
			regions.add(GlobalVariables.codeTyrol);
		}

		try {
			List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance().getBulletins(startDate,
					endDate, regions);
			JSONArray jsonResult = new JSONArray();
			if (bulletins != null) {
				for (AvalancheBulletin bulletin : bulletins) {
					jsonResult.put(bulletin.toJSON());
				}
			}
			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.OBSERVER })
	@Path("/locked")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLockedBulletins(@QueryParam("region") String bulletin) {
		logger.debug("GET JSON locked bulletins");
		JSONArray json = new JSONArray();
		for (DateTime date : AvalancheBulletinController.getInstance().getLockedBulletins(bulletin))
			json.put(date.toString(GlobalVariables.formatterDateTime));
		return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getPublishedXMLBulletins(
			@ApiParam(value = "Starttime in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions, @QueryParam("lang") LanguageCode language) {
		logger.debug("GET published XML bulletins");

		DateTime startDate = null;
		DateTime endDate = null;

		if (regions.isEmpty()) {
			regions.add(GlobalVariables.codeTrentino);
			regions.add(GlobalVariables.codeSouthTyrol);
			regions.add(GlobalVariables.codeTyrol);
		}

		if (date != null)
			startDate = DateTime.parse(date, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
		else
			startDate = (new DateTime().withTimeAtStartOfDay()).toDateTime(DateTimeZone.UTC);
		endDate = startDate.plusDays(1);

		try {
			String caaml = AvalancheBulletinController.getInstance().getPublishedBulletinsCaaml(startDate, endDate,
					regions, language);
			if (caaml != null) {
				return Response.ok(caaml, MediaType.APPLICATION_XML).build();
			} else {
				logger.debug("No bulletins with status published.");
				return Response.noContent().build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins - " + e.getMessage());
			try {
				return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toXML()).build();
			} catch (Exception ex) {
				return Response.status(400).type(MediaType.APPLICATION_XML).build();
			}
		} catch (TransformerException e) {
			logger.warn("Error loading bulletins - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_XML).build();
		} catch (ParserConfigurationException e) {
			logger.warn("Error loading bulletins - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_XML).build();
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublishedJSONBulletins(
			@ApiParam(value = "Starttime in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions, @QueryParam("lang") LanguageCode language) {
		logger.debug("GET published JSON bulletins");

		DateTime startDate = null;
		DateTime endDate = null;

		if (regions.isEmpty()) {
			regions.add(GlobalVariables.codeTrentino);
			regions.add(GlobalVariables.codeSouthTyrol);
			regions.add(GlobalVariables.codeTyrol);
		}

		if (date != null)
			startDate = DateTime.parse(date, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
		else
			startDate = (new DateTime().withTimeAtStartOfDay()).toDateTime(DateTimeZone.UTC);
		endDate = startDate.plusDays(1);

		try {
			Collection<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance()
					.getPublishedBulletinsJson(startDate, endDate, regions);
			JSONArray jsonResult = new JSONArray();
			if (bulletins != null) {
				for (AvalancheBulletin bulletin : bulletins) {
					jsonResult.put(bulletin.toSmallJSON());
				}
			}
			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Path("/highest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHighestDangerRating(
			@ApiParam(value = "Starttime in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions) {
		logger.debug("GET highest danger rating");

		DateTime startDate = null;
		DateTime endDate = null;

		if (regions.isEmpty()) {
			regions.add(GlobalVariables.codeTrentino);
			regions.add(GlobalVariables.codeSouthTyrol);
			regions.add(GlobalVariables.codeTyrol);
		}

		if (date != null)
			startDate = DateTime.parse(date, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
		else
			startDate = (new DateTime().withTimeAtStartOfDay()).toDateTime(DateTimeZone.UTC);
		endDate = startDate.plusDays(1);

		try {
			DangerRating highestDangerRating = AvalancheBulletinController.getInstance()
					.getHighestDangerRating(startDate, endDate, regions);

			JSONObject jsonResult = new JSONObject();
			jsonResult.put("dangerRating", highestDangerRating);
			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading highest danger rating - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	// @Secured({ Role.ADMIN, Role.FORECASTER,
	// Role.OBSERVER })
	@Path("/status")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("startDate") String start,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("endDate") String end) {
		DateTime startDate = null;
		DateTime endDate = null;

		if (start != null)
			startDate = DateTime.parse(start, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
		else
			startDate = (new DateTime().withTimeAtStartOfDay()).toDateTime(DateTimeZone.UTC);

		if (end != null)
			endDate = DateTime.parse(end, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);

		try {
			Map<DateTime, BulletinStatus> status = AvalancheReportController.getInstance().getStatus(startDate, endDate,
					region);
			JSONArray jsonResult = new JSONArray();

			for (Entry<DateTime, BulletinStatus> entry : status.entrySet()) {
				JSONObject json = new JSONObject();
				json.put("date", entry.getKey().toString(GlobalVariables.formatterDateTime));
				json.put("status", entry.getValue().toString());
				jsonResult.put(json);
			}

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading status - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.OBSERVER })
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
			logger.warn("Error loading bulletin: " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.FORECASTER })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJSONBulletins(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			String bulletinsString, @QueryParam("region") String region, @Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletins");

		try {
			DateTime startDate = null;
			DateTime endDate = null;
			if (date != null)
				startDate = DateTime.parse(date, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
			else
				throw new AlbinaException("No date!");
			endDate = startDate.plusDays(1);

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

			AvalancheBulletinController.getInstance().saveBulletins(bulletins, startDate, endDate, region, null);

			AvalancheReportController.getInstance().saveReport(startDate, region, user);

			JSONObject jsonObject = new JSONObject();
			// TODO return some meaningful path
			return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
					.entity(jsonObject.toString()).build();
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin - " + e.getMessage());
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
			DateTime startDate = null;
			DateTime endDate = null;
			if (date != null)
				startDate = DateTime.parse(date, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
			else
				throw new AlbinaException("No date!");
			endDate = startDate.plusDays(1);

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

			AvalancheBulletinController.getInstance().saveBulletins(bulletins, startDate, endDate, region,
					new DateTime());
			DateTime publicationDate = new DateTime();
			AvalancheBulletinController.getInstance().submitBulletins(startDate, endDate, region);
			AvalancheBulletinController.getInstance().publishBulletins(startDate, endDate, region, publicationDate);
			List<String> avalancheReportIds = new ArrayList<String>();
			String avalancheReportId = AvalancheReportController.getInstance().changeReport(startDate, region, user);
			avalancheReportIds.add(avalancheReportId);

			PublicationController.getInstance().change(avalancheReportIds, bulletins);

			return Response.ok(MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	/*
	 * @POST
	 * 
	 * @Secured({ Role.FORECASTER })
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * createJSONBulletin(String bulletinString, @Context SecurityContext
	 * securityContext) { logger.debug("POST JSON bulletin");
	 * 
	 * JSONObject bulletinJson = new JSONObject(bulletinString);
	 * 
	 * JSONObject validationResult =
	 * eu.albina.json.JsonValidator.validateAvalancheBulletin(bulletinString); if
	 * (validationResult.length() == 0) { String username =
	 * securityContext.getUserPrincipal().getName(); AvalancheBulletin bulletin =
	 * new AvalancheBulletin(bulletinJson, username); try { Serializable bulletinId
	 * = AvalancheBulletinController.getInstance().saveBulletin(bulletin); if
	 * (bulletinId == null) { JSONObject jsonObject = new JSONObject();
	 * jsonObject.append("message", "Bulletin not saved!"); return
	 * Response.status(400).type(MediaType.APPLICATION_JSON).entity(jsonObject.
	 * toString()).build(); } else { JSONObject jsonObject = new JSONObject();
	 * jsonObject.put("bulletinId", bulletinId); return
	 * Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(bulletinId)
	 * ).build())
	 * .type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build(); } }
	 * catch (AlbinaException e) { logger.warn("Error creating bulletin - " +
	 * e.getMessage()); return
	 * Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).
	 * build(); } } else return
	 * Response.status(400).type(MediaType.APPLICATION_JSON).entity(validationResult
	 * .toString()).build(); }
	 * 
	 * @PUT
	 * 
	 * @Secured({ Role.FORECASTER })
	 * 
	 * @Path("/{bulletinId}")
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * updateJSONBulletin(@PathParam("bulletinId") String bulletinId, String
	 * bulletinString,
	 * 
	 * @Context SecurityContext securityContext) {
	 * logger.debug("PUT JSON bulletin");
	 * 
	 * JSONObject bulletinJson = new JSONObject(bulletinString);
	 * 
	 * JSONObject validationResult =
	 * eu.albina.json.JsonValidator.validateAvalancheBulletin(bulletinString); if
	 * (validationResult.length() == 0) { AvalancheBulletin bulletin = new
	 * AvalancheBulletin(bulletinJson, null);
	 * bulletin.setCreator(bulletinJson.getString("creator"));
	 * bulletin.setId(bulletinJson.getString("id")); try {
	 * AvalancheBulletinController.getInstance().updateBulletin(bulletin); return
	 * Response.ok().type(MediaType.APPLICATION_JSON).build(); } catch
	 * (AlbinaException e) { logger.warn("Error updating bulletin - " +
	 * e.getMessage()); return
	 * Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().
	 * toString()).build(); } } else return
	 * Response.status(400).type(MediaType.APPLICATION_JSON).entity(validationResult
	 * .toString()).build(); }
	 * 
	 * @DELETE
	 * 
	 * @Secured({ Role.FORECASTER })
	 * 
	 * @Path("/{bulletinId}")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON) public Response
	 * deleteJSONBulletin(@PathParam("bulletinId") String bulletinId,
	 * 
	 * @Context SecurityContext securityContext) {
	 * logger.debug("DELETE JSON bulletin: " + bulletinId);
	 * 
	 * try { User user =
	 * UserController.getInstance().getUser(securityContext.getUserPrincipal().
	 * getName());
	 * AvalancheBulletinController.getInstance().deleteBulletin(bulletinId,
	 * user.getRole()); return
	 * Response.ok(uri.getAbsolutePathBuilder().path(String.valueOf(bulletinId)).
	 * build()) .type(MediaType.APPLICATION_JSON).build(); } catch (AlbinaException
	 * e) { logger.warn("Error deleting bulletin - " + e.getMessage()); return
	 * Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().
	 * toString()).build(); } }
	 */

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

			if (region != null && AuthorizationUtil.hasPermissionForRegion(user, region)) {
				DateTime startDate = null;
				DateTime endDate = null;

				if (date != null)
					startDate = DateTime.parse(date, GlobalVariables.parserDateTime);
				else
					throw new AlbinaException("No date!");
				endDate = startDate.plusDays(1);

				AvalancheBulletinController.getInstance().submitBulletins(startDate, endDate, region);
				AvalancheReportController.getInstance().submitReport(startDate, region, user);

				return Response.ok(MediaType.APPLICATION_JSON).build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error submitting bulletins - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	/**
	 * Publish an major update to an already published bulletin (not at 5PM nor
	 * 8AM).
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

			if (region != null && AuthorizationUtil.hasPermissionForRegion(user, region)) {
				DateTime startDate = null;
				DateTime endDate = null;

				if (date != null)
					startDate = DateTime.parse(date, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
				else
					throw new AlbinaException("No date!");
				endDate = startDate.plusDays(1);

				DateTime publicationDate = new DateTime();

				AvalancheBulletinController.getInstance().publishBulletins(startDate, endDate, region, publicationDate);
				List<String> avalancheReportIds = new ArrayList<String>();
				String avalancheReportId = AvalancheReportController.getInstance().publishReport(startDate, region,
						user, publicationDate);
				avalancheReportIds.add(avalancheReportId);

				List<String> regions = new ArrayList<String>();
				regions.add(region);

				try {
					List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance()
							.getBulletins(startDate, endDate, regions);
					PublicationController.getInstance().update(avalancheReportIds, bulletins, regions);
				} catch (AlbinaException e) {
					logger.warn("Error loading bulletins - " + e.getMessage());
					throw new AlbinaException(e.getMessage());
				} catch (MessagingException e) {
					logger.warn("Error sending emails - " + e.getMessage());
					throw new AlbinaException(e.getMessage());
				}

				return Response.ok(MediaType.APPLICATION_JSON).build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error publishing bulletins - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.FORECASTER })
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkBulletins(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST publish bulletins");

		try {
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region != null && AuthorizationUtil.hasPermissionForRegion(user, region)) {
				DateTime startDate = null;
				DateTime endDate = null;

				if (date != null)
					startDate = DateTime.parse(date, GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
				else
					throw new AlbinaException("No date!");
				endDate = startDate.plusDays(1);

				JSONArray result = AvalancheBulletinController.getInstance().checkBulletins(startDate, endDate, region);
				return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}
}
