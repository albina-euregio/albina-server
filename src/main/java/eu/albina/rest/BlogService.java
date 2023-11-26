/*******************************************************************************
 * Copyright (C) 2022 Norbert Lanzanasto
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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.albina.controller.RegionController;
import eu.albina.controller.publication.BlogItem;
import eu.albina.controller.publication.RapidMailController;
import eu.albina.controller.publication.TelegramController;
import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.TelegramConfiguration;
import eu.albina.controller.publication.PushNotificationUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.publication.BlogController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/blogs")
@Tag(name = "blogs")
public class BlogService {

	private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

	@Context
	UriInfo uri;

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/publish/latest")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendLatestBlogPost(@QueryParam("region") String regionId, @QueryParam("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {}", regionId, language);
			sendLatestBlogPostEmail(regionId, language);
			sendLatestBlogPostTelegram(regionId, language);
			sendLatestBlogPostPush(regionId, language);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/publish/latest/email")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendLatestBlogPostEmail(@QueryParam("region") String regionId, @QueryParam("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via email", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = BlogController.getLatestBlogPost(config);
			RapidMailConfiguration mailConfig = RapidMailController.getConfiguration(config.getRegion(), config.getLanguageCode(), null).orElseThrow();
			MultichannelMessage posting = BlogController.getSocialMediaPosting(config, blogPost.getId());
			RapidMailController.sendEmail(mailConfig, posting);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error sending latest blog post", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/publish/latest/telegram")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendLatestBlogPostTelegram(@QueryParam("region") String regionId, @QueryParam("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via telegram", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = BlogController.getLatestBlogPost(config);
			MultichannelMessage posting = BlogController.getSocialMediaPosting(config, blogPost.getId());
			TelegramConfiguration telegramConfig = TelegramController.getConfiguration(config.getRegion(), config.getLanguageCode()).orElseThrow();
			TelegramController.trySend(telegramConfig, posting, 3);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error sending latest blog post", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/publish/latest/push")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendLatestBlogPostPush(@QueryParam("region") String regionId, @QueryParam("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via push", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = BlogController.getLatestBlogPost(config);
			MultichannelMessage posting = BlogController.getSocialMediaPosting(config, blogPost.getId());
			new PushNotificationUtil().send(posting);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error sending latest blog post", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	private static BlogConfiguration getBlogConfiguration(String regionId, LanguageCode language) throws AlbinaException {
		if (language == null) {
			throw new AlbinaException("No language defined!");
		}
		Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
		if (region.isPublishBlogs()) {
			throw new AlbinaException("Publishing blogs is disabled for region " + regionId);
		}
		return BlogController.getConfiguration(region, language).orElseThrow();
	}
}
