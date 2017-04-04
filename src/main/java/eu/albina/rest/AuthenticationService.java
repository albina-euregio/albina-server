package eu.albina.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.albina.controller.AuthenticationController;
import eu.albina.model.Credentials;

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
			return Response.ok(token).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}