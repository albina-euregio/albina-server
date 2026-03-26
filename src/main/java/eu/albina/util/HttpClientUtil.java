// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
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
