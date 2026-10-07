// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public interface HttpClientUtil {

	static HttpClient.Builder newClientBuilder() {
		return newClientBuilder(10000);
	}

	static HttpClient.Builder newClientBuilder(int readTimeout) {
		return HttpClient.newBuilder()
			.connectTimeout(Duration.ofMillis(readTimeout));
	}

	static String queryParams(Map<String, Object> data) {
		return data.entrySet().stream()
			.map(entry -> entry.getKey() + "=" + URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8))
			.collect(Collectors.joining("&"));
	}

	static boolean isUrlAvailable(HttpClient client, String url) {
		Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
		try {
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.method("HEAD", HttpRequest.BodyPublishers.noBody())
				.build();
			HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
			return response.statusCode() >= 200 && response.statusCode() < 300;
		} catch (Exception e) {
			logger.warn("Could not reach attachment URL {}: {}", url, e.getMessage());
			return false;
		}
	}

	static void checkResponse(HttpResponse<String> response) throws IOException {
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to fetch posts from %s: %s".formatted(response.request().uri(), response.body()));
		}
	}

	@Factory
	class HttpClientFactory {

		@Bean
		public HttpClient httpClient() {
			return newClientBuilder().build();
		}
	}
}
