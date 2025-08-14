// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.albina.util.JsonUtil;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import eu.albina.controller.RegionController;
import eu.albina.controller.ServerInstanceController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/regions")
@Tag(name = "regions")
public class RegionService {

	private static final Logger logger = LoggerFactory.getLogger(RegionService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.SUPERADMIN, Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get all regions")
	@ApiResponse(description = "regions", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Region.class))))
	public Response getRegions(@Context SecurityContext securityContext) {
		logger.debug("GET JSON regions");

		// TODO check if user has ADMIN rights for this region

		try {
			List<Region> regions = RegionController.getInstance().getRegions();
			return Response.ok(regions).build();
		} catch (HibernateException he) {
			logger.warn("Error loading regions", he);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(he.toString()).build();
		}
	}

	@GET
	@Path("/region")
	@Secured({ Role.SUPERADMIN, Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get region for ID")
	@ApiResponse(description = "region", content = @Content(schema = @Schema(implementation = Region.class)))
	public Response getRegion(@QueryParam("region") String regionId, @Context SecurityContext securityContext) {
		logger.debug("GET JSON region");

		// TODO check if user has ADMIN rights for this region

		try {
			Region region = RegionController.getInstance().getRegion(regionId);
			return Response.ok(region).build();
		} catch (HibernateException he) {
			logger.warn("Error loading region", he);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(he.toString()).build();
		}
	}

	@PUT
	@Secured({ Role.SUPERADMIN, Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update region")
	public Response updateRegion(
		@Parameter(schema = @Schema(implementation = Region.class)) String regionString,
		@Context SecurityContext securityContext) {
		logger.debug("PUT JSON region");

		// TODO check if user has ADMIN rights for this region (UserRegionRoleLinks.class)

		try {
			Region region = new Region(regionString, RegionController.getInstance()::getRegion);

			// check if region id already exists
			if (RegionController.getInstance().regionExists(region.getId())) {
				Region existing =  RegionController.getInstance().getRegion(region.getId());
				// Avoid overwriting fields that are not contained in the JSON object sent by the frontend.
				// This happens whenever new fields are added to the backend but not yet to the frontend.
				JsonUtil.ALBINA_OBJECT_MAPPER.readerForUpdating(existing).readValue(regionString);
				RegionController.getInstance().updateRegion(existing);
				return Response.ok(existing.toJSON()).build();
			} else {
				logger.warn("Error updating region - Region does not exist");
				JSONObject json = new JSONObject();
				json.append("message", "Error updating region - Region does not exists");
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(json).build();
			}
		} catch (HibernateException e) {
			logger.warn("Error updating region", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		} catch (JsonProcessingException e) {
			logger.warn("Error deserializing region", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@POST
	@Secured({ Role.SUPERADMIN, Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create region")
	public Response createRegion(
		@Parameter(schema = @Schema(implementation = Region.class)) String regionString,
		@Context SecurityContext securityContext) {
		logger.debug("POST JSON region");
		try {
			Region region = new Region(regionString, RegionController.getInstance()::getRegion);
			region.setServerInstance(ServerInstanceController.getInstance().getLocalServerInstance());

			// check if id already exists
			if (!RegionController.getInstance().regionExists(region.getId())) {
				RegionController.getInstance().createRegion(region);
				JSONObject jsonObject = new JSONObject();
				return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
						.entity(jsonObject.toString()).build();
			} else {
				logger.warn("Error creating region - Region already exists");
				JSONObject json = new JSONObject();
				json.append("message", "Error creating region - Region already exists");
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(json).build();
			}
		} catch (JsonProcessingException e) {
			logger.warn("Error deserializing region", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/locked")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLockedRegions(@QueryParam("region") String region, @Context SecurityContext securityContext) {
		logger.debug("GET JSON locked regions");

		JSONArray json = new JSONArray();
		for (Instant date : RegionController.getInstance().getLockedRegions(region))
			json.put(DateTimeFormatter.ISO_INSTANT.format(date));
		return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
	}
}
