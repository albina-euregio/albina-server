package eu.albina.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.AlbinaUtil;
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
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL, Role.EVTZ, Role.VIENNA })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJSONBulletins(
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions) {
		logger.debug("GET JSON bulletins");

		DateTime startDate = null;
		DateTime endDate = null;

		if (date != null)
			startDate = DateTime.parse(date, GlobalVariables.parserDateTime);
		else
			startDate = new DateTime().withTimeAtStartOfDay();
		endDate = startDate.plusDays(1);

		if (regions.isEmpty()) {
			regions.add("IT-32");
			regions.add("AT-07");
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
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL, Role.EVTZ, Role.VIENNA })
	@Path("/locked")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLockedBulletins(@QueryParam("region") String bulletin) {
		logger.debug("GET JSON locked bulletins");
		// TODO check if query param "region" is correct
		JSONArray json = new JSONArray();
		for (DateTime date : AvalancheBulletinController.getInstance().getLockedBulletins(bulletin))
			json.put(date.toString(GlobalVariables.formatterDateTime));
		return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getXMLBulletins(
			@ApiParam(value = "Starttime in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@QueryParam("regions") List<String> regions, @QueryParam("lang") LanguageCode language) {
		logger.debug("GET XML bulletins");

		DateTime startDate = null;
		DateTime endDate = null;

		if (regions.isEmpty()) {
			regions.add("IT-32-TN");
			regions.add("IT-32-BZ");
			regions.add("AT-07");
		}

		if (date != null)
			startDate = DateTime.parse(date, GlobalVariables.parserDateTime);
		else
			startDate = new DateTime().withTimeAtStartOfDay();
		endDate = startDate.plusDays(1);

		try {
			List<AvalancheBulletin> bulletins = AvalancheReportController.getInstance().getPublishedBulletins(startDate,
					endDate, regions);

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = AlbinaUtil.createObsCollectionHeaderCaaml(doc);

			// create meta data
			boolean hasDaytimeDependency = false;
			DateTime publicationDate = new DateTime();
			if (bulletins != null) {
				for (AvalancheBulletin bulletin : bulletins) {
					if (bulletin.getStatus(regions) == BulletinStatus.published) {
						if (bulletin.hasDaytimeDependency())
							hasDaytimeDependency = true;
						if (bulletin.getPublicationDate() != null
								&& bulletin.getPublicationDate().isBefore(publicationDate))
							publicationDate = bulletin.getPublicationDate();
					}
				}
			}

			Element metaDataProperty = doc.createElement("metaDataProperty");
			Element metaData = doc.createElement("MetaData");
			Element dateTimeReport = doc.createElement("dateTimeReport");
			dateTimeReport.appendChild(doc.createTextNode(publicationDate.toString(GlobalVariables.formatterDateTime)));
			metaData.appendChild(dateTimeReport);

			Element customData = doc.createElement("customData");
			Element daytimeDependency = doc.createElement("albina:daytimeDependency");
			if (hasDaytimeDependency) {
				daytimeDependency.appendChild(doc.createTextNode("true"));
				customData.appendChild(daytimeDependency);

				Element dangerRatingMapAM300 = doc.createElement("albina:DangerRatingMap");
				Element resolutionAM300 = doc.createElement("albina:resolution");
				resolutionAM300.appendChild(doc.createTextNode("300"));
				dangerRatingMapAM300.appendChild(resolutionAM300);
				Element filetypeAM300 = doc.createElement("albina:filetype");
				filetypeAM300.appendChild(doc.createTextNode("jpg"));
				dangerRatingMapAM300.appendChild(filetypeAM300);
				Element daytimeAM300 = doc.createElement("albina:daytime");
				daytimeAM300.appendChild(doc.createTextNode("am"));
				dangerRatingMapAM300.appendChild(daytimeAM300);
				Element urlAM300 = doc.createElement("albina:url");
				urlAM300.appendChild(doc.createTextNode(GlobalVariables.univieBaseUrlTN
						+ startDate.toString(GlobalVariables.formatterDate) + "_am_trentino_300.jpg"));
				dangerRatingMapAM300.appendChild(urlAM300);
				customData.appendChild(dangerRatingMapAM300);

				Element dangerRatingMapPM300 = doc.createElement("albina:DangerRatingMap");
				Element resolutionPM300 = doc.createElement("albina:resolution");
				resolutionPM300.appendChild(doc.createTextNode("300"));
				dangerRatingMapPM300.appendChild(resolutionPM300);
				Element filetypePM300 = doc.createElement("albina:filetype");
				filetypePM300.appendChild(doc.createTextNode("jpg"));
				dangerRatingMapPM300.appendChild(filetypePM300);
				Element daytimePM300 = doc.createElement("albina:daytime");
				daytimePM300.appendChild(doc.createTextNode("pm"));
				dangerRatingMapPM300.appendChild(daytimePM300);
				Element urlPM300 = doc.createElement("albina:url");
				urlPM300.appendChild(doc.createTextNode(GlobalVariables.univieBaseUrlTN
						+ startDate.toString(GlobalVariables.formatterDate) + "_pm_trentino_300.jpg"));
				dangerRatingMapPM300.appendChild(urlPM300);
				customData.appendChild(dangerRatingMapPM300);

				Element dangerRatingMapAM150 = doc.createElement("albina:DangerRatingMap");
				Element resolutionAM150 = doc.createElement("albina:resolution");
				resolutionAM150.appendChild(doc.createTextNode("150"));
				dangerRatingMapAM150.appendChild(resolutionAM150);
				Element filetypeAM150 = doc.createElement("albina:filetype");
				filetypeAM150.appendChild(doc.createTextNode("jpg"));
				dangerRatingMapAM150.appendChild(filetypeAM150);
				Element daytimeAM150 = doc.createElement("albina:daytime");
				daytimeAM150.appendChild(doc.createTextNode("am"));
				dangerRatingMapAM150.appendChild(daytimeAM150);
				Element urlAM150 = doc.createElement("albina:url");
				urlAM150.appendChild(doc.createTextNode(GlobalVariables.univieBaseUrlTN
						+ startDate.toString(GlobalVariables.formatterDate) + "_am_trentino_150.jpg"));
				dangerRatingMapAM150.appendChild(urlAM150);
				customData.appendChild(dangerRatingMapAM150);

				Element dangerRatingMapPM150 = doc.createElement("albina:DangerRatingMap");
				Element resolutionPM150 = doc.createElement("albina:resolution");
				resolutionPM150.appendChild(doc.createTextNode("150"));
				dangerRatingMapPM150.appendChild(resolutionPM150);
				Element filetypePM150 = doc.createElement("albina:filetype");
				filetypePM150.appendChild(doc.createTextNode("jpg"));
				dangerRatingMapPM150.appendChild(filetypePM150);
				Element daytimePM150 = doc.createElement("albina:daytime");
				daytimePM150.appendChild(doc.createTextNode("pm"));
				dangerRatingMapPM150.appendChild(daytimePM150);
				Element urlPM150 = doc.createElement("albina:url");
				urlPM150.appendChild(doc.createTextNode(GlobalVariables.univieBaseUrlTN
						+ startDate.toString(GlobalVariables.formatterDate) + "_pm_trentino_150.jpg"));
				dangerRatingMapPM150.appendChild(urlPM150);
				customData.appendChild(dangerRatingMapPM150);

			} else {
				daytimeDependency.appendChild(doc.createTextNode("false"));
				customData.appendChild(daytimeDependency);

				Element dangerRatingMapFullday300 = doc.createElement("albina:DangerRatingMap");
				Element resolutionFullday300 = doc.createElement("albina:resolution");
				resolutionFullday300.appendChild(doc.createTextNode("300"));
				dangerRatingMapFullday300.appendChild(resolutionFullday300);
				Element filetypeFullday300 = doc.createElement("albina:filetype");
				filetypeFullday300.appendChild(doc.createTextNode("jpg"));
				dangerRatingMapFullday300.appendChild(filetypeFullday300);
				Element daytimeFullday300 = doc.createElement("albina:daytime");
				daytimeFullday300.appendChild(doc.createTextNode("fullday"));
				dangerRatingMapFullday300.appendChild(daytimeFullday300);
				Element urlFullday300 = doc.createElement("albina:url");
				urlFullday300.appendChild(doc.createTextNode(GlobalVariables.univieBaseUrlTN
						+ startDate.toString(GlobalVariables.formatterDate) + "_fullday_trentino_300.jpg"));
				dangerRatingMapFullday300.appendChild(urlFullday300);
				customData.appendChild(dangerRatingMapFullday300);

				Element dangerRatingMapFullday150 = doc.createElement("albina:DangerRatingMap");
				Element resolutionFullday150 = doc.createElement("albina:resolution");
				resolutionFullday150.appendChild(doc.createTextNode("150"));
				dangerRatingMapFullday150.appendChild(resolutionFullday150);
				Element filetypeFullday150 = doc.createElement("albina:filetype");
				filetypeFullday150.appendChild(doc.createTextNode("jpg"));
				dangerRatingMapFullday150.appendChild(filetypeFullday150);
				Element daytimeFullday150 = doc.createElement("albina:daytime");
				daytimeFullday150.appendChild(doc.createTextNode("fullday"));
				dangerRatingMapFullday150.appendChild(daytimeFullday150);
				Element urlFullday150 = doc.createElement("albina:url");
				urlFullday150.appendChild(doc.createTextNode(GlobalVariables.univieBaseUrlTN
						+ startDate.toString(GlobalVariables.formatterDate) + "_fullday_trentino_150.jpg"));
				dangerRatingMapFullday150.appendChild(urlFullday150);
				customData.appendChild(dangerRatingMapFullday150);
			}
			metaData.appendChild(customData);

			metaDataProperty.appendChild(metaData);
			rootElement.appendChild(metaDataProperty);

			Element observations = doc.createElement("observations");

			boolean found = false;

			if (bulletins != null) {
				for (AvalancheBulletin bulletin : bulletins) {
					if (bulletin.getStatus(regions) == BulletinStatus.published) {
						observations.appendChild(bulletin.toCAAML(doc, language));
						found = true;
					}
				}
			}
			rootElement.appendChild(observations);
			doc.appendChild(rootElement);
			if (found) {
				return Response.ok(AlbinaUtil.convertDocToString(doc), MediaType.APPLICATION_XML).build();
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
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL, Role.EVTZ, Role.VIENNA })
	@Path("/status")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date) {
		DateTime startDate = null;

		if (date != null)
			startDate = DateTime.parse(date, GlobalVariables.parserDateTime);
		else
			startDate = new DateTime().withTimeAtStartOfDay();

		try {
			BulletinStatus status = AvalancheReportController.getInstance().getStatus(startDate, region);
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("status", status.toString());
			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading status - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL, Role.EVTZ, Role.VIENNA })
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
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Path("/test")
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
				startDate = DateTime.parse(date, GlobalVariables.parserDateTime);
			else
				throw new AlbinaException("No date!");
			endDate = startDate.plusDays(1);

			JSONArray bulletinsJson = new JSONArray(bulletinsString);
			List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
			for (int i = 0; i < bulletinsJson.length(); i++) {
				// TODO validate
				JSONObject bulletinJson = bulletinsJson.getJSONObject(i);
				String username = securityContext.getUserPrincipal().getName();
				AvalancheBulletin bulletin = new AvalancheBulletin(bulletinJson, username);
				if (bulletin.affectsRegion(region))
					bulletins.add(bulletin);
			}

			AvalancheBulletinController.getInstance().saveBulletins(bulletins, startDate, endDate, region);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
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
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJSONBulletin(String bulletinString, @Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletin");

		JSONObject bulletinJson = new JSONObject(bulletinString);

		JSONObject validationResult = eu.albina.json.JsonValidator.validateAvalancheBulletin(bulletinString);
		if (validationResult.length() == 0) {
			String username = securityContext.getUserPrincipal().getName();
			AvalancheBulletin bulletin = new AvalancheBulletin(bulletinJson, username);
			try {
				Serializable bulletinId = AvalancheBulletinController.getInstance().saveBulletin(bulletin);
				if (bulletinId == null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.append("message", "Bulletin not saved!");
					return Response.status(400).type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("bulletinId", bulletinId);
					return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(bulletinId)).build())
							.type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
				}
			} catch (AlbinaException e) {
				logger.warn("Error creating bulletin - " + e.getMessage());
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
			}
		} else
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(validationResult.toString()).build();
	}

	@PUT
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Path("/{bulletinId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateJSONBulletin(@PathParam("bulletinId") String bulletinId, String bulletinString,
			@Context SecurityContext securityContext) {
		logger.debug("PUT JSON bulletin");

		JSONObject bulletinJson = new JSONObject(bulletinString);

		JSONObject validationResult = eu.albina.json.JsonValidator.validateAvalancheBulletin(bulletinString);
		if (validationResult.length() == 0) {
			AvalancheBulletin bulletin = new AvalancheBulletin(bulletinJson, null);
			bulletin.setCreator(bulletinJson.getString("creator"));
			bulletin.setId(bulletinJson.getString("id"));
			try {
				AvalancheBulletinController.getInstance().updateBulletin(bulletin);
				return Response.ok().type(MediaType.APPLICATION_JSON).build();
			} catch (AlbinaException e) {
				logger.warn("Error updating bulletin - " + e.getMessage());
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
			}
		} else
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(validationResult.toString()).build();
	}

	@DELETE
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Path("/{bulletinId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteJSONBulletin(@PathParam("bulletinId") String bulletinId,
			@Context SecurityContext securityContext) {
		logger.debug("DELETE JSON bulletin: " + bulletinId);

		try {
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			AvalancheBulletinController.getInstance().deleteBulletin(bulletinId, user.getRole());
			return Response.ok(uri.getAbsolutePathBuilder().path(String.valueOf(bulletinId)).build())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error deleting bulletin - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Path("/submit")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response submitBulletins(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST submit bulletins");

		try {
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region != null && AuthorizationUtil.hasPermissionForRegion(user.getRole(), region)) {
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

	@POST
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Path("/publish")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response publishBulletins(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST publish bulletins");

		try {
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region != null && AuthorizationUtil.hasPermissionForRegion(user.getRole(), region)) {
				DateTime startDate = null;
				DateTime endDate = null;

				if (date != null)
					startDate = DateTime.parse(date, GlobalVariables.parserDateTime);
				else
					throw new AlbinaException("No date!");
				endDate = startDate.plusDays(1);

				DateTime publicationDate = new DateTime();

				AvalancheBulletinController.getInstance().publishBulletins(startDate, endDate, region, publicationDate);
				AvalancheReportController.getInstance().publishReport(startDate, region, user, publicationDate);

				return Response.ok(MediaType.APPLICATION_JSON).build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error publishing bulletins - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkBulletins(@QueryParam("region") String region,
			@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST publish bulletins");

		try {
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region != null && AuthorizationUtil.hasPermissionForRegion(user.getRole(), region)) {
				DateTime startDate = null;
				DateTime endDate = null;

				if (date != null)
					startDate = DateTime.parse(date, GlobalVariables.parserDateTime);
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
