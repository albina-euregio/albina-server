// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.publication.PushNotificationUtil;
import eu.albina.model.PushSubscription;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/push")
@Tag(name = "push")
@Secured(SecurityRule.IS_ANONYMOUS)
public class PushNotificationService {
	private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

	@Inject
	RegionRepository regionRepository;

	@Inject
	PushNotificationUtil.PushSubscriptionRepository pushSubscriptionRepository;

	@Inject
	private PushNotificationUtil pushNotificationUtil;

	@Serdeable
	record VapidPublicKey(String vapidPublicKey) {
	}

	@Get("/key")
	@Operation(summary = "Get VAPID public key")
	public Object key() {
	return new VapidPublicKey(pushNotificationUtil.getConfiguration().getVapidPublicKey());
	}

	@Post("/subscribe")
	@Operation(summary = "Subscribe push notification")
	public void subscribe(@Body PushSubscription subscription) {
		logger.info("Subscribing {}", subscription);
		pushSubscriptionRepository.save(subscription);
		pushNotificationUtil.sendWelcomePushMessage(subscription, regionRepository.findById(subscription.getRegion()).orElseThrow());
	}

	@Post("/unsubscribe")
	@Operation(summary = "Unsubscribe push notification")
	public void unsubscribe(@Body PushSubscription subscription) {
		logger.info("Unsubscribing {}", subscription);
		subscription = pushSubscriptionRepository.findByEndpointAndAuthAndP256dh(subscription.getEndpoint(), subscription.getAuth(), subscription.getP256dh()).orElseThrow();
		pushSubscriptionRepository.delete(subscription);
	}
}
