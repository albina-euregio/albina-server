// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.EmailUtil;
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
				return EmailUtil.createBulletinEmailHtml(avalancheReport, lang);
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
		return avalancheReport.getRegion().getWebsiteUrlWithDate(lang, avalancheReport);
	}

	@Override
	public String getAttachmentUrl() {
		return avalancheReport.getRegion().getMapsUrl(lang, avalancheReport, avalancheReport) + "/" + MapUtil.getOverviewMapFilename(avalancheReport.getRegion(), DaytimeDependency.fd, false);
	}

	@Override
	public String getSubject() {
		Region region = avalancheReport.getRegion();
		String bundleString = avalancheReport.getStatus() == BulletinStatus.republished
			? lang.getBundleString("email.subject.update")
			: lang.getBundleString("email.subject");

		return MessageFormat.format(bundleString, lang.getBundleString("headline"), lang.getRegionName(region.getId()))
			+ avalancheReport.getDate(lang);
	}

	@Override
	public String getSocialMediaText() {
		String dateString = avalancheReport.getDate(lang);
		String bulletinUrl = avalancheReport.getRegion().getWebsiteUrlWithDate(lang, avalancheReport);
		return avalancheReport.getStatus() == BulletinStatus.republished
			? MessageFormat.format(lang.getBundleString("social-media.message.update"), lang.getBundleString("headline"), dateString, bulletinUrl)
			: MessageFormat.format(lang.getBundleString("social-media.message"), lang.getBundleString("headline"), dateString, bulletinUrl);
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
