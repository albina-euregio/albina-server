// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

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

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/user")
@Tag(name = "user")
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get all users")
	@ApiResponse(description = "users", content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class))))
	public Response getUsers(@Context SecurityContext securityContext) {
		logger.debug("GET JSON users");
		try {
			JSONArray jsonArray = UserController.getInstance().getUsersJson();
			return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading users", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/roles")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get roles of logged-in user")
	@ApiResponse(description = "roles", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
	public Response getRoles(@Context SecurityContext securityContext) {
		logger.debug("GET JSON roles");
		try {
			JSONArray jsonArray = UserController.getInstance().getRolesJson();
			return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading roles", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/regions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get regions of logged-in user")
	@ApiResponse(description = "regions", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
	public Response getRegions(@Context SecurityContext securityContext) {
		logger.debug("GET JSON regions");
		try {
			List<String> ids = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return Response.ok(ids, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create user")
	public Response createUser(
		@Parameter(schema = @Schema(implementation = User.class)) String userString,
		@Context SecurityContext securityContext) {
		logger.debug("POST JSON user");
		JSONObject userJson = new JSONObject(userString);
		User user = new User(userJson, RegionController.getInstance()::getRegion);
		user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

		// check if email already exists
		if (!UserController.getInstance().userExists(user.getEmail())) {
			UserController.getInstance().createUser(user);
			JSONObject jsonObject = new JSONObject();
			return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
					.entity(jsonObject.toString()).build();
		} else {
			String message = "Error creating user - User already exists";
			logger.warn(message);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(new AlbinaException(message).toJSON()).build();
		}
	}

	@PUT
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update own user")
	public Response updateOwnUser(
		@Parameter(schema = @Schema(implementation = User.class)) String userString,
		@Context SecurityContext securityContext) {
		logger.debug("PUT JSON user");
		try {
			JSONObject userJson = new JSONObject(userString);
			User user = new User(userJson, RegionController.getInstance()::getRegion);

			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();

			// check if email already exists
			if (user.getEmail().equals(username)) {
				UserController.getInstance().updateUser(user);
				JSONObject jsonObject = new JSONObject();
				return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
						.entity(jsonObject.toString()).build();
			} else {
				throw new AlbinaException("Updating user not allowed");
			}
		} catch (AlbinaException e) {
			logger.warn("Error updating user", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	static class ChangePassword {
		public String oldPassword;
		public String newPassword;
	}

	static class ResetPassword {
		public String newPassword;
	}

	@PUT
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/change")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Change password")
	public Response changePassword(ChangePassword data, @Context SecurityContext securityContext) {
		logger.debug("PUT JSON password");
		try {
			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();

			UserController.getInstance().changePassword(username, data.oldPassword, data.newPassword);

			JSONObject jsonObject = new JSONObject();
			return Response.ok().type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
		} catch (AlbinaException e) {
			logger.warn("Error changing password", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	static class CheckPassword {
		public String password;
	}

	@PUT
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Check password")
	public Response checkPassword(CheckPassword data, @Context SecurityContext securityContext) {
		logger.debug("GET JSON check password");
		try {
			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();

			if (UserController.getInstance().checkPassword(username, data.password))
				return Response.ok(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
						.build();
			else
				return Response.status(400).type(MediaType.APPLICATION_JSON).build();
		} catch (HibernateException e) {
			logger.warn("Error checking password", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@DELETE
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Delete user")
	public void deleteUser(@PathParam("id") String id) {
		logger.info("DELETE JSON user {}", id);
		UserController.delete(id);
	}

	@PUT
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{id}/reset")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Reset user password")
	public Response resetPassword(@PathParam("id") String id, ResetPassword data, @Context SecurityContext securityContext) {
		logger.debug("PUT JSON user password");
		try {
			UserController.getInstance().resetPassword(id, data.newPassword);

			JSONObject jsonObject = new JSONObject();
			return Response.ok().type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
		} catch (AlbinaException e) {
			logger.warn("Error changing password", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@PUT
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update user")
	public Response updateUser(
		@PathParam("id") String id,
		@Parameter(schema = @Schema(implementation = User.class)) String userString,
		@Context SecurityContext securityContext) {
		logger.debug("PUT JSON user");
		try {
			JSONObject userJson = new JSONObject(userString);
			User user = new User(userJson, RegionController.getInstance()::getRegion);

			// check if email already exists
			if (UserController.getInstance().userExists(user.getEmail())) {
				UserController.getInstance().updateUser(user);
				JSONObject jsonObject = new JSONObject();
				return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
						.entity(jsonObject.toString()).build();
			} else {
				throw new AlbinaException("User does not exist");
			}
		} catch (AlbinaException e) {
			logger.warn("Error updating user", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

}
