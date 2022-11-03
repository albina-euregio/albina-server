/*******************************************************************************
 * Copyright (C) 2021 albina
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
package eu.albina.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.PushSubscriptionController;
import eu.albina.model.PushSubscription;
import eu.albina.util.PushNotificationUtil;

@Path("/push")
@Tag(name = "push")
public class PushNotificationService {
	private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

	static class VapidKey {
		public String vapidPublicKey = PushNotificationUtil.getConfiguration().getVapidPublicKey();
	}

	@GET
	@Path("/key")
	@Produces(MediaType.APPLICATION_JSON)
	public VapidKey key() {
		return new VapidKey();
	}

	@POST
	@Path("/subscribe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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
	public Response unsubscribe(PushSubscription subscription) {
		logger.info("Unsubscribing {}", subscription);
		PushSubscriptionController.delete(subscription);
		return Response.ok().build();
	}
}
