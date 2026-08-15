// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import eu.albina.controller.PasskeyRepository;
import eu.albina.controller.UserRepository;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Passkey;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.util.EcKeys;
import eu.albina.util.GlobalVariables;
import eu.albina.webauthn.AuthenticatorData;
import eu.albina.webauthn.Cbor;
import eu.albina.webauthn.CoseKey;
import eu.albina.webauthn.WebauthnConfig;

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
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Registers and authenticates passkeys, and runs the WebAuthn registration/login ceremonies (<a
 * href="https://developer.mozilla.org/en-US/docs/Web/Security/Authentication/Passkeys">passkeys</a>)
 * behind them. Hand-rolled rather than built on a library such as Yubico's {@code
 * webauthn-server-core}: that library (and its main Java alternatives) pulls in Jackson 2 databind
 * and BouncyCastle, both of which this project deliberately routes around elsewhere (see the
 * Jackson 3 migration and the BouncyCastle exclusions on the PDF dependencies). The JDK alone is
 * enough: {@link Cbor} decodes the small CBOR subset CTAP2 authenticators emit, and EC/RSA/Ed25519
 * signature verification are all native to {@code java.security} (Ed25519 since JDK 15, JEP 339).
 */
@Controller("/authentication/passkey")
@Tag(name = "authentication")
@Transactional
public class PasskeyService {

	private static final Logger logger = LoggerFactory.getLogger(PasskeyService.class);
	private static final Base64.Encoder BASE64URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder BASE64URL_DECODER = Base64.getUrlDecoder();
	private static final long CHALLENGE_TIMEOUT_MILLIS = 60_000;

	@Inject
	AuthenticationService authenticationService;

	@Inject
	UserRepository userRepository;

	@Inject
	PasskeyRepository passkeyRepository;

	@Inject
	GlobalVariables globalVariables;

	@Inject
	ObjectMapper objectMapper;

	private record ChallengeEntry(@Nullable String username, byte[] challenge, Instant createdAt) {
	}

