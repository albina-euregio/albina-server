// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.webauthn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import eu.albina.controller.PasskeyRepository;
import eu.albina.controller.UserRepository;
import eu.albina.model.Passkey;
import eu.albina.model.User;
import eu.albina.util.EcKeys;
import eu.albina.util.GlobalVariables;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server side of the WebAuthn registration/login ceremonies (<a
 * href="https://developer.mozilla.org/en-US/docs/Web/Security/Authentication/Passkeys">passkeys</a>).
 * Hand-rolled rather than built on a library such as Yubico's {@code webauthn-server-core}: that
 * library (and its main Java alternatives) pulls in Jackson 2 databind and BouncyCastle, both of
 * which this project deliberately routes around elsewhere (see the Jackson 3 migration and the
 * BouncyCastle exclusions on the PDF dependencies). The JDK alone is enough: {@link Cbor} decodes
 * the small CBOR subset CTAP2 authenticators emit, and EC/RSA/Ed25519 signature verification are
 * all native to {@code java.security} (Ed25519 since JDK 15, JEP 339).
 */
@Singleton
public class WebAuthnService {

	private static final Logger logger = LoggerFactory.getLogger(WebAuthnService.class);
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final Base64.Encoder BASE64URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder BASE64URL_DECODER = Base64.getUrlDecoder();
	private static final long CHALLENGE_TIMEOUT_MILLIS = 60_000;

	@Inject
	GlobalVariables globalVariables;

	@Inject
	UserRepository userRepository;

	@Inject
	PasskeyRepository passkeyRepository;

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
	}

	// ---- registration ----

	public RegistrationChallenge beginRegistration(User user) {
		byte[] challenge = randomBytes(32);
		String state = UUID.randomUUID().toString();
		challenges.put(state, new ChallengeEntry(user.getEmail(), challenge, Instant.now()));

		List<CredentialDescriptor> exclude = passkeyRepository.findByOwner(user).stream()
			.map(p -> new CredentialDescriptor("public-key", p.getCredentialId()))
			.toList();

		PublicKeyCredentialCreationOptions options = new PublicKeyCredentialCreationOptions(
			base64url(challenge),
			new RelyingParty(globalVariables.getWebauthnRpId(), globalVariables.getWebauthnRpName()),
			new UserEntity(base64url(user.getEmail().getBytes(StandardCharsets.UTF_8)), user.getEmail(),
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

	public Passkey finishRegistration(User user, String state, RegistrationCredential credential, @Nullable String name) {
		ChallengeEntry entry = consumeChallenge(state, user.getEmail());
		ClientData clientData = parseClientData(credential.response().clientDataJSON());
		if (!"webauthn.create".equals(clientData.type())) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected clientData type: " + clientData.type());
		}
		verifyChallengeAndOrigin(entry, clientData);

		byte[] attestationObjectBytes = BASE64URL_DECODER.decode(credential.response().attestationObject());
		Map<Object, Object> attestationObject = Cbor.asMap(Cbor.decode(attestationObjectBytes));
		String fmt = (String) attestationObject.get("fmt");
		byte[] authData = (byte[]) attestationObject.get("authData");
		AuthenticatorData parsed = AuthenticatorData.parse(authData);
		verifyRpIdHash(parsed.rpIdHash());
		if (!parsed.userPresent()) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "User presence flag not set");
		}
		if (parsed.credentialPublicKey() == null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Authenticator did not return a public key");
		}
		logger.debug("Registering passkey (attestation format '{}') for {}", fmt, user.getEmail());

		String credentialId = base64url(parsed.credentialId());
		if (passkeyRepository.findByCredentialId(credentialId).isPresent()) {
			throw new HttpStatusException(HttpStatus.CONFLICT, "This passkey is already registered");
		}

		Passkey passkey = new Passkey();
		passkey.setOwner(user);
		passkey.setCredentialId(credentialId);
		passkey.setPublicKeyCose(Base64.getEncoder().encodeToString(parsed.credentialPublicKeyRaw()));
		passkey.setSignCount(parsed.signCount());
		passkey.setName(name != null && !name.isBlank() ? name : "Passkey");
		passkey.setLastUsedAt(Instant.now());
		return passkeyRepository.save(passkey);
	}

	// ---- login ----

	public LoginChallenge beginLogin(@Nullable String username) {
		byte[] challenge = randomBytes(32);
		String state = UUID.randomUUID().toString();
		String normalizedUsername = username != null ? username.toLowerCase() : null;

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
			base64url(challenge), globalVariables.getWebauthnRpId(), allow, "preferred", CHALLENGE_TIMEOUT_MILLIS);
		return new LoginChallenge(state, options);
	}

	public User finishLogin(String state, AuthenticationCredential credential) {
		ChallengeEntry entry = consumeChallenge(state, null);
		ClientData clientData = parseClientData(credential.response().clientDataJSON());
		if (!"webauthn.get".equals(clientData.type())) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected clientData type: " + clientData.type());
		}
		verifyChallengeAndOrigin(entry, clientData);

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
		verifyRpIdHash(authenticatorData.rpIdHash());
		if (!authenticatorData.userPresent()) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "User presence flag not set");
		}

		CoseKey coseKey = CoseKey.parse(Cbor.asMap(Cbor.decode(Base64.getDecoder().decode(passkey.getPublicKeyCose()))));
		byte[] clientDataHash = sha256(BASE64URL_DECODER.decode(credential.response().clientDataJSON()));
		byte[] signedData = EcKeys.concat(authenticatorDataBytes, clientDataHash);
		byte[] signature = BASE64URL_DECODER.decode(credential.response().signature());
		if (!verifySignature(coseKey, signedData, signature)) {
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

		return user;
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

	private ClientData parseClientData(String clientDataJsonBase64url) {
		byte[] bytes = BASE64URL_DECODER.decode(clientDataJsonBase64url);
		try {
			return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8), ClientData.class);
		} catch (IOException e) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid clientDataJSON");
		}
	}

	private void verifyChallengeAndOrigin(ChallengeEntry entry, ClientData clientData) {
		byte[] givenChallenge = BASE64URL_DECODER.decode(clientData.challenge());
		if (!MessageDigest.isEqual(entry.challenge(), givenChallenge)) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Challenge mismatch");
		}
		if (!globalVariables.getWebauthnOrigin().equals(clientData.origin())) {
			logger.warn("Rejected WebAuthn ceremony from unexpected origin: {}", clientData.origin());
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected origin");
		}
	}

	private void verifyRpIdHash(byte[] rpIdHash) {
		byte[] expected = sha256(globalVariables.getWebauthnRpId().getBytes(StandardCharsets.UTF_8));
		if (!MessageDigest.isEqual(expected, rpIdHash)) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "RP ID hash mismatch");
		}
	}

	private static boolean verifySignature(CoseKey coseKey, byte[] signedData, byte[] signature) {
		try {
			Signature verifier = Signature.getInstance(coseKey.signatureAlgorithm());
			verifier.initVerify(coseKey.publicKey());
			verifier.update(signedData);
			return verifier.verify(signature);
		} catch (GeneralSecurityException e) {
			logger.warn("Failed to verify passkey signature", e);
			return false;
		}
	}

	private static byte[] sha256(byte[] input) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(input);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e); // SHA-256 is always available on the JDK
		}
	}

	private static byte[] randomBytes(int length) {
		byte[] bytes = new byte[length];
		RANDOM.nextBytes(bytes);
		return bytes;
	}

	private static String base64url(byte[] bytes) {
		return BASE64URL_ENCODER.encodeToString(bytes);
	}
}
