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
package eu.albina.controller.socialmedia;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.model.socialmedia.TelegramConfig;
import eu.albina.util.EmailUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.PushNotificationUtil;

public class BlogController extends CommonProcessor {
	private static final Logger logger = LoggerFactory.getLogger(BlogController.class);

	private static final int BLOGGER_SOCKET_TIMEOUT = 10000;
	private static final int BLOGGER_CONNECTION_TIMEOUT = 10000;

	private static BlogController instance = null;
	protected final HashMap<String, DateTime> lastFetch = new HashMap<>();
	private final Executor executor;

	private BlogController() {
		executor = Executor.newInstance(sslHttpClient());
		DateTime date = new DateTime();
		for (String blogId : GlobalVariables.blogIds.values()) {
			lastFetch.put(blogId, date);
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

	public CloseableHttpClient sslHttpClient() {
		// Trust own CA and all self-signed certs
		return HttpClients.custom().build();
	}

	protected List<Blogger.Item> getBlogPosts(String region, LanguageCode lang) throws IOException {
		String blogId = getBlogId(region, lang);
		if (blogId == null) {
			return Collections.emptyList();
		}
		try {
			String uri = new URIBuilder(GlobalVariables.blogApiUrl + blogId + "/posts")
				.addParameter("key", GlobalVariables.googleApiKey)
				.addParameter("startDate", lastFetch.get(blogId).toString(GlobalVariables.formatterDateTime))
				.addParameter("fetchBodies", Boolean.TRUE.toString())
				.addParameter("fetchImages", Boolean.TRUE.toString())
				.toString();
			logger.debug("URI: " + uri);
			Request request = Request.Get(uri).connectTimeout(BLOGGER_CONNECTION_TIMEOUT)
					.socketTimeout(BLOGGER_SOCKET_TIMEOUT);
			logger.debug("Start date for " + region + ": " + lastFetch.get(blogId).toString());
			lastFetch.put(blogId, new DateTime());
			HttpResponse response = executor.execute(request).returnResponse();
			logger.debug("New start date for " + region + ": " + lastFetch.get(blogId).toString());
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String entityString = EntityUtils.toString(entity, "UTF-8");
				List<Blogger.Item> blogPosts = new CommonProcessor().fromJson(entityString, Blogger.Root.class).items;
				logger.info("Found {} new blog posts for region={} lang={} url={}", blogPosts.size(), region, lang, uri);
				return blogPosts;
			} else {
				throw new IOException("Failed to fetch blog posts: " + response);
			}
		} catch (URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}

	protected String getBlogPost(String blogPostId, String region, LanguageCode lang) throws IOException {
		String blogId = getBlogId(region, lang);
		if (blogId != null) {
			Request request = Request
					.Get(GlobalVariables.blogApiUrl + blogId + "/posts/" + blogPostId + "?key="
							+ GlobalVariables.googleApiKey)
					.connectTimeout(BLOGGER_CONNECTION_TIMEOUT).socketTimeout(BLOGGER_SOCKET_TIMEOUT);
			HttpResponse response = executor.execute(request).returnResponse();
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String entityString = EntityUtils.toString(entity, "UTF-8");
				return new CommonProcessor().fromJson(entityString, Blogger.Item.class).content;
			}
		}
		return null;
	}

	private String getBlogId(String region, LanguageCode lang) {
		return GlobalVariables.blogIds.get(region, lang);
	}

	public void sendNewBlogPosts(String region, LanguageCode lang) {
		if (getBlogId(region, lang) != null) {
			try {
				List<Blogger.Item> blogPosts = getBlogPosts(region, lang);
				for (Blogger.Item object : blogPosts) {
					sendNewBlogPostToMessengerpeople(object, region, lang);
					sendNewBlogPostToRapidmail(object, region, lang);
					sendNewBlogPostToTelegramChannel(object, region, lang);
					sendNewBlogPostToPushNotification(region, lang, object);
				}
			} catch (IOException e) {
				logger.warn("Blog posts could not be retrieved: " + region + ", " + lang.toString(), e);
			}
		}
	}

	void sendNewBlogPostToMessengerpeople(Blogger.Item item, String region, LanguageCode lang) {
		logger.info("Sending new blog post to messengerpeople ...");

		String message = getBlogMessage(item, region, lang);
		String attachmentUrl = getAttachmentUrl(item);

		try {
			RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
			MessengerPeopleProcessorController.getInstance().sendNewsLetter(rc.getMessengerPeopleConfig(), lang,
					message, attachmentUrl);
		} catch (AlbinaException e) {
			logger.warn("Blog post could not be sent to messengerpeople: " + region + ", " + lang.toString(), e);
		} catch (IOException e) {
			logger.warn("Blog post could not be sent to messengerpeople: " + region + "," + lang.toString(), e);
		}
	}

	void sendNewBlogPostToTelegramChannel(Blogger.Item item, String region, LanguageCode lang) {
		logger.info("Sending new blog post to telegram channel ...");

		String message = getBlogMessage(item, region, lang);
		String attachmentUrl = getAttachmentUrl(item);

		try {
			TelegramChannelProcessorController ctTc = TelegramChannelProcessorController.getInstance();
			RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
			Set<TelegramConfig> telegramConfigs = rc.getTelegramConfigs();
			TelegramConfig config = telegramConfigs.stream()
				.filter(telegramConfig -> Objects.equals(telegramConfig.getLanguageCode(), lang))
				.findFirst()
				.orElseThrow(() -> new AlbinaException("No configuration for telegram channel found (" + region + ", " + lang + ")"));
			if (attachmentUrl != null) {
				ctTc.sendPhoto(config, message, attachmentUrl);
			} else {
				ctTc.sendMessage(config, message);
			}
		} catch (AlbinaException e) {
			logger.warn("Blog post could not be sent to telegram channel: " + region + ", " + lang.toString(), e);
		} catch (IOException | URISyntaxException e) {
			logger.warn("Blog post could not be sent to telegram channel: " + region + "," + lang.toString(), e);
		}
	}

	private void sendNewBlogPostToRapidmail(Blogger.Item item, String region, LanguageCode lang) {
		logger.debug("Sending new blog post to rapidmail ...");

		String subject = item.title;
		String blogPostId = item.id;

		try {
			String htmlString = getBlogPost(blogPostId, region, lang);
			if (htmlString != null && !htmlString.isEmpty())
				EmailUtil.getInstance().sendBlogPostEmailRapidmail(lang, region, htmlString, subject);
		} catch (IOException e) {
			logger.warn("Blog post could not be retrieved: " + region + ", " + lang.toString(), e);
		} catch (URISyntaxException e) {
			logger.warn("Blog post email could not be sent: " + region + ", " + lang.toString(), e);
		}
	}

	private void sendNewBlogPostToPushNotification(String region, LanguageCode lang, Blogger.Item object) {
		String message = getBlogMessage(object, region, lang);
		String attachmentUrl = getAttachmentUrl(object);
		String blogUrl = getBlogUrl(object, region, lang);
		PushNotificationUtil pushNotificationUtil = PushNotificationUtil.getInstance();
		pushNotificationUtil.sendBulletinNewsletter(message, lang, Collections.singletonList(region), attachmentUrl, blogUrl);
	}

	private String getBlogMessage(Blogger.Item item, String region, LanguageCode lang) {
		return item.title + ": " + getBlogUrl(item, region, lang);
	}

	private String getBlogUrl(Blogger.Item item, String region, LanguageCode lang) {
		return GlobalVariables.getAvalancheReportFullBlogUrl(lang) + getBlogUrl(region, lang) + "/" + item.id;
	}

	private String getAttachmentUrl(Blogger.Item item) {
		if (item.images != null && !item.images.isEmpty()) {
			return item.images.get(0).url;
		} else {
			return null;
		}
	}

	private String getBlogUrl(String region, LanguageCode lang) {
		return GlobalVariables.blogUrls.get(region, lang);
	}
}
