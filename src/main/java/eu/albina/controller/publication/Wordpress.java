// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import com.google.common.collect.MoreCollectors;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.util.HttpClientUtil;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Singleton
public class Wordpress {

	@Inject
	ObjectMapper objectMapper;

	public List<Item> getBlogPosts(BlogConfiguration config, HttpClient client) throws IOException, InterruptedException {
		// https://developer.wordpress.org/rest-api/reference/posts/#arguments
		OffsetDateTime lastPublishedTimestamp = Objects.requireNonNull(config.getLastPublishedTimestamp(), "lastPublishedTimestamp");
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + "posts?" + HttpClientUtil.queryParams(Map.of(
			"lang", config.getLanguageCode(),
			"after", lastPublishedTimestamp.toInstant().plusSeconds(1).toString(),
			"order", "asc"
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		Item[] items = objectMapper.readValue(response.body(), Item[].class);
		return List.of(items);
	}

	public Item getLatestBlogPost(BlogConfiguration config, HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + "posts?" + HttpClientUtil.queryParams(Map.of(
			"lang", config.getLanguageCode(),
			"per_page", Integer.toString(1)
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		Item[] items = objectMapper.readValue(response.body(), Item[].class);
		return Arrays.stream(items).collect(MoreCollectors.onlyElement());
	}

	public Item getBlogPost(BlogConfiguration config, String blogPostId, HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + "posts/" + blogPostId)).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), Item.class);
	}

	@Serdeable
	record Item(
		long id,
		String date,
		String date_gmt,
		String link,
		Rendered title,
		Rendered content,
		Rendered excerpt,
		long featured_media,
		long[] categories,
		String polylang_current_lang,
		PolylangTranslation[] polylang_translations,
		String featured_image_url
	) implements BlogItem {

		@Override
		public String getId() {
			return String.valueOf(id);
		}

		@Override
		public String getTitle() {
			return StringEscapeUtils.unescapeHtml4(title.rendered);
		}

		@Override
		public String getContent() {
			return content.rendered;
		}

		@Override
		public OffsetDateTime getPublished() {
			return LocalDateTime.parse(date_gmt).atOffset(ZoneOffset.UTC);
		}

		@Override
		public String getAttachmentUrl() {
			return featured_image_url;
		}
	}

	@Serdeable
	record Rendered(String rendered) {
	}

	@Serdeable
	record PolylangTranslation(String locale, long id) {
	}
}
