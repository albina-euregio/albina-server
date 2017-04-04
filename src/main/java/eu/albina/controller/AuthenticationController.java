package eu.albina.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import eu.albina.exception.AlbinaException;
import eu.albina.model.User;

/**
 * Controller for authentication.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class AuthenticationController extends AlbinaController {
	// private static Logger logger =
	// LoggerFactory.getLogger(AuthenticationController.class);
	private static AuthenticationController instance = null;
	private static List<String> tokens = new ArrayList<String>();

	private AuthenticationController() {
	}

	public static AuthenticationController getInstance() {
		if (instance == null) {
			instance = new AuthenticationController();
		}
		return instance;
	}

	public void authenticate(String username, String password) throws Exception {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			User user = session.get(User.class, username);
			if (user == null) {
				transaction.rollback();
				throw new AlbinaException("No user with username: " + username);
			}
			transaction.commit();

			if (!BCrypt.checkpw(password, user.getPassword()))
				throw new AlbinaException("Password not correct!");

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public String issueToken(String username) throws IllegalArgumentException, UnsupportedEncodingException {
		Algorithm algorithm = Algorithm.HMAC256("secret");
		// TODO check what issuer means
		String token = JWT.create().withIssuer("auth0").sign(algorithm);
		tokens.add(token);
		return token;
	}

	public void isTokenValid(String token) throws Exception {
		// TODO check validity of token
		if (!tokens.contains(token))
			throw new AlbinaException("Not authorized!");
	}
}