// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import com.google.common.collect.MoreCollectors;
import eu.albina.model.publication.BlogConfiguration;

import eu.albina.util.HttpClientUtil;
import eu.albina.util.JsonUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface Blogger {

	static List<Item> getBlogPosts(BlogConfiguration config, HttpClient client) throws IOException, InterruptedException {
		OffsetDateTime lastPublishedTimestamp = Objects.requireNonNull(config.getLastPublishedTimestamp(), "lastPublishedTimestamp");
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + config.getBlogId() + "/posts?" + HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey"),
			"startDate", lastPublishedTimestamp.toInstant().plusSeconds(1).toString(),
			"fetchBodies", Boolean.TRUE.toString(),
			"fetchImages", Boolean.TRUE.toString()
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		Root root = JsonUtil.parseUsingJackson(response.body(), Root.class);
		return root.items;
	}

	static Item getLatestBlogPost(BlogConfiguration config, HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + config.getBlogId() + "/posts?" + HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey"),
			"fetchBodies", Boolean.TRUE.toString(),
			"fetchImages", Boolean.TRUE.toString(),
			"maxResults", Integer.toString(1)
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		Root root = JsonUtil.parseUsingJackson(response.body(), Root.class);
		return root.items.stream().collect(MoreCollectors.onlyElement());
	}

	static Item getBlogPost(BlogConfiguration config, String blogPostId, HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + config.getBlogId() + "/posts/" + blogPostId + "?" + HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey")
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return JsonUtil.parseUsingJackson(response.body(), Item.class);
	}

	class Root {
		public String kind;
		public String nextPageToken;
		public List<Item> items = new ArrayList<>();
		public String etag;
	}

	class Item implements BlogItem {
		public String kind;
		public String id;
		public String content;
		public Blog blog;
		public String published;
		public String updated;
		public String url;
		public String selfLink;
		public String title;
		public List<Image> images;
		public Author author;
		public Replies replies;
		public List<String> labels;
		public String etag;

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
				return images.get(0).url;
			} else {
				return null;
			}
		}
	}

	class Author {
		public String id;
		public String displayName;
		public String url;
		public Image image;
	}

	class Image {
		public String url;
	}

	class Blog {
		public String id;
	}

	class Replies {
		public String totalItems;
		public String selfLink;
	}

}
