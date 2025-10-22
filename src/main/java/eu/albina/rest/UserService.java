// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.Authentication;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.UserRepository;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.util.JsonUtil;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/user")
@Tag(name = "user")
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Inject
	RegionRepository regionRepository;

	@Inject
	UserRepository userRepository;

	@Get
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all users")
	@JsonView(JsonUtil.Views.Internal.class)
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	@Get("/roles")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all roles")
	public Role[] getRoles() {
		return Role.values();
	}

	@Get("/regions")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all regions")
	public List<String> getRegions() {
		return regionRepository.findAll().stream().map(Region::getId).collect(Collectors.toList());
	}

	@Post
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create or update user")
	public User saveUser(
		@Body User user,
		Authentication authentication) {
		boolean isAdmin = authentication.getRoles().contains(Role.Str.ADMIN);
		Optional<User> existingUser = userRepository.findById(user.getEmail());

		if (existingUser.isPresent()) {
			if (!isAdmin && !authentication.getName().equals(user.getEmail())) {
				throw new HttpStatusException(HttpStatus.FORBIDDEN, "Only admins can update other users.");
			}
			user.setPassword(existingUser.get().getPassword());
			userRepository.update(user);
		} else {
			if (!isAdmin) {
				throw new HttpStatusException(HttpStatus.FORBIDDEN, "Only admins can create other users.");
			}
			user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
			userRepository.save(user);
		}
		return user;
	}

	@Serdeable
	public record ChangePassword(String oldPassword, String newPassword) {
	}

	@Put("/change")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Change password")
	public void changePassword(@Body ChangePassword data, Principal principal) {
		try {
			String username = principal.getName();
			userRepository.changePassword(username, data.oldPassword, data.newPassword);
		} catch (Exception e) {
			logger.warn("Error changing password", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Error changing password");
		}
	}

	@Serdeable
	public record CheckPassword(String password) {
	}

	@Put("/check")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Check password")
	public void checkPassword(@Body CheckPassword data, Principal principal) {
		try {
			String username = principal.getName();
			userRepository.authenticate(username, data.password);
		} catch (Exception e) {
			logger.warn("Error checking password", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Error checking password");
		}
	}

	@Delete("/{id}")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete user")
	public void deleteUser(@PathVariable("id") String id) {
		try {
			userRepository.delete(id);
		} catch (Exception e) {
			logger.warn("Error deleting user", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Error deleting user");
		}
	}

	@Serdeable
	public record ResetPassword(String newPassword) {
	}

	@Put("/{id}/reset")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Reset user password")
	public void resetPassword(@PathVariable("id") String id, @Body ResetPassword data) {
		try {
			userRepository.resetPassword(id, data.newPassword);
		} catch (Exception e) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Error changing password");
		}
	}
}
