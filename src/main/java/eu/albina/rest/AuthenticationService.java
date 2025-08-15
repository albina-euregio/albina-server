// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import eu.albina.controller.AuthenticationController;
import eu.albina.controller.UserController;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/authentication")
@Tag(name = "authentication")
@SecurityScheme(
	name = AuthenticationService.SECURITY_SCHEME,
	description="Obtained from POST /authentication",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT")
public class AuthenticationService {

	public static final String SECURITY_SCHEME = "authentication";

	static class Credentials {
		public String username;
		public String password;
	}

	static class Token {
		public String access_token;
	}

	static class AuthenticationResponse {
		public User user;
		public Set<Region> regions;
		public String access_token;
	}

	static class Username {
		public String username;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Authenticate user")
	@ApiResponse(description = "token", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
	public Response login(Credentials credentials) {
		String username = credentials.username.toLowerCase();
		String password = credentials.password;

		try {
			UserController.getInstance().authenticate(username, password);
			String accessToken = AuthenticationController.getInstance().issueAccessToken(username);

			User user = UserController.getInstance().getUser(username);
			AuthenticationResponse result = new AuthenticationResponse();
			result.user = user;
			result.regions = user.getRegions();
			result.access_token = accessToken;

			return Response.ok(result, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Refresh token")
	@ApiResponse(description = "token", content = @Content(schema = @Schema(implementation = Token.class)))
	public Response refreshToken(@Context SecurityContext securityContext) {
		try {
			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();

			String accessToken = AuthenticationController.getInstance().issueAccessToken(username);

			Token jsonResult = new Token();
			jsonResult.access_token = accessToken;
			return Response.ok(jsonResult, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Test access token")
	@ApiResponse(description = "token", content = @Content(schema = @Schema(implementation = Username.class)))
	@Path("/test")
	public Response testAuth(@Context SecurityContext securityContext) {
		try {
			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();

			Username jsonResult = new Username();
			jsonResult.username = username;
			return Response.ok(jsonResult, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}
