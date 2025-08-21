// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.util.Set;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;

import eu.albina.controller.AuthenticationController;
import eu.albina.controller.UserController;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/authentication")
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

	@Post
	@Operation(summary = "Authenticate user")
	@ApiResponse(description = "token", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
	public HttpResponse<?> login(Credentials credentials) {
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

			return HttpResponse.ok(result);
		} catch (Exception e) {
			return HttpResponse.unauthorized();
		}
	}

	@Get
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Refresh token")
	@ApiResponse(description = "token", content = @Content(schema = @Schema(implementation = Token.class)))
	public HttpResponse<?> refreshToken(Principal principal) {
		try {
			String username = principal.getName();

			String accessToken = AuthenticationController.getInstance().issueAccessToken(username);

			Token jsonResult = new Token();
			jsonResult.access_token = accessToken;
			return HttpResponse.ok(jsonResult);
		} catch (Exception e) {
			return HttpResponse.unauthorized();
		}
	}

	@Get("/test")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Test access token")
	@ApiResponse(description = "token", content = @Content(schema = @Schema(implementation = Username.class)))
	public HttpResponse<?> testAuth(Principal principal) {
		try {
			String username = principal.getName();

			Username jsonResult = new Username();
			jsonResult.username = username;
			return HttpResponse.ok(jsonResult);
		} catch (Exception e) {
			return HttpResponse.unauthorized();
		}
	}
}
