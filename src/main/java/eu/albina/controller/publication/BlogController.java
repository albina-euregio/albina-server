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
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Strings;

import com.google.common.collect.MoreCollectors;
import eu.albina.util.HttpClientUtil;
import eu.albina.util.LinkUtil;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.GoogleBloggerConfiguration;
import eu.albina.util.EmailUtil;
import eu.albina.util.HibernateUtil;
import eu.albina.util.PushNotificationUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

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

	protected GoogleBloggerConfiguration getConfiguration(Region region, LanguageCode languageCode) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			GoogleBloggerConfiguration result = null;
			if (region != null && !Strings.isNullOrEmpty(region.getId()) && languageCode != null) {
				try {
					result = (GoogleBloggerConfiguration) entityManager.createQuery(HibernateUtil.queryGetGoogleBloggerConfiguration)
					.setParameter("region", region)
					.setParameter("lang", languageCode).getSingleResult();
				} catch (Exception e) {
					logger.warn("No google blogger configuration found for {} [{}]", region.getId(), languageCode.toString());
					return null;
				}
			} else {
				throw new HibernateException("No region or language defined!");
			}
			return result;
		});
	}

	protected void updateConfigurationLastPublished(GoogleBloggerConfiguration config, Blogger.Item object) {
		if (!object.published.toInstant().isAfter(config.getLastPublishedTimestamp().toInstant())) {
			return;
		}
		config.setLastPublishedBlogId(object.id);
		config.setLastPublishedTimestamp(object.published);
		logger.info("Updating lastPublishedTimestamp={} lastPublishedBlogId={} for {} [{}]",
			config.getLastPublishedTimestamp(), config.getLastPublishedBlogId(), config.getRegion().getId(), config.getLanguageCode());
		HibernateUtil.getInstance().runTransaction(entityManager -> entityManager.merge(config));
	}

	protected List<Blogger.Item> getBlogPosts(GoogleBloggerConfiguration config) throws IOException {
		if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
			return Collections.emptyList();
		}

		OffsetDateTime lastPublishedTimestamp = Objects.requireNonNull(config.getLastPublishedTimestamp(), "lastPublishedTimestamp");
		WebTarget request = client.target(config.getBlogApiUrl() + config.getBlogId() + "/posts")
			.queryParam("key", config.getApiKey())
			.queryParam("startDate", lastPublishedTimestamp.toInstant().plusSeconds(1).toString())
			.queryParam("fetchBodies", Boolean.TRUE.toString())
			.queryParam("fetchImages", Boolean.TRUE.toString());
		Blogger.Root root = request.request().get(Blogger.Root.class);
		List<Blogger.Item> blogPosts = root.items;
		logger.info("Found {} new blog posts for region={} lang={} url={}", blogPosts.size(), config.getRegion().getId(), config.getLanguageCode().toString(), request.getUri());
		return blogPosts;
	}

	protected Blogger.Item getLatestBlogPost(GoogleBloggerConfiguration config) throws IOException {
		if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}

		WebTarget request = client.target(config.getBlogApiUrl() + config.getBlogId() + "/posts")
			.queryParam("key", config.getApiKey())
			.queryParam("fetchBodies", Boolean.TRUE.toString())
			.queryParam("fetchImages", Boolean.TRUE.toString())
			.queryParam("maxResults", Integer.toString(1));
		Blogger.Root root = request.request().get(Blogger.Root.class);
		List<Blogger.Item> blogPosts = root.items;
		logger.info("Fetched latest blog post for region={} lang={} url={}", config.getRegion().getId(), config.getLanguageCode().toString(), request.getUri());
		return blogPosts.stream().collect(MoreCollectors.onlyElement());
	}

	protected String getBlogPost(String blogPostId, Region region, LanguageCode lang) throws IOException {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);

		if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}

		return client.target(config.getBlogApiUrl() + config.getBlogId() + "/posts/" + blogPostId)
			.queryParam("key", config.getApiKey())
			.request()
			.get(Blogger.Item.class)
			.content;
	}

	public void sendNewBlogPosts(Region region, LanguageCode lang) {
		if (region.isPublishBlogs()) {
			GoogleBloggerConfiguration config = this.getConfiguration(region, lang);

			if (config != null && config.getBlogId() != null && config.getApiKey() != null && config.getBlogApiUrl() != null) {
				try {
					List<Blogger.Item> blogPosts = getBlogPosts(config);
					for (Blogger.Item object : blogPosts) {
						sendNewBlogPostToRapidmail(object, region, lang, false);
						sendNewBlogPostToTelegramChannel(object, region, lang, false);
						sendNewBlogPostToPushNotification(region, lang, object, false);
						updateConfigurationLastPublished(config, object);
					}
				} catch (IOException e) {
					logger.warn("Blog posts could not be retrieved: " + region.getId() + ", " + lang.toString(), e);
				}
			}
		} else {
			logger.info("Sending blog posts disabled for {} [{}]", region.getId(), lang.toString());
		}
	}

	public void sendLatestBlogPost(Region region, LanguageCode lang, boolean test) {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);
		if (region.isPublishBlogs()) {
			if (config != null && config.getBlogId() != null && config.getApiKey() != null && config.getBlogApiUrl() != null) {
				try {
					Blogger.Item blogPost = getLatestBlogPost(config);
					sendNewBlogPostToRapidmail(blogPost, config.getRegion(), config.getLanguageCode(), test);
					sendNewBlogPostToTelegramChannel(blogPost, config.getRegion(), config.getLanguageCode(), test);
					sendNewBlogPostToPushNotification(config.getRegion(), config.getLanguageCode(), blogPost, test);
					updateConfigurationLastPublished(config, blogPost);
				} catch (IOException e) {
					logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
				}
			}
		} else {
			logger.info("Sending blog posts disabled for {} [{}]", config.getRegion().getId(), config.getLanguageCode());
		}
	}

	public void sendLatestBlogPostEmail(Region region, LanguageCode lang, boolean test) {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);
		if (region.isPublishBlogs()) {
			if (config != null && config.getBlogId() != null && config.getApiKey() != null && config.getBlogApiUrl() != null) {
				try {
					Blogger.Item blogPost = getLatestBlogPost(config);
					sendNewBlogPostToRapidmail(blogPost, config.getRegion(), config.getLanguageCode(), test);
				} catch (IOException e) {
					logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
				}
			}
		}
	}

	public void sendLatestBlogPostTelegram(Region region, LanguageCode lang, boolean test) {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);
		if (region.isPublishBlogs()) {
			if (config != null && config.getBlogId() != null && config.getApiKey() != null && config.getBlogApiUrl() != null) {
				try {
					Blogger.Item blogPost = getLatestBlogPost(config);
					sendNewBlogPostToTelegramChannel(blogPost, config.getRegion(), config.getLanguageCode(), test);
				} catch (IOException e) {
					logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
				}
			}
		}
	}

	public void sendLatestBlogPostPush(Region region, LanguageCode lang, boolean test) {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);
		if (region.isPublishBlogs()) {
			if (config != null && config.getBlogId() != null && config.getApiKey() != null && config.getBlogApiUrl() != null) {
				try {
					Blogger.Item blogPost = getLatestBlogPost(config);
					sendNewBlogPostToPushNotification(config.getRegion(), config.getLanguageCode(), blogPost, test);
				} catch (IOException e) {
					logger.warn("Latest blog post could not be retrieved: " + config.getRegion().getId() + ", " + config.getLanguageCode().toString(), e);
				}
			}
		}
	}

	void sendNewBlogPostToTelegramChannel(Blogger.Item item, Region region, LanguageCode lang, boolean test) {
		logger.info("Sending new blog post to telegram channel ...");

		String message = getBlogMessage(item, region, lang);
		String attachmentUrl = getAttachmentUrl(item);

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

	private void sendNewBlogPostToRapidmail(Blogger.Item item, Region region, LanguageCode lang, boolean test) {
		logger.debug("Sending new blog post to rapidmail ...");

		String subject = item.title;
		String blogPostId = item.id;

		try {
			String htmlString = getBlogPost(blogPostId, region, lang);
			if (htmlString != null && !htmlString.isEmpty())
				EmailUtil.getInstance().sendBlogPostEmailRapidmail(lang, region, htmlString, subject, test);
		} catch (IOException e) {
			logger.warn("Blog post could not be retrieved: " + region.getId() + ", " + lang.toString(), e);
		} catch (URISyntaxException e) {
			logger.warn("Blog post email could not be sent: " + region.getId() + ", " + lang.toString(), e);
		}
	}

	private void sendNewBlogPostToPushNotification(Region region, LanguageCode lang, Blogger.Item object, boolean test) {
		String message = getBlogMessage(object, region, lang);
		String attachmentUrl = getAttachmentUrl(object);
		String blogUrl = getBlogUrl(object, region, lang);
		PushNotificationUtil pushNotificationUtil = new PushNotificationUtil();
		pushNotificationUtil.sendBulletinNewsletter(message, lang, region, attachmentUrl, blogUrl, test);
	}

	private String getBlogMessage(Blogger.Item item, Region region, LanguageCode lang) {
		return item.title + ": " + getBlogUrl(item, region, lang);
	}

	private String getBlogUrl(Blogger.Item item, Region region, LanguageCode lang) {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);
		return LinkUtil.getAvalancheReportFullBlogUrl(lang, region) + config.getBlogUrl() + "/" + item.id;
	}

	private String getAttachmentUrl(Blogger.Item item) {
		if (item.images != null && !item.images.isEmpty()) {
			return item.images.get(0).url;
		} else {
			return null;
		}
	}
}
