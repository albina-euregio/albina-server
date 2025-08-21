// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

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

@Controller("/email")
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

	@Post("/subscribe")
	@Operation(summary = "Subscribe email notification")
	public HttpResponse<?> addSubscriber(EmailSubscription json) {
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
			return HttpResponse.ok();
		} catch (Exception e) {
			logger.warn("Error subscribe", e);
			return HttpResponse.badRequest().body(e.getMessage());
		}
	}

	// @DELETE
	// @Path("/unsubscribe")
	// @Produces(MediaType.APPLICATION_JSON)
	// @Consumes(MediaType.APPLICATION_JSON)
	public HttpResponse<?> deleteSubscriber(EmailSubscription json) {
        logger.debug("DELETE JSON subscriber: {}", json.email);

		try {
			SubscriberController.getInstance().deleteSubscriber(json.email);
			return HttpResponse.ok();
		} catch (HibernateException he) {
			logger.warn("Error unsubscribe", he);
			return HttpResponse.badRequest().body(he.getMessage());
		}
	}

	static class Token {
		String token;
	}

	// @PUT
	// @Path("/confirm")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	public HttpResponse<?> confirmSubscription(Token json) {
		try {
            logger.debug("POST JSON confirm: {}", json.token);
			DecodedJWT decodedToken = AuthenticationController.getInstance().decodeToken(json.token);
			Date currentDate = new Date();
			if (currentDate.after(decodedToken.getExpiresAt())) {
				logger.warn("Token expired!");
				throw new AlbinaException("Token expired!");
			}
			SubscriberController.getInstance().confirmSubscriber(decodedToken.getSubject());
			return HttpResponse.ok();
		} catch (AlbinaException e) {
			logger.warn("Error confirm", e);
			return HttpResponse.badRequest().body(e.toJSON());
		} catch (HibernateException he) {
			logger.warn("Error confirm", he);
			return HttpResponse.badRequest().body(he.getMessage());
		}
	}
}
