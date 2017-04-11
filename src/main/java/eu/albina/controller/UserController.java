package eu.albina.controller;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import eu.albina.exception.AlbinaException;
import eu.albina.model.User;

/**
 * Controller for users.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class UserController extends AlbinaController {
	// private static Logger logger =
	// LoggerFactory.getLogger(UserController.class);
	private static UserController instance = null;

	private UserController() {
	}

	public static UserController getInstance() {
		if (instance == null) {
			instance = new UserController();
		}
		return instance;
	}

	public User getUser(String username) throws Exception {
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

			return user;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}
}