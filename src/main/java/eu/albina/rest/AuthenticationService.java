// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.github.openjson.JSONObject;

import eu.albina.controller.AuthenticationController;
import eu.albina.controller.UserController;
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

	static class UserAndToken extends User {
		public String access_token;
	}

	static class Username {
		public String username;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Authenticate user")
	@ApiResponse(description = "token", content = @Content(schema = @Schema(implementation = UserAndToken.class)))
	public Response login(Credentials credentials) {
		String username = credentials.username.toLowerCase();
		String password = credentials.password;

		try {
			UserController.getInstance().authenticate(username, password);
			String accessToken = AuthenticationController.getInstance().issueAccessToken(username);

			User user = UserController.getInstance().getUser(username);
			JSONObject jsonResult = user.toJSON();
			jsonResult.put("access_token", accessToken);

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
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

			JSONObject jsonResult = new JSONObject();
			jsonResult.put("access_token", accessToken);

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
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

			JSONObject jsonResult = new JSONObject();
			jsonResult.put("username", username);

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}
