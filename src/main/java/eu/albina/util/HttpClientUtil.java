// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HttpClientUtil {

	Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	static ClientBuilder newClientBuilder() {
		return newClientBuilder(10000);
	}

	static ClientBuilder newClientBuilder(int readTimeout) {
		return ClientBuilder.newBuilder()
			.connectTimeout(10000, TimeUnit.MILLISECONDS)
			.readTimeout(readTimeout, TimeUnit.MILLISECONDS)
			.register((ClientRequestFilter) requestContext -> logger.info("Sending {} {}", requestContext.getMethod(), requestContext.getUri()));
	}
}
