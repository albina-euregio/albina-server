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
import java.util.Objects;
import java.util.Optional;

import eu.albina.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.util.HibernateUtil;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.ws.rs.client.Client;

public interface BlogController {
	 Logger logger = LoggerFactory.getLogger(BlogController.class);
	 Client client = HttpClientUtil.newClientBuilder().build();

	static Optional<BlogConfiguration> getConfiguration(Region region, LanguageCode languageCode) {
		Objects.requireNonNull(region, "region");
		Objects.requireNonNull(region.getId(), "region.getId()");
		Objects.requireNonNull(languageCode, "languageCode");
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			try {
                BlogConfiguration configuration = (BlogConfiguration) entityManager.createQuery(HibernateUtil.queryGetBlogConfiguration)
					.setParameter("region", region)
					.setParameter("lang", languageCode)
					.getSingleResult();
				if (configuration == null || configuration.getBlogApiUrl() == null) {
					throw new NoResultException();
				}
				return Optional.of(configuration);
			} catch (PersistenceException e) {
                logger.warn("No blog configuration found for {} [{}]", region.getId(), languageCode);
                return Optional.empty();
            }
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
		return new MultichannelMessage() {
			@Override
			public Region getRegion() {
				return config.getRegion();
			}

			@Override
			public LanguageCode getLanguageCode() {
				return config.getLanguageCode();
			}

			@Override
			public String getWebsiteUrl() {
				return blogPost.getAvalancheReportUrl(config);
			}

			@Override
			public String getAttachmentUrl() {
				return blogPost.getAttachmentUrl();
			}

			@Override
			public String getSubject() {
				return blogPost.getTitle();
			}

			@Override
			public String getSocialMediaText() {
				return blogPost.getTitleAndUrl(config);
			}

			@Override
			public String getHtmlMessage() {
				return blogPost.getContent();
			}

			@Override
			public String toString() {
				return toDefaultString();
			}
		};
	}

	static void sendNewBlogPosts(Region region, LanguageCode lang) {
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
			posting.sendToAllChannels();
			updateConfigurationLastPublished(config, object);
		}
	}

}
