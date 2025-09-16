// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.net.HttpHeaders;
import eu.albina.util.JsonUtil;
import io.micronaut.http.MediaType;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.WhatsAppConfiguration;
import eu.albina.util.HibernateUtil;
import eu.albina.util.HttpClientUtil;
import jakarta.persistence.PersistenceException;

public interface WhatsAppController {
	Logger logger = LoggerFactory.getLogger(WhatsAppController.class);
	HttpClient client = HttpClientUtil.newClientBuilder().build();

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

	static void sendPhoto(WhatsAppConfiguration config, String message, String attachmentUrl)
		throws IOException, HibernateException, InterruptedException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending photo {} and message {} to whatsapp channel using config {}", attachmentUrl, message, config);

		String payload = JsonUtil.writeValueUsingJackson(Map.of(
			"to", chatId,
			"media", attachmentUrl,
			"caption", message
		));

		HttpRequest request = HttpRequest.newBuilder(URI.create("https://gate.whapi.cloud/messages/image"))
			.POST(HttpRequest.BodyPublishers.ofString(payload))
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
			.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			logger.warn("Error publishing on whatsapp channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.statusCode() + "): " + response.body());
		}
	}

	static void sendMessage(WhatsAppConfiguration config, String message) throws HibernateException, IOException, InterruptedException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending message {} to whatsapp channel using config {}", message, config);

		String payload = JsonUtil.writeValueUsingJackson(Map.of(
			"to", chatId,
			"body", message
		));

		HttpRequest request = HttpRequest.newBuilder(URI.create("https://gate.whapi.cloud/messages/text"))
			.POST(HttpRequest.BodyPublishers.ofString(payload))
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
			.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			logger.warn("Error publishing on whatsapp channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.statusCode() + "): " + response.body());
		}
	}

	static String getHealth(WhatsAppConfiguration config) throws HibernateException, IOException, InterruptedException {
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");

		HttpRequest request = HttpRequest.newBuilder(URI.create("https://gate.whapi.cloud/health"))
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
			.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			logger.warn("Error connecting to whapi.cloud (error code "
				+ response.statusCode() + "): " + response.body());
		}
		return response.body();
	}
}
