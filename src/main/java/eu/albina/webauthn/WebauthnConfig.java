// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.webauthn;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @param rpId   The WebAuthn Relying Party ID: a registrable domain suffix of every origin passkeys are used from.
 * @param origin The origin (scheme + host + port) this server instance's frontend is deployed at.
 */
public record WebauthnConfig(String rpId, String rpName, String origin) {

	private static final Logger logger = LoggerFactory.getLogger(WebauthnConfig.class);

	public void verifyChallengeAndOrigin(byte[] expectedChallenge, byte[] givenChallenge, String givenOrigin) {
		if (!MessageDigest.isEqual(expectedChallenge, givenChallenge)) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Challenge mismatch");
		}
		if (!origin.equals(givenOrigin)) {
			logger.warn("Rejected WebAuthn ceremony from unexpected origin: {}", givenOrigin);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected origin");
		}
	}

	public void verifyRpIdHash(byte[] rpIdHash) {
		byte[] expected = sha256(rpId.getBytes(StandardCharsets.UTF_8));
		if (!MessageDigest.isEqual(expected, rpIdHash)) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "RP ID hash mismatch");
		}
	}

	private static byte[] sha256(byte[] input) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(input);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e); // SHA-256 is always available on the JDK
		}
	}
}
