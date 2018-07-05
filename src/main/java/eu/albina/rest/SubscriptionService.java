package eu.albina.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.SubscriberController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Subscriber;
import io.swagger.annotations.Api;

@Path("/email")
@Api(value = "/email")
public class SubscriptionService {

	private static Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

	@Context
	UriInfo uri;

	@POST
	@Path("/subscribe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJSONBulletins(Subscriber subscriber) {
		logger.debug("POST JSON subscribe");

		try {
			SubscriberController.getInstance().createSubscriber(subscriber);
			return Response.ok().build();
		} catch (AlbinaException e) {
			logger.warn("Error subscribe - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@DELETE
	@Path("/unsubscribe")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteSubscriber(String email) {
		logger.debug("DELETE JSON subscriber: " + email);

		try {
			SubscriberController.getInstance().deleteSubscriber(email);
			return Response.ok().build();
		} catch (AlbinaException e) {
			logger.warn("Error unsubscribe - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@PUT
	@Path("/confirm")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response confirmSubscription(String email) {
		logger.debug("POST JSON confirm");

		try {
			SubscriberController.getInstance().confirmSubscriber(email);
			return Response.ok().build();
		} catch (AlbinaException e) {
			logger.warn("Error confirm - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}
}
