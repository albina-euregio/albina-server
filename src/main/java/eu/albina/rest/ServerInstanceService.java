// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import com.google.common.base.MoreObjects;
import eu.albina.controller.RegionController;

import eu.albina.controller.publication.BlogController;
import eu.albina.controller.publication.BlogItem;
import eu.albina.controller.publication.TelegramController;
import eu.albina.controller.publication.WhatsAppController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.model.publication.TelegramConfiguration;
import eu.albina.model.publication.WhatsAppConfiguration;
import eu.albina.util.GlobalVariables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		ServerInstance serverInstance) {
		try {
			ServerInstanceController.getInstance().updateServerInstance(serverInstance);
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
		ServerInstance serverInstance,
		@Context SecurityContext securityContext) {
		logger.debug("POST JSON server");

		// check if id already exists
		if (serverInstance.getId() == null || !ServerInstanceController.getInstance().serverInstanceExists(serverInstance.getId())) {
			ServerInstanceController.getInstance().createServerInstance(serverInstance);
			return Response.created(uri.getAbsolutePathBuilder().path("").build()).build();
		} else {
			String msg = "Error creating server instance - Server instance already exists";
			logger.warn(msg);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(msg).build();
		}
	}

	static class PublicLocalServerConfiguration {
		public final String name;
		public final String apiUrl;
		public final String version;

		public PublicLocalServerConfiguration(String name, String apiUrl, String version) {
			this.name = name;
			this.apiUrl = apiUrl;
			this.version = version;
		}
	}

	@GET
	@Path("/info")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get public local server configuration")
	@ApiResponse(description = "public configuration", content = @Content(schema = @Schema(implementation = PublicLocalServerConfiguration.class)))
	public Response getPublicLocalServerConfiguration() {
		try {
			ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
			PublicLocalServerConfiguration r = new PublicLocalServerConfiguration(serverInstance.getName(), serverInstance.getApiUrl(), GlobalVariables.version);
            return Response.ok(r, MediaType.APPLICATION_JSON).build();
		} catch (HibernateException he) {
			logger.warn("Error loading local server configuration", he);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(he.toString()).build();
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
			return Response.ok(serverInstance, MediaType.APPLICATION_JSON).build();
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
			return Response.ok(externalServerInstances, MediaType.APPLICATION_JSON).build();
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
		logger.info("Testing WhatsAppController");
		WhatsAppConfiguration whatsAppConfiguration = WhatsAppController.getConfiguration(region, language).orElseThrow();
		Response whapiResponse = WhatsAppController.getHealth(whatsAppConfiguration);
		logger.info("Testing Blog");
		BlogConfiguration config = BlogController.getConfiguration(region, language);
		BlogItem latestBlogPost = BlogController.getLatestBlogPost(config);
		return Map.of(
			"region", MoreObjects.firstNonNull(region, ""),
			"telegram", MoreObjects.firstNonNull(me, ""),
			"whatsapp", MoreObjects.firstNonNull(whapiResponse, ""),
			"blog", MoreObjects.firstNonNull(latestBlogPost, "")
		);
	}
}
