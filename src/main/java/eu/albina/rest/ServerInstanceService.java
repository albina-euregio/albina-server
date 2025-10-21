// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.List;

import eu.albina.controller.ServerInstanceRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;

import eu.albina.util.GlobalVariables;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.Role;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/server")
@Tag(name = "server")
public class ServerInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(ServerInstanceService.class);

	@Inject
	private ServerInstanceRepository serverInstanceRepository;

	@Inject
	private GlobalVariables globalVariables;

	@Put
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update server configuration")
	public HttpResponse<?> updateServerConfiguration(@Body ServerInstance serverInstance) {
		serverInstanceRepository.update(serverInstance);
		return HttpResponse.noContent();
	}

	@Post
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create server configuration")
	public HttpResponse<?> createServerConfiguration(@Body ServerInstance serverInstance) {
		logger.debug("POST JSON server");

		// check if id already exists
		if (serverInstance.getId() == null || !serverInstanceRepository.existsById(serverInstance.getId())) {
			serverInstanceRepository.save(serverInstance);
			return HttpResponse.created(serverInstance);
		} else {
			String msg = "Error creating server instance - Server instance already exists";
			logger.warn(msg);
			return HttpResponse.badRequest().body(msg);
		}
	}

	@Serdeable
	public record PublicLocalServerConfiguration(String name, String apiUrl, String version) {
	}

	@Get("/info")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Get public local server configuration")
	@ApiResponse(description = "public configuration", content = @Content(schema = @Schema(implementation = PublicLocalServerConfiguration.class)))
	public PublicLocalServerConfiguration getPublicLocalServerConfiguration() {
		ServerInstance serverInstance = serverInstanceRepository.findByExternalServerFalse();
		return new PublicLocalServerConfiguration(serverInstance.getName(), serverInstance.getApiUrl(), globalVariables.version);
	}

	@Get
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get local server configuration")
	@ApiResponse(description = "configuration", content = @Content(schema = @Schema(implementation = ServerInstance.class)))
	public HttpResponse<?> getLocalServerConfiguration() {
		logger.debug("GET JSON server");
		try {
			ServerInstance serverInstance = serverInstanceRepository.findByExternalServerFalse();
			return HttpResponse.ok(serverInstance);
		} catch (PersistenceException he) {
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
			List<ServerInstance> externalServerInstances = serverInstanceRepository.getExternalServerInstances();
			return HttpResponse.ok(externalServerInstances);
		} catch (PersistenceException he) {
			logger.warn("Error loading local server configuration", he);
			return HttpResponse.badRequest().body(he.toString());
		}
	}
}
