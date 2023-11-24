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
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import eu.albina.util.HttpClientUtil;
import eu.albina.util.LinkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.util.EmailUtil;
import eu.albina.util.HibernateUtil;
import eu.albina.util.PushNotificationUtil;

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

	static BlogItem getLatestBlogPost(BlogConfiguration config) throws IOException {
		if (config == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}

		BlogItem blogPost = config.isBlogger()
			? Blogger.getLatestBlogPost(config, client)
			: Wordpress.getLatestBlogPost(config, client);
		logger.info("Fetched latest blog post for region={} lang={}", config.getRegion().getId(), config.getLanguageCode().toString());
		return blogPost;
	}

	static BlogItem getBlogPost(BlogConfiguration config, String blogPostId) throws IOException {
		if (config == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}

		return config.isBlogger()
			? Blogger.getBlogPost(config, blogPostId, client)
			: Wordpress.getBlogPost(config, blogPostId, client);
	}

	static void sendBlogPost(BlogConfiguration config, BlogItem object) {
		try {
			sendBlogPostToRapidmail(config, object);
		} catch (Exception e) {
			logger.warn("Blog post could not be sent to email: " + config, e);
		}

		try {
			sendBlogPostToTelegramChannel(config, object);
		} catch (Exception e) {
			logger.warn("Blog post could not be sent to telegram channel: " + config, e);
		}

		try {
			sendBlogPostToPushNotification(config, object);
		} catch (Exception e) {
			logger.warn("Blog post could not be sent to push notifications: " + config, e);
		}

		updateConfigurationLastPublished(config, object);
	}

	static void sendBlogPostToTelegramChannel(BlogConfiguration config, BlogItem item) throws IOException, URISyntaxException {
		logger.info("Sending new blog post to telegram channel ...");

		String message = getBlogMessage(config, item);
		String attachmentUrl = item.getAttachmentUrl();
		TelegramController telegramController = TelegramController.getInstance();

		if (attachmentUrl != null) {
			telegramController.sendPhoto(config.getRegion(), config.getLanguageCode(), message, attachmentUrl);
		} else {
			telegramController.sendMessage(config.getRegion(), config.getLanguageCode(), message);
		}
	}

	static void sendBlogPostToRapidmail(BlogConfiguration config, BlogItem item) throws IOException, URISyntaxException {
		logger.debug("Sending new blog post to rapidmail ...");

		String subject = item.getTitle();
		String blogPostId = item.getId();
		String htmlString = getBlogPost(config, blogPostId).getContent();
		if (htmlString == null || htmlString.isEmpty()) {
			return;
		}

		EmailUtil.getInstance().sendBlogPostEmailRapidmail(config.getLanguageCode(), config.getRegion(), htmlString, subject);
	}

	static void sendBlogPostToPushNotification(BlogConfiguration config, BlogItem object) {
		String message = getBlogMessage(config, object);
		String attachmentUrl = object.getAttachmentUrl();
		String blogUrl = getBlogUrl(config, object);
		PushNotificationUtil pushNotificationUtil = new PushNotificationUtil();
		pushNotificationUtil.sendBulletinNewsletter(message, config.getLanguageCode(), config.getRegion(), attachmentUrl, blogUrl);
	}

	static String getBlogMessage(BlogConfiguration config, BlogItem item) {
		return item.getTitle() + ": " + getBlogUrl(config, item);
	}

	static String getBlogUrl(BlogConfiguration config, BlogItem item) {
		return LinkUtil.getAvalancheReportFullBlogUrl(config.getLanguageCode(), config.getRegion()) + config.getBlogUrl() + "/" + item.getId();
	}

}
