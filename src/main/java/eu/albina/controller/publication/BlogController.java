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

import javax.persistence.PersistenceException;
import javax.ws.rs.client.Client;

public class BlogController {
	private static final Logger logger = LoggerFactory.getLogger(BlogController.class);
	private static BlogController instance = null;

	private final Client client = HttpClientUtil.newClientBuilder().build();

	/**
	 * Returns the {@code BlogController} object associated with the current Java
	 * application.
	 *
	 * @return the {@code BlogController} object associated with the current Java
	 * application.
	 */
	public static BlogController getInstance() {
		if (instance == null) {
			instance = new BlogController();
		}
		return instance;
	}

	protected Optional<BlogConfiguration> getConfiguration(Region region, LanguageCode languageCode) {
		Objects.requireNonNull(region, "region");
		Objects.requireNonNull(region.getId(), "region.getId()");
		Objects.requireNonNull(languageCode, "languageCode");
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			try {
                BlogConfiguration configuration = (BlogConfiguration) entityManager.createQuery(HibernateUtil.queryGetBlogConfiguration)
					.setParameter("region", region)
					.setParameter("lang", languageCode)
					.getSingleResult();
				return Optional.ofNullable(configuration);
			} catch (PersistenceException e) {
                logger.warn("No blog configuration found for {} [{}]", region.getId(), languageCode);
                return Optional.empty();
            }
		});
	}

	protected void updateConfigurationLastPublished(BlogConfiguration config, BlogItem object) {
		if (!object.getPublished().toInstant().isAfter(config.getLastPublishedTimestamp().toInstant())) {
			return;
		}
		config.setLastPublishedBlogId(object.getId());
		config.setLastPublishedTimestamp(object.getPublished());
		logger.info("Updating lastPublishedTimestamp={} lastPublishedBlogId={} for {} [{}]",
			config.getLastPublishedTimestamp(), config.getLastPublishedBlogId(), config.getRegion().getId(), config.getLanguageCode());
		HibernateUtil.getInstance().runTransaction(entityManager -> entityManager.merge(config));
	}

	protected List<? extends BlogItem> getBlogPosts(BlogConfiguration config) throws IOException {
		if (config == null || config.getBlogApiUrl() == null) {
			return Collections.emptyList();
		}

		List<? extends BlogItem> blogPosts = config.isBlogger()
			? Blogger.getBlogPosts(config, client)
			: Wordpress.getBlogPosts(config, client);
		logger.info("Found {} new blog posts for region={} lang={}", blogPosts.size(), config.getRegion().getId(), config.getLanguageCode().toString());
		return blogPosts;
	}

	public BlogItem getLatestBlogPost(Region region, LanguageCode lang) throws IOException, NoSuchElementException {
		return getLatestBlogPost(getConfiguration(region, lang).orElseThrow());
	}

	protected BlogItem getLatestBlogPost(BlogConfiguration config) throws IOException {
		if (config == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}

		BlogItem blogPost = config.isBlogger()
			? Blogger.getLatestBlogPost(config, client)
			: Wordpress.getLatestBlogPost(config, client);
		logger.info("Fetched latest blog post for region={} lang={}", config.getRegion().getId(), config.getLanguageCode().toString());
		return blogPost;
	}

	protected BlogItem getBlogPost(BlogConfiguration config, String blogPostId) throws IOException {
		if (config == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}

		return config.isBlogger()
			? Blogger.getBlogPost(config, blogPostId, client)
			: Wordpress.getBlogPost(config, blogPostId, client);
	}

	public void sendNewBlogPosts(Region region, LanguageCode lang) {
        if (!region.isPublishBlogs()) {
			return;
		}
        BlogConfiguration config = this.getConfiguration(region, lang).orElse(null);
        if (config == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            List<? extends BlogItem> blogPosts = getBlogPosts(config);
            for (BlogItem object : blogPosts) {
                sendNewBlogPostToRapidmail(config, object);
                sendNewBlogPostToTelegramChannel(config, object);
                sendNewBlogPostToPushNotification(config, object);
                updateConfigurationLastPublished(config, object);
            }
        } catch (IOException e) {
            logger.warn("Blog posts could not be retrieved: " + region.getId() + ", " + lang.toString(), e);
        }
    }

	public void sendLatestBlogPost(Region region, LanguageCode lang) {
        if (!region.isPublishBlogs()) {
			return;
        }
		BlogConfiguration config = this.getConfiguration(region, lang).orElse(null);
        if (config == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            BlogItem blogPost = getLatestBlogPost(config);
            sendNewBlogPostToRapidmail(config, blogPost);
            sendNewBlogPostToTelegramChannel(config, blogPost);
            sendNewBlogPostToPushNotification(config, blogPost);
            updateConfigurationLastPublished(config, blogPost);
        } catch (IOException e) {
            logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
        }
    }

	public void sendLatestBlogPostEmail(Region region, LanguageCode lang) {
        if (!region.isPublishBlogs()) {
            return;
        }
		BlogConfiguration config = this.getConfiguration(region, lang).orElse(null);
        if (config == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            BlogItem blogPost = getLatestBlogPost(config);
            sendNewBlogPostToRapidmail(config, blogPost);
        } catch (IOException e) {
            logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
        }
    }

	public void sendLatestBlogPostTelegram(Region region, LanguageCode lang) {
        if (!region.isPublishBlogs()) {
            return;
        }
		BlogConfiguration config = this.getConfiguration(region, lang).orElse(null);
        if (config == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            BlogItem blogPost = getLatestBlogPost(config);
            sendNewBlogPostToTelegramChannel(config, blogPost);
        } catch (IOException e) {
            logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
        }
    }

	public void sendLatestBlogPostPush(Region region, LanguageCode lang) {
        if (!region.isPublishBlogs()) {
            return;
        }
		BlogConfiguration config = this.getConfiguration(region, lang).orElse(null);
        if (config == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            BlogItem blogPost = getLatestBlogPost(config);
            sendNewBlogPostToPushNotification(config, blogPost);
        } catch (IOException e) {
            logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
        }
    }

	void sendNewBlogPostToTelegramChannel(BlogConfiguration config, BlogItem item) {
		logger.info("Sending new blog post to telegram channel ...");

		try {
			String message = getBlogMessage(config, item);
			String attachmentUrl = item.getAttachmentUrl();
			TelegramController telegramController = TelegramController.getInstance();

			if (attachmentUrl != null) {
				telegramController.sendPhoto(config.getRegion(), config.getLanguageCode(), message, attachmentUrl);
			} else {
				telegramController.sendMessage(config.getRegion(), config.getLanguageCode(), message);
			}
		} catch (Exception e) {
			logger.warn("Blog post could not be sent to telegram channel: " + config.getRegion() + "," + config.getLanguageCode().toString(), e);
		}
	}

	private void sendNewBlogPostToRapidmail(BlogConfiguration config, BlogItem item) {
		logger.debug("Sending new blog post to rapidmail ...");

		try {
			String subject = item.getTitle();
			String blogPostId = item.getId();
			String htmlString = getBlogPost(config, blogPostId).getContent();
			if (htmlString != null && !htmlString.isEmpty())
				EmailUtil.getInstance().sendBlogPostEmailRapidmail(config.getLanguageCode(), config.getRegion(), htmlString, subject);
		} catch (Exception e) {
			logger.warn("Blog post email could not be sent: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
		}
	}

	private void sendNewBlogPostToPushNotification(BlogConfiguration config, BlogItem object) {
		String message = getBlogMessage(config, object);
		String attachmentUrl = object.getAttachmentUrl();
		String blogUrl = getBlogUrl(config, object);
		PushNotificationUtil pushNotificationUtil = new PushNotificationUtil();
		pushNotificationUtil.sendBulletinNewsletter(message, config.getLanguageCode(), config.getRegion(), attachmentUrl, blogUrl);
	}

	private String getBlogMessage(BlogConfiguration config, BlogItem item) {
		return item.getTitle() + ": " + getBlogUrl(config, item);
	}

	private String getBlogUrl(BlogConfiguration config, BlogItem item) {
		return LinkUtil.getAvalancheReportFullBlogUrl(config.getLanguageCode(), config.getRegion()) + config.getBlogUrl() + "/" + item.getId();
	}

}
