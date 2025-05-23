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
package eu.albina.controller.publication;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.util.HibernateUtil;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import javax.ws.rs.client.Client;

public interface BlogController {
	Logger logger = LoggerFactory.getLogger(BlogController.class);
	Client client = HttpClientUtil.newClientBuilder().build();

	static BlogConfiguration getConfiguration(Region region, LanguageCode languageCode) throws NoResultException {
		Objects.requireNonNull(region, "region");
		Objects.requireNonNull(region.getId(), "region.getId()");
		Objects.requireNonNull(languageCode, "languageCode");

		return HibernateUtil.getInstance().run(entityManager -> {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<BlogConfiguration> select = criteriaBuilder.createQuery(BlogConfiguration.class);
			Root<BlogConfiguration> root = select.from(BlogConfiguration.class);
			select.where(
				criteriaBuilder.notEqual(root.get("blogId"), BlogConfiguration.TECH_BLOG_ID),
				criteriaBuilder.equal(root.get("lang"), languageCode),
				criteriaBuilder.equal(root.get("region"), region)
			);
			return entityManager.createQuery(select).getSingleResult();
		});
	}

	static void updateConfigurationLastPublished(BlogConfiguration config, BlogItem object) {
		if (!object.getPublished().toInstant().isAfter(config.getLastPublishedTimestamp().toInstant())) {
			return;
		}
		config.setLastPublishedBlogId(object.getId());
		config.setLastPublishedTimestamp(object.getPublished());
		logger.info("Updating lastPublishedTimestamp={} lastPublishedBlogId={} for {}",
			config.getLastPublishedTimestamp(), config.getLastPublishedBlogId(), config);
		HibernateUtil.getInstance().runTransaction(entityManager -> entityManager.merge(config));
	}

	static List<? extends BlogItem> getBlogPosts(BlogConfiguration config) throws IOException {
		if (config == null || config.getBlogApiUrl() == null) {
			return Collections.emptyList();
		}

		List<? extends BlogItem> blogPosts = config.isBlogger()
			? Blogger.getBlogPosts(config, client)
			: Wordpress.getBlogPosts(config, client);
		logger.info("Found {} new blog posts for {}", blogPosts.size(), config);
		return blogPosts;
	}

	static BlogItem getLatestBlogPost(BlogConfiguration config) {
		Objects.requireNonNull(config, "config");
		Objects.requireNonNull(config.getBlogApiUrl(), "config.getBlogApiUrl");

		BlogItem blogPost = config.isBlogger()
			? Blogger.getLatestBlogPost(config, client)
			: Wordpress.getLatestBlogPost(config, client);
		logger.info("Fetched latest blog post for region={} lang={}", config.getRegion().getId(), config.getLanguageCode().toString());
		return blogPost;
	}

	static BlogItem getBlogPost(BlogConfiguration config, String blogPostId) {
		Objects.requireNonNull(config, "config");
		Objects.requireNonNull(config.getBlogApiUrl(), "config.getBlogApiUrl");

		return config.isBlogger()
			? Blogger.getBlogPost(config, blogPostId, client)
			: Wordpress.getBlogPost(config, blogPostId, client);
	}

	static MultichannelMessage getSocialMediaPosting(BlogConfiguration config, String blogPostId) {
		BlogItem blogPost = getBlogPost(config, blogPostId);
		return MultichannelMessage.of(config, blogPost);
	}

	static void sendNewBlogPosts(Region region, LanguageCode lang) {
		if (!region.isPublishBlogs()) {
			logger.debug("Publishing blogs is disabled for region {}", region);
			return;
		}

		BlogConfiguration config;
		try {
			config = getConfiguration(region, lang);
		} catch (NoResultException e) {
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
			posting.sendToAllChannels();
			updateConfigurationLastPublished(config, object);
		}
	}

	static void sendNewBlogPosts(String blogId, String subjectMatter, Region regionOverride) {
		BlogConfiguration config;
		try {
			config = HibernateUtil.getInstance().run(entityManager -> {
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<BlogConfiguration> select = criteriaBuilder.createQuery(BlogConfiguration.class);
				Root<BlogConfiguration> root = select.from(BlogConfiguration.class);
				select.where(criteriaBuilder.equal(root.get("blogId"), blogId));
				return entityManager.createQuery(select).getSingleResult();
			});
		} catch (NoResultException e) {
			logger.debug("No blog configuration found for {}", blogId);
			return;
		}
		config.setRegion(regionOverride);

		List<? extends BlogItem> blogPosts;
		try {
			blogPosts = getBlogPosts(config);
		} catch (IOException e) {
			logger.warn("Blog posts could not be retrieved: " + config, e);
			return;
		}

		for (BlogItem object : blogPosts) {
			MultichannelMessage posting = getSocialMediaPosting(config, object.getId());
			posting.tryRunWithLogging("Email newsletter", () -> {
				RapidMailConfiguration mailConfig = RapidMailController.getConfiguration(null, config.getLanguageCode(), subjectMatter)
					.orElseThrow(() -> new NoSuchElementException("No RapidMailConfiguration found for " + subjectMatter));
				mailConfig.setRegion(regionOverride);
				RapidMailController.sendEmail(mailConfig, posting.getHtmlMessage(), posting.getSubject());
				return null;
			});
			updateConfigurationLastPublished(config, object);
		}

	}

}
