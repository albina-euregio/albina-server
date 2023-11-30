/*******************************************************************************
 * Copyright (C) 2023 albina
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

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;

class BlogItemMultichannelMessage implements MultichannelMessage {
	private final BlogConfiguration config;
	private final BlogItem blogPost;

	public BlogItemMultichannelMessage(BlogConfiguration config, BlogItem blogPost) {
		this.config = config;
		this.blogPost = blogPost;
	}

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
