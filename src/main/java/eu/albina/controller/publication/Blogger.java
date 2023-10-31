/*******************************************************************************
 * Copyright (C) 2020 albina
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.controller.publication;

import com.google.common.collect.MoreCollectors;
import eu.albina.model.publication.BlogConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Blogger {

	static List<Item> getBlogPosts(BlogConfiguration config, Client client) {
		OffsetDateTime lastPublishedTimestamp = Objects.requireNonNull(config.getLastPublishedTimestamp(), "lastPublishedTimestamp");
		WebTarget request = client.target(config.getBlogApiUrl() + config.getBlogId() + "/posts")
			.queryParam("key", Objects.requireNonNull(config.getApiKey(), "apiKey"))
			.queryParam("startDate", lastPublishedTimestamp.toInstant().plusSeconds(1).toString())
			.queryParam("fetchBodies", Boolean.TRUE.toString())
			.queryParam("fetchImages", Boolean.TRUE.toString());
		Root root = request.request().get(Root.class);
		return root.items;
	}

	static Item getLatestBlogPost(BlogConfiguration config, Client client) {
		WebTarget request = client.target(config.getBlogApiUrl() + config.getBlogId() + "/posts")
			.queryParam("key", Objects.requireNonNull(config.getApiKey(), "apiKey"))
			.queryParam("fetchBodies", Boolean.TRUE.toString())
			.queryParam("fetchImages", Boolean.TRUE.toString())
			.queryParam("maxResults", Integer.toString(1));
		Root root = request.request().get(Root.class);
		return root.items.stream().collect(MoreCollectors.onlyElement());
	}

	static Item getBlogPost(BlogConfiguration config, String blogPostId, Client client) {
		return client.target(config.getBlogApiUrl() + config.getBlogId() + "/posts/" + blogPostId)
			.queryParam("key", Objects.requireNonNull(config.getApiKey(), "apiKey"))
			.request()
			.get(Item.class);
	}

	public static class Root {
		public String kind;
		public String nextPageToken;
		public List<Item> items = new ArrayList<>();
		public String etag;
	}

	public static class Item implements BlogItem {
		public String kind;
		public String id;
		public String content;
		public Blog blog;
		public OffsetDateTime published;
		public OffsetDateTime updated;
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
			return published;
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

	public static class Author {
		public String id;
		public String displayName;
		public String url;
		public Image image;
	}

	public static class Image {
		public String url;
	}

	public static class Blog {
		public String id;
	}

	public static class Replies {
		public String totalItems;
		public String selfLink;
	}

}
