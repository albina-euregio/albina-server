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
package eu.albina.controller.publication;

import java.io.IOException;
import java.net.URISyntaxException;

import com.google.common.base.Strings;
import org.hibernate.HibernateException;
import eu.albina.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.TelegramConfiguration;
import eu.albina.util.HibernateUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class TelegramController {
	private static final Logger logger = LoggerFactory.getLogger(TelegramController.class);

	private static TelegramController instance = null;
	private final Client client = HttpClientUtil.newClientBuilder().build();

	public static TelegramController getInstance() {
		if (instance == null) {
			instance = new TelegramController();
		}
		return instance;
	}

	public TelegramController() {
	}

	private TelegramConfiguration getConfiguration(Region region, LanguageCode languageCode) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			TelegramConfiguration result = null;
			if (region != null && !Strings.isNullOrEmpty(region.getId())) {
				result = (TelegramConfiguration) entityManager.createQuery(HibernateUtil.queryGetTelegramConfiguration)
				.setParameter("regionId", region.getId())
				.setParameter("lang", languageCode).getSingleResult();
			} else {
				throw new HibernateException("No region defined!");
			}
			if (result != null)
				return result;
			else
				throw new HibernateException("No telegram configuration found for " + region + " [" + languageCode + "]");
		});
	}

	public Response sendPhoto(Region region, LanguageCode lang, String message, String attachmentUrl, boolean test)
			throws IOException, URISyntaxException, HibernateException {
		TelegramConfiguration config = this.getConfiguration(region, lang);

		if (config == null || config.getChatId() == null || config.getApiToken() == null) {
			throw new IOException("Chat ID not found");
		}

		String chatId = test ? "aws_test" : config.getChatId();

		WebTarget request = client.target(String.format("https://api.telegram.org/bot%s/sendPhoto", config.getApiToken()))
			.queryParam("chat_id", chatId)
			.queryParam("caption", message)
			.queryParam("photo", attachmentUrl);
		return execute(request, config);
	}

	public Response sendMessage(Region region, LanguageCode lang, String message, boolean test) throws IOException, URISyntaxException, HibernateException {
		TelegramConfiguration config = this.getConfiguration(region, lang);
		
		if (config == null || config.getChatId() == null || config.getApiToken() == null) {
			throw new IOException("Chat ID not found");
		}
		
		String chatId = test ? "aws_test" : config.getChatId();

		WebTarget request = client.target(String.format("https://api.telegram.org/bot%s/sendMessage", config.getApiToken()))
			.queryParam("chat_id", chatId)
			.queryParam("text", message);
		return execute(request, config);
	}

	private Response execute(WebTarget request, TelegramConfiguration config) {
		final Response response = request.request().get();
		if (response.getStatusInfo().getStatusCode() != 200) {
			logger.warn("Error publishing on telegram channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.getStatusInfo().getStatusCode() + "): " + response.readEntity(String.class));
		}
		return response;
	}
}
