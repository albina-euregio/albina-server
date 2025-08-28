// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.List;
import java.util.Map;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;

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
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
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
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/server")
@Tag(name = "server")
public class ServerInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(ServerInstanceService.class);

	@Put
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update server configuration")
	public HttpResponse<?> updateServerConfiguration(@Body ServerInstance serverInstance) {
		try {
			ServerInstanceController.getInstance().updateServerInstance(serverInstance);
			return HttpResponse.noContent();
		} catch (AlbinaException e) {
			logger.warn("Error updating local server configuration", e);
			return HttpResponse.badRequest();
		}
	}

	@Post
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create server configuration")
	public HttpResponse<?> createServerConfiguration(@Body ServerInstance serverInstance) {
		logger.debug("POST JSON server");

		// check if id already exists
		if (serverInstance.getId() == null || !ServerInstanceController.getInstance().serverInstanceExists(serverInstance.getId())) {
			ServerInstanceController.getInstance().createServerInstance(serverInstance);
			return HttpResponse.created(serverInstance);
		} else {
			String msg = "Error creating server instance - Server instance already exists";
			logger.warn(msg);
			return HttpResponse.badRequest().body(msg);
		}
	}

	@Serdeable
	public static class PublicLocalServerConfiguration {
		public final String name;
		public final String apiUrl;
		public final String version;

		public PublicLocalServerConfiguration(String name, String apiUrl, String version) {
			this.name = name;
			this.apiUrl = apiUrl;
			this.version = version;
		}

		public String getName() {
			return name;
		}

		public String getApiUrl() {
			return apiUrl;
		}

		public String getVersion() {
			return version;
		}
	}

	@Get("/info")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Get public local server configuration")
	@ApiResponse(description = "public configuration", content = @Content(schema = @Schema(implementation = PublicLocalServerConfiguration.class)))
	public PublicLocalServerConfiguration getPublicLocalServerConfiguration() {
		ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
		return new PublicLocalServerConfiguration(serverInstance.getName(), serverInstance.getApiUrl(), GlobalVariables.version);
	}

	@Get
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get local server configuration")
	@ApiResponse(description = "configuration", content = @Content(schema = @Schema(implementation = ServerInstance.class)))
	public HttpResponse<?> getLocalServerConfiguration() {
		logger.debug("GET JSON server");
		try {
			ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
			return HttpResponse.ok(serverInstance);
		} catch (HibernateException he) {
			logger.warn("Error loading local server configuration", he);
			return HttpResponse.badRequest().body(he.toString());
		}
	}

	@Get("/external")
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get external server configurations")
	@ApiResponse(description = "configuration", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServerInstance.class))))
	public HttpResponse<?> getExternalServerConfigurations() {
		logger.debug("GET JSON external servers");
		try {
			List<ServerInstance> externalServerInstances = ServerInstanceController.getInstance().getExternalServerInstances();
			return HttpResponse.ok(externalServerInstances);
		} catch (HibernateException he) {
			logger.warn("Error loading local server configuration", he);
			return HttpResponse.badRequest().body(he.toString());
		}
	}

	@Get("/health")
	@Secured({Role.Str.SUPERADMIN, Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Perform health checks")
	public Map<String, Object> getHealth(
		@QueryValue("region") String regionId,
		@QueryValue("lang") LanguageCode language
	) throws Exception {
		Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
		logger.info("Testing TelegramController");
		TelegramConfiguration telegramConfig = TelegramController.getConfiguration(region, language).orElseThrow();
		Object me = TelegramController.getMe(telegramConfig);
		logger.info("Testing WhatsAppController");
		WhatsAppConfiguration whatsAppConfiguration = WhatsAppController.getConfiguration(region, language).orElseThrow();
		Object whapiResponse = WhatsAppController.getHealth(whatsAppConfiguration);
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
