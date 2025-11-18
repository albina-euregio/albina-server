// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import eu.albina.controller.CrudRepository;
import eu.albina.controller.RegionRepository;
import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.controller.publication.PublicationController;
import eu.albina.controller.publication.rapidmail.RapidMailController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.model.publication.RapidMailConfiguration;
import io.micronaut.data.annotation.Repository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class BlogController {
	private static final Logger logger = LoggerFactory.getLogger(BlogController.class);

	@Inject
	Blogger blogger;

	@Inject
	Wordpress wordpress;

	@Inject
	BlogConfigurationRepository blogConfigurationRepository;

	@Inject
	RegionRepository regionRepository;

	@Inject
	PublicationController publicationController;

	@Inject
	RapidMailController rapidMailController;

	@Repository
	public interface BlogConfigurationRepository extends CrudRepository<BlogConfiguration, Long> {
		Optional<BlogConfiguration> findByBlogId(String blogId);

		Optional<BlogConfiguration> findByRegionAndLanguageCode(Region region, LanguageCode languageCode);
	}

	public Optional<BlogConfiguration> getConfiguration(Region region, LanguageCode languageCode) throws NoResultException {
		return blogConfigurationRepository.findByRegionAndLanguageCode(region, languageCode);
	}

	public void updateConfigurationLastPublished(BlogConfiguration config, BlogItem object) {
		if (!object.getPublished().toInstant().isAfter(config.getLastPublishedTimestamp().toInstant())) {
			return;
		}
		config.setLastPublishedBlogId(object.getId());
		config.setLastPublishedTimestamp(object.getPublished());
		logger.info("Updating lastPublishedTimestamp={} lastPublishedBlogId={} for {}",
			config.getLastPublishedTimestamp(), config.getLastPublishedBlogId(), config);
		blogConfigurationRepository.update(config);
	}

	public List<? extends BlogItem> getBlogPosts(BlogConfiguration config) throws IOException, InterruptedException {
		if (config == null || config.getBlogApiUrl() == null) {
			return Collections.emptyList();
		}

		List<? extends BlogItem> blogPosts = config.isBlogger()
			? blogger.getBlogPosts(config)
			: wordpress.getBlogPosts(config);
		logger.info("Found {} new blog posts for {}", blogPosts.size(), config);
		return blogPosts;
	}

	public BlogItem getLatestBlogPost(BlogConfiguration config) throws IOException, InterruptedException {
		Objects.requireNonNull(config, "config");
		Objects.requireNonNull(config.getBlogApiUrl(), "config.getBlogApiUrl");

		BlogItem blogPost = config.isBlogger()
			? blogger.getLatestBlogPost(config)
			: wordpress.getLatestBlogPost(config);
		logger.info("Fetched latest blog post for region={} lang={}", config.getRegion().getId(), config.getLanguageCode().toString());
		return blogPost;
	}

	public BlogItem getBlogPost(BlogConfiguration config, String blogPostId) throws IOException, InterruptedException {
		Objects.requireNonNull(config, "config");
		Objects.requireNonNull(config.getBlogApiUrl(), "config.getBlogApiUrl");

		return config.isBlogger()
			? blogger.getBlogPost(config, blogPostId)
			: wordpress.getBlogPost(config, blogPostId);
	}

	public MultichannelMessage getSocialMediaPosting(BlogConfiguration config, String blogPostId) throws IOException, InterruptedException {
		BlogItem blogPost = getBlogPost(config, blogPostId);
		return new BlogItemMultichannelMessage(config, blogPost);
	}

	public void sendNewBlogPosts(Region region, LanguageCode lang) throws IOException, InterruptedException {
		if (!region.isPublishBlogs()) {
			logger.debug("Publishing blogs is disabled for region {}", region);
			return;
		}

		BlogConfiguration config = getConfiguration(region, lang).orElse(null);
		if (config == null) {
			logger.debug("No blog configuration found for region {} and lang {}", region, lang);
			return;
		}

		List<? extends BlogItem> blogPosts;
		try {
			blogPosts = getBlogPosts(config);
		} catch (IOException e) {
			logger.warn("Blog posts could not be retrieved: " + region.getId() + ", " + lang.toString(), e);
			return;
		}

		for (BlogItem object : blogPosts) {
			MultichannelMessage posting = getSocialMediaPosting(config, object.getId());
			posting.sendToAllChannels(publicationController);
			updateConfigurationLastPublished(config, object);
		}
	}

	@Transactional
	public void sendNewBlogPosts(String blogId, String subjectMatter) throws IOException, InterruptedException {
		Objects.requireNonNull(blogId);
		Objects.requireNonNull(subjectMatter);
		BlogConfiguration config = blogConfigurationRepository.findByBlogId(blogId).orElse(null);
		if (config == null) {
			logger.debug("No blog configuration found for {}", blogId);
			return;
		}
		Region regionOverride = regionRepository.findById(BlogConfiguration.TECH_BLOG_REGION_OVERRIDE).orElseThrow();
		config.setRegion(regionOverride);

		List<? extends BlogItem> blogPosts;
		try {
			blogPosts = getBlogPosts(config);
		} catch (IOException | InterruptedException e) {
			logger.warn("Blog posts could not be retrieved: " + config, e);
			return;
		}

		for (BlogItem object : blogPosts) {
			MultichannelMessage posting = getSocialMediaPosting(config, object.getId());
			posting.tryRunWithLogging("Email newsletter", () -> {
				RapidMailConfiguration mailConfig = rapidMailController.getConfiguration(config.getLanguageCode(), subjectMatter)
					.orElseThrow(() -> new NoSuchElementException("No RapidMailConfiguration found for " + subjectMatter));
				mailConfig.setRegion(regionOverride);
				rapidMailController.sendEmail(mailConfig, posting.getHtmlMessage(), posting.getSubject());
				return null;
			});
			updateConfigurationLastPublished(config, object);
		}

	}

}
