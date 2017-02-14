package org.avalanches.ais.rest;

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
import javax.ws.rs.core.UriInfo;

import org.avalanches.ais.controller.SnowProfileController;
import org.avalanches.ais.exception.AvalancheInformationSystemException;
import org.avalanches.ais.model.SnowProfile;
import org.avalanches.ais.model.enumerations.Aspect;
import org.avalanches.ais.model.enumerations.CountryCode;
import org.avalanches.ais.util.GlobalVariables;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;

@Path("/profiles")
@Api(value = "/profiles")
public class SnowProfileService {

	private static Logger logger = LoggerFactory.getLogger(SnowProfileService.class);

	@Context
	UriInfo uri;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getJSONProfiles(@QueryParam("country") CountryCode country, @QueryParam("region") String region,
			@QueryParam("subregion") String subregion, @QueryParam("from") String from, @QueryParam("to") String to,
			@QueryParam("above") int above, @QueryParam("below") int below,
			@QueryParam("aspects") List<Aspect> aspects) {
		logger.debug("GET JSON profiles");

		DateTime startDate = null;
		DateTime endDate = null;

		if (from != null)
			startDate = DateTime.parse(from, GlobalVariables.formatterDateTime);
		if (to != null)
			endDate = DateTime.parse(to, GlobalVariables.formatterDateTime);

		try {
			List<SnowProfile> profiles = SnowProfileController.getInstance().getSnowProfiles(1, country, region,
					subregion, startDate, endDate, above, below, aspects);
			JSONObject jsonResult = new JSONObject();
			if (profiles != null) {
				for (SnowProfile snowProfile : profiles) {
					jsonResult.put(String.valueOf(snowProfile.getId()), snowProfile.toSmallJSON());
				}
			}
			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AvalancheInformationSystemException e) {
			logger.warn("Error loading profiles - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@GET
	@Path("/{profileId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getJSONProfile(@PathParam("profileId") String profileId) {
		logger.debug("GET JSON profile: " + profileId);
		try {
			SnowProfile profile = SnowProfileController.getInstance().getSnowProfile(profileId);
			if (profile == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("message", "Profile not found for ID: " + profileId);
				return Response.status(Response.Status.NOT_FOUND).entity(jsonObject.toString()).build();
			}
			String json = profile.toJSON().toString();
			return Response.ok(json, MediaType.APPLICATION_JSON).build();
		} catch (AvalancheInformationSystemException e) {
			logger.warn("Error loading profile: " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createJSONProfile(String profileString) {
		logger.debug("POST JSON profile");

		JSONObject profileJson = new JSONObject(profileString);

		JSONObject validationResult = org.avalanches.ais.json.JsonValidator.validateSnowProfile(profileString);
		if (validationResult.length() == 0) {
			SnowProfile profile = new SnowProfile(profileJson);
			try {
				Serializable profileId = SnowProfileController.getInstance().saveSnowProfile(profile);
				if (profileId == null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.append("message", "Profile not saved!");
					return Response.status(400).type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("profileId", profileId);
					return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(profileId)).build())
							.type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
				}
			} catch (AvalancheInformationSystemException e) {
				logger.warn("Error creating profile - " + e.getMessage());
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
			}
		} else
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(validationResult.toString()).build();
	}

	@PUT
	@Path("/{profileId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateJSONProfile(@PathParam("profileId") String profileId, String profileString) {
		logger.debug("PUT JSON profile");

		JSONObject profileJson = new JSONObject(profileString);

		JSONObject validationResult = org.avalanches.ais.json.JsonValidator.validateSnowProfile(profileString);
		if (validationResult.length() == 0) {
			SnowProfile profile = new SnowProfile(profileJson);
			profile.setId(profileId);
			try {
				SnowProfileController.getInstance().updateSnowProfile(profile);
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("profileId", profileId);
				return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(profileId)).build())
						.type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
			} catch (AvalancheInformationSystemException e) {
				logger.warn("Error updating profile - " + e.getMessage());
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
			}
		} else
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(validationResult.toString()).build();
	}

	@DELETE
	@Path("/{profileId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteJSONProfile(@PathParam("profileId") String profileId) {
		logger.debug("DELETE JSON profile: " + profileId);
		try {
			SnowProfileController.getInstance().deleteSnowProfile(profileId);
			return Response.ok().build();
		} catch (AvalancheInformationSystemException e) {
			logger.warn("Error deleting profile - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

}
