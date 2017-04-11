package eu.albina.rest;

import java.io.Serializable;
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

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.CountryCode;
import eu.albina.rest.filter.Secured;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;

@Path("/bulletins")
@Api(value = "/bulletins")
public class AvalancheBulletinService {

	private static Logger logger = LoggerFactory.getLogger(AvalancheBulletinService.class);

	@Context
	UriInfo uri;

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJSONBulletins(@QueryParam("country") CountryCode country, @QueryParam("region") String region,
			@QueryParam("from") String from, @QueryParam("until") String until) {
		logger.debug("GET JSON bulletins");

		DateTime startDate = null;
		DateTime endDate = null;

		if (from != null)
			startDate = DateTime.parse(from, GlobalVariables.formatterDateTime);
		if (until != null)
			endDate = DateTime.parse(until, GlobalVariables.formatterDateTime);

		try {
			List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance().getBulletin(1, country, region,
					startDate, endDate);
			JSONObject jsonResult = new JSONObject();
			if (bulletins != null) {
				for (AvalancheBulletin bulletin : bulletins) {
					jsonResult.put(String.valueOf(bulletin.getId()), bulletin.toJSON());
				}
			}
			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@GET
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
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJSONBulletin(String bulletinString, @Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletin");

		JSONObject bulletinJson = new JSONObject(bulletinString);

		JSONObject validationResult = eu.albina.json.JsonValidator.validateAvalancheBulletin(bulletinString);
		if (validationResult.length() == 0) {
			AvalancheBulletin bulletin = new AvalancheBulletin(bulletinJson, securityContext.getUserPrincipal().getName());
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
	@Secured
	@Path("/{bulletinId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateJSONBulletin(@PathParam("bulletinId") String bulletinId, String bulletinString,
			@Context SecurityContext securityContext) {
		logger.debug("PUT JSON bulletin");

		JSONObject bulletinJson = new JSONObject(bulletinString);

		JSONObject validationResult = eu.albina.json.JsonValidator.validateSnowProfile(bulletinString);
		if (validationResult.length() == 0) {
			AvalancheBulletin bulletin = new AvalancheBulletin(bulletinJson, securityContext.getUserPrincipal().getName());
			bulletin.setId(bulletinId);
			try {
				AvalancheBulletinController.getInstance().updateBulletin(bulletin);
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("bulletinId", bulletinId);
				return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(bulletinId)).build())
						.type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
			} catch (AlbinaException e) {
				logger.warn("Error updating bulletin - " + e.getMessage());
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
			}
		} else
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(validationResult.toString()).build();
	}

	@DELETE
	@Secured
	@Path("/{bulletinId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteJSONProfile(@PathParam("bulletinId") String bulletinId) {
		logger.debug("DELETE JSON bulletin: " + bulletinId);
		try {
			AvalancheBulletinController.getInstance().deleteBulletin(bulletinId);
			return Response.ok().type(MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error deleting bulletin - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}
}
