// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import java.util.Objects;

import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.util.EmailUtil;

record BlogItemMultichannelMessage(BlogConfiguration config, Region region, BlogItem blogPost) implements MultichannelMessage {

	@Override
	public Region getRegion() {
		return region;
	}

	@Override
	public LanguageCode getLanguageCode() {
		return config.getLanguageCode();
	}

	@Override
	public String getWebsiteUrl() {
		return blogPost.getAvalancheReportUrl(config, region);
	}

	@Override
	public String getAttachmentUrl() {
		return blogPost.attachmentUrl();
	}

	@Override
	public String getSubject() {
		return blogPost.title();
	}

	@Override
	public String getSocialMediaText() {
		return blogPost.getTitleAndUrl(config, region);
	}

	@Override
	public String getHtmlMessage() {
		return Objects.requireNonNullElse(blogPost.content(), "") + EmailUtil.createFooterHtml(region, config.getLanguageCode());
	}

	@Override
	public String toString() {
		return toDefaultString();
	}
}
