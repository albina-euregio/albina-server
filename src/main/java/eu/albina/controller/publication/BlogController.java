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

import com.google.common.base.Strings;

import eu.albina.util.HttpClientUtil;
import eu.albina.util.LinkUtil;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.util.EmailUtil;
import eu.albina.util.HibernateUtil;
import eu.albina.util.PushNotificationUtil;

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

	protected BlogConfiguration getConfiguration(Region region, LanguageCode languageCode) {
		if (region == null || Strings.isNullOrEmpty(region.getId()) || languageCode == null) {
			throw new HibernateException("No region or language defined!");
		}
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			try {
				return (BlogConfiguration) entityManager.createQuery(HibernateUtil.queryGetGoogleBloggerConfiguration)
					.setParameter("region", region)
					.setParameter("lang", languageCode).getSingleResult();
            } catch (Exception e) {
                logger.warn("No google blogger configuration found for {} [{}]", region.getId(), languageCode);
                return null;
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
		if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
			return Collections.emptyList();
		}

		List<? extends BlogItem> blogPosts = Blogger.getBlogPosts(config, client);
		logger.info("Found {} new blog posts for region={} lang={}", blogPosts.size(), config.getRegion().getId(), config.getLanguageCode().toString());
		return blogPosts;
	}

	protected BlogItem getLatestBlogPost(BlogConfiguration config) throws IOException {
		if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}

		BlogItem blogPost = Blogger.getLatestBlogPost(config, client);
		logger.info("Fetched latest blog post for region={} lang={}", config.getRegion().getId(), config.getLanguageCode().toString());
		return blogPost;
	}

	protected String getBlogPost(BlogConfiguration config, String blogPostId) throws IOException {
		if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}

		return Blogger.getBlogPost(config, blogPostId, client);
	}

	public void sendNewBlogPosts(Region region, LanguageCode lang) {
        if (!region.isPublishBlogs()) {
			return;
		}
        BlogConfiguration config = this.getConfiguration(region, lang);
        if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            List<? extends BlogItem> blogPosts = getBlogPosts(config);
            for (BlogItem object : blogPosts) {
                sendNewBlogPostToRapidmail(object, region, lang, false);
                sendNewBlogPostToTelegramChannel(object, region, lang, false);
                sendNewBlogPostToPushNotification(region, lang, object, false);
                updateConfigurationLastPublished(config, object);
            }
        } catch (IOException e) {
            logger.warn("Blog posts could not be retrieved: " + region.getId() + ", " + lang.toString(), e);
        }
    }

	public void sendLatestBlogPost(Region region, LanguageCode lang, boolean test) {
        if (!region.isPublishBlogs()) {
			return;
        }
		BlogConfiguration config = this.getConfiguration(region, lang);
        if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            BlogItem blogPost = getLatestBlogPost(config);
            sendNewBlogPostToRapidmail(blogPost, config.getRegion(), config.getLanguageCode(), test);
            sendNewBlogPostToTelegramChannel(blogPost, config.getRegion(), config.getLanguageCode(), test);
            sendNewBlogPostToPushNotification(config.getRegion(), config.getLanguageCode(), blogPost, test);
            updateConfigurationLastPublished(config, blogPost);
        } catch (IOException e) {
            logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
        }
    }

	public void sendLatestBlogPostEmail(Region region, LanguageCode lang, boolean test) {
        if (!region.isPublishBlogs()) {
            return;
        }
		BlogConfiguration config = this.getConfiguration(region, lang);
        if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            BlogItem blogPost = getLatestBlogPost(config);
            sendNewBlogPostToRapidmail(blogPost, config.getRegion(), config.getLanguageCode(), test);
        } catch (IOException e) {
            logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
        }
    }

	public void sendLatestBlogPostTelegram(Region region, LanguageCode lang, boolean test) {
        if (!region.isPublishBlogs()) {
            return;
        }
		BlogConfiguration config = this.getConfiguration(region, lang);
        if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            BlogItem blogPost = getLatestBlogPost(config);
            sendNewBlogPostToTelegramChannel(blogPost, config.getRegion(), config.getLanguageCode(), test);
        } catch (IOException e) {
            logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
        }
    }

	public void sendLatestBlogPostPush(Region region, LanguageCode lang, boolean test) {
        if (!region.isPublishBlogs()) {
            return;
        }
		BlogConfiguration config = this.getConfiguration(region, lang);
        if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
            return;
        }
        try {
            BlogItem blogPost = getLatestBlogPost(config);
            sendNewBlogPostToPushNotification(config.getRegion(), config.getLanguageCode(), blogPost, test);
        } catch (IOException e) {
            logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
        }
    }

	void sendNewBlogPostToTelegramChannel(BlogItem item, Region region, LanguageCode lang, boolean test) {
		logger.info("Sending new blog post to telegram channel ...");

		String message = getBlogMessage(item, region, lang);
		String attachmentUrl = item.getAttachmentUrl();

		try {
			TelegramController telegramController = TelegramController.getInstance();

			if (attachmentUrl != null) {
				telegramController.sendPhoto(region, lang, message, attachmentUrl, test);
			} else {
				telegramController.sendMessage(region, lang, message, test);
			}
		} catch (IOException | URISyntaxException e) {
			logger.warn("Blog post could not be sent to telegram channel: " + region + "," + lang.toString(), e);
		}
	}

	private void sendNewBlogPostToRapidmail(BlogItem item, Region region, LanguageCode lang, boolean test) {
		logger.debug("Sending new blog post to rapidmail ...");

		String subject = item.getTitle();
		String blogPostId = item.getId();

		try {
			String htmlString = getBlogPost(this.getConfiguration(region, lang), blogPostId);
			if (htmlString != null && !htmlString.isEmpty())
				EmailUtil.getInstance().sendBlogPostEmailRapidmail(lang, region, htmlString, subject, test);
		} catch (IOException e) {
			logger.warn("Blog post could not be retrieved: " + region.getId() + ", " + lang.toString(), e);
		} catch (URISyntaxException e) {
			logger.warn("Blog post email could not be sent: " + region.getId() + ", " + lang.toString(), e);
		}
	}

	private void sendNewBlogPostToPushNotification(Region region, LanguageCode lang, BlogItem object, boolean test) {
		String message = getBlogMessage(object, region, lang);
		String attachmentUrl = object.getAttachmentUrl();
		String blogUrl = getBlogUrl(object, region, lang);
		PushNotificationUtil pushNotificationUtil = new PushNotificationUtil();
		pushNotificationUtil.sendBulletinNewsletter(message, lang, region, attachmentUrl, blogUrl, test);
	}

	private String getBlogMessage(BlogItem item, Region region, LanguageCode lang) {
		return item.getTitle() + ": " + getBlogUrl(item, region, lang);
	}

	private String getBlogUrl(BlogItem item, Region region, LanguageCode lang) {
		BlogConfiguration config = this.getConfiguration(region, lang);
		return LinkUtil.getAvalancheReportFullBlogUrl(lang, region) + config.getBlogUrl() + "/" + item.getId();
	}

}
