// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import eu.albina.controller.RegionController;
import eu.albina.model.Region;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.hibernate.HibernateException;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/user")
@Tag(name = "user")
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Context
	UriInfo uri;

	@Get
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all users")
	@ApiResponse(description = "users", content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class))))
	public HttpResponse<?> getUsers(@Context SecurityContext securityContext) {
		logger.debug("GET JSON users");
		try {
			List<User> users = UserController.getInstance().getUsers();
			return HttpResponse.ok(users);
		} catch (Exception e) {
			logger.warn("Error loading users", e);
			return HttpResponse.unauthorized();
		}
	}

	@Get("/roles")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all roles")
	@ApiResponse(description = "roles", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Role.class))))
	public HttpResponse<?> getRoles(@Context SecurityContext securityContext) {
		logger.debug("GET JSON roles");
		Role[] roles = Role.values();
		return HttpResponse.ok(roles);
	}

	@Get("/regions")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all regions")
	@ApiResponse(description = "regions", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
	public HttpResponse<?> getRegions(@Context SecurityContext securityContext) {
		logger.debug("GET JSON regions");
		try {
			List<String> ids = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return HttpResponse.ok(ids);
		} catch (Exception e) {
			logger.warn("Error loading regions", e);
			return HttpResponse.unauthorized();
		}
	}

	@Post("/create")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create user")
	public HttpResponse<?> createUser(
		@Parameter(schema = @Schema(implementation = User.class)) User user) {
		logger.debug("POST JSON user");
		user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

		// check if email already exists
		if (!UserController.getInstance().userExists(user.getEmail())) {
			UserController.getInstance().createUser(user);
			return HttpResponse.created(uri.getAbsolutePathBuilder().path("").build());
		} else {
			String message = "Error creating user - User already exists";
			logger.warn(message);
			return HttpResponse.badRequest().body(new AlbinaException(message).toJSON());
		}
	}

	@PUT
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update own user")
	public HttpResponse<?> updateOwnUser(
		@Parameter(schema = @Schema(implementation = User.class)) User user,
		@Context SecurityContext securityContext) {
		logger.debug("PUT JSON user");
		try {
			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();

			// check if email already exists
			if (user.getEmail().equals(username)) {
				UserController.getInstance().updateUser(user);
				return HttpResponse.created(uri.getAbsolutePathBuilder().path("").build());
			} else {
				throw new AlbinaException("Updating user not allowed");
			}
		} catch (AlbinaException e) {
			logger.warn("Error updating user", e);
			return HttpResponse.badRequest().body(e.toJSON());
		}
	}

	static class ChangePassword {
		public String oldPassword;
		public String newPassword;
	}

	static class ResetPassword {
		public String newPassword;
	}

	@Put("/change")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Change password")
	public HttpResponse<?> changePassword(ChangePassword data, Principal principal) {
		logger.debug("PUT JSON password");
		try {
			String username = principal.getName();

			UserController.getInstance().changePassword(username, data.oldPassword, data.newPassword);

			return HttpResponse.ok();
		} catch (AlbinaException e) {
			logger.warn("Error changing password", e);
			return HttpResponse.badRequest().body(e.toJSON());
		}
	}

	static class CheckPassword {
		public String password;
	}

	@Put("/check")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Check password")
	public HttpResponse<?> checkPassword(CheckPassword data, Principal principal) {
		logger.debug("GET JSON check password");
		try {
			String username = principal.getName();

			if (UserController.getInstance().checkPassword(username, data.password)) {
				return HttpResponse.ok();
			} else {
				return HttpResponse.badRequest();
			}
		} catch (HibernateException e) {
			logger.warn("Error checking password", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Delete("/{id}")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete user")
	public void deleteUser(@PathVariable("id") String id) {
		logger.info("DELETE JSON user {}", id);
		UserController.delete(id);
	}

	@Put("/{id}/reset")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Reset user password")
	public HttpResponse<?> resetPassword(@PathVariable("id") String id, ResetPassword data) {
		logger.debug("PUT JSON user password");
		try {
			UserController.getInstance().resetPassword(id, data.newPassword);

			return HttpResponse.ok();
		} catch (AlbinaException e) {
			logger.warn("Error changing password", e);
			return HttpResponse.badRequest().body(e.toJSON());
		}
	}

	@Put("/{id}")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update user")
	public HttpResponse<?> updateUser(
		@PathVariable("id") String id,
		@Parameter(schema = @Schema(implementation = User.class)) User user) {
		logger.debug("PUT JSON user");
		try {
			// check if email already exists
			if (UserController.getInstance().userExists(user.getEmail())) {
				UserController.getInstance().updateUser(user);
				return HttpResponse.ok();
			} else {
				throw new AlbinaException("User does not exist");
			}
		} catch (AlbinaException e) {
			logger.warn("Error updating user", e);
			return HttpResponse.badRequest().body(e.toJSON());
		}
	}

}
