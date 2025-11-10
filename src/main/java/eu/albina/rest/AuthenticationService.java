// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import eu.albina.controller.UserRepository;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.JsonUtil;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.security.annotation.Secured;

import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.generator.AccessTokenConfigurationProperties;
import io.micronaut.security.token.render.AccessRefreshToken;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
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
	AccessTokenConfigurationProperties accessTokenConfigurationProperties;

	@Inject
	private UserRepository userRepository;

	@Serdeable
	public record Credentials(String username, String password) {
	}

	@Serdeable
	public record Token(String access_token) {
	}

	@Serdeable
	public record AuthenticationResponse(User user, Collection<Region> regions, String access_token) {
	}

	@Serdeable
	public record Username(String username) {
	}

	@Post
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Authenticate user")
	@JsonView(JsonUtil.Views.Internal.class)
	public AuthenticationResponse login(@Body Credentials credentials) {
		String username = credentials.username.toLowerCase();
		String password = credentials.password;

		try {
			userRepository.authenticate(username, password);
			User user = userRepository.findById(username).orElseThrow();
			List<String> roles = user.getRoles().stream().map(Role::toString).toList();
			Authentication authentication = Authentication.build(username, roles);
			AccessRefreshToken token = tokenGenerator.generate(authentication).orElseThrow();

			return new AuthenticationResponse(user, user.getRegions(), token.getAccessToken());
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
	}

	@Get("/test")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Test access token")
	public Username testAuth(Principal principal) {
		String username = principal.getName();
		return new Username(username);
	}

	@Scheduled(initialDelay = "1s", fixedRate = "1m")
	public void updateExpiration() {
		ZonedDateTime now = ZonedDateTime.now(AlbinaUtil.localZone());
		long seconds = ChronoUnit.SECONDS.between(
			now,
			now.plusDays(1).withHour(3) // tomorrow at 03:00
		);
		accessTokenConfigurationProperties.setExpiration(((int) seconds));
	}
}
