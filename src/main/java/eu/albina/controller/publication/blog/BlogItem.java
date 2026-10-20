// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record BlogItem(
	String id,
	String title,
	String content,
	OffsetDateTime published,
	Collection<String> categories,
	String attachmentUrl,
	Map<LanguageCode, String> translations
	) {

	public String getTitleAndUrl(BlogConfiguration config, Region region) {
		return title() + ": " + this.getAvalancheReportUrl(config, region);
	}

	public String getAvalancheReportUrl(BlogConfiguration config, Region region) {
		return region.getWebsiteUrl(config.getLanguageCode()) + "/blog/" + config.getBlogUrl() + "/" + id();
	}
}
