// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.filter;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import com.google.common.hash.Hashing;

/**
 * Handles ETag generation as well as If-None-Match request Header. This filter will erase the contents of the
 * generated response (headers and payload) if the generated ETag value and the provided If-None-Match
 * request Header matches each other. In such cases the status code will also be updated to 304 (Not Modified).
 * <p>
 * ETag will be calculated based on the {@link Object#toString()} of the response entity.
 *
 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.19">ETag header field definition</a>
 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.26">If-None-Match header field definition</a>
 */
@Provider
public class ETagFilter implements ContainerRequestFilter, ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) {
		final String value = requestContext.getHeaders().getFirst(HttpHeaders.IF_NONE_MATCH);
		if (value != null) {
			requestContext.setProperty(HttpHeaders.IF_NONE_MATCH, value.replace("\"", ""));
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		if (!"GET".equals(requestContext.getMethod())
				|| responseContext.getStatus() != Response.Status.OK.getStatusCode()) {
			return;
		}
		final String entity = String.valueOf(responseContext.getEntity());
		final String eTag = Hashing.sha256().hashString(entity, StandardCharsets.UTF_8).toString();
		if (Objects.equals(requestContext.getProperty(HttpHeaders.IF_NONE_MATCH), eTag)) {
			responseContext.setEntity(null);
			responseContext.setStatusInfo(Response.Status.NOT_MODIFIED);
			final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
			headers.keySet().forEach(headers::remove);
		}
		responseContext.getHeaders().putSingle(HttpHeaders.ETAG, "\"" + eTag + "\"");
	}

}
