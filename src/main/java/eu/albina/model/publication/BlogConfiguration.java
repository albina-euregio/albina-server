// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.google.common.base.MoreObjects;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "blog_configurations")
@Serdeable
public class BlogConfiguration implements Serializable {

	public static final String TECH_BLOG_ID = "tech";
	public static final String TECH_BLOG_REGION_OVERRIDE = "AT-07";

	public boolean isBlogger() {
		return "https://www.googleapis.com/blogger/v3/blogs/".equals(blogApiUrl);
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "REGION_ID")
	private Region region;

	@Column(name = "API_KEY", length = 191)
	private String apiKey;

	@Column(name = "BLOG_ID", length = 191)
	private String blogId;

	@Column(name = "BLOG_URL", length = 191)
	private String blogUrl;

	@Column(name = "BLOG_API_URL",  length = 191)
	private String blogApiUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	private LanguageCode languageCode;

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
		return languageCode;
	}

	public void setLanguageCode(LanguageCode languageCode) {
		this.languageCode = languageCode;
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
			.add("blogId", blogId)
			.add("blogUrl", blogUrl)
			.add("blogApiUrl", blogApiUrl)
			.add("languageCode", languageCode)
			.add("lastPublishedBlogId", lastPublishedBlogId)
			.add("lastPublishedTimestamp", lastPublishedTimestamp)
			.toString();
	}
}
