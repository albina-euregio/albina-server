// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import com.google.common.cache.LoadingCache;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

@Singleton
class Wordpress implements AbstractBlog {

	@Inject
	HttpClient client;

	@Inject
	ObjectMapper objectMapper;

	final LoadingCache<URI, List<Item>> postsCache = HttpClientUtil.newHttpCache(() -> client, body->
		List.of(objectMapper.readValue(body, Item[].class)));

	final LoadingCache<URI, Item> postCache = HttpClientUtil.newHttpCache(() -> client, body ->
		objectMapper.readValue(body, Item.class));

	final LoadingCache<URI, List<Category>> categoriesCache = HttpClientUtil.newHttpCache(() -> client, body ->
		List.of(objectMapper.readValue(body, Category[].class)));

	@Override
	public List<BlogItem> getCachedBlogPosts(BlogConfiguration config, String searchText, String searchCategory, Instant startDate, Instant endDate) throws ExecutionException {
		// https://developer.wordpress.org/rest-api/reference/posts/#arguments
		// https://developer.wordpress.org/rest-api/using-the-rest-api/global-parameters/#_embed
		Map<String, Object> params = new TreeMap<>(Map.of(
			"lang", config.getLanguageCode(),
			"_embed", "wp:term",
			"_fields", String.join(",",
				"_links",
				"_embedded",
				"categories",
				"date",
				"featured_image_url",
				"featured_media",
				"id",
				"link",
				"polylang_current_lang",
				"polylang_translations",
				"tags",
				"title"
			),
			"per_page", Integer.toString(99)
		));
		if (searchText != null && !searchText.isBlank()) {
			params.put("search", searchText);
		}
		if (searchCategory != null && !searchCategory.isBlank()) {
			params.put("categories", searchCategory);
		}
		if (startDate != null && endDate != null) {
			params.put("after", startDate);
			params.put("before", endDate);
		}
		URI uri = URI.create(config.getBlogApiUrl() + "posts?" + HttpClientUtil.queryParams(params));
		List<Category> categories = getCachedCategories(config);
		return postsCache.get(uri).stream().map(item -> item.toBlogItem(categories)).toList();
	}

	@Override
	public BlogItem getCachedBlogPost(BlogConfiguration config, String blogPostId) throws ExecutionException {
		URI uri = URI.create(config.getBlogApiUrl() + "posts/" + blogPostId);
		List<Category> categories = getCachedCategories(config);
		return postCache.get(uri).toBlogItem(categories);
	}

	List<Category> getCachedCategories(BlogConfiguration config) throws ExecutionException {
		// https://developer.wordpress.org/rest-api/reference/categories/#arguments
		Map<String, Object> params = Map.of(
			"lang", config.getLanguageCode(),
			"_fields", String.join(",",
				"id",
				"count",
				"description",
				"name",
				"slug",
				"taxonomy",
				"polylang_current_lang",
				"polylang_translations"
			),
			"per_page", Integer.toString(99)
		);
		URI uri = URI.create(config.getBlogApiUrl() + "categories?" + HttpClientUtil.queryParams(params));
		return categoriesCache.get(uri);
	}

	@Override
	public List<BlogItem> getBlogPosts(BlogConfiguration config) throws IOException, InterruptedException {
		// https://developer.wordpress.org/rest-api/reference/posts/#arguments
		OffsetDateTime lastPublishedTimestamp = Objects.requireNonNull(config.getLastPublishedTimestamp(), "lastPublishedTimestamp");
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + "posts?" + HttpClientUtil.queryParams(Map.of(
			"lang", config.getLanguageCode(),
			"after", lastPublishedTimestamp.toInstant().plusSeconds(1).toString(),
			"order", "asc"
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		HttpClientUtil.checkResponse(response);
		Item[] items = objectMapper.readValue(response.body(), Item[].class);
		return Arrays.stream(items).map(item -> item.toBlogItem(null)).toList();
	}

	@Override
	public BlogItem getLatestBlogPost(BlogConfiguration config) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + "posts?" + HttpClientUtil.queryParams(Map.of(
			"lang", config.getLanguageCode(),
			"per_page", Integer.toString(1)
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		HttpClientUtil.checkResponse(response);
		Item[] items = objectMapper.readValue(response.body(), Item[].class);
		return Arrays.stream(items).collect(MoreCollectors.onlyElement()).toBlogItem(null);
	}

	@Override
	public BlogItem getBlogPost(BlogConfiguration config, String blogPostId) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + "posts/" + blogPostId)).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		HttpClientUtil.checkResponse(response);
		return objectMapper.readValue(response.body(), Item.class).toBlogItem(null);
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
		List<Long> categories,
		String polylang_current_lang,
		PolylangTranslation[] polylang_translations,
		String featured_image_url
	) {
		public BlogItem toBlogItem(List<Category> categories) {
			categories = Objects.requireNonNullElse(categories, List.of());
			return new BlogItem(
				String.valueOf(id),
				StringEscapeUtils.unescapeHtml4(title.rendered),
				content != null ? content.rendered : null,
				LocalDateTime.parse(Objects.requireNonNullElse(date_gmt, date)).atOffset(ZoneOffset.UTC),
				categories.stream().filter(c -> this.categories.contains(c.id)).map(Category::name).toList(),
				featured_image_url
			);
		}
	}

	@Serdeable
	record Category(
		long id,
		long count,
		String description,
		String name,
		String slug,
		String taxonomy,
		String polylangCurrentLang,
		PolylangTranslation[] polylang_translations
	) {
	}

	@Serdeable
	record Rendered(String rendered) {
	}

	@Serdeable
	record PolylangTranslation(String locale, long id) {
	}
}
