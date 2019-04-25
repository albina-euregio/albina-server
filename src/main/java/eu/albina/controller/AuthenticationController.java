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
 * Controller for authentication.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class AuthenticationController {
	// private static Logger logger =
	// LoggerFactory.getLogger(AuthenticationController.class);
	private static AuthenticationController instance = null;
	private JWTVerifier verifier;

	private AuthenticationController() {
		Algorithm algorithm;
		try {
			algorithm = Algorithm.HMAC256(GlobalVariables.tokenEncodingSecret);
			verifier = JWT.require(algorithm).withIssuer(GlobalVariables.tokenEncodingIssuer).build();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static AuthenticationController getInstance() {
		if (instance == null) {
			instance = new AuthenticationController();
		}
		return instance;
	}

	public void authenticate(String username, String password) throws Exception {
		User user = UserController.getInstance().getUser(username);
		if (!BCrypt.checkpw(password, user.getPassword()))
			throw new AlbinaException("Password not correct!");
	}

	public String issueAccessToken(String username) throws IllegalArgumentException, UnsupportedEncodingException {
		Algorithm algorithm = Algorithm.HMAC256(GlobalVariables.tokenEncodingSecret);
		long time = System.currentTimeMillis() + GlobalVariables.accessTokenExpirationDuration;
		Date expirationTime = new Date(time);
		Date issuedAt = new Date();
		String token = JWT.create().withIssuer(GlobalVariables.tokenEncodingIssuer).withSubject(username)
				.withIssuedAt(issuedAt).withExpiresAt(expirationTime).sign(algorithm);
		return token;
	}

	public String issueRefreshToken(String username) throws IllegalArgumentException, UnsupportedEncodingException {
		Algorithm algorithm = Algorithm.HMAC256(GlobalVariables.tokenEncodingSecret);
		long time = System.currentTimeMillis() + GlobalVariables.refreshTokenExpirationDuration;
		Date expirationTime = new Date(time);
		Date issuedAt = new Date();
		String token = JWT.create().withIssuer(GlobalVariables.tokenEncodingIssuer).withSubject(username)
				.withIssuedAt(issuedAt).withExpiresAt(expirationTime).sign(algorithm);
		return token;
	}

	public DecodedJWT decodeToken(String token) throws AlbinaException {
		try {
			return verifier.verify(token);
		} catch (JWTVerificationException exception) {
			throw new AlbinaException("Not authorized");
		}
	}

	public String refreshToken(String username) throws IllegalArgumentException, UnsupportedEncodingException {
		Algorithm algorithm = Algorithm.HMAC256(GlobalVariables.tokenEncodingSecret);
		long time = System.currentTimeMillis() + GlobalVariables.accessTokenExpirationDuration;
		Date expirationTime = new Date(time);
		Date issuedAt = new Date();
		String token = JWT.create().withIssuer(GlobalVariables.tokenEncodingIssuer).withSubject(username)
				.withIssuedAt(issuedAt).withExpiresAt(expirationTime).sign(algorithm);
		return token;
	}

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
