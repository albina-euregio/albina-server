/*******************************************************************************
 * Copyright (C) 2021 albina
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
package eu.albina.util;

import java.text.MessageFormat;
import java.util.List;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.LanguageCode;

interface SocialMediaUtil {

	default void sendBulletinNewsletters(AvalancheReport avalancheReport) {
		for (LanguageCode lang : LanguageCode.ENABLED) {
			sendBulletinNewsletters(avalancheReport, lang);
		}
	}

	default void sendBulletinNewsletters(AvalancheReport avalancheReport, LanguageCode lang) {
		String message = getSocialMediaText(avalancheReport, lang);
		String attachmentUrl = LinkUtil.getSocialMediaAttachmentUrl(avalancheReport.getRegion(), lang, avalancheReport.getBulletins(), avalancheReport.getServerInstance());
		String bulletinUrl = LinkUtil.getBulletinUrl(avalancheReport.getBulletins(), lang, avalancheReport.getRegion());
		sendBulletinNewsletter(message, lang, avalancheReport.getRegion(), attachmentUrl, bulletinUrl, avalancheReport.getStatus() == BulletinStatus.test);
	}

	static String getSocialMediaText(AvalancheReport avalancheReport, LanguageCode lang) {
		List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();
		String dateString = AlbinaUtil.getDate(bulletins, lang);
		String bulletinUrl = LinkUtil.getBulletinUrl(bulletins, lang, avalancheReport.getRegion());
		if (avalancheReport.getStatus() == BulletinStatus.republished) {
			return MessageFormat.format(lang.getBundleString("social-media.message.update"),
				lang.getBundleString("website.name"), dateString, bulletinUrl);
		} else {
			return MessageFormat.format(lang.getBundleString("social-media.message"),
				lang.getBundleString("website.name"), dateString, bulletinUrl);
		}
	}

	void sendBulletinNewsletter(String message, LanguageCode lang, Region region, String attachmentUrl, String bulletinUrl, boolean test);
}
