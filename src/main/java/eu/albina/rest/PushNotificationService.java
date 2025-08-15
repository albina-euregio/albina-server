// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.PushSubscriptionController;
import eu.albina.controller.publication.PushNotificationUtil;
import eu.albina.model.PushSubscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/push")
@Tag(name = "push")
public class PushNotificationService {
	private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

	@GET
	@Path("/key")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get VAPID public key")
	public Object key() {
		return new Object() {
			public String vapidPublicKey = PushNotificationUtil.getConfiguration().orElseThrow().getVapidPublicKey();
		};
	}

	@POST
	@Path("/subscribe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Subscribe push notification")
	public Response subscribe(PushSubscription subscription) {
		logger.info("Subscribing {}", subscription);
		PushSubscriptionController.create(subscription);
		new PushNotificationUtil().sendWelcomePushMessage(subscription);
		return Response.ok().build();
	}

	@POST
	@Path("/unsubscribe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Unsubscribe push notification")
	public Response unsubscribe(PushSubscription subscription) {
		logger.info("Unsubscribing {}", subscription);
		PushSubscriptionController.delete(subscription);
		return Response.ok().build();
	}
}
