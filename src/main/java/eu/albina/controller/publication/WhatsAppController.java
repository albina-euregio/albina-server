// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import com.google.common.net.HttpHeaders;
import eu.albina.controller.CrudRepository;
import eu.albina.model.Region;
import eu.albina.model.StatusInformation;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.TelegramConfiguration;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
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
	 * Class for Whapi responses.
	 * See <a href="https://support.whapi.cloud/help-desk/receiving/webhooks/incoming-webhooks-format/account-and-device-status">support.whapi.cloud</a>
	 * @param code 1,2,3,4,5
	 * @param text INIT, LAUNCH, QR, AUTH, ERROR
	 */
	@Serdeable
	public record WhatsAppStatus(int code, String text){}

	@Serdeable
	public record HealthResponse(WhatsAppStatus status) {}

	@Repository
	public interface WhatsAppConfigurationRepository extends CrudRepository<WhatsAppConfiguration, Long> {
		List<WhatsAppConfiguration> findByRegion(Region region);
		Optional<WhatsAppConfiguration> findByRegionAndLanguageCode(Region region, LanguageCode languageCode);
	}

	public List<WhatsAppConfiguration> getConfigurations(Region region) {
		return whatsAppConfigurationRepository.findByRegion(region);
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

	public CompletableFuture<StatusInformation> getStatusAsync(WhatsAppConfiguration config, String statusTitle) {
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");

		HttpRequest request = HttpRequest.newBuilder(URI.create("https://gate.whapi.cloud/health"))
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
			.build();

		logger.info("Sending request {}", request.uri());

		// Note: Whapi can be extremely slow in answering requests when using non-existing channels and/or API keys
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenApply(response -> mapToStatusInformation(response, statusTitle))
			.exceptionally(ex ->
				new StatusInformation(false, statusTitle, ex.getMessage())
			);
	}

	private StatusInformation mapToStatusInformation(HttpResponse<String> response, String statusTitle) {
		if (response.statusCode() != 200) {
			String message = "Error connecting to whapi.cloud (error code " + response.statusCode() + "): " + response.body();
			return new StatusInformation(false, statusTitle, message);
		}
		try {
			HealthResponse healthResponse = objectMapper.readValue(response.body(), HealthResponse.class);
			if (healthResponse.status.code != 4) {
				return new StatusInformation(false, statusTitle, "Whapi response: " + healthResponse.status.text);
			} else {
				return new StatusInformation(true, statusTitle, "Whapi response: " + healthResponse.status.text);
			}
		} catch (IOException e) {
			return new StatusInformation(false, statusTitle, "Invalid Whapi JSON response: " + e.getMessage());
		}
	}
}
