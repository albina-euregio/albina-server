// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.io.Serializable;
import java.util.List;

import jakarta.inject.Singleton;
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
@Singleton
public class UserController {

	/**
	 * Return {@code true} if the user with {@code username} exists.
	 *
	 * @param username
	 *            the username of the desired user
	 * @return {@code true} if the user with {@code username} exists
	 */
	public boolean userExists(String username) {
		return HibernateUtil.getInstance().run(entityManager -> entityManager.find(User.class, username) != null);
	}

	public void updateUser(User user) throws AlbinaException {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			User originalUser = entityManager.find(User.class, user.getEmail());
			if (originalUser == null) {
				throw new HibernateException("No user with username: " + user.getEmail());
			}
			originalUser.setImage(user.getImage());
			originalUser.setName(user.getName());
			originalUser.setOrganization(user.getOrganization());
			originalUser.setRoles(user.getRoles());
			originalUser.setRegions(user.getRegions());
			originalUser.setLanguage(user.getLanguage());
			entityManager.persist(originalUser);

			return null;
		});
	}

	/**
	 * Return the {@code User} with the specified {@code username}.
	 *
	 * @param username
	 *            the username of the desired user
	 * @return the {@code User} with the specified {@code username}
	 */
	public User getUser(String username) {
		return HibernateUtil.getInstance().run(entityManager -> {
			User user = entityManager.find(User.class, username);
			if (user == null) {
				throw new HibernateException("No user with username: " + username);
			}
			return user;
		});
	}

	/**
	 * Return all {@code User}.
	 *
	 * @return list of all {@code User}
	 */
	public List<User> getUsers() {
		return HibernateUtil.getInstance().run(entityManager ->
			entityManager.createQuery(HibernateUtil.queryGetUsers, User.class).getResultList());
	}

	/**
	 * Save a {@code user} to the database.
	 *
	 * @param user the user to be saved
	 */
	public void createUser(User user) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(user);
			return null;
		});
	}

	/**
	 * Change the password of a user.
	 *
	 * @param username    the username of the user whose password should be changed
	 * @param oldPassword the old password
	 * @param newPassword the new password
	 * @throws AlbinaException if the user does not exist or the password is wrong
	 */
	public void changePassword(String username, String oldPassword, String newPassword) throws AlbinaException {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			User user = entityManager.find(User.class, username);
			if (user == null) {
				throw new HibernateException("No user with username: " + username);
			}
			if (BCrypt.checkpw(oldPassword, user.getPassword())) {
				user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
			} else {
				throw new HibernateException("Password incorrect");
			}
			return null;
		});
	}

	/**
	 * Reset the password of a user.
	 *
	 * @param username
	 *            the username of the user whose password should be changed
	 * @param newPassword
	 *            the new password
	 * @return the email address of the user whose password was changed
	 * @throws AlbinaException
	 *             if the user does not exist
	 */
	public Serializable resetPassword(String username, String newPassword) throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			User user = entityManager.find(User.class, username);
			if (user == null) {
				throw new HibernateException("No user with username: " + username);
			}
			user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
			return user.getEmail();
		});
	}

	/**
	 * Check the {@code password} for the user with the specified {@code username}.
	 *
	 * @param username
	 *            the username of the user whose password should be checked
	 * @param password
	 *            the password
	 * @return {@code true} if the password is correct
	 */
	public boolean checkPassword(String username, String password) {
		return HibernateUtil.getInstance().run(entityManager -> {
			User user = entityManager.find(User.class, username);
			if (user == null) {
				throw new HibernateException("No user with username: " + username);
			}
			return BCrypt.checkpw(password, user.getPassword());
		});
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
		User user = getUser(username);
		if (user == null)
			throw new AlbinaException("User does not exist!");
		if (user.isDeleted())
			throw new AlbinaException("User has been deleted!");
		if (!BCrypt.checkpw(password, user.getPassword()))
			throw new AlbinaException("Password not correct!");
	}

	public static void delete(String id) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			User user = entityManager.find(User.class, id);
			user.setDeleted(true);
			entityManager.persist(user);
			return null;
		});
	}
}
