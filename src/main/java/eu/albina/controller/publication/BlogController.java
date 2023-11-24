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

import eu.albina.exception.AlbinaException;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.TelegramConfiguration;
import eu.albina.util.HttpClientUtil;
import eu.albina.util.LinkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
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
			RapidMailConfiguration mailConfig = RapidMailController.getConfiguration(config.getRegion(), config.getLanguageCode(), null).orElseThrow();
			sendBlogPostToRapidmail(config, object, mailConfig);
		} catch (Exception e) {
			logger.warn("Blog post could not be sent to email: " + config, e);
		}

		try {
			TelegramConfiguration telegramConfig = TelegramController.getConfiguration(config.getRegion(), config.getLanguageCode()).orElseThrow();
			sendBlogPostToTelegramChannel(config, object, telegramConfig);
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

	static void sendBlogPostToTelegramChannel(BlogConfiguration config, BlogItem item, TelegramConfiguration telegramConfig) throws IOException {
		logger.info("Sending blog post to telegram channel ...");
		String message = getBlogMessage(config, item);
		String attachmentUrl = item.getAttachmentUrl();

		TelegramController.sendPhotoOrMessage(telegramConfig, message, attachmentUrl);
	}

	static void sendBlogPostToRapidmail(BlogConfiguration config, BlogItem item, RapidMailConfiguration mailConfig) throws IOException, AlbinaException {
		logger.debug("Sending new blog post to rapidmail ...");

		String subject = item.getTitle();
		String blogPostId = item.getId();
		String htmlString = getBlogPost(config, blogPostId).getContent();
		if (htmlString == null || htmlString.isEmpty()) {
			return;
		}

		RapidMailController.sendEmail(mailConfig, htmlString, subject);
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
