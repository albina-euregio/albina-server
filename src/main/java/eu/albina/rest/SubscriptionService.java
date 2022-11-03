/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.albina.controller.RegionController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import org.hibernate.HibernateException;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
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

	static class SubscriberJson {
		public String email;

		public String regions;

		public LanguageCode language;
	}

	@POST
	@Path("/subscribe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addSubscriber(SubscriberJson json) {
		logger.debug("POST JSON subscribe");
		final Region region = RegionController.getInstance().getRegion(json.regions);
		final List<Region> regions = Collections.singletonList(region);
		final Subscriber subscriber = new Subscriber();
		subscriber.setEmail(json.email);
		subscriber.setRegions(regions);
		subscriber.setLanguage(json.language);

		try {
			SubscriberController.getInstance().createSubscriber(subscriber);
			SubscriberController.getInstance().createSubscriberRapidmail(subscriber);
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
	public Response deleteSubscriber(String email) {
		JSONObject json = new JSONObject(email);
        logger.debug("DELETE JSON subscriber: {}", json.getString("email"));

		try {
			SubscriberController.getInstance().deleteSubscriber(json.getString("email"));
			return Response.ok().build();
		} catch (HibernateException | JSONException he) {
			logger.warn("Error unsubscribe", he);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(he.getMessage()).build();
		}
	}

	// @PUT
	// @Path("/confirm")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	public Response confirmSubscription(String token) {
		try {
			JSONObject json = new JSONObject(token);
            logger.debug("POST JSON confirm: {}", json.getString("token"));
			DecodedJWT decodedToken = AuthenticationController.getInstance().decodeToken(json.getString("token"));
			Date currentDate = new Date();
			if (currentDate.after(decodedToken.getExpiresAt())) {
				logger.warn("Token expired!");
				throw new AlbinaException("Token expired!");
			}
			SubscriberController.getInstance().confirmSubscriber(decodedToken.getSubject());
			return Response.ok().build();
		} catch (AlbinaException e) {
			logger.warn("Error confirm", e);
			return Response.status(404).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		} catch (HibernateException | JSONException he) {
			logger.warn("Error confirm", he);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(he.getMessage()).build();
		}
	}
}
