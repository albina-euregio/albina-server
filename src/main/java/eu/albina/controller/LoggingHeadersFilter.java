package eu.albina.controller;

import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.security.token.Claims;
import io.micronaut.security.token.jwt.validator.JsonWebTokenParser;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@ServerFilter(Filter.MATCH_ALL_PATTERN)
class LoggingHeadersFilter implements Ordered {

	private static final Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	private static final String X_REQUEST_ID = "X-Request-ID";

	@Inject
	JsonWebTokenParser<?> jsonWebTokenParser;

	@RequestFilter
	void filterRequest(HttpRequest<?> request) {
		if (request.getMethod() == HttpMethod.GET || request.getMethod() == HttpMethod.OPTIONS) {
			return;
		}

		String requestID = UUID.randomUUID().toString();
		MDC.clear();
		MDC.put(X_REQUEST_ID, requestID);
		request.getHeaders().asMap().putIfAbsent(X_REQUEST_ID, List.of(requestID));

		String username = request.getUserPrincipal()
			.map(Principal::getName)
			.or(() -> request.getHeaders().getAuthorization()
				.map(h -> h.startsWith("Bearer ") ? h.substring("Bearer ".length()) : null)
				.flatMap(jsonWebTokenParser::parseClaims)
				.map(c -> String.valueOf(c.get(Claims.SUBJECT))))
			.orElse(null);
		logger.info("User {} executes {} {}", username, request.getMethod(), request.getUri());
	}
}
