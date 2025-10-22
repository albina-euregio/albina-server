// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.exception.AlbinaException;
import eu.albina.model.User;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

	Logger logger = LoggerFactory.getLogger(UserRepository.class);

	@Override
	default List<User> findAll() {
		return findByDeletedFalse();
	}

	List<User> findByDeletedFalse();

	/**
	 * Change the password of a user.
	 *
	 * @param username    the username of the user whose password should be changed
	 * @param oldPassword the old password
	 * @param newPassword the new password
	 * @throws AlbinaException if the user does not exist or the password is wrong
	 */
	default void changePassword(String username, String oldPassword, String newPassword) throws AlbinaException {
		authenticate(username, oldPassword);
		User user = findByIdOrElseThrow(username);
		user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
		update(user);
	}

	/**
	 * Reset the password of a user.
	 *
	 * @param username    the username of the user whose password should be changed
	 * @param newPassword the new password
	 * @return the email address of the user whose password was changed
	 * @throws AlbinaException if the user does not exist
	 */
	default void resetPassword(String username, String newPassword) throws AlbinaException {
		User user = findByIdOrElseThrow(username);
		user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
		update(user);
	}

	/**
	 * Checks if the credentials belong to a registered user.
	 *
	 * @param username the username
	 * @param password the password
	 * @throws AlbinaException if the credentials are not valid
	 */
	default void authenticate(String username, String password) throws AlbinaException {
		try {
			User user = findByIdOrElseThrow(username);
			if (user.isDeleted())
				throw new AlbinaException("User has been deleted!");
			if (!BCrypt.checkpw(password, user.getPassword()))
				throw new AlbinaException("Password not correct!");
		} catch (AlbinaException e) {
			logger.warn("Failed to authenticate {}: {}", username, e.getMessage());
			throw new AlbinaException("Incorrect username or password entered!");
		}
	}

	default void delete(String username) throws AlbinaException {
		User user = findByIdOrElseThrow(username);
		user.setDeleted(true);
		update(user);
	}

	default User findByIdOrElseThrow(String username) throws AlbinaException {
		return findById(username).orElseThrow(() -> new AlbinaException("No user with username: " + username));
	}

	default User findByIdOrElseThrow(Principal principal) throws AlbinaException {
		return findByIdOrElseThrow(principal.getName());
	}

}
