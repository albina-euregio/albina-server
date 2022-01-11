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
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.MoreCollectors;
import eu.albina.util.HttpClientUtil;
import eu.albina.util.LinkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.model.socialmedia.TelegramConfig;
import eu.albina.util.EmailUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.PushNotificationUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

public class BlogController {
	private static final Logger logger = LoggerFactory.getLogger(BlogController.class);
	private static BlogController instance = null;

	protected final HashMap<String, Instant> lastFetch = new HashMap<>();
	private final Client client = HttpClientUtil.newClientBuilder().build();

	private BlogController() {
		Instant date = Instant.now();
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

	protected List<Blogger.Item> getBlogPosts(String region, LanguageCode lang) throws IOException {
		String blogId = getBlogId(region, lang);
		if (blogId == null) {
			return Collections.emptyList();
		}
		WebTarget request = client.target(GlobalVariables.blogApiUrl + blogId + "/posts")
			.queryParam("key", GlobalVariables.googleApiKey)
			.queryParam("startDate", lastFetch.get(blogId).toString())
			.queryParam("fetchBodies", Boolean.TRUE.toString())
			.queryParam("fetchImages", Boolean.TRUE.toString());
		logger.debug("Start date for {}: {}", blogId, lastFetch.get(blogId).toString());
		lastFetch.put(blogId, Instant.now());
		Blogger.Root root = request.request().get(Blogger.Root.class);
		List<Blogger.Item> blogPosts = root.items;
		logger.info("Found {} new blog posts for region={} lang={} url={}", blogPosts.size(), region, lang, request.getUri());
		return blogPosts;
	}

	protected Blogger.Item getLatestBlogPost(String region, LanguageCode lang) throws IOException {
		String blogId = getBlogId(region, lang);
		if (blogId == null) {
			throw new IOException("Blog ID not found");
		}
		WebTarget request = client.target(GlobalVariables.blogApiUrl + blogId + "/posts")
			.queryParam("key", GlobalVariables.googleApiKey)
			.queryParam("fetchBodies", Boolean.TRUE.toString())
			.queryParam("fetchImages", Boolean.TRUE.toString())
			.queryParam("maxResults", Integer.toString(1));
		lastFetch.put(blogId, Instant.now());
		Blogger.Root root = request.request().get(Blogger.Root.class);
		List<Blogger.Item> blogPosts = root.items;
		logger.info("Fetched latest blog post for region={} lang={} url={}", region, lang, request.getUri());
		return blogPosts.stream().collect(MoreCollectors.onlyElement());
	}

	protected String getBlogPost(String blogPostId, String region, LanguageCode lang) throws IOException {
		String blogId = getBlogId(region, lang);
		if (blogId == null) {
			return null;
		}
		return client.target(GlobalVariables.blogApiUrl + blogId + "/posts/" + blogPostId)
			.queryParam("key", GlobalVariables.googleApiKey)
			.request()
			.get(Blogger.Item.class)
			.content;
	}

	private String getBlogId(String region, LanguageCode lang) {
		return GlobalVariables.blogIds.get(region, lang);
	}

	public void sendNewBlogPosts(String region, LanguageCode lang) {
		if (getBlogId(region, lang) != null) {
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
	}

	public void sendLatestBlogPost(String region, LanguageCode lang, boolean test) {
		if (getBlogId(region, lang) != null) {
			try {
				Blogger.Item blogPost = getLatestBlogPost(region, lang);
				sendNewBlogPostToRapidmail(blogPost, region, lang, test);
				sendNewBlogPostToTelegramChannel(blogPost, region, lang, test);
				sendNewBlogPostToPushNotification(region, lang, blogPost, test);
			} catch (IOException e) {
				logger.warn("Latest blog post could not be retrieved: " + region + ", " + lang.toString(), e);
			}
		}
	}

	public void sendLatestBlogPostEmail(String region, LanguageCode lang, boolean test) {
		if (getBlogId(region, lang) != null) {
			try {
				Blogger.Item blogPost = getLatestBlogPost(region, lang);
				sendNewBlogPostToRapidmail(blogPost, region, lang, test);
			} catch (IOException e) {
				logger.warn("Latest blog post could not be retrieved: " + region + ", " + lang.toString(), e);
			}
		}
	}

	public void sendLatestBlogPostTelegram(String region, LanguageCode lang, boolean test) {
		if (getBlogId(region, lang) != null) {
			try {
				Blogger.Item blogPost = getLatestBlogPost(region, lang);
				sendNewBlogPostToTelegramChannel(blogPost, region, lang, test);
			} catch (IOException e) {
				logger.warn("Latest blog post could not be retrieved: " + region + ", " + lang.toString(), e);
			}
		}
	}

	public void sendLatestBlogPostPush(String region, LanguageCode lang, boolean test) {
		if (getBlogId(region, lang) != null) {
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
			TelegramChannelProcessorController ctTc = TelegramChannelProcessorController.getInstance();
			RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
			Set<TelegramConfig> telegramConfigs = rc.getTelegramConfigs();
			TelegramConfig config = telegramConfigs.stream()
				.filter(telegramConfig -> Objects.equals(telegramConfig.getLanguageCode(), lang))
				.findFirst()
				.orElseThrow(() -> new AlbinaException("No configuration for telegram channel found (" + region + ", " + lang + ")"));
			if (attachmentUrl != null) {
				ctTc.sendPhoto(config, message, attachmentUrl, test);
			} else {
				ctTc.sendMessage(config, message, test);
			}
		} catch (AlbinaException e) {
			logger.warn("Blog post could not be sent to telegram channel: " + region + ", " + lang.toString(), e);
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
		return LinkUtil.getAvalancheReportFullBlogUrl(lang) + getBlogUrl(region, lang) + "/" + item.id;
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
