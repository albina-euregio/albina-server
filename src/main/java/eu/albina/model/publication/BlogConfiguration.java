/*******************************************************************************
 * Copyright (C) 2020 Avalanche.report
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
package eu.albina.model.publication;

import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "blog_configurations")
public class BlogConfiguration implements Serializable {

	public boolean isBlogger() {
		return "https://www.googleapis.com/blogger/v3/blogs/".equals(blogApiUrl);
	}

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	private Region region;

	@Column(name = "API_KEY")
	private String apiKey;

	@Column(name = "BLOG_ID")
	private String blogId;

	@Column(name = "BLOG_URL")
	private String blogUrl;

	@Column(name = "BLOG_API_URL")
	private String blogApiUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE")
	private LanguageCode lang;

	@Column(name = "LAST_PUBLISHED_BLOG_ID")
	private String lastPublishedBlogId;

	@Column(name = "LAST_PUBLISHED_TIMESTAMP")
	private OffsetDateTime lastPublishedTimestamp;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getBlogId() {
		return blogId;
	}

	public void setBlogId(String blogId) {
		this.blogId = blogId;
	}

	public String getBlogUrl() {
		return blogUrl;
	}

	public void setBlogUrl(String blogUrl) {
		this.blogUrl = blogUrl;
	}

	public String getBlogApiUrl() {
		return blogApiUrl;
	}

	public void setBlogApiUrl(String blogApiUrl) {
		this.blogApiUrl = blogApiUrl;
	}

	public LanguageCode getLanguageCode() {
		return lang;
	}

	public void setLanguageCode(LanguageCode languageCode) {
		this.lang = languageCode;
	}

	public String getLastPublishedBlogId() {
		return lastPublishedBlogId;
	}

	public void setLastPublishedBlogId(String lastPublishedBlogId) {
		this.lastPublishedBlogId = lastPublishedBlogId;
	}

	public OffsetDateTime getLastPublishedTimestamp() {
		return lastPublishedTimestamp;
	}

	public void setLastPublishedTimestamp(OffsetDateTime lastPublishedTimestamp) {
		this.lastPublishedTimestamp = lastPublishedTimestamp;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("region", region)
			.add("apiKey", apiKey)
			.add("blogId", blogId)
			.add("blogUrl", blogUrl)
			.add("blogApiUrl", blogApiUrl)
			.add("languageCode", lang)
			.add("lastPublishedBlogId", lastPublishedBlogId)
			.add("lastPublishedTimestamp", lastPublishedTimestamp)
			.toString();
	}
}
