// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.GenericEntity;
import org.hibernate.HibernateException;
import eu.albina.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.TelegramConfiguration;
import eu.albina.util.HibernateUtil;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface TelegramController {
	Logger logger = LoggerFactory.getLogger(TelegramController.class);
	Client client = HttpClientUtil.newClientBuilder().build();

	static Optional<TelegramConfiguration> getConfiguration(Region region, LanguageCode languageCode) {
		Objects.requireNonNull(region, "region");
		Objects.requireNonNull(region.getId(), "region.getId()");
		Objects.requireNonNull(languageCode, "languageCode");

		return HibernateUtil.getInstance().run(entityManager -> {
			try {
				TelegramConfiguration result = (TelegramConfiguration) entityManager.createQuery(HibernateUtil.queryGetTelegramConfiguration)
					.setParameter("region", region)
					.setParameter("lang", languageCode)
					.getSingleResult();
				return Optional.ofNullable(result);
			} catch (PersistenceException e) {
				return Optional.empty();
			}
		});
	}

	static Void trySend(TelegramConfiguration config, MultichannelMessage posting, int retry) throws Exception {
		try {
			String message = posting.getSocialMediaText();
			String attachmentUrl = posting.getAttachmentUrl();
			if (attachmentUrl != null) {
				sendPhoto(config, message, attachmentUrl);
			} else {
				sendMessage(config, message);
			}
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
				return trySend(config, posting, newRetry);
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
	static Response sendPhoto(TelegramConfiguration config, String message, String attachmentUrl)
		throws IOException, HibernateException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending photo {} and message {} to telegram channel using config {}", attachmentUrl, message, config);

		List<EntityPart> parts = List.of(
			EntityPart.withName("photo")
				.fileName(attachmentUrl.substring(attachmentUrl.lastIndexOf("/") + 1))
				.content(new URL(attachmentUrl).openStream())
				.mediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE).build()
		);
		GenericEntity<List<EntityPart>> multiPart = new GenericEntity<>(parts) {
		};

		Response response = HttpClientUtil
			.newClientBuilder(80000) // sending photos may a while
			.build()
			.target(String.format("https://api.telegram.org/bot%s/sendPhoto", apiToken))
			.queryParam("chat_id", chatId)
			.queryParam("caption", message)
			.request()
			.post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA));

		if (response.getStatusInfo().getStatusCode() != 200) {
			// FIXME throw exception?
			logger.warn("Error publishing on telegram channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.getStatusInfo().getStatusCode() + "): " + response.readEntity(String.class));
		}
		return response;
	}

	static Response sendMessage(TelegramConfiguration config, String message) throws HibernateException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending message {} to telegram channel using config {}", message, config);

		WebTarget request = client.target(String.format("https://api.telegram.org/bot%s/sendMessage", apiToken))
			.queryParam("chat_id", chatId)
			.queryParam("text", message);
		return execute(request, config);
	}

	/**
	 * A simple method for testing your bot's authentication token. Requires no parameters. Returns basic information about the bot in form of a User object.
	 * @see <a href="https://core.telegram.org/bots/api#getme">https://core.telegram.org/bots/api#getme</a>
	 */
	static Response getMe(TelegramConfiguration config) throws HibernateException {
		WebTarget request = client.target(String.format("https://api.telegram.org/bot%s/getMe", config.getApiToken()));
		return execute(request, config);
	}

	static Response execute(WebTarget request, TelegramConfiguration config) {
		final Response response = request.request().get();
		if (response.getStatusInfo().getStatusCode() != 200) {
			logger.warn("Error publishing on telegram channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.getStatusInfo().getStatusCode() + "): " + response.readEntity(String.class));
		}
		return response;
	}
}
