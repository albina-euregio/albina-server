// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import eu.albina.model.publication.BlogConfiguration;

import java.time.OffsetDateTime;
import java.util.Collection;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record BlogItem(
	String id,
	String title,
	String content,
	OffsetDateTime published,
	Collection<String> categories,
	String attachmentUrl
	) {

	public String getTitleAndUrl(BlogConfiguration config) {
		return title() + ": " + this.getAvalancheReportUrl(config);
	}

	public String getAvalancheReportUrl(BlogConfiguration config) {
		return config.getRegion().getWebsiteUrl(config.getLanguageCode()) + "/blog/" + config.getBlogUrl() + "/" + id();
	}
}
