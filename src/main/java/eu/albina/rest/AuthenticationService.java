package eu.albina.rest;

import java.security.Principal;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AuthenticationController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Credentials;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.annotations.Api;

@Path("/authentication")
@Api(value = "/authentication")
public class AuthenticationService {

	private static Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

	@Context
	UriInfo uri;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(Credentials credentials) {
		String username = credentials.getUsername();
		String password = credentials.getPassword();

		try {
			AuthenticationController.getInstance().authenticate(username, password);
			String accessToken = AuthenticationController.getInstance().issueAccessToken(username);
			String refreshToken = AuthenticationController.getInstance().issueRefreshToken(username);

			User user = UserController.getInstance().getUser(username);
			JSONObject jsonResult = user.toJSON();
			jsonResult.put("access_token", accessToken);
			jsonResult.put("refresh_token", refreshToken);

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL, Role.STYRIA })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response refreshToken(@Context SecurityContext securityContext) {
		try {
			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();

			String accessToken = AuthenticationController.getInstance().issueAccessToken(username);

			JSONObject jsonResult = new JSONObject();
			jsonResult.put("access_token", accessToken);

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(String userString, @Context SecurityContext securityContext) {
		logger.debug("POST JSON user");
		try {
			JSONObject userJson = new JSONObject(userString);
			User user = new User(userJson);

			// check if email already exists
			if (!UserController.getInstance().userExists(user.getEmail())) {
				UserController.getInstance().createUser(user);
				JSONObject jsonObject = new JSONObject();
				// TODO return some meaningful path
				return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
						.entity(jsonObject.toString()).build();
			} else {
				logger.warn("Error creating user - User already exists");
				JSONObject json = new JSONObject();
				json.append("message", "Error creating user - User already exists");
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(json).build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error creating user - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@PUT
	@Secured({ Role.ADMIN })
	@Path("/change")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(String data, @Context SecurityContext securityContext) {
		logger.debug("POST JSON user");
		try {
			JSONObject dataJson = new JSONObject(data);

			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();

			UserController.getInstance().changePassword(username, dataJson.get("oldPassword").toString(),
					dataJson.get("newPassword").toString());

			JSONObject jsonObject = new JSONObject();
			// TODO return some meaningful path
			return Response.created(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
					.entity(jsonObject.toString()).build();
		} catch (AlbinaException e) {
			logger.warn("Error changing password - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@PUT
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkPassword(String data, @Context SecurityContext securityContext) {
		logger.debug("GET JSON check password");
		try {
			JSONObject dataJson = new JSONObject(data);

			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();

			if (dataJson.has("password")
					&& UserController.getInstance().checkPassword(username, dataJson.getString("password")))
				return Response.ok(uri.getAbsolutePathBuilder().path("").build()).type(MediaType.APPLICATION_JSON)
						.build();
			else
				return Response.status(400).type(MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error checking password - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}
}