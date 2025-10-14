// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.exception.AlbinaException;
import eu.albina.model.User;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
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
		User user = findById(username).orElseThrow(() -> new AlbinaException("No user with username: " + username));
		if (BCrypt.checkpw(oldPassword, user.getPassword())) {
			user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
		} else {
			throw new AlbinaException("Password incorrect");
		}
	}

	/**
	 * Reset the password of a user.
	 *
	 * @param username    the username of the user whose password should be changed
	 * @param newPassword the new password
	 * @return the email address of the user whose password was changed
	 * @throws AlbinaException if the user does not exist
	 */
	default Serializable resetPassword(String username, String newPassword) throws AlbinaException {
		User user = findById(username).orElseThrow(() -> new AlbinaException("No user with username: " + username));
		user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
		return user.getEmail();
	}

	/**
	 * Check the {@code password} for the user with the specified {@code username}.
	 *
	 * @param username the username of the user whose password should be checked
	 * @param password the password
	 * @return {@code true} if the password is correct
	 */
	default boolean checkPassword(String username, String password) throws AlbinaException {
		User user = findById(username).orElseThrow(() -> new AlbinaException("No user with username: " + username));
		return BCrypt.checkpw(password, user.getPassword());
	}

	/**
	 * Checks if the credentials belong to a registered user.
	 *
	 * @param username the username
	 * @param password the password
	 * @throws AlbinaException if the credentials are not valid
	 */
	default void authenticate(String username, String password) throws AlbinaException {
		User user = findById(username).orElseThrow(() -> new AlbinaException("No user with username: " + username));
		if (user.isDeleted())
			throw new AlbinaException("User has been deleted!");
		if (!BCrypt.checkpw(password, user.getPassword()))
			throw new AlbinaException("Password not correct!");
	}

	default void delete(String username) throws AlbinaException {
		User user = findById(username).orElseThrow(() -> new AlbinaException("No user with username: " + username));
		user.setDeleted(true);
		save(user);
	}

}
