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

import java.util.List;
import java.util.Map;

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

import com.google.common.base.MoreObjects;
import eu.albina.controller.RegionController;

import eu.albina.controller.publication.BlogController;
import eu.albina.controller.publication.BlogItem;
import eu.albina.controller.publication.TelegramController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.model.publication.TelegramConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import eu.albina.controller.ServerInstanceController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/server")
@Tag(name = "server")
public class ServerInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(ServerInstanceService.class);

	@Context
	UriInfo uri;

	@PUT
	@Secured({ Role.SUPERADMIN, Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update server configuration")
	public Response updateServerConfiguration(
		@Parameter(schema = @Schema(implementation = ServerInstance.class)) String serverInstance) {
		try {
			JSONObject serverInstanceJson = new JSONObject(serverInstance);
			ServerInstanceController.getInstance().updateServerInstance(new ServerInstance(serverInstanceJson, RegionController.getInstance()::getRegion));
			return Response.ok().build();
		} catch (AlbinaException e) {
			logger.warn("Error updating local server configuration", e);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Secured({ Role.SUPERADMIN, Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create server configuration")
	public Response createServerConfiguration(
		@Parameter(schema = @Schema(implementation = ServerInstance.class)) String serverString,
		@Context SecurityContext securityContext) {
		logger.debug("POST JSON server");
		JSONObject serverJson = new JSONObject(serverString);
		ServerInstance serverInstance = new ServerInstance(serverJson, RegionController.getInstance()::getRegion);

		// check if id already exists
		if (serverInstance.getId() == null || !ServerInstanceController.getInstance().serverInstanceExists(serverInstance.getId())) {
			ServerInstanceController.getInstance().createServerInstance(serverInstance);
			JSONObject jsonObject = new JSONObject();
			return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
					.entity(jsonObject.toString()).build();
		} else {
			logger.warn("Error creating server instance - Server instance already exists");
			JSONObject json = new JSONObject();
			json.append("message", "Error creating server instance - Server instance already exists");
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(json).build();
		}
	}

	@GET
	@Secured({ Role.SUPERADMIN, Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get local server configuration")
	@ApiResponse(description = "configuration", content = @Content(schema = @Schema(implementation = ServerInstance.class)))
	public Response getLocalServerConfiguration() {
		logger.debug("GET JSON server");
		try {
			ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
			JSONObject json = serverInstance.toJSON();
			String str = json.toString();
			return Response.ok(str, MediaType.APPLICATION_JSON).build();
		} catch (HibernateException he) {
			logger.warn("Error loading local server configuration", he);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(he.toString()).build();
		}
	}

	@GET
	@Secured({ Role.SUPERADMIN, Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/external")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get external server configurations")
	@ApiResponse(description = "configuration", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServerInstance.class))))
	public Response getExternalServerConfigurations() {
		logger.debug("GET JSON external servers");
		try {
			List<ServerInstance> externalServerInstances = ServerInstanceController.getInstance().getExternalServerInstances();
			if (externalServerInstances != null) {
				JSONArray jsonResult = new JSONArray();
				for (ServerInstance serverInstance : externalServerInstances)
					jsonResult.put(serverInstance.toJSON());
				return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
			} else {
				return Response.ok("", MediaType.APPLICATION_JSON).build();
			}
		} catch (HibernateException he) {
			logger.warn("Error loading local server configuration", he);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(he.toString()).build();
		}
	}

	@GET
	@Secured({Role.SUPERADMIN, Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/health")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Perform health checks")
	public Map<String, Object> getHealth(
		@QueryParam("region") String regionId,
		@QueryParam("lang") LanguageCode language
	) throws Exception {
		Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
		logger.info("Testing TelegramController");
		TelegramConfiguration telegramConfig = TelegramController.getConfiguration(region, language).orElseThrow();
		Response me = TelegramController.getMe(telegramConfig);
		logger.info("Testing Blog");
		BlogConfiguration config = BlogController.getConfiguration(region, language).orElseThrow();
		BlogItem latestBlogPost = BlogController.getLatestBlogPost(config);
		return Map.of(
			"region", MoreObjects.firstNonNull(region, ""),
			"telegram", MoreObjects.firstNonNull(me, ""),
			"blog", MoreObjects.firstNonNull(latestBlogPost, "")
		);
	}
}
