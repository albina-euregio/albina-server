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
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Strings;

import eu.albina.util.LinkUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.RegionController;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.GoogleBloggerConfiguration;
import eu.albina.util.EmailUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;
import eu.albina.util.PushNotificationUtil;

public class BlogController extends CommonProcessor {
	private static final Logger logger = LoggerFactory.getLogger(BlogController.class);

	private static final int BLOGGER_SOCKET_TIMEOUT = 10000;
	private static final int BLOGGER_CONNECTION_TIMEOUT = 10000;

	private static BlogController instance = null;
	protected final HashMap<String, Instant> lastFetch = new HashMap<>();
	private final Executor executor;

	private BlogController() {
		executor = Executor.newInstance(sslHttpClient());
		Instant date = Instant.now();

		for (String region : GlobalVariables.regionsEuregio) {
			for (LanguageCode lang : LanguageCode.SOCIAL_MEDIA) {
				try {
					GoogleBloggerConfiguration config = this.getConfiguration(region, lang);
					if (config != null)
						lastFetch.put(config.getBlogId(), date);
				} catch (Exception e) {
					logger.info("No Google Blogger configuration for " + region  + "[" + lang + "]");
				}
			}
		}
	}

	/**
	 * Returns the {@code BlogController} object associated with the current Java
	 * application.
	 *
	 * @return the {@code BlogController} object associated with the current Java
	 *         application.
	 */
	public static BlogController getInstance() {
		if (instance == null) {
			instance = new BlogController();
		}
		return instance;
	}

