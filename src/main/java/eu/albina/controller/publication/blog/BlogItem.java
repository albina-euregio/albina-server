// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import eu.albina.model.publication.BlogConfiguration;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;

@Introspected
public interface BlogItem {
	@JsonInclude
	String getId();

	@JsonInclude
	String getTitle();

	@JsonInclude
	String getContent();

	@JsonInclude
	OffsetDateTime getPublished();

	@JsonInclude
	String getAttachmentUrl();

	default String getTitleAndUrl(BlogConfiguration config) {
		return getTitle() + ": " + this.getAvalancheReportUrl(config);
	}

	default String getAvalancheReportUrl(BlogConfiguration config) {
		return  config.getRegion().getWebsiteUrl(config.getLanguageCode()) + "/blog/" + config.getBlogUrl() + "/" + getId();
	}
}
