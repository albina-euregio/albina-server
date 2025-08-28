// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.Collections;
import java.util.Objects;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import eu.albina.controller.RegionController;
import eu.albina.controller.publication.RapidMailController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.rapidmail.recipients.post.PostRecipientsRequest;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.SubscriberController;
import eu.albina.model.Subscriber;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/email")
@Tag(name = "email")
public class SubscriptionService {

	private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

	@Serdeable
	static class EmailSubscription {
		public String email;

		public String regions;

		public LanguageCode language;

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getRegions() {
			return regions;
		}

		public void setRegions(String regions) {
			this.regions = regions;
		}

		public LanguageCode getLanguage() {
			return language;
		}

		public void setLanguage(LanguageCode language) {
			this.language = language;
		}
	}

	@Post("/subscribe")
	@Operation(summary = "Subscribe email notification")
	public HttpResponse<?> addSubscriber(@Body EmailSubscription json) {
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
}
