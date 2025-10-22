// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.List;

import eu.albina.controller.ServerInstanceRepository;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
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

import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.Role;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/server")
@Tag(name = "server")
public class ServerInstanceService {

	@Inject
	private ServerInstanceRepository serverInstanceRepository;

	@Inject
	private GlobalVariables globalVariables;


	@Post
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create or update server configuration")
	public ServerInstance saveServerConfiguration(@Body ServerInstance serverInstance) {
		if (serverInstance.getId() == null || !serverInstanceRepository.existsById(serverInstance.getId())) {
			serverInstanceRepository.save(serverInstance);
		} else {
			serverInstanceRepository.update(serverInstance);
		}
		return serverInstance;
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
	public ServerInstance getLocalServerConfiguration() {
		return serverInstanceRepository.findByExternalServerFalse();
	}

	@Get("/external")
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get external server configurations")
	@ApiResponse(description = "configuration", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServerInstance.class))))
	public List<ServerInstance> getExternalServerConfigurations() {
		return serverInstanceRepository.getExternalServerInstances();
	}
}
