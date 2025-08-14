// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.openjson.JSONArray;

import org.hibernate.HibernateException;
import org.mindrot.jbcrypt.BCrypt;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.UserRegionRoleLink;
import eu.albina.model.enumerations.Role;
import eu.albina.util.HibernateUtil;

/**
 * Controller for users.
 *
 * @author Norbert Lanzanasto
 *
 */
public class UserController {
	// private static Logger logger = LoggerFactory.getLogger(UserController.class);
	private static UserController instance = null;

	/**
	 * Private constructor.
	 */
	private UserController() {
	}

	/**
	 * Returns the {@code UserController} object associated with the current Java
	 * application.
	 *
	 * @return the {@code UserController} object associated with the current Java
	 *         application.
	 */
	public static UserController getInstance() {
		if (instance == null) {
			instance = new UserController();
		}
		return instance;
	}

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

	public User updateUser(User user) throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
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

			return user;
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

	public JSONArray getUsersJson() throws AlbinaException {
		List<User> users = this.getUsers();
		if (users != null) {
			JSONArray jsonResult = new JSONArray();
			users.stream().filter(user -> !user.isDeleted()).forEach(user -> {
				try {
					jsonResult.put(user.toMediumJSON());
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			});

			return jsonResult;
		} else
			throw new AlbinaException("Users could not be loaded!");
	}

	public JSONArray getRolesJson() throws AlbinaException {
		List<String> roles = Stream.of(Role.values())
                               .map(Enum::name)
                               .collect(Collectors.toList());
		if (roles != null) {
			JSONArray jsonResult = new JSONArray();
			for (String role : roles)
				jsonResult.put(role);

			return jsonResult;
		} else
			throw new AlbinaException("Roles could not be loaded!");
	}

	/**
	 * Save a {@code user} to the database.
	 *
	 * @param user
	 *            the user to be saved
	 * @return the email address of the saved user
	 */
	public Serializable createUser(User user) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(user);
			return user.getEmail();
		});
	}

	/**
	 * Change the password of a user.
	 *
	 * @param username
	 *            the username of the user whose password should be changed
	 * @param oldPassword
	 *            the old password
	 * @param newPassword
	 *            the new password
	 * @return the email address of the user whose password was changed
	 * @throws AlbinaException
	 *             if the user does not exist or the password is wrong
	 */
	public Serializable changePassword(String username, String oldPassword, String newPassword) throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			User user = entityManager.find(User.class, username);
			if (user == null) {
				throw new HibernateException("No user with username: " + username);
			}
			if (BCrypt.checkpw(oldPassword, user.getPassword())) {
				user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
			} else {
				throw new HibernateException("Password incorrect");
			}
			return user.getEmail();
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

	public boolean isUserInRegionRole(String userEmail, String regionId, Role role) {
		return HibernateUtil.getInstance().run(entityManager -> {
			Region region = entityManager.find(Region.class, regionId);
			List<UserRegionRoleLink> links = entityManager.createQuery(HibernateUtil.queryGetUserRegionRoleLinks, UserRegionRoleLink.class)
				.setParameter("userEmail", userEmail)
				.setParameter("region", region)
				.getResultList();
			return links.stream().anyMatch(link -> link.getRole().equals(role));
		});
	}
}
