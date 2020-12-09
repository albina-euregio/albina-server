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
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.socialmedia.TelegramConfig;

public class TelegramChannelProcessorController extends CommonProcessor {
	private static Logger logger = LoggerFactory.getLogger(TelegramChannelProcessorController.class);

	private static TelegramChannelProcessorController instance = null;
	private int CONNECTION_TIMEOUT = 10000;
	private int SOCKET_TIMEOUT = 10000;

	public static TelegramChannelProcessorController getInstance() {
		if (instance == null) {
			instance = new TelegramChannelProcessorController();
		}
		return instance;
	}

	public TelegramChannelProcessorController() {
	}

	public HttpResponse sendNewsletter(TelegramConfig config, String message, String attachmentUrl) throws IOException {
		String urlString = "https://api.telegram.org/bot%s/sendPhoto?chat_id=%s&caption=%s&photo=%s";

		urlString = String.format(urlString, config.getApiToken(), config.getChatId(),
				URLEncoder.encode(message, "UTF-8"), URLEncoder.encode(attachmentUrl, "UTF-8"));

		logger.info("URL: " + urlString);

		Request request = Request.Get(urlString).connectTimeout(CONNECTION_TIMEOUT).socketTimeout(SOCKET_TIMEOUT);
		HttpResponse response = request.execute().returnResponse();

		// Go ahead only if success
		if (response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Error publishing report on telegram channel for "
					+ config.getRegionConfiguration().getRegion().getId() + " (error code "
					+ response.getStatusLine().getStatusCode() + ")");
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);
			logger.warn(content);
		}

		return response;
	}

	public HttpResponse sendFile(TelegramConfig config, String message, String attachmentUrl) throws IOException {
		String urlString = "https://api.telegram.org/bot%s/sendDocument?chat_id=%s&caption=%s&document=%s";

		urlString = String.format(urlString, config.getApiToken(), config.getChatId(),
				URLEncoder.encode(message, "UTF-8"), URLEncoder.encode(attachmentUrl, "UTF-8"));

		Request request = Request.Get(urlString).connectTimeout(CONNECTION_TIMEOUT).socketTimeout(SOCKET_TIMEOUT);
		HttpResponse response = request.execute().returnResponse();

		// Go ahead only if success
		if (response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Error publishing report on telegram channel for "
					+ config.getRegionConfiguration().getRegion().getId() + " (error code "
					+ response.getStatusLine().getStatusCode() + ")");
		}

		return response;
	}
}