	protected GoogleBloggerConfiguration getConfiguration(String regionId, LanguageCode languageCode) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			GoogleBloggerConfiguration result = null;
			if (!Strings.isNullOrEmpty(regionId) && languageCode != null) {
				result = (GoogleBloggerConfiguration) entityManager.createQuery(HibernateUtil.queryGetGoogleBloggerConfiguration)
				.setParameter("regionId", regionId)
				.setParameter("lang", languageCode).getSingleResult();
			} else {
				throw new HibernateException("No region or language defined!");
			}
			if (result != null)
				return result;
			else
				throw new HibernateException("No telegram configuration found for " + regionId + " [" + languageCode + "]");
		});
	}

	public CloseableHttpClient sslHttpClient() {
		// Trust own CA and all self-signed certs
		return HttpClients.custom().build();
	}

	protected List<Blogger.Item> getBlogPosts(String regionId, LanguageCode lang) throws IOException {
		GoogleBloggerConfiguration config = this.getConfiguration(regionId, lang);

		if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
			return Collections.emptyList();
		}
		try {
			String uri = new URIBuilder(config.getBlogApiUrl() + config.getBlogId() + "/posts")
				.addParameter("key", config.getApiKey())
				.addParameter("startDate", lastFetch.get(config.getBlogId()).toString())
				.addParameter("fetchBodies", Boolean.TRUE.toString())
				.addParameter("fetchImages", Boolean.TRUE.toString())
				.toString();
            logger.debug("URI: {}", uri);
			Request request = Request.Get(uri).connectTimeout(BLOGGER_CONNECTION_TIMEOUT)
					.socketTimeout(BLOGGER_SOCKET_TIMEOUT);
			logger.debug("Start date for {}: {}", config.getBlogId(), lastFetch.get(config.getBlogId()).toString());
			lastFetch.put(config.getBlogId(), Instant.now());
			HttpResponse response = executor.execute(request).returnResponse();
			logger.debug("New start date for {}: {}", config.getBlogId(), lastFetch.get(config.getBlogId()).toString());
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String entityString = EntityUtils.toString(entity, "UTF-8");
				List<Blogger.Item> blogPosts = new CommonProcessor().fromJson(entityString, Blogger.Root.class).items;
				logger.info("Found {} new blog posts for region={} lang={} url={}", blogPosts.size(), regionId, lang, uri);
				return blogPosts;
			} else {
				throw new IOException("Failed to fetch blog posts: " + response);
			}
		} catch (URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}

	protected Blogger.Item getLatestBlogPost(String region, LanguageCode lang) throws IOException {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);

		if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}
		try {
			String uri = new URIBuilder(config.getBlogApiUrl() + config.getBlogId() + "/posts")
				.addParameter("key", config.getApiKey())
				.addParameter("fetchBodies", Boolean.TRUE.toString())
				.addParameter("fetchImages", Boolean.TRUE.toString())
				.addParameter("maxResults", "1")
				.toString();
            logger.debug("URI: {}", uri);
			Request request = Request.Get(uri).connectTimeout(BLOGGER_CONNECTION_TIMEOUT)
					.socketTimeout(BLOGGER_SOCKET_TIMEOUT);
			HttpResponse response = executor.execute(request).returnResponse();
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String entityString = EntityUtils.toString(entity, "UTF-8");
				List<Blogger.Item> blogPosts = new CommonProcessor().fromJson(entityString, Blogger.Root.class).items;
				logger.info("Fetched latest blog post for region={} lang={} url={}", blogPosts.size(), region, lang, uri);
				if (blogPosts.size() > 0)
					return blogPosts.get(0);
				else
					throw new IOException("No blog post found");
			} else {
				throw new IOException("Failed to fetch latest blog post: " + response);
			}
		} catch (URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}

	protected String getBlogPost(String blogPostId, String region, LanguageCode lang) throws IOException {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);

		if (config == null || config.getBlogId() == null || config.getApiKey() == null || config.getBlogApiUrl() == null) {
			throw new IOException("Blog ID not found");
		}
		Request request = Request
				.Get(config.getBlogApiUrl() + config.getBlogId() + "/posts/" + blogPostId + "?key="
						+ config.getApiKey())
				.connectTimeout(BLOGGER_CONNECTION_TIMEOUT).socketTimeout(BLOGGER_SOCKET_TIMEOUT);
		HttpResponse response = executor.execute(request).returnResponse();
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String entityString = EntityUtils.toString(entity, "UTF-8");
			return new CommonProcessor().fromJson(entityString, Blogger.Item.class).content;
		}
		return null;
	}

	public void sendNewBlogPosts(String region, LanguageCode lang) {
		if (RegionController.getInstance().isPublishBlogs(region)) {
			GoogleBloggerConfiguration config = this.getConfiguration(region, lang);

			if (config.getBlogId() != null) {
				try {
					List<Blogger.Item> blogPosts = getBlogPosts(region, lang);
					for (Blogger.Item object : blogPosts) {
						sendNewBlogPostToRapidmail(object, region, lang, false);
						sendNewBlogPostToTelegramChannel(object, region, lang, false);
						sendNewBlogPostToPushNotification(region, lang, object, false);
					}
				} catch (IOException e) {
					logger.warn("Blog posts could not be retrieved: " + region + ", " + lang.toString(), e);
				}
			}
		} else {
			logger.info("Sending blog posts disabled for " + region + " [" + lang + "]");
		}
	}

	public void sendLatestBlogPost(String region, LanguageCode lang, boolean test) {
		if (RegionController.getInstance().isPublishBlogs(region)) {
			GoogleBloggerConfiguration config = this.getConfiguration(region, lang);

			if (config.getBlogId() != null) {
				try {
					Blogger.Item blogPost = getLatestBlogPost(region, lang);
					sendNewBlogPostToRapidmail(blogPost, region, lang, test);
					sendNewBlogPostToTelegramChannel(blogPost, region, lang, test);
					sendNewBlogPostToPushNotification(region, lang, blogPost, test);
				} catch (IOException e) {
					logger.warn("Latest blog post could not be retrieved: " + region + ", " + lang.toString(), e);
				}
			}
		} else {
			logger.info("Sending blog posts disabled for " + region + " [" + lang + "]");
		}
	}

	public void sendLatestBlogPostEmail(String region, LanguageCode lang, boolean test) {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);

		if (config.getBlogId() != null) {
			try {
				Blogger.Item blogPost = getLatestBlogPost(region, lang);
				sendNewBlogPostToRapidmail(blogPost, region, lang, test);
			} catch (IOException e) {
				logger.warn("Latest blog post could not be retrieved: " + region + ", " + lang.toString(), e);
			}
		}
	}

	public void sendLatestBlogPostTelegram(String region, LanguageCode lang, boolean test) {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);

		if (config.getBlogId() != null) {
			try {
				Blogger.Item blogPost = getLatestBlogPost(region, lang);
				sendNewBlogPostToTelegramChannel(blogPost, region, lang, test);
			} catch (IOException e) {
				logger.warn("Latest blog post could not be retrieved: " + region + ", " + lang.toString(), e);
			}
		}
	}

	public void sendLatestBlogPostPush(String region, LanguageCode lang, boolean test) {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);

		if (config.getBlogId() != null) {
			try {
				Blogger.Item blogPost = getLatestBlogPost(region, lang);
				sendNewBlogPostToPushNotification(region, lang, blogPost, test);
			} catch (IOException e) {
				logger.warn("Latest blog post could not be retrieved: " + region + ", " + lang.toString(), e);
			}
		}
	}

	void sendNewBlogPostToTelegramChannel(Blogger.Item item, String region, LanguageCode lang, boolean test) {
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

	private void sendNewBlogPostToRapidmail(Blogger.Item item, String region, LanguageCode lang, boolean test) {
		logger.debug("Sending new blog post to rapidmail ...");

		String subject = item.title;
		String blogPostId = item.id;

		try {
			String htmlString = getBlogPost(blogPostId, region, lang);
			if (htmlString != null && !htmlString.isEmpty())
				EmailUtil.getInstance().sendBlogPostEmailRapidmail(lang, region, htmlString, subject, test);
		} catch (IOException e) {
			logger.warn("Blog post could not be retrieved: " + region + ", " + lang.toString(), e);
		} catch (URISyntaxException e) {
			logger.warn("Blog post email could not be sent: " + region + ", " + lang.toString(), e);
		}
	}

	private void sendNewBlogPostToPushNotification(String region, LanguageCode lang, Blogger.Item object, boolean test) {
		String message = getBlogMessage(object, region, lang);
		String attachmentUrl = getAttachmentUrl(object);
		String blogUrl = getBlogUrl(object, region, lang);
		PushNotificationUtil pushNotificationUtil = new PushNotificationUtil();
		pushNotificationUtil.sendBulletinNewsletter(message, lang, Collections.singletonList(region), attachmentUrl, blogUrl, test);
	}

	private String getBlogMessage(Blogger.Item item, String region, LanguageCode lang) {
		return item.title + ": " + getBlogUrl(item, region, lang);
	}

	private String getBlogUrl(Blogger.Item item, String region, LanguageCode lang) {
		GoogleBloggerConfiguration config = this.getConfiguration(region, lang);
		return LinkUtil.getAvalancheReportFullBlogUrl(lang) + config.getBlogUrl() + "/" + item.id;
	}

	private String getAttachmentUrl(Blogger.Item item) {
		if (item.images != null && !item.images.isEmpty()) {
			return item.images.get(0).url;
		} else {
			return null;
		}
	}
}
