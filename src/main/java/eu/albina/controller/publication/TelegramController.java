// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.albina.util.HttpClientUtil;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.TelegramConfiguration;

@Singleton
public class TelegramController {
	private static final Logger logger = LoggerFactory.getLogger(TelegramController.class);

	@Inject
	HttpClient client;

	@Inject
	TelegramConfigurationRepository telegramConfigurationRepository;

	@Repository
	public interface TelegramConfigurationRepository extends CrudRepository<TelegramConfiguration, Long> {
		Optional<TelegramConfiguration> findByRegionAndLanguageCode(Region region, LanguageCode languageCode);
	}

	public Optional<TelegramConfiguration> getConfiguration(Region region, LanguageCode languageCode) {
		return telegramConfigurationRepository.findByRegionAndLanguageCode(region, languageCode);
	}

	public Void trySend(TelegramConfiguration config, MultichannelMessage posting, int retry) throws Exception {
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
	public HttpResponse<String> sendPhoto(TelegramConfiguration config, String message, String attachmentUrl)
		throws IOException, InterruptedException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending photo {} and message {} to telegram channel using config {}", attachmentUrl, message, config);

		String boundary = UUID.randomUUID().toString();
		String fileName = attachmentUrl.substring(attachmentUrl.lastIndexOf("/") + 1);

		HttpClient client = HttpClientUtil
			.newClientBuilder(80000) // sending photos may a while
			.build();
		HttpRequest request = HttpRequest.newBuilder(URI.create(String.format("https://api.telegram.org/bot%s/sendPhoto?chat_id=%s&caption=%s",
				apiToken,
				URLEncoder.encode(chatId, StandardCharsets.UTF_8),
				URLEncoder.encode(message, StandardCharsets.UTF_8)
			)))
			.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA + "; boundary=" + boundary)
			.POST(HttpRequest.BodyPublishers.ofByteArrays(List.of(
				"--%s\r\n".formatted(boundary).getBytes(StandardCharsets.UTF_8),
				"Content-Disposition: form-data; name=\"photo\"; filename=\"%s\"\r\n".formatted(fileName).getBytes(StandardCharsets.UTF_8),
				"Content-Type: %s\r\n".formatted(MediaType.APPLICATION_OCTET_STREAM).getBytes(StandardCharsets.UTF_8),
				"\r\n".getBytes(StandardCharsets.UTF_8),
				URI.create(attachmentUrl).toURL().openStream().readAllBytes(),
				"\r\n".getBytes(StandardCharsets.UTF_8),
				"--%s--\r\n".formatted(boundary).getBytes(StandardCharsets.UTF_8)
			)))
			.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			// FIXME throw exception?
			logger.warn("Error publishing on telegram channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.statusCode() + "): " + response.body());
		}
		return response;
	}

	public HttpResponse<String> sendMessage(TelegramConfiguration config, String message) throws IOException, InterruptedException {
		Objects.requireNonNull(config, "config");
		String chatId = Objects.requireNonNull(config.getChatId(), "config.getChatId");
		String apiToken = Objects.requireNonNull(config.getApiToken(), "config.getApiToken");
		logger.info("Sending message {} to telegram channel using config {}", message, config);

		HttpRequest request = HttpRequest.newBuilder(URI.create(String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s",
			apiToken,
			URLEncoder.encode(chatId, StandardCharsets.UTF_8),
			URLEncoder.encode(message, StandardCharsets.UTF_8)
		))).build();
		return execute(request, config);
	}

	/**
	 * A simple method for testing your bot's authentication token. Requires no parameters. Returns basic information about the bot in form of a User object.
	 *
	 * @see <a href="https://core.telegram.org/bots/api#getme">https://core.telegram.org/bots/api#getme</a>
	 */
	public HttpResponse<String> getMe(TelegramConfiguration config) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(String.format("https://api.telegram.org/bot%s/getMe", config.getApiToken()))).build();
		return execute(request, config);
	}

	public HttpResponse<String> execute(HttpRequest request, TelegramConfiguration config) throws IOException, InterruptedException {
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			logger.warn("Error publishing on telegram channel for "
				+ config.getRegion().getId() + " (error code "
				+ response.statusCode() + "): " + response.body());
		}
		return response;
	}
}
