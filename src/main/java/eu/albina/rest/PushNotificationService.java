// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.RegionController;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.PushSubscriptionController;
import eu.albina.controller.publication.PushNotificationUtil;
import eu.albina.model.PushSubscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/push")
@Tag(name = "push")
public class PushNotificationService {
	private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

	@Inject
	RegionController regionController;

	@Get("/key")
	@Operation(summary = "Get VAPID public key")
	public Object key() {
		return new Object() {
			public String vapidPublicKey = PushNotificationUtil.getConfiguration().orElseThrow().getVapidPublicKey();
		};
	}

	@Post("/subscribe")
	@Operation(summary = "Subscribe push notification")
	public HttpResponse<?> subscribe(@Body PushSubscription subscription) {
		logger.info("Subscribing {}", subscription);
		PushSubscriptionController.create(subscription);
		new PushNotificationUtil().sendWelcomePushMessage(subscription, regionController.getRegion(subscription.getRegion()));
		return HttpResponse.noContent();
	}

	@Post("/unsubscribe")
	@Operation(summary = "Unsubscribe push notification")
	public HttpResponse<?> unsubscribe(@Body PushSubscription subscription) {
		logger.info("Unsubscribing {}", subscription);
		PushSubscriptionController.delete(subscription);
		return HttpResponse.noContent();
	}
}
