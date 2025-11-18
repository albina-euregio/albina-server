// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import com.google.common.collect.MoreCollectors;
import eu.albina.model.publication.BlogConfiguration;

import eu.albina.util.HttpClientUtil;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Singleton
class Blogger {

	@Inject
	HttpClient client;

	@Inject
	ObjectMapper objectMapper;

	public List<Item> getBlogPosts(BlogConfiguration config) throws IOException, InterruptedException {
		OffsetDateTime lastPublishedTimestamp = Objects.requireNonNull(config.getLastPublishedTimestamp(), "lastPublishedTimestamp");
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + config.getBlogId() + "/posts?" + HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey"),
			"startDate", lastPublishedTimestamp.toInstant().plusSeconds(1).toString(),
			"fetchBodies", Boolean.TRUE.toString(),
			"fetchImages", Boolean.TRUE.toString()
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		Root root = objectMapper.readValue(response.body(), Root.class);
		return root.items != null ? root.items : Collections.emptyList();
	}

	public Item getLatestBlogPost(BlogConfiguration config) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + config.getBlogId() + "/posts?" + HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey"),
			"fetchBodies", Boolean.TRUE.toString(),
			"fetchImages", Boolean.TRUE.toString(),
			"maxResults", Integer.toString(1)
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		Root root = objectMapper.readValue(response.body(), Root.class);
		return root.items.stream().collect(MoreCollectors.onlyElement());
	}

	public Item getBlogPost(BlogConfiguration config, String blogPostId) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + config.getBlogId() + "/posts/" + blogPostId + "?" + HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey")
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), Item.class);
	}

	@Serdeable
	record Root(
		String kind,
		String nextPageToken,
		List<Item> items,
		String etag
	) {
	}

	@Serdeable
	record Item(
		String kind,
		String id,
		String content,
		Blog blog,
		String published,
		String updated,
		String url,
		String selfLink,
		String title,
		List<Image> images,
		Author author,
		Replies replies,
		List<String> labels,
		String etag
	) implements BlogItem {

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getContent() {
			return content;
		}

		@Override
		public OffsetDateTime getPublished() {
			return OffsetDateTime.parse(published);
		}

		@Override
		public String getAttachmentUrl() {
			if (images != null && !images.isEmpty()) {
				return images.getFirst().url;
			} else {
				return null;
			}
		}
	}

	@Serdeable
	record Author(String id, String displayName, String url, Image image) {
	}

	@Serdeable
	record Image(String url) {
	}

	@Serdeable
	record Blog(String id) {
	}

	@Serdeable
	record Replies(String totalItems, String selfLink) {
	}

}
