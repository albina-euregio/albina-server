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
import java.net.URISyntaxException;

import eu.albina.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.socialmedia.TelegramConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class TelegramChannelProcessorController {
	private static final Logger logger = LoggerFactory.getLogger(TelegramChannelProcessorController.class);

	private static TelegramChannelProcessorController instance = null;

	private final Client client = HttpClientUtil.newClientBuilder().build();

	public static TelegramChannelProcessorController getInstance() {
		if (instance == null) {
			instance = new TelegramChannelProcessorController();
		}
		return instance;
	}

	public TelegramChannelProcessorController() {
	}

	public Response sendPhoto(TelegramConfig config, String message, String attachmentUrl, boolean test)
			throws IOException, URISyntaxException {
		// https://core.telegram.org/bots/api#sendphoto
		String chatId = test ? "@aws_test" : config.getChatId();
		WebTarget request = client.target(String.format("https://api.telegram.org/bot%s/sendPhoto", config.getApiToken()))
			.queryParam("chat_id", chatId)
			.queryParam("caption", message)
			.queryParam("photo", attachmentUrl);
		return execute(request, config);
	}

	public Response sendMessage(TelegramConfig config, String message, boolean test) throws IOException, URISyntaxException {
		// https://core.telegram.org/bots/api#sendmessage
		String chatId = test ? "@aws_test" : config.getChatId();
		WebTarget request = client.target(String.format("https://api.telegram.org/bot%s/sendMessage", config.getApiToken()))
			.queryParam("chat_id", chatId)
			.queryParam("text", message);
		return execute(request, config);
	}

	private Response execute(WebTarget request, TelegramConfig config) {
		final Response response = request.request().get();
		if (response.getStatusInfo().getStatusCode() != 200) {
			logger.warn("Error publishing on telegram channel for "
				+ config.getRegionConfiguration().getRegion().getId() + " (error code "
				+ response.getStatusInfo().getStatusCode() + "): " + response.readEntity(String.class));
		}
		return response;
	}
}
