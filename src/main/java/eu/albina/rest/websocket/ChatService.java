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
package eu.albina.rest.websocket;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
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

import com.github.openjson.JSONArray;

import eu.albina.rest.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.ChatController;
import eu.albina.model.ChatMessage;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@Path("/chat")
@Tag(name = "chat")
public class ChatService {

	private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getJsonChatMessages(
			@Parameter(description = "Start date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String date) {
		logger.debug("GET JSON chat messages");

		Instant dateTime = null;

		try {
			if (date != null)
				dateTime = OffsetDateTime.parse(date).toInstant();

			List<ChatMessage> chatMessages = ChatController.getInstance().getChatMessages(dateTime);
			JSONArray json = new JSONArray();
			for (ChatMessage entry : chatMessages) {
				json.put(entry.toJSON());
			}
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (HibernateException e) {
			logger.warn("Error loading chat messages", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getActiveUsers() {
		logger.debug("GET JSON active users");

		// List<User> activeUsers = ChatController.getInstance().getActiveUsers();
		Collection<String> activeUsers = ChatEndpoint.getActiveUsers();
		JSONArray json = new JSONArray();
		for (String entry : activeUsers)
			json.put(entry);

		return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
	}
}
