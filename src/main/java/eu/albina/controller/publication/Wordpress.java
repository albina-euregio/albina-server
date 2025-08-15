// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import com.google.common.collect.MoreCollectors;
import eu.albina.model.publication.BlogConfiguration;
import org.apache.commons.text.StringEscapeUtils;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface Wordpress {

	static List<Item> getBlogPosts(BlogConfiguration config, Client client) {
		// https://developer.wordpress.org/rest-api/reference/posts/#arguments
		OffsetDateTime lastPublishedTimestamp = Objects.requireNonNull(config.getLastPublishedTimestamp(), "lastPublishedTimestamp");
		WebTarget request = client.target(config.getBlogApiUrl() + "posts")
			.queryParam("lang", config.getLanguageCode())
			.queryParam("after", lastPublishedTimestamp.toInstant().plusSeconds(1).toString())
			.queryParam("order", "asc");
		Item[] items = request.request().get(Item[].class);
		return List.of(items);
	}

	static Item getLatestBlogPost(BlogConfiguration config, Client client) {
		WebTarget request = client.target(config.getBlogApiUrl() + "posts")
			.queryParam("lang", config.getLanguageCode())
			.queryParam("per_page", Integer.toString(1));
		Item[] items = request.request().get(Item[].class);
		return Arrays.stream(items).collect(MoreCollectors.onlyElement());
	}

	static Item getBlogPost(BlogConfiguration config, String blogPostId, Client client) {
		return client.target(config.getBlogApiUrl() + "posts/" + blogPostId)
			.request()
			.get(Item.class);
	}

	class Item implements BlogItem {
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

	class Rendered {
		public String rendered;
	}

	class PolylangTranslation {
		public String locale;
		public long id;
	}
}
