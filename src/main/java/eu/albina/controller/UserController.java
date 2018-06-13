package eu.albina.controller;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.HibernateException;
import org.mindrot.jbcrypt.BCrypt;

import eu.albina.exception.AlbinaException;
import eu.albina.model.User;
import eu.albina.util.HibernateUtil;

/**
 * Controller for users.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class UserController {
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

	public boolean userExists(String username) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			User user = entityManager.find(User.class, username);
			if (user == null)
				return false;
			transaction.commit();

			return true;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public User getUser(String username) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			User user = entityManager.find(User.class, username);
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
			entityManager.close();
		}
	}

	public Serializable createUser(User user) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.persist(user);
			transaction.commit();
			return user.getEmail();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public Serializable changePassword(String username, String oldPassword, String newPassword) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			User user = entityManager.find(User.class, username);
			if (user == null) {
				transaction.rollback();
				throw new AlbinaException("No user with username: " + username);
			}
			String encryptedOldPassword = BCrypt.hashpw(oldPassword, BCrypt.gensalt());
			if (user.getPassword().equals(encryptedOldPassword))
				user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
			transaction.commit();

			return user.getEmail();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}
}