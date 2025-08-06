package eu.albina.controller.publication;

import eu.albina.model.publication.BlogConfiguration;
import eu.albina.util.LinkUtil;

import java.time.OffsetDateTime;

public interface BlogItem {
	String getId();

	String getTitle();

	String getContent();

	OffsetDateTime getPublished();

	String getAttachmentUrl();

	default String getTitleAndUrl(BlogConfiguration config) {
		return getTitle() + ": " + this.getAvalancheReportUrl(config);
	}

	default String getAvalancheReportUrl(BlogConfiguration config) {
		return  config.getRegion().getWebsiteUrl(config.getLanguageCode()) + "/blog/" + config.getBlogUrl() + "/" + getId();
	}
}
