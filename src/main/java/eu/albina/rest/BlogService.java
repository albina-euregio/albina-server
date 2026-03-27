// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.publication.PublicationController;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;

import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.publication.blog.BlogController;
import eu.albina.controller.publication.blog.BlogItem;
import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.model.publication.BlogConfiguration;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Year;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller("/blogs")
@Tag(name = "blogs")
public class BlogService {

	private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

	@Inject
	RegionRepository regionRepository;

	@Inject
	BlogController blogController;

	@Inject
	private PublicationController publicationController;

	@Post("/publish/latest")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public void sendLatestBlogPost(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {}", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.id());
			posting.sendToAllChannels(publicationController);
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/publish/latest/email")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public void sendLatestBlogPostEmail(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via email", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.id());
			posting.sendMails(publicationController);
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/publish/latest/telegram")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public void sendLatestBlogPostTelegram(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via telegram", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.id());
			posting.sendTelegramMessage(publicationController);
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/publish/latest/whatsapp")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public void sendLatestBlogPostWhatsApp(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via whatsapp", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.id());
			posting.sendWhatsAppMessage(publicationController);
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/publish/latest/push")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public void sendLatestBlogPostPush(@QueryValue("region") String regionId, @QueryValue("lang") LanguageCode language) {
		try {
			logger.debug("POST send latest blog post for {} in {} via push", regionId, language);
			BlogConfiguration config = getBlogConfiguration(regionId, language);
			BlogItem blogPost = blogController.getLatestBlogPost(config);
			MultichannelMessage posting = blogController.getSocialMediaPosting(config, blogPost.id());
			posting.sendPushNotifications(publicationController);
		} catch (Exception e) {
			logger.warn("Error sending latest blog post", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
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

	@Get("/posts")
	@Secured(SecurityRule.IS_ANONYMOUS)
	List<BlogItem> getBlogPosts(
		@QueryValue(value = "region", defaultValue = "AT-07") String regionId,
		@QueryValue(value = "lang", defaultValue = "de") LanguageCode language,
		@QueryValue(defaultValue = "") String searchText,
		@QueryValue(defaultValue = "") String searchCategory,
		@QueryValue(defaultValue = "0") Year year) throws ExecutionException {
		BlogConfiguration configuration = blogController.getConfiguration(new Region(regionId), language).orElseThrow();
		return blogController.blogImplementation(configuration).getCachedBlogPosts(configuration, searchText, searchCategory, year);
	}

	@Get("/post")
	@Secured(SecurityRule.IS_ANONYMOUS)
	BlogItem getBlogPosts(
		@QueryValue(value = "region", defaultValue = "AT-07") String regionId,
		@QueryValue(value = "lang", defaultValue = "de") LanguageCode language,
		@QueryValue String id) throws ExecutionException {
		BlogConfiguration configuration = blogController.getConfiguration(new Region(regionId), language).orElseThrow();
		return blogController.blogImplementation(configuration).getCachedBlogPost(configuration, id);
	}
}
