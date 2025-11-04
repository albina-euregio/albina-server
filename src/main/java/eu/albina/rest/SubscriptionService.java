// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.Collections;
import java.util.Objects;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.SubscriberRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import eu.albina.controller.publication.rapidmail.RapidMailController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.RapidMailConfiguration;
import io.micronaut.http.exceptions.HttpStatusException;
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
	public void addSubscriber(@Body EmailSubscription json) {
		logger.debug("POST JSON subscribe");
		Objects.requireNonNull(json.language, "language");
		final Region region = regionRepository.findById(json.regions).orElseThrow();
		final Subscriber subscriber = subscriberRepository.findById(json.email).orElseGet(Subscriber::new);
		subscriber.setEmail(json.email);
		subscriber.setRegions(Collections.singletonList(region));
		subscriber.setLanguage(json.language);

		try {
			RapidMailConfiguration config = rapidMailController.getConfiguration(region, subscriber.getLanguage()).orElseThrow();
			rapidMailController.createRecipient(config, subscriber);
			subscriberRepository.saveOrUpdate(subscriber, Subscriber::getEmail);
		} catch (Exception e) {
			logger.warn("Error subscribe", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}
