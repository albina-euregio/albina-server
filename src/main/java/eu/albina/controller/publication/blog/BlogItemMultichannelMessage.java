// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;

record BlogItemMultichannelMessage(BlogConfiguration config, BlogItem blogPost) implements MultichannelMessage {

	@Override
	public Region getRegion() {
		return config.getRegion();
	}

	@Override
	public LanguageCode getLanguageCode() {
		return config.getLanguageCode();
	}

	@Override
	public String getWebsiteUrl() {
		return blogPost.getAvalancheReportUrl(config);
	}

	@Override
	public String getAttachmentUrl() {
		return blogPost.getAttachmentUrl();
	}

	@Override
	public String getSubject() {
		return blogPost.getTitle();
	}

	@Override
	public String getSocialMediaText() {
		return blogPost.getTitleAndUrl(config);
	}

	@Override
	public String getHtmlMessage() {
		return blogPost.getContent();
	}

	@Override
	public String toString() {
		return toDefaultString();
	}
}
