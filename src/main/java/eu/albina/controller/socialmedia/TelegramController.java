/*******************************************************************************
 * Copyright (C) 2020 Norbert Lanzanasto
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import com.google.common.base.Strings;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.TelegramConfiguration;
import eu.albina.util.HibernateUtil;

public class TelegramController extends CommonProcessor {
	private static final Logger logger = LoggerFactory.getLogger(TelegramController.class);

	private static TelegramController instance = null;
	private final int CONNECTION_TIMEOUT = 10000;
	private final int SOCKET_TIMEOUT = 10000;

	public static TelegramController getInstance() {
		if (instance == null) {
			instance = new TelegramController();
		}
		return instance;
	}

	public TelegramController() {
	}

	private TelegramConfiguration getConfiguration(String regionId, LanguageCode languageCode) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			TelegramConfiguration result = null;
			if (!Strings.isNullOrEmpty(regionId)) {
				result = (TelegramConfiguration) entityManager.createQuery(HibernateUtil.queryGetTelegramConfiguration)
				.setParameter("regionId", regionId)
				.setParameter("lang", languageCode).getSingleResult();
			} else {
				throw new HibernateException("No region defined!");
			}
			if (result != null)
				return result;
			else
				throw new HibernateException("No telegram configuration found for " + regionId + " [" + languageCode + "]");
		});
	}

	public HttpResponse sendPhoto(String region, LanguageCode lang, String message, String attachmentUrl, boolean test)
			throws IOException, URISyntaxException, HibernateException {
		TelegramConfiguration config = this.getConfiguration(region, lang);

		String chatId = test ? "aws_test" : config.getChatId();

		URIBuilder uriBuilder = new URIBuilder(
				String.format("https://api.telegram.org/bot%s/sendPhoto", config.getApiToken()))
						.addParameter("chat_id", chatId).addParameter("caption", message);
		if (attachmentUrl != null) {
			uriBuilder.addParameter("photo", attachmentUrl);
		}
		URI uri = uriBuilder.build();
		logger.info("URL: {}", uri);
		Request request = Request.Get(uri).connectTimeout(CONNECTION_TIMEOUT).socketTimeout(SOCKET_TIMEOUT);
		HttpResponse response = request.execute().returnResponse();

		// Go ahead only if success
		if (response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Error publishing photo on telegram channel for "
					+ config.getRegion().getId() + " (error code "
					+ response.getStatusLine().getStatusCode() + ")");
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);
			logger.warn(content);
		}

		return response;
	}

	public HttpResponse sendMessage(String region, LanguageCode lang, String message, boolean test) throws IOException, URISyntaxException, HibernateException {
		TelegramConfiguration config = this.getConfiguration(region, lang);

		String chatId = test ? "aws_test" : config.getChatId();
		URIBuilder uriBuilder = new URIBuilder(
			String.format("https://api.telegram.org/bot%s/sendMessage", config.getApiToken()))
					.addParameter("chat_id", chatId).addParameter("text", message);
		URI uri = uriBuilder.build();
		logger.info("URL: {}", uri);
		Request request = Request.Get(uri).connectTimeout(CONNECTION_TIMEOUT).socketTimeout(SOCKET_TIMEOUT);
		HttpResponse response = request.execute().returnResponse();

		// Go ahead only if success
		if (response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Error publishing message on telegram channel for "
					+ config.getRegion().getId() + " (error code "
					+ response.getStatusLine().getStatusCode() + ")");
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);
			logger.warn(content);
		}

		return response;
	}

	public HttpResponse sendFile(String region, LanguageCode lang, String message, String attachmentUrl, boolean test) throws IOException, HibernateException {
		TelegramConfiguration config = this.getConfiguration(region, lang);
		String urlString = "https://api.telegram.org/bot%s/sendDocument?chat_id=%s&caption=%s&document=%s";

		String chatId = test ? "aws_test" : config.getChatId();

		urlString = String.format(urlString, config.getApiToken(), chatId,
				URLEncoder.encode(message, "UTF-8"), URLEncoder.encode(attachmentUrl, "UTF-8"));

		Request request = Request.Get(urlString).connectTimeout(CONNECTION_TIMEOUT).socketTimeout(SOCKET_TIMEOUT);
		HttpResponse response = request.execute().returnResponse();

		// Go ahead only if success
		if (response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Error publishing report on telegram channel for "
					+ config.getRegion().getId() + " (error code "
					+ response.getStatusLine().getStatusCode() + ")");
		}

		return response;
	}
}
