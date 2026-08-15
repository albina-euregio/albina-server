// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.PasskeyRepository;
import eu.albina.controller.UserRepository;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Passkey;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.webauthn.WebAuthnService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.Nullable;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@Controller("/authentication/passkey")
@Tag(name = "authentication")
@Transactional
public class PasskeyService {

	@Inject
	WebAuthnService webAuthnService;

	@Inject
	AuthenticationService authenticationService;

	@Inject
	UserRepository userRepository;

	@Inject
	PasskeyRepository passkeyRepository;

	/** What a passkey management UI needs to show — never the credential ID or public key. */
	@Serdeable
	public record PasskeyInfo(String id, @Nullable String name, Instant createdAt, @Nullable Instant lastUsedAt) {
		static PasskeyInfo of(Passkey p) {
			return new PasskeyInfo(p.getId(), p.getName(), p.getCreatedAt(), p.getLastUsedAt());
		}
	}

	@Serdeable
	public record RegisterFinishRequest(String state, WebAuthnService.RegistrationCredential credential, @Nullable String name) {
	}

	@Serdeable
	public record LoginBeginRequest(@Nullable String username) {
	}

	@Serdeable
	public record LoginFinishRequest(String state, WebAuthnService.AuthenticationCredential credential) {
	}

	@Get
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "List the current user's passkeys")
	public List<PasskeyInfo> listPasskeys(Principal principal) throws AlbinaException {
		User user = userRepository.findByIdOrElseThrow(principal);
		return passkeyRepository.findByOwner(user).stream().map(PasskeyInfo::of).toList();
	}

	@Post("/register/options")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Begin registering a new passkey for the current user")
	public WebAuthnService.RegistrationChallenge beginRegistration(Principal principal) throws AlbinaException {
		User user = userRepository.findByIdOrElseThrow(principal);
		return webAuthnService.beginRegistration(user);
	}

	@Post("/register")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Finish registering a new passkey for the current user")
	public HttpResponse<PasskeyInfo> finishRegistration(Principal principal, @Body RegisterFinishRequest request)
			throws AlbinaException {
		User user = userRepository.findByIdOrElseThrow(principal);
		Passkey passkey = webAuthnService.finishRegistration(user, request.state(), request.credential(), request.name());
		return HttpResponse.created(PasskeyInfo.of(passkey));
	}

	@Delete("/{id}")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete one of the current user's passkeys")
	public HttpResponse<Void> deletePasskey(Principal principal, @PathVariable String id) throws AlbinaException {
		User user = userRepository.findByIdOrElseThrow(principal);
		Passkey passkey = passkeyRepository.findById(id)
			.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No passkey with id: " + id));
		if (!passkey.getOwner().getEmail().equalsIgnoreCase(user.getEmail())) {
			// do not distinguish between an unowned and a nonexistent passkey
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "No passkey with id: " + id);
		}
		passkeyRepository.deleteById(id);
		return HttpResponse.noContent();
	}

	@Post("/login/options")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Begin a passkey login")
	public WebAuthnService.LoginChallenge beginLogin(@Nullable @Body LoginBeginRequest request) {
		return webAuthnService.beginLogin(request != null ? request.username() : null);
	}

	@Post("/login")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Finish a passkey login")
	public AuthenticationService.AuthenticationResponse finishLogin(@Body LoginFinishRequest request) {
		User user = webAuthnService.finishLogin(request.state(), request.credential());
		return authenticationService.issueToken(user);
	}
}
