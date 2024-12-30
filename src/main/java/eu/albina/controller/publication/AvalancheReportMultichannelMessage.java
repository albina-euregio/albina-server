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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.EmailUtil;
import eu.albina.util.LinkUtil;
import freemarker.template.TemplateException;

class AvalancheReportMultichannelMessage implements MultichannelMessage {
	private final AvalancheReport avalancheReport;
	private final LanguageCode lang;
	private final Supplier<String> htmlMessage;

	public AvalancheReportMultichannelMessage(AvalancheReport avalancheReport, LanguageCode lang) {
		this.avalancheReport = avalancheReport;
		this.lang = lang;
		this.htmlMessage = Suppliers.memoize(() -> {
			try {
				return EmailUtil.getInstance().createBulletinEmailHtml(avalancheReport, lang);
			} catch (IOException | TemplateException e) {
				throw new RuntimeException("Failed to create bulletin email", e);
			}
		});
	}

	@Override
	public Region getRegion() {
		return avalancheReport.getRegion();
	}

	@Override
	public LanguageCode getLanguageCode() {
		return lang;
	}

	@Override
	public String getWebsiteUrl() {
		return LinkUtil.getBulletinUrl(avalancheReport, lang);
	}

	@Override
	public String getAttachmentUrl() {
		return LinkUtil.getSocialMediaAttachmentUrl(avalancheReport, lang);
	}

	@Override
	public String getSubject() {
		Region region = avalancheReport.getRegion();
		String bundleString = avalancheReport.getStatus() == BulletinStatus.republished
			? lang.getBundleString("email.subject.update")
			: lang.getBundleString("email.subject");

		return MessageFormat.format(bundleString, lang.getBundleString("website.name", region))
			+ lang.getRegionName(region.getId()) + avalancheReport.getDate(lang);
	}

	@Override
	public String getSocialMediaText() {
		String dateString = avalancheReport.getDate(lang);
		String bulletinUrl = LinkUtil.getBulletinUrl(avalancheReport, lang);
		return avalancheReport.getStatus() == BulletinStatus.republished
			? MessageFormat.format(lang.getBundleString("social-media.message.update"), lang.getBundleString("website.name"), dateString, bulletinUrl)
			: MessageFormat.format(lang.getBundleString("social-media.message"), lang.getBundleString("website.name"), dateString, bulletinUrl);
	}

	@Override
	public String getHtmlMessage() {
		return htmlMessage.get();
	}

	@Override
	public String toString() {
		return toDefaultString();
	}
}
