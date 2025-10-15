// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.Collections;
import java.util.Objects;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.SubscriberRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import eu.albina.controller.publication.RapidMailController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.rapidmail.recipients.post.PostRecipientsRequest;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Subscriber;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/email")
@Tag(name = "email")
@Secured(SecurityRule.IS_ANONYMOUS)
public class SubscriptionService {

	private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

	@Inject
	RegionRepository regionRepository;

	@Inject
	SubscriberRepository subscriberRepository;

	@Inject
	private RapidMailController rapidMailController;

	@Serdeable
	public record EmailSubscription(String email, String regions, LanguageCode language) {
	}

	@Post("/subscribe")
	@Operation(summary = "Subscribe email notification")
	public HttpResponse<?> addSubscriber(@Body EmailSubscription json) {
		logger.debug("POST JSON subscribe");
		Objects.requireNonNull(json.language, "language");
		final Region region = regionRepository.findById(json.regions).orElseThrow();
		final Subscriber subscriber = new Subscriber();
		subscriber.setEmail(json.email);
		subscriber.setRegions(Collections.singletonList(region));
		subscriber.setLanguage(json.language);

		PostRecipientsRequest recipient = new PostRecipientsRequest();
		recipient.setEmail(subscriber.getEmail());

		try {
			RapidMailConfiguration config = rapidMailController.getConfiguration(region, subscriber.getLanguage(), null).orElseThrow();
			subscriberRepository.save(subscriber);
			rapidMailController.createRecipient(config, recipient);
			return HttpResponse.ok();
		} catch (Exception e) {
			logger.warn("Error subscribe", e);
			return HttpResponse.badRequest().body(e.getMessage());
		}
	}
}
