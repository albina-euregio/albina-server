/*******************************************************************************
 * Copyright (C) 2021 albina-euregio
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

import org.joda.time.DateTime;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

interface SocialMediaUtil {

	default void sendBulletinNewsletters(List<AvalancheBulletin> bulletins, List<String> regions, boolean update) {
		for (LanguageCode lang : GlobalVariables.socialMediaLanguages) {
			DateTime date = AlbinaUtil.getDate(bulletins);
			String message = getSocialMediaText(date, update, lang);
			String attachmentUrl = getSocialMediaAttachmentUrl(bulletins);
			sendBulletinNewsletter(message, lang, regions, attachmentUrl);
		}
	}

	static String getSocialMediaText(DateTime date, boolean update, LanguageCode lang) {
		String dateString = lang.getBundleString("day." + date.getDayOfWeek())
			+ date.toString(lang.getBundleString("date-time-format"));
		if (update) {
			return MessageFormat.format(lang.getBundleString("social-media.message.update"),
				lang.getBundleString("avalanche-report.name"), dateString, GlobalVariables.getBulletinUrl(lang, date));
		} else {
			return MessageFormat.format(lang.getBundleString("social-media.message"),
				lang.getBundleString("avalanche-report.name"), dateString, GlobalVariables.getBulletinUrl(lang, date));
		}
	}

	static String getSocialMediaAttachmentUrl(List<AvalancheBulletin> bulletins) {
		String validityDate = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
		return GlobalVariables.getServerMainUrl() + GlobalVariables.avalancheReportFilesUrl
			+ validityDate + "/" + publicationTime + "/"
			+ AlbinaUtil.getRegionOverviewMapFilename("", "jpg").replace("map.jpg", "thumbnail.jpg");
	}

	void sendBulletinNewsletter(String message, LanguageCode lang, List<String> regions, String attachmentUrl);
}
