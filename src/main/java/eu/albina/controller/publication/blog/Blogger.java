// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

@Singleton
class Blogger implements AbstractBlog {

	@Inject
	HttpClient client;

	@Inject
	ObjectMapper objectMapper;

	final LoadingCache<URI, List<BlogItem>> postsCache = CacheBuilder.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(5))
		.build(new CacheLoader<>() {
			@Override
			public List<BlogItem> load(URI uri) throws Exception {
				HttpRequest request = HttpRequest.newBuilder(uri).build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				Root root = objectMapper.readValue(response.body(), Root.class);
				return root.toBlogItems();
			}
		});

	final LoadingCache<URI, BlogItem> postCache = CacheBuilder.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(5))
		.build(new CacheLoader<>() {
			@Override
			public BlogItem load(URI uri) throws Exception {
				HttpRequest request = HttpRequest.newBuilder(uri).build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				return objectMapper.readValue(response.body(), Item.class).toBlogItem();
			}
		});

	@Override
	public List<BlogItem> getCachedBlogPosts(BlogConfiguration config, String searchText, String searchCategory, Instant startDate, Instant endDate) throws ExecutionException {
		Map<String, Object> params = new TreeMap<>(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey"),
			"fetchBodies", Boolean.TRUE.toString(),
			"fetchImages", Boolean.TRUE.toString(),
			"maxResults", Integer.toString(500)
		));
		if (searchText != null && !searchText.isBlank()) {
			params.put("q", searchText);
		}
		if (startDate != null && endDate != null) {
			params.put("startDate", startDate);
			params.put("endDate", endDate);
		}
		URI uri = URI.create("%s?%s".formatted(posts(config), HttpClientUtil.queryParams(params)));
		return postsCache.get(uri);
	}

	@Override
	public BlogItem getCachedBlogPost(BlogConfiguration config, String blogPostId) throws ExecutionException {
		URI uri = URI.create("%s/%s?%s".formatted(posts(config), blogPostId, HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey")
		))));
		return postCache.get(uri);
	}

	private static String posts(BlogConfiguration config) {
		return config.getBlogApiUrl() + config.getBlogId() + "/posts";
	}

	@Override
	public List<BlogItem> getBlogPosts(BlogConfiguration config) throws IOException, InterruptedException {
		OffsetDateTime lastPublishedTimestamp = Objects.requireNonNull(config.getLastPublishedTimestamp(), "lastPublishedTimestamp");
		HttpRequest request = HttpRequest.newBuilder(URI.create("%s?%s".formatted(posts(config), HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey"),
			"startDate", lastPublishedTimestamp.toInstant().plusSeconds(1).toString(),
			"fetchBodies", Boolean.TRUE.toString(),
			"fetchImages", Boolean.TRUE.toString()
		))))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		Root root = objectMapper.readValue(response.body(), Root.class);
		return root.toBlogItems();
	}

	@Override
	public BlogItem getLatestBlogPost(BlogConfiguration config) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create("%s?%s".formatted(posts(config), HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey"),
			"fetchBodies", Boolean.TRUE.toString(),
			"fetchImages", Boolean.TRUE.toString(),
			"maxResults", Integer.toString(1)
		))))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		Root root = objectMapper.readValue(response.body(), Root.class);
		return root.toBlogItems().stream().collect(MoreCollectors.onlyElement());
	}

	@Override
	public BlogItem getBlogPost(BlogConfiguration config, String blogPostId) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create("%s/%s?%s".formatted(posts(config), blogPostId, HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey")
		))))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), Item.class).toBlogItem();
	}

	record Root(
		String kind,
		String nextPageToken,
		List<Item> items,
		String etag
	) {

		public List<BlogItem> toBlogItems() {
			if (items == null) {
				return List.of();
			}
			return items.stream().map(Item::toBlogItem).toList();
		}
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
	)  {

		public BlogItem toBlogItem() {
			return new BlogItem(
				id,
				title,
				content,
				OffsetDateTime.parse(published),
				null,
				images != null && !images.isEmpty() ? images.getFirst().url : null
			);
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