	private final Cache<String, ChallengeEntry> challenges = CacheBuilder.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(5))
		.maximumSize(10_000)
		.build();

	// ---- wire types: these mirror the JSON shapes navigator.credentials.create()/get() expect and produce ----

	@Serdeable
	public record RelyingParty(String id, String name) {
	}

	@Serdeable
	public record UserEntity(String id, String name, String displayName) {
	}

	@Serdeable
	public record PubKeyCredParam(String type, long alg) {
	}

	@Serdeable
	public record AuthenticatorSelection(String residentKey, String userVerification) {
	}

	@Serdeable
	public record CredentialDescriptor(String type, String id) {
	}

	@Serdeable
	public record PublicKeyCredentialCreationOptions(
		String challenge,
		RelyingParty rp,
		UserEntity user,
		List<PubKeyCredParam> pubKeyCredParams,
		AuthenticatorSelection authenticatorSelection,
		String attestation,
		List<CredentialDescriptor> excludeCredentials,
		long timeout
	) {
	}

	@Serdeable
	public record RegistrationChallenge(String state, PublicKeyCredentialCreationOptions publicKey) {
	}

	@Serdeable
	public record PublicKeyCredentialRequestOptions(
		String challenge,
		String rpId,
		List<CredentialDescriptor> allowCredentials,
		String userVerification,
		long timeout
	) {
	}

	@Serdeable
	public record LoginChallenge(String state, PublicKeyCredentialRequestOptions publicKey) {
	}

	@Serdeable
	public record AttestationResponse(String clientDataJSON, String attestationObject) {
	}

	@Serdeable
	public record RegistrationCredential(String id, String type, AttestationResponse response) {
	}

	@Serdeable
	public record AssertionResponse(String clientDataJSON, String authenticatorData, String signature,
			@Nullable String userHandle) {
	}

	@Serdeable
	public record AuthenticationCredential(String id, String type, AssertionResponse response) {
	}

	@Serdeable
	@JsonIgnoreProperties(ignoreUnknown = true)
	record ClientData(String type, String challenge, String origin) {
		static ClientData parse(ObjectMapper objectMapper, String clientDataJsonBase64url) {
			byte[] bytes = BASE64URL_DECODER.decode(clientDataJsonBase64url);
			try {
				return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8), ClientData.class);
			} catch (IOException e) {
				throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid clientDataJSON");
			}
		}
	}

	/** What a passkey management UI needs to show — never the credential ID or public key. */
	@Serdeable
	public record PasskeyInfo(String id, @Nullable String name, Instant createdAt, @Nullable Instant lastUsedAt) {
		static PasskeyInfo of(Passkey p) {
			return new PasskeyInfo(p.getId(), p.getName(), p.getCreatedAt(), p.getLastUsedAt());
		}
	}

	@Serdeable
	public record RegisterFinishRequest(String state, RegistrationCredential credential, @Nullable String name) {
	}

	@Serdeable
	public record LoginBeginRequest(@Nullable String username) {
	}

	@Serdeable
	public record LoginFinishRequest(String state, AuthenticationCredential credential) {
	}

	// ---- REST endpoints ----

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
	public RegistrationChallenge beginRegistration(Principal principal) throws AlbinaException {
		User user = userRepository.findByIdOrElseThrow(principal);
		WebauthnConfig webauthn = globalVariables.getWebauthnConfig();
		byte[] challenge = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
		String state = UUID.randomUUID().toString();
		challenges.put(state, new ChallengeEntry(user.getEmail(), challenge, Instant.now()));

		List<CredentialDescriptor> exclude = passkeyRepository.findByOwner(user).stream()
			.map(p -> new CredentialDescriptor("public-key", p.getCredentialId()))
			.toList();

		PublicKeyCredentialCreationOptions options = new PublicKeyCredentialCreationOptions(
			BASE64URL_ENCODER.encodeToString(challenge),
			new RelyingParty(webauthn.rpId(), webauthn.rpName()),
			new UserEntity(BASE64URL_ENCODER.encodeToString(user.getEmail().getBytes(StandardCharsets.UTF_8)), user.getEmail(),
				user.getName() != null && !user.getName().isBlank() ? user.getName() : user.getEmail()),
			List.of(new PubKeyCredParam("public-key", -7), new PubKeyCredParam("public-key", -257),
				new PubKeyCredParam("public-key", -8)),
			new AuthenticatorSelection("required", "preferred"),
			"none",
			exclude,
			CHALLENGE_TIMEOUT_MILLIS
		);
		return new RegistrationChallenge(state, options);
	}

	@Post("/register")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Finish registering a new passkey for the current user")
	public HttpResponse<PasskeyInfo> finishRegistration(Principal principal, @Body RegisterFinishRequest request)
			throws AlbinaException {
		User user = userRepository.findByIdOrElseThrow(principal);
		RegistrationCredential credential = request.credential();
		ChallengeEntry entry = consumeChallenge(request.state(), user.getEmail());
		ClientData clientData = ClientData.parse(objectMapper, credential.response().clientDataJSON());
		if (!"webauthn.create".equals(clientData.type())) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected clientData type: " + clientData.type());
		}
		WebauthnConfig webauthn = globalVariables.getWebauthnConfig();
		webauthn.verifyChallengeAndOrigin(entry.challenge(), BASE64URL_DECODER.decode(clientData.challenge()), clientData.origin());

		byte[] attestationObjectBytes = BASE64URL_DECODER.decode(credential.response().attestationObject());
		Map<Object, Object> attestationObject = Cbor.asMap(Cbor.decode(attestationObjectBytes));
		String fmt = (String) attestationObject.get("fmt");
		byte[] authData = (byte[]) attestationObject.get("authData");
		AuthenticatorData parsed = AuthenticatorData.parse(authData);
		webauthn.verifyRpIdHash(parsed.rpIdHash());
		if (!parsed.userPresent()) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "User presence flag not set");
		}
		if (parsed.credentialPublicKey() == null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Authenticator did not return a public key");
		}
		logger.debug("Registering passkey (attestation format '{}') for {}", fmt, user.getEmail());

		String credentialId = BASE64URL_ENCODER.encodeToString(parsed.credentialId());
		if (passkeyRepository.findByCredentialId(credentialId).isPresent()) {
			throw new HttpStatusException(HttpStatus.CONFLICT, "This passkey is already registered");
		}

		Passkey passkey = new Passkey();
		passkey.setOwner(user);
		passkey.setCredentialId(credentialId);
		passkey.setPublicKeyCose(Base64.getEncoder().encodeToString(parsed.credentialPublicKeyRaw()));
		passkey.setSignCount(parsed.signCount());
		passkey.setName(request.name() != null && !request.name().isBlank() ? request.name() : "Passkey");
		passkey.setLastUsedAt(Instant.now());
		passkey = passkeyRepository.save(passkey);
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
	public LoginChallenge beginLogin(@Nullable @Body LoginBeginRequest request) {
		byte[] challenge = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
		String state = UUID.randomUUID().toString();
		String normalizedUsername = request != null && request.username() != null ? request.username().toLowerCase() : null;

		List<CredentialDescriptor> allow = List.of();
		if (normalizedUsername != null) {
			// an unknown username still yields an (empty) allow list rather than leaking account existence
			allow = userRepository.findById(normalizedUsername)
				.map(passkeyRepository::findByOwner)
				.orElseGet(List::of)
				.stream()
				.map(p -> new CredentialDescriptor("public-key", p.getCredentialId()))
				.toList();
		}

		challenges.put(state, new ChallengeEntry(normalizedUsername, challenge, Instant.now()));
		PublicKeyCredentialRequestOptions options = new PublicKeyCredentialRequestOptions(
			BASE64URL_ENCODER.encodeToString(challenge), globalVariables.getWebauthnConfig().rpId(), allow, "preferred", CHALLENGE_TIMEOUT_MILLIS);
		return new LoginChallenge(state, options);
	}

	@Post("/login")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Finish a passkey login")
	public AuthenticationService.AuthenticationResponse finishLogin(@Body LoginFinishRequest request) {
		AuthenticationCredential credential = request.credential();
		ChallengeEntry entry = consumeChallenge(request.state(), null);
		ClientData clientData = ClientData.parse(objectMapper, credential.response().clientDataJSON());
		if (!"webauthn.get".equals(clientData.type())) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected clientData type: " + clientData.type());
		}
		WebauthnConfig webauthn = globalVariables.getWebauthnConfig();
		webauthn.verifyChallengeAndOrigin(entry.challenge(), BASE64URL_DECODER.decode(clientData.challenge()), clientData.origin());

		Passkey passkey = passkeyRepository.findByCredentialId(credential.id())
			.orElseThrow(() -> new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unknown passkey"));
		User user = passkey.getOwner();
		if (user.isDeleted()) {
			// do not distinguish a deleted account's passkey from an unknown one
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unknown passkey");
		}
		if (entry.username() != null && !user.getEmail().equalsIgnoreCase(entry.username())) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Passkey does not belong to the requested account");
		}

		byte[] authenticatorDataBytes = BASE64URL_DECODER.decode(credential.response().authenticatorData());
		AuthenticatorData authenticatorData = AuthenticatorData.parse(authenticatorDataBytes);
		webauthn.verifyRpIdHash(authenticatorData.rpIdHash());
		if (!authenticatorData.userPresent()) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "User presence flag not set");
		}

		CoseKey coseKey = CoseKey.parse(Cbor.asMap(Cbor.decode(Base64.getDecoder().decode(passkey.getPublicKeyCose()))));
		byte[] clientDataHash = WebauthnConfig.sha256(BASE64URL_DECODER.decode(credential.response().clientDataJSON()));
		byte[] signedData = EcKeys.concat(authenticatorDataBytes, clientDataHash);
		byte[] signature = BASE64URL_DECODER.decode(credential.response().signature());
		try {
			if (!coseKey.verifySignature(signedData, signature)) {
				throw new GeneralSecurityException();
			}
		} catch (GeneralSecurityException e) {
			logger.warn("Failed to verify passkey signature", e);
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Invalid passkey signature");
		}

		long newSignCount = authenticatorData.signCount();
		long storedSignCount = passkey.getSignCount();
		// per spec: an authenticator with a counter must strictly increase it; one without must always report 0
		if ((newSignCount != 0 || storedSignCount != 0) && newSignCount <= storedSignCount) {
			logger.warn("Passkey {} sign count did not increase ({} -> {}), possible cloned authenticator",
				passkey.getId(), storedSignCount, newSignCount);
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Passkey rejected (sign count did not increase)");
		}
		passkey.setSignCount(newSignCount);
		passkey.setLastUsedAt(Instant.now());
		passkeyRepository.update(passkey);

		return authenticationService.issueToken(user);
	}

	// ---- shared verification helpers ----

	private ChallengeEntry consumeChallenge(String state, @Nullable String expectedUsername) {
		ChallengeEntry entry = challenges.getIfPresent(state);
		challenges.invalidate(state);
		if (entry == null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unknown or expired challenge");
		}
		if (expectedUsername != null && !expectedUsername.equalsIgnoreCase(entry.username())) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Challenge does not belong to this user");
		}
		return entry;
	}

}
