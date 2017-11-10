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

	public DecodedJWT decodeToken(String token) throws Exception {
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

			if (user.getRole().equals(Role.fromString(role)))
				return true;
			else
				return false;
		} finally {
			entityManager.close();
		}
	}
}