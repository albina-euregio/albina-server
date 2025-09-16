// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.net.URLEncoder;
import java.net.http.HttpClient;
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
}
