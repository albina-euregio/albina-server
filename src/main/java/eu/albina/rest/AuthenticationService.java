package eu.albina.rest;

import java.security.Principal;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONObject;

import eu.albina.controller.AuthenticationController;
import eu.albina.controller.UserController;
import eu.albina.model.Credentials;
import eu.albina.model.User;
import eu.albina.rest.filter.Secured;

@Path("/authentication")
public class AuthenticationService {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(Credentials credentials) {
		String username = credentials.getUsername();
		String password = credentials.getPassword();

		try {
			AuthenticationController.getInstance().authenticate(username, password);
			String token = AuthenticationController.getInstance().issueToken(username);
			JSONObject jsonResult = new JSONObject();
			jsonResult.put("token", token);

			User user = UserController.getInstance().getUser(username);
			jsonResult.put("username", user.getName());

			jsonResult.put("image", user.getImage());

			return Response.ok(jsonResult.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@GET
	@Secured
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response refreshToken(@Context SecurityContext securityContext) {
		try {
			Principal principal = securityContext.getUserPrincipal();
			String username = principal.getName();
			String token = AuthenticationController.getInstance().refreshToken(username);
			return Response.ok(token).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}