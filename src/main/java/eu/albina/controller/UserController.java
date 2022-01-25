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

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			User user = entityManager.find(User.class, username);
			return user != null;
		});
	}

	public User updateUser(User user) throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			User originalUser = entityManager.find(User.class, user.getEmail());
			if (originalUser == null) {
				throw new HibernateException("No user with username: " + user.getEmail());
			}
			originalUser.setName(user.getName());
			originalUser.setOrganization(user.getOrganization());
			originalUser.setRoles(user.getRoles());
			originalUser.setRegions(user.getRegions());
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
	public User getUser(String username) throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
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
	@SuppressWarnings("unchecked")
	public List<User> getUsers() throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<User> users = null;
			users = entityManager.createQuery(HibernateUtil.queryGetUsers).getResultList();
			return users;
		});
	}

	public JSONArray getUsersJson() throws AlbinaException {
		List<User> users = this.getUsers();
		if (users != null) {
			JSONArray jsonResult = new JSONArray();
			for (User user : users)
				jsonResult.put(user.toJSON());

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

	public JSONArray getRegionsJson() throws AlbinaException {
		JSONArray jsonResult = new JSONArray();
		for (String regionId : RegionController.getInstance().getActiveRegions().stream().filter(region -> !region.isExternalInstance()).map(Region::getId).collect(Collectors.toList()))
			jsonResult.put(regionId);
		return jsonResult;
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
	 *            the old password (encrypted)
	 * @param newPassword
	 *            the new password (encrypted)
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
	 * Check the {@code password} for the user with the specified {@code username}.
	 *
	 * @param username
	 *            the username of the user whose password should be checked
	 * @param password
	 *            the password
	 * @return {@code true} if the password is correct
	 */
	public boolean checkPassword(String username, String password) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			User user = entityManager.find(User.class, username);
			if (user == null) {
				throw new HibernateException("No user with username: " + username);
			}
			return BCrypt.checkpw(password, user.getPassword());
		});
	}

	public static void delete(String id) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			User user = entityManager.find(User.class, id);
			entityManager.remove(user);
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	public boolean isUserInRegionRole(String userEmail, String regionId, Role role) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<UserRegionRoleLink> links = entityManager.createQuery(HibernateUtil.queryGetUserRegionRoleLinks)
				.setParameter("userEmail", userEmail)
				.setParameter("regionId", regionId)
				.getResultList();
			return links.stream().anyMatch(link -> link.getRole().equals(role));
		});
	}
}
