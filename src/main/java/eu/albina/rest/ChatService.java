package eu.albina.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.ChatController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.ChatMessage;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path("/chat")
@Api(value = "/chat")
public class ChatService {

	private static Logger logger = LoggerFactory.getLogger(ChatService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getJsonChatMessages(
			@ApiParam(value = "Starttime in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date) {
		logger.debug("GET JSON chat messages");

		DateTime dateTime = null;

		if (date != null)
			dateTime = DateTime.parse(date, GlobalVariables.parserDateTime);

		try {
			List<ChatMessage> chatMessages = ChatController.getInstance().getChatMessages(dateTime);
			JSONArray json = new JSONArray();
			for (ChatMessage entry : chatMessages) {
				json.put(entry.toJSON());
			}
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading chat messages - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.TRENTINO, Role.TYROL, Role.SOUTH_TYROL })
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getActiveUsers() {
		logger.debug("GET JSON active users");

		List<User> activeUsers = ChatController.getInstance().getActiveUsers();
		JSONArray json = new JSONArray();
		for (User entry : activeUsers)
			json.put(entry.toJSON());

		return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
	}
}
