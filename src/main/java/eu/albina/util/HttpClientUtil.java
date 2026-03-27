// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.apache.commons.io.function.IOFunction;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
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

	static <T> LoadingCache<URI, T> newHttpCache(Supplier<HttpClient> client, IOFunction<String, T> function) {
		return CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(5))
			.build(new CacheLoader<>() {
				@Override
				public T load(URI uri) throws Exception {
					HttpRequest request = HttpRequest.newBuilder(uri).build();
					HttpResponse<String> response = client.get().send(request, HttpResponse.BodyHandlers.ofString());
					checkResponse(response);
					return function.apply(response.body());
				}
			});
	}

	@Factory
	class HttpClientFactory {

		@Bean
		public HttpClient httpClient() {
			return newClientBuilder().build();
		}
	}
}
