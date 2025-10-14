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
	static class Item implements BlogItem {
		public long id;
		public String date;
		public String date_gmt;
		public String link;
		public Rendered title;
		public Rendered content;
		public Rendered excerpt;
		public long featured_media;
		public long[] categories;
		public String polylang_current_lang;
		public PolylangTranslation[] polylang_translations;
		public String featured_image_url;

		public void setId(long id) {
			this.id = id;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public void setDate_gmt(String date_gmt) {
			this.date_gmt = date_gmt;
		}

		public void setLink(String link) {
			this.link = link;
		}

		public void setTitle(Rendered title) {
			this.title = title;
		}

		public void setContent(Rendered content) {
			this.content = content;
		}

		public void setExcerpt(Rendered excerpt) {
			this.excerpt = excerpt;
		}

		public void setFeatured_media(long featured_media) {
			this.featured_media = featured_media;
		}

		public void setCategories(long[] categories) {
			this.categories = categories;
		}

		public void setPolylang_current_lang(String polylang_current_lang) {
			this.polylang_current_lang = polylang_current_lang;
		}

		public void setPolylang_translations(PolylangTranslation[] polylang_translations) {
			this.polylang_translations = polylang_translations;
		}

		public void setFeatured_image_url(String featured_image_url) {
			this.featured_image_url = featured_image_url;
		}

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
	static class Rendered {
		public String rendered;

		public void setRendered(String rendered) {
			this.rendered = rendered;
		}
	}

	@Serdeable
	static class PolylangTranslation {
		public String locale;
		public long id;

		public void setLocale(String locale) {
			this.locale = locale;
		}

		public void setId(long id) {
			this.id = id;
		}
	}
}
