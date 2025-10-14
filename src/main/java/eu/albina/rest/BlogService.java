// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.publication.PushNotificationUtil;
import eu.albina.controller.publication.WhatsAppController;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.publication.BlogController;
import eu.albina.controller.publication.BlogItem;
import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.model.publication.BlogConfiguration;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/blogs")
@Tag(name = "blogs")
public class BlogService {

	private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

	@Inject
	RegionRepository regionRepository;

	@Inject
	private WhatsAppController whatsAppController;

	@Inject
	private PushNotificationUtil pushNotificationUtil;

	@Inject
	BlogController blogController;

	@Post("/publish/latest")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public HttpResponse<String> sendLatestBlogPost(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {}", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.getId());
			posting.sendToAllChannels(whatsAppController, pushNotificationUtil);

			return HttpResponse.ok("{}");
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Post("/publish/latest/email")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public HttpResponse<?> sendLatestBlogPostEmail(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via email", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.getId());
			posting.sendMails();

			return HttpResponse.noContent();
		} catch (AlbinaException e) {
			logger.warn("Error sending latest blog post", e);
			return HttpResponse.badRequest().body(e.toJSON());
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Post("/publish/latest/telegram")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public HttpResponse<?> sendLatestBlogPostTelegram(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via telegram", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.getId());
			posting.sendTelegramMessage();

			return HttpResponse.noContent();
		} catch (AlbinaException e) {
			logger.warn("Error sending latest blog post", e);
			return HttpResponse.badRequest().body(e.toJSON());
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Post("/publish/latest/whatsapp")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public HttpResponse<?> sendLatestBlogPostWhatsApp(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via whatsapp", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.getId());
			posting.sendWhatsAppMessage(whatsAppController);

			return HttpResponse.noContent();
		} catch (AlbinaException e) {
			logger.warn("Error sending latest blog post", e);
			return HttpResponse.badRequest().body(e.toJSON());
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Post("/publish/latest/push")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public HttpResponse<?> sendLatestBlogPostPush(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via push", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.getId());
			posting.sendPushNotifications(pushNotificationUtil);

			return HttpResponse.noContent();
		} catch (AlbinaException e) {
			logger.warn("Error sending latest blog post", e);
			return HttpResponse.badRequest().body(e.toJSON());
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	private BlogConfiguration getBlogConfiguration(String regionId, LanguageCode language) throws AlbinaException {
		if (language == null) {
			throw new AlbinaException("No language defined!");
		}
		Region region = regionRepository.findById(regionId).orElseThrow();
		if (!region.isPublishBlogs()) {
			throw new AlbinaException("Publishing blogs is disabled for region " + regionId);
		}
		return blogController.getConfiguration(region, language).orElseThrow();
	}
}
