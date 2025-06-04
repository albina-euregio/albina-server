/*******************************************************************************
 * Copyright (C) 2025 albina
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
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.WhatsAppConfiguration;
import eu.albina.util.HibernateUtil;
import eu.albina.util.HttpClientUtil;
import jakarta.persistence.PersistenceException;

public interface WhatsAppController {
	Logger logger = LoggerFactory.getLogger(WhatsAppController.class);
	Client client = HttpClientUtil.newClientBuilder().build();

	static Optional<WhatsAppConfiguration> getConfiguration(Region region, LanguageCode languageCode) {
		Objects.requireNonNull(region, "region");
		Objects.requireNonNull(region.getId(), "region.getId()");
		Objects.requireNonNull(languageCode, "languageCode");

		return HibernateUtil.getInstance().run(entityManager -> {
			try {
				WhatsAppConfiguration result = (WhatsAppConfiguration) entityManager.createQuery(HibernateUtil.queryGetWhatsAppConfiguration)
					.setParameter("region", region)
					.setParameter("lang", languageCode)
					.getSingleResult();
				return Optional.ofNullable(result);
			} catch (PersistenceException e) {
				return Optional.empty();
			}
		});
	}

	static Void trySend(WhatsAppConfiguration config, MultichannelMessage posting, int retry) throws Exception {
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
			logger.warn("Error while sending bulletin newsletter to WhatsApp channel! Retrying " + newRetry + " times in " + Duration.ofMillis(delay), e);
			final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
			executorService.schedule(() -> {
				executorService.shutdown();
				return trySend(config, posting, newRetry);
			}, delay, TimeUnit.MILLISECONDS);
		}
		return null;
	}

	static Response sendPhoto(WhatsAppConfiguration config, String message, String attachmentUrl)
			throws IOException, HibernateException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending photo {} and message {} to whatsapp channel using config {}", attachmentUrl, message, config);

		String payload = new Gson().toJson(Map.of(
			"to", chatId,
			"media", attachmentUrl,
			"caption", message
		));

		Response response = client.target("https://gate.whapi.cloud/messages/image")
			.request(MediaType.APPLICATION_JSON)
			.header("authorization", "Bearer " + apiToken)
			.post(Entity.entity(payload, MediaType.APPLICATION_JSON));
		if (response.getStatusInfo().getStatusCode() != 200) {
			logger.warn("Error publishing on whatsapp channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.getStatusInfo().getStatusCode() + "): " + response.readEntity(String.class));
		}
		return response;
	}

	static Response sendMessage(WhatsAppConfiguration config, String message) throws HibernateException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending message {} to whatsapp channel using config {}", message, config);

		String payload = new Gson().toJson(Map.of(
			"to", chatId,
			"body", message
		));

		Response response = client.target("https://gate.whapi.cloud/messages/text")
			.request(MediaType.APPLICATION_JSON)
			.header("authorization", "Bearer " + apiToken)
			.post(Entity.entity(payload, MediaType.APPLICATION_JSON));
		if (response.getStatusInfo().getStatusCode() != 200) {
			logger.warn("Error publishing on whatsapp channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.getStatusInfo().getStatusCode() + "): " + response.readEntity(String.class));
		}
		return response;
	}

	static Response getHealth(WhatsAppConfiguration config) throws HibernateException {
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		Response response = client.target("https://gate.whapi.cloud/health")
				.request(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer " + apiToken)
				.get();

		if (response.getStatusInfo().getStatusCode() != 200) {
			logger.warn("Error connecting to whapi.cloud (error code "
				+ response.getStatusInfo().getStatusCode() + "): " + response.readEntity(String.class));
		}
		return response;
	}
}
