// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import com.google.common.hash.Hashing;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.serde.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;

/**
 * A cached response body together with its strong ETag, used to answer conditional requests
 * ({@code If-None-Match}) with {@code 304 Not Modified}.
 *
 * @param body the response body
 * @param etag the quoted ETag, a SHA-256 hash of the JSON-serialized body
 * @param <T>  the type of the response body
 */
record Cached<T>(T body, String etag) {

	/**
	 * Wraps the given body and computes its ETag from the JSON serialization.
	 *
	 * @param objectMapper the mapper used to serialize the body
	 * @param body         the response body
	 * @param <T>          the type of the response body
	 * @return the body with its ETag
	 * @throws IOException if the body cannot be serialized
	 */
	static <T> Cached<T> withETag(ObjectMapper objectMapper, T body) throws IOException {
		String hash = Hashing.sha256().hashBytes(objectMapper.writeValueAsBytes(body)).toString();
		return new Cached<>(body, "\"" + hash + "\"");
	}

	/**
	 * Creates the HTTP response including the {@code ETag} header. Returns {@code 304 Not Modified}
	 * without body if {@code If-None-Match} matches the ETag (weak comparison, {@code *} matches any),
	 * otherwise {@code 200 OK} with the body.
	 *
	 * @param cached      the cached body with its ETag
	 * @param ifNoneMatch the value of the {@code If-None-Match} request header, or empty if absent
	 * @param <T>         the type of the response body
	 * @return the HTTP response
	 */
	static <T> HttpResponse<T> toResponse(Cached<T> cached, String ifNoneMatch) {
		boolean notModified = Arrays.stream(ifNoneMatch.split(","))
			.map(String::trim)
			.map(tag -> tag.startsWith("W/") ? tag.substring(2) : tag)
			.anyMatch(tag -> tag.equals("*") || tag.equals(cached.etag()));
		if (notModified) {
			return HttpResponse.<T>notModified().header(HttpHeaders.ETAG, cached.etag());
		}
		return HttpResponse.ok(cached.body()).header(HttpHeaders.ETAG, cached.etag());
	}
}
