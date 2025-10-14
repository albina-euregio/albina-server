// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Singleton
public class Blogger {

	@Inject
	ObjectMapper objectMapper;

	public List<Item> getBlogPosts(BlogConfiguration config, HttpClient client) throws IOException, InterruptedException {
		OffsetDateTime lastPublishedTimestamp = Objects.requireNonNull(config.getLastPublishedTimestamp(), "lastPublishedTimestamp");
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + config.getBlogId() + "/posts?" + HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey"),
			"startDate", lastPublishedTimestamp.toInstant().plusSeconds(1).toString(),
			"fetchBodies", Boolean.TRUE.toString(),
			"fetchImages", Boolean.TRUE.toString()
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		Root root = objectMapper.readValue(response.body(), Root.class);
		return root.items;
	}

	public Item getLatestBlogPost(BlogConfiguration config, HttpClient client) throws IOException, InterruptedException {
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

	public Item getBlogPost(BlogConfiguration config, String blogPostId, HttpClient client) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create(config.getBlogApiUrl() + config.getBlogId() + "/posts/" + blogPostId + "?" + HttpClientUtil.queryParams(Map.of(
			"key", Objects.requireNonNull(config.getApiKey(), "apiKey")
		)))).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), Item.class);
	}

	@Serdeable
	static class Root {
		public String kind;
		public String nextPageToken;
		public List<Item> items = new ArrayList<>();
		public String etag;

		public void setKind(String kind) {
			this.kind = kind;
		}

		public void setNextPageToken(String nextPageToken) {
			this.nextPageToken = nextPageToken;
		}

		public void setItems(List<Item> items) {
			this.items = items;
		}

		public void setEtag(String etag) {
			this.etag = etag;
		}
	}

	@Serdeable
	static class Item implements BlogItem {
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

		public void setKind(String kind) {
			this.kind = kind;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public void setBlog(Blog blog) {
			this.blog = blog;
		}

		public void setPublished(String published) {
			this.published = published;
		}

		public void setUpdated(String updated) {
			this.updated = updated;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setSelfLink(String selfLink) {
			this.selfLink = selfLink;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setImages(List<Image> images) {
			this.images = images;
		}

		public void setAuthor(Author author) {
			this.author = author;
		}

		public void setReplies(Replies replies) {
			this.replies = replies;
		}

		public void setLabels(List<String> labels) {
			this.labels = labels;
		}

		public void setEtag(String etag) {
			this.etag = etag;
		}

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

	@Serdeable
	static class Author {
		public String id;
		public String displayName;
		public String url;
		public Image image;

		public void setId(String id) {
			this.id = id;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setImage(Image image) {
			this.image = image;
		}
	}

	@Serdeable
	static class Image {
		public String url;

		public void setUrl(String url) {
			this.url = url;
		}
	}

	@Serdeable
	static class Blog {
		public String id;

		public void setId(String id) {
			this.id = id;
		}
	}

	@Serdeable
	static class Replies {
		public String totalItems;
		public String selfLink;

		public void setTotalItems(String totalItems) {
			this.totalItems = totalItems;
		}

		public void setSelfLink(String selfLink) {
			this.selfLink = selfLink;
		}
	}

}
