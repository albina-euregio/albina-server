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
}
