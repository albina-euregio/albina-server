// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Date;

import ch.rasc.webpush.ServerKeys;
import eu.albina.util.AlbinaUtil;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import eu.albina.exception.AlbinaException;
import org.slf4j.LoggerFactory;

/**
 * Controller handling the authentication and authorization.
 *
 * @author Norbert Lanzanasto
 *
 */
public class AuthenticationController {
	public static final String JWT_ES256_PRIVATE_KEY_ENV = "ALBINA_JWT_ES256_PRIVATE_KEY";
	public static final String JWT_ES256_PUBLIC_KEY_ENV = "ALBINA_JWT_ES256_PUBLIC_KEY";
	public static final String JWT_SECRET_ENV = "ALBINA_JWT_SECRET";
	public static final String JWT_ISSUER = "albina";

	private static AuthenticationController instance = null;

	private final Algorithm algorithm;
	private final JWTVerifier verifier;

	/**
	 * Private constructor. Initializing the used algorithm.
	 */
	private AuthenticationController() {
		try {
			algorithm = getAlgorithm();
			verifier = JWT.require(algorithm).withIssuer(JWT_ISSUER).build();
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Failed to initialize controller", e);
		}
	}

	private Algorithm getAlgorithm() {
		if (System.getenv(JWT_ES256_PRIVATE_KEY_ENV) != null && System.getenv(JWT_ES256_PUBLIC_KEY_ENV) != null) {
			try {
				return new ServerKeys(
					System.getenv(JWT_ES256_PUBLIC_KEY_ENV),
					System.getenv(JWT_ES256_PRIVATE_KEY_ENV)).toJwtAlgorithm();
			} catch (Exception e) {
				LoggerFactory.getLogger(AuthenticationController.class).warn("Failed to initialize Algorithm.ECDSA256", e);
			}
		}
		String tokenEncodingSecret = System.getenv(JWT_SECRET_ENV);
		if (tokenEncodingSecret == null || tokenEncodingSecret.length() < 32) {
			tokenEncodingSecret = new BigInteger(512, new SecureRandom()).toString(36);
		}
		return Algorithm.HMAC256(tokenEncodingSecret);
	}

	/**
	 * Returns the AuthenticationController object associated with the current Java
	 * application.
	 *
	 * @return the <code>AuthenticationController</code> object associated with the
	 *         current Java application.
	 */
	public static AuthenticationController getInstance() {
		if (instance == null) {
			instance = new AuthenticationController();
		}
		return instance;
	}

	/**
	 * Creates an access token for the given user.
	 *
	 * @param username
	 *            the username the token should be generated for
	 * @return the access token for the given user
	 * @throws IllegalArgumentException
	 *             if the sign process fails
	 */
	public String issueAccessToken(String username) throws IllegalArgumentException {
		return issueAccessToken(algorithm, username);
	}

	public static String issueAccessToken(Algorithm algorithm, String username) {
		Date expirationTime = Date.from(LocalDate.now().atStartOfDay(AlbinaUtil.localZone())
			.plusDays(1)
			.withHour(3) // tomorrow at 03:00
			.toInstant());
		Date issuedAt = new Date();
		return JWT.create().withIssuer(JWT_ISSUER).withSubject(username)
				.withIssuedAt(issuedAt).withExpiresAt(expirationTime).sign(algorithm);
	}

	/**
	 * Decode a given token with JWT.
	 *
	 * @param token
	 *            the encoded token
	 * @return the decoded token
	 * @throws AlbinaException
	 *             if the verfication of the token fails
	 */
	public DecodedJWT decodeToken(String token) throws AlbinaException {
		try {
			return verifier.verify(token);
		} catch (JWTVerificationException exception) {
			throw new AlbinaException("Not authorized");
		}
	}

}
