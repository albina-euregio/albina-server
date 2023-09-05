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
import java.net.URL;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.hibernate.HibernateException;
import eu.albina.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.TelegramConfiguration;
import eu.albina.util.HibernateUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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
				.setParameter("region", region)
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

	public Void trySendPhoto(Region region, LanguageCode lang, String message, String attachmentUrl, int retry) throws Exception {
		try {
			sendPhoto(region, lang, message, attachmentUrl);
		} catch (Exception e) {
			if (retry <= 0) {
				throw e;
			}
			final int newRetry = retry - 1;
			final int delay = 50_000 + new Random().nextInt(20_000); // after 50..70s
			logger.warn("Error while sending bulletin newsletter to telegram channel! Retrying " + newRetry + " times in " + Duration.ofMillis(delay), e);
			final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
			executorService.schedule(() -> {
				executorService.shutdown();
				return trySendPhoto(region, lang, message, attachmentUrl, newRetry);
			}, delay, TimeUnit.MILLISECONDS);
		}
		return null;
	}


	/**
	 * Send photo to Telegram channel using HTTP multipart/form-data
	 *
	 * @see <a href="https://core.telegram.org/bots/api#sendphoto">https://core.telegram.org/bots/api#sendphoto</a>
	 * @see <a href="https://core.telegram.org/bots/api#inputfile">https://core.telegram.org/bots/api#inputfile</a>
	 */
	public Response sendPhoto(Region region, LanguageCode lang, String message, String attachmentUrl)
			throws IOException, URISyntaxException, HibernateException {
		TelegramConfiguration config = this.getConfiguration(region, lang);

		if (config == null || config.getChatId() == null || config.getApiToken() == null) {
			throw new IOException("Chat ID not found");
		}

		String chatId = config.getChatId();

		MultiPart multiPart = new FormDataMultiPart();
		multiPart.bodyPart(new StreamDataBodyPart(
			"photo",
			new URL(attachmentUrl).openStream(),
			attachmentUrl.substring(attachmentUrl.lastIndexOf("/") + 1),
			MediaType.APPLICATION_OCTET_STREAM_TYPE));

		Response response = HttpClientUtil
			.newClientBuilder(80000) // sending photos may a while
			.register(MultiPartFeature.class)
			.build()
			.target(String.format("https://api.telegram.org/bot%s/sendPhoto", config.getApiToken()))
			.register(MultiPartFeature.class)
			.queryParam("chat_id", chatId)
			.queryParam("caption", message)
			.request()
			.post(Entity.entity(multiPart, multiPart.getMediaType()));

		if (response.getStatusInfo().getStatusCode() != 200) {
			// FIXME throw exception?
			logger.warn("Error publishing on telegram channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.getStatusInfo().getStatusCode() + "): " + response.readEntity(String.class));
		}
		return response;
	}

	public Response sendMessage(Region region, LanguageCode lang, String message) throws IOException, URISyntaxException, HibernateException {
		TelegramConfiguration config = this.getConfiguration(region, lang);

		if (config == null || config.getChatId() == null || config.getApiToken() == null) {
			throw new IOException("Chat ID not found");
		}

		String chatId = config.getChatId();

		WebTarget request = client.target(String.format("https://api.telegram.org/bot%s/sendMessage", config.getApiToken()))
			.queryParam("chat_id", chatId)
			.queryParam("text", message);
		return execute(request, config);
	}

	/**
	 * A simple method for testing your bot's authentication token. Requires no parameters. Returns basic information about the bot in form of a User object.
	 * @see <a href="https://core.telegram.org/bots/api#getme">https://core.telegram.org/bots/api#getme</a>
	 */
	public Response getMe(Region region, LanguageCode lang) throws IOException, HibernateException {
		TelegramConfiguration config = this.getConfiguration(region, lang);
		if (config == null || config.getChatId() == null || config.getApiToken() == null) {
			throw new IOException("Chat ID not found");
		}
		WebTarget request = client.target(String.format("https://api.telegram.org/bot%s/getMe", config.getApiToken()));
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
