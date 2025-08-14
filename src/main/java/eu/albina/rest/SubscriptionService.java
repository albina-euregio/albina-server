// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.albina.controller.RegionController;
import eu.albina.controller.publication.RapidMailController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.rapidmail.recipients.post.PostRecipientsRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.interfaces.DecodedJWT;

import eu.albina.controller.AuthenticationController;
import eu.albina.controller.SubscriberController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Subscriber;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/email")
@Tag(name = "email")
public class SubscriptionService {

	private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

	@Context
	UriInfo uri;

	static class EmailSubscription {
		public String email;

		public String regions;

		public LanguageCode language;
	}

	@POST
	@Path("/subscribe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Subscribe email notification")
	public Response addSubscriber(EmailSubscription json) {
		logger.debug("POST JSON subscribe");
		Objects.requireNonNull(json.language, "language");
		final Region region = RegionController.getInstance().getRegion(json.regions);
		final Subscriber subscriber = new Subscriber();
		subscriber.setEmail(json.email);
		subscriber.setRegions(Collections.singletonList(region));
		subscriber.setLanguage(json.language);

		PostRecipientsRequest recipient = new PostRecipientsRequest();
		recipient.setEmail(subscriber.getEmail());

		try {
			RapidMailConfiguration config = RapidMailController.getConfiguration(region, subscriber.getLanguage(), null).orElseThrow();
			SubscriberController.getInstance().createSubscriber(subscriber);
			RapidMailController.createRecipient(config, recipient);
			return Response.ok().build();
		} catch (Exception e) {
			logger.warn("Error subscribe", e);
			return Response.status(404).type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build();
		}
	}

	// @DELETE
	// @Path("/unsubscribe")
	// @Produces(MediaType.APPLICATION_JSON)
	// @Consumes(MediaType.APPLICATION_JSON)
	public Response deleteSubscriber(EmailSubscription json) {
        logger.debug("DELETE JSON subscriber: {}", json.email);

		try {
			SubscriberController.getInstance().deleteSubscriber(json.email);
			return Response.ok().build();
		} catch (HibernateException he) {
			logger.warn("Error unsubscribe", he);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(he.getMessage()).build();
		}
	}

	static class Token {
		String token;
	}

	// @PUT
	// @Path("/confirm")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	public Response confirmSubscription(Token json) {
		try {
            logger.debug("POST JSON confirm: {}", json.token);
			DecodedJWT decodedToken = AuthenticationController.getInstance().decodeToken(json.token);
			Date currentDate = new Date();
			if (currentDate.after(decodedToken.getExpiresAt())) {
				logger.warn("Token expired!");
				throw new AlbinaException("Token expired!");
			}
			SubscriberController.getInstance().confirmSubscriber(decodedToken.getSubject());
			return Response.ok().build();
		} catch (AlbinaException e) {
			logger.warn("Error confirm", e);
			return Response.status(404).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		} catch (HibernateException he) {
			logger.warn("Error confirm", he);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(he.getMessage()).build();
		}
	}
}
