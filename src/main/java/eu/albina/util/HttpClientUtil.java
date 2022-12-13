package eu.albina.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import java.util.concurrent.TimeUnit;

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
