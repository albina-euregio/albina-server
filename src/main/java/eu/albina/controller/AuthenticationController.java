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
package eu.albina.controller;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.mindrot.jbcrypt.BCrypt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import eu.albina.exception.AlbinaException;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;

/**
 * Controller handling the authentication and authorization.
 *
 * @author Norbert Lanzanasto
 *
 */
public class AuthenticationController {
	private static AuthenticationController instance = null;
	private Algorithm algorithm;
	private JWTVerifier verifier;

	/**
	 * Private constructor. Initializing the used algorithm.
	 */
	private AuthenticationController() {
		try {
			algorithm = Algorithm.HMAC256(GlobalVariables.tokenEncodingSecret);
			verifier = JWT.require(algorithm).withIssuer(GlobalVariables.tokenEncodingIssuer).build();
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			throw new IllegalStateException("Failed to initialize controller", e);
		}
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
	 * Checks if the credentials belong to a registered user.
	 *
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @throws AlbinaException
	 *             if the credentials are not valid
	 */
	public void authenticate(String username, String password) throws Exception {
		User user = UserController.getInstance().getUser(username);
		if (!BCrypt.checkpw(password, user.getPassword()))
			throw new AlbinaException("Password not correct!");
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
		long time = System.currentTimeMillis() + GlobalVariables.accessTokenExpirationDuration;
		Date expirationTime = new Date(time);
		Date issuedAt = new Date();
		String token = JWT.create().withIssuer(GlobalVariables.tokenEncodingIssuer).withSubject(username)
				.withIssuedAt(issuedAt).withExpiresAt(expirationTime).sign(algorithm);
		return token;
	}

	/**
	 * Creates a refresh token for the given user.
	 *
	 * @param username
	 *            the username the token should be generated for
	 * @return the refresh token for the given user
	 * @throws IllegalArgumentException
	 *             if the sign process fails
	 */
	public String issueRefreshToken(String username) throws IllegalArgumentException {
		long time = System.currentTimeMillis() + GlobalVariables.refreshTokenExpirationDuration;
		Date expirationTime = new Date(time);
		Date issuedAt = new Date();
		String token = JWT.create().withIssuer(GlobalVariables.tokenEncodingIssuer).withSubject(username)
				.withIssuedAt(issuedAt).withExpiresAt(expirationTime).sign(algorithm);
		return token;
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

	/**
	 * Refresh the token for a given user.
	 *
	 * @param username
	 *            the username
	 * @return the refreshed access token for the given user
	 * @throws IllegalArgumentException
	 *             if the sign process fails
	 */
	public String refreshToken(String username) throws IllegalArgumentException {
		long time = System.currentTimeMillis() + GlobalVariables.accessTokenExpirationDuration;
		Date expirationTime = new Date(time);
		Date issuedAt = new Date();
		String token = JWT.create().withIssuer(GlobalVariables.tokenEncodingIssuer).withSubject(username)
				.withIssuedAt(issuedAt).withExpiresAt(expirationTime).sign(algorithm);
		return token;
	}

	/**
	 * Checks if a user in a given role.
	 *
	 * @param role
	 *            the role to be checked if the user is in
	 * @param username
	 *            the user to be checked
	 * @return <code>true</code> if the user is in the given role
	 */
	public boolean isUserInRole(String role, String username) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			User user = entityManager.find(User.class, username);
			if (user == null) {
				transaction.rollback();
				return false;
			}
			transaction.commit();

			for (Role userRole : user.getRoles()) {
				if (userRole.equals(Role.fromString(role)))
					return true;
			}
			return false;
		} finally {
			entityManager.close();
		}
	}
}
