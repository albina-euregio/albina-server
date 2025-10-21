// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.UserRepository;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.util.JsonUtil;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
	@ApiResponse(description = "users", content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class))))
	@JsonView(JsonUtil.Views.Internal.class)
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	@Get("/roles")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all roles")
	@ApiResponse(description = "roles", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Role.class))))
	public HttpResponse<?> getRoles() {
		Role[] roles = Role.values();
		return HttpResponse.ok(roles);
	}

	@Get("/regions")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all regions")
	@ApiResponse(description = "regions", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
	public HttpResponse<?> getRegions() {
		try {
			List<String> ids = regionRepository.findAll().stream().map(Region::getId).collect(Collectors.toList());
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
		@Body User user) {
		try {
			if (userRepository.existsById(user.getEmail())) {
				throw new AlbinaException("User already exists!");
			}
			user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
			userRepository.save(user);
			return HttpResponse.noContent();
		} catch (Exception e) {
			logger.warn("Error creating user", e);
			return HttpResponse.badRequest().body(e.getMessage());
		}
	}

	@Put
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update own user")
	public HttpResponse<?> updateOwnUser(
		@Body User user,
		Principal principal) {
		try {
			String username = principal.getName();
			if (!Objects.equals(user.getEmail(), username)) {
				throw new AlbinaException("Updating user not allowed");
			}
			userRepository.updateKeepPassword(user);
			return HttpResponse.noContent();
		} catch (Exception e) {
			logger.warn("Error updating user", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Serdeable
	public record ChangePassword(String oldPassword, String newPassword) {
	}

	@Serdeable
	public record ResetPassword(String newPassword) {
	}

	@Put("/change")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Change password")
	public HttpResponse<?> changePassword(@Body ChangePassword data, Principal principal) {
		try {
			String username = principal.getName();
			userRepository.changePassword(username, data.oldPassword, data.newPassword);
			return HttpResponse.ok();
		} catch (Exception e) {
			logger.warn("Error changing password", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Serdeable
	public record CheckPassword(String password) {
	}

	@Put("/check")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Check password")
	public HttpResponse<?> checkPassword(@Body CheckPassword data, Principal principal) {
		try {
			String username = principal.getName();
			userRepository.authenticate(username, data.password);
			return HttpResponse.ok();
		} catch (Exception e) {
			logger.warn("Error checking password", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Delete("/{id}")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete user")
	public MutableHttpResponse<String> deleteUser(@PathVariable("id") String id) {
		try {
			userRepository.delete(id);
			return HttpResponse.ok();
		} catch (Exception e) {
			logger.warn("Error deleting user", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Put("/{id}/reset")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Reset user password")
	public HttpResponse<?> resetPassword(@PathVariable("id") String id, @Body ResetPassword data) {
		try {
			userRepository.resetPassword(id, data.newPassword);
			return HttpResponse.ok();
		} catch (Exception e) {
			logger.warn("Error changing password", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Put("/{id}")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update user")
	public HttpResponse<?> updateUser(
		@PathVariable("id") String id,
		@Body User user) {
		try {
			userRepository.updateKeepPassword(user);
			return HttpResponse.ok();
		} catch (Exception e) {
			logger.warn("Error updating user", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

}
