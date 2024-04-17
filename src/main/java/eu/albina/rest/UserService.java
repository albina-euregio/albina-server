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
package eu.albina.rest;

import java.security.Principal;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.hibernate.HibernateException;
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
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
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
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
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
			JSONArray jsonArray = RegionController.getInstance().getRegionsJson();
			return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading regions", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
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

		// check if email already exists
		if (!UserController.getInstance().userExists(user.getEmail())) {
			UserController.getInstance().createUser(user);
			JSONObject jsonObject = new JSONObject();
			return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
					.entity(jsonObject.toString()).build();
		} else {
			logger.warn("Error creating user - User already exists");
			JSONObject json = new JSONObject();
			json.append("message", "Error creating user - User already exists");
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(json).build();
		}
	}

	@PUT
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update user")
	public Response updateUser(
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
				logger.warn("Error updating user - User does not exist");
				JSONObject json = new JSONObject();
				json.append("message", "Error updating user - User does not exists");
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(json).build();
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
	@Path("/{id}/change")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Reset user password")
	public Response changeUserPassword(@PathParam("id") String id, ResetPassword data, @Context SecurityContext securityContext) {
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

}
