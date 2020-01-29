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
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.util.EmailUtil;
import eu.albina.util.GlobalVariables;

public class BlogController extends CommonProcessor {
	private static Logger logger = LoggerFactory.getLogger(BlogController.class);

	private static final int BLOGGER_SOCKET_TIMEOUT = 10000;
	private static final int BLOGGER_CONNECTION_TIMEOUT = 10000;

	private static BlogController instance = null;
	private HashMap<String, DateTime> lastFetch;
	private Executor executor;

	private BlogController() {
		executor = Executor.newInstance(sslHttpClient());
		lastFetch = new HashMap<String, DateTime>();
		DateTime date = new DateTime();
		// REGION
		lastFetch.put("Test", date);
		for (String region : GlobalVariables.regionsEuregio)
			lastFetch.put(region, date);
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

	private JSONArray getBlogPosts(String region, LanguageCode lang) throws ClientProtocolException, IOException {
		String blogId = getBlogId(region, lang);
		String uri = GlobalVariables.blogApiUrl + blogId + "/posts?key=" + GlobalVariables.googleApiKey + "&startDate="
				+ URLEncoder.encode(lastFetch.get(region).toString(GlobalVariables.formatterDateTime), "UTF-8");
		logger.debug("URI: " + uri);
		Request request = Request.Get(uri).connectTimeout(BLOGGER_CONNECTION_TIMEOUT)
				.socketTimeout(BLOGGER_SOCKET_TIMEOUT);
		logger.debug("Start date for " + region + ": " + lastFetch.get(region).toString());
		lastFetch.put(region, new DateTime());
		HttpResponse response = executor.execute(request).returnResponse();
		logger.debug("New start date for " + region + ": " + lastFetch.get(region).toString());
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String entityString = EntityUtils.toString(entity, "UTF-8");
			JSONObject jsonObject = new JSONObject(entityString);
			if (jsonObject.has("items"))
				return jsonObject.getJSONArray("items");
			else
				return new JSONArray();
		}
		return new JSONArray();
	}

	private String getBlogPost(String blogPostId, String region, LanguageCode lang)
			throws ClientProtocolException, IOException {
		String blogId = getBlogId(region, lang);
		Request request = Request
				.Get(GlobalVariables.blogApiUrl + "/" + blogId + "/posts/" + blogPostId + "?key="
						+ GlobalVariables.googleApiKey)
				.connectTimeout(BLOGGER_CONNECTION_TIMEOUT).socketTimeout(BLOGGER_SOCKET_TIMEOUT);
		HttpResponse response = executor.execute(request).returnResponse();
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String entityString = EntityUtils.toString(entity, "UTF-8");
			JSONObject jsonObject = new JSONObject(entityString);
			return jsonObject.getString("content");
		}
		return null;
	}

	// LANG
	// REGION
	private String getBlogId(String region, LanguageCode lang) {
		switch (region) {
		case "AT-07":
			switch (lang) {
			case de:
				return GlobalVariables.blogIdTyrolDe;
			case it:
				return GlobalVariables.blogIdTyrolIt;
			case en:
				return GlobalVariables.blogIdTyrolEn;
			default:
				return null;
			}
		case "IT-32-BZ":
			switch (lang) {
			case de:
				return GlobalVariables.blogIdSouthTyrolDe;
			case it:
				return GlobalVariables.blogIdSouthTyrolDe;
			default:
				return null;
			}
		case "IT-32-TN":
			switch (lang) {
			case it:
				return GlobalVariables.blogIdTrentinoIt;
			default:
				return null;
			}
		case "Test":
			return GlobalVariables.blogIdTest;
		default:
			return null;
		}
	}

	public void sendNewBlogPosts(String region, LanguageCode lang) {
		if (getBlogId(region, lang) != null) {
			try {
				JSONArray blogPosts = getBlogPosts(region, lang);

				logger.info("Found " + blogPosts.length() + " new blog posts!");
				for (Object object : blogPosts)
					if (object instanceof JSONObject) {
						logger.info(((JSONObject) object).toString());
						sendNewBlogPostToMessengerpeople((JSONObject) object, region, lang);
						sendNewBlogPostToRapidmail((JSONObject) object, region, lang);
					}
			} catch (IOException e) {
				logger.warn("Blog posts could not be retrieved: " + region + ", " + lang.toString(), e);
			}
		}
	}

	private void sendNewBlogPostToMessengerpeople(JSONObject object, String region, LanguageCode lang) {
		logger.info("Sending new blog post to messengerpeople ...");

		StringBuilder sb = new StringBuilder();
		sb.append(object.getString("title"));
		sb.append(": ");
		sb.append(getBlogPostLink(object, region, lang));

		JSONArray imagesArray = object.getJSONArray("images");
		JSONObject image = (JSONObject) imagesArray.get(0);
		String attachmentUrl = image.getString("url");

		try {
			RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
			MessengerPeopleProcessorController.getInstance().sendNewsLetter(rc.getMessengerPeopleConfig(), lang,
					sb.toString(), attachmentUrl);
		} catch (AlbinaException e) {
			logger.warn("Blog post could not be sent to messengerpeople: " + region + ", " + lang.toString(), e);
		} catch (IOException e) {
			logger.warn("Blog post could not be sent to messengerpeople: " + region + "," + lang.toString(),e );
		}
	}

	private void sendNewBlogPostToRapidmail(JSONObject object, String region, LanguageCode lang) {
		logger.debug("Sending new blog post to rapidmail ...");

		String subject = object.getString("title");
		String blogPostId = object.getString("id");

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

	private String getBlogPostLink(JSONObject object, String region, LanguageCode lang) {
		StringBuilder sb = new StringBuilder();
		sb.append(GlobalVariables.getAvalancheReportBaseUrl(lang));
		sb.append(getBlogUrl(region, lang));
		sb.append("/");
		sb.append(object.getString("id"));

		return sb.toString();
	}

	// LANG
	// REGION
	private String getBlogUrl(String region, LanguageCode lang) {
		switch (region) {
		case "AT-07":
			switch (lang) {
			case de:
				return GlobalVariables.blogUrlTyrolDe;
			case it:
				return GlobalVariables.blogUrlTyrolIt;
			case en:
				return GlobalVariables.blogUrlTyrolEn;
			default:
				return null;
			}
		case "IT-32-BZ":
			switch (lang) {
			case de:
				return GlobalVariables.blogUrlSouthTyrolDe;
			case it:
				return GlobalVariables.blogUrlSouthTyrolDe;
			default:
				return null;
			}
		case "IT-32-TN":
			switch (lang) {
			case it:
				return GlobalVariables.blogUrlTrentinoIt;
			default:
				return null;
			}
		default:
			return null;
		}
	}
}
