// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import com.google.common.net.HttpHeaders;
import eu.albina.controller.CrudRepository;
import eu.albina.model.Region;
import eu.albina.model.StatusInformation;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.WhatsAppConfiguration;
import io.micronaut.data.annotation.Repository;
import io.micronaut.http.MediaType;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Singleton
public class WhatsAppController {
	private static final Logger logger = LoggerFactory.getLogger(WhatsAppController.class);

	@Inject
	ObjectMapper objectMapper;

	@Inject
	HttpClient client;

	@Inject
	WhatsAppConfigurationRepository whatsAppConfigurationRepository;

	/**
	 *
	 * @param code Whapi returns numbers 1,2,3,4,5, if albina-server encounters error it sets code to -1, code 0 is used if no whatsapp config is set
	 * @param text INIT, LAUNCH, QR, AUTH, ERROR
	 */
	@Serdeable
	public record WhatsAppStatus(int code, String text){}

	@Serdeable
	public record HealthResponse(WhatsAppStatus status) {}

	@Repository
	public interface WhatsAppConfigurationRepository extends CrudRepository<WhatsAppConfiguration, Long> {
		Optional<WhatsAppConfiguration> findByRegionAndLanguageCode(Region region, LanguageCode languageCode);
	}

	public Optional<WhatsAppConfiguration> getConfiguration(Region region, LanguageCode languageCode) {
		return whatsAppConfigurationRepository.findByRegionAndLanguageCode(region, languageCode);
	}

	public Void trySend(WhatsAppConfiguration config, MultichannelMessage posting, int retry) throws Exception {
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

	public void sendPhoto(WhatsAppConfiguration config, String message, String attachmentUrl)
		throws IOException, InterruptedException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending photo {} and message {} to whatsapp channel using config {}", attachmentUrl, message, config);

		String payload = objectMapper.writeValueAsString(Map.of(
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

	public void sendMessage(WhatsAppConfiguration config, String message) throws IOException, InterruptedException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending message {} to whatsapp channel using config {}", message, config);

		String payload = objectMapper.writeValueAsString(Map.of(
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

	public StatusInformation getStatus(WhatsAppConfiguration config, String statusTitle) throws IOException, InterruptedException {
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");

		HttpRequest request = HttpRequest.newBuilder(URI.create("https://gate.whapi.cloud/health"))
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
			.build();

		logger.info("Sending request {}", request.uri());

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			String message = "Error connecting to whapi.cloud (error code "
				+ response.statusCode() + "): " + response.body();
			logger.warn(message);
			return new StatusInformation(false, statusTitle, message);
		}
		HealthResponse healthResponse = objectMapper.readValue(response.body(), HealthResponse.class);
		if(healthResponse.status.code != 4) {
			return new StatusInformation(false, statusTitle,"Whapi response: " + healthResponse.status.text);
		}
		return new StatusInformation(true, statusTitle, "Whapi response: " + healthResponse.status.text);
	}
}
