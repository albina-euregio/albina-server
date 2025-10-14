// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import eu.albina.controller.UserRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;

import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.render.AccessRefreshToken;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

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

	@Inject
	AccessRefreshTokenGenerator tokenGenerator;

	@Inject
	private UserRepository userRepository;

	@Serdeable
	public static class Credentials {
		public String username;
		public String password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	@Serdeable
	public static class Token {
		public String access_token;
	}

	@Serdeable
	public static class AuthenticationResponse {
		public User user;
		public Collection<Region> regions;
		public String access_token;

		public User getUser() {
			return user;
		}

		public Collection<Region> getRegions() {
			return regions;
		}

		public String getAccess_token() {
			return access_token;
		}
	}

	@Serdeable
	public static class Username {
		public String username;
	}

	@Post
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Authenticate user")
	@ApiResponse(description = "token", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
	public HttpResponse<?> login(@Body Credentials credentials) {
		String username = credentials.username.toLowerCase();
		String password = credentials.password;

		try {
			userRepository.authenticate(username, password);
			User user = userRepository.findById(username).orElseThrow();
			List<String> roles = user.getRoles().stream().map(Role::toString).toList();
			Authentication authentication = Authentication.build(username, roles);
			AccessRefreshToken token = tokenGenerator.generate(authentication).orElseThrow();

			AuthenticationResponse result = new AuthenticationResponse();
			result.user = user;
			result.regions = user.getRegions();
			result.access_token = token.getAccessToken();

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
		return HttpResponse.serverError();
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
