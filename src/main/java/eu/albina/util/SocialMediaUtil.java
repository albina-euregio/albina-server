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
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;

interface SocialMediaUtil {

	default void sendBulletinNewsletters(List<AvalancheBulletin> bulletins, Region region, boolean update, boolean test, ServerInstance serverInstance) {
		for (LanguageCode lang : LanguageCode.ENABLED) {
			sendBulletinNewsletters(bulletins, region, update, lang, test, serverInstance);
		}
	}

	default void sendBulletinNewsletters(List<AvalancheBulletin> bulletins, Region region, boolean update, LanguageCode lang, boolean test, ServerInstance serverInstance) {
		String message = getSocialMediaText(bulletins, region, update, lang);
		String attachmentUrl = LinkUtil.getSocialMediaAttachmentUrl(region, lang, bulletins, serverInstance);
		String bulletinUrl = LinkUtil.getBulletinUrl(bulletins, lang, region);
		sendBulletinNewsletter(message, lang, region, attachmentUrl, bulletinUrl, test);
	}

	static String getSocialMediaText(List<AvalancheBulletin> bulletins, Region region, boolean update, LanguageCode lang) {
		String dateString = AlbinaUtil.getDate(bulletins, lang);
		String bulletinUrl = LinkUtil.getBulletinUrl(bulletins, lang, region);
		if (update) {
			return MessageFormat.format(lang.getBundleString("social-media.message.update"),
				lang.getBundleString("website.name"), dateString, bulletinUrl);
		} else {
			return MessageFormat.format(lang.getBundleString("social-media.message"),
				lang.getBundleString("website.name"), dateString, bulletinUrl);
		}
	}

	void sendBulletinNewsletter(String message, LanguageCode lang, Region region, String attachmentUrl, String bulletinUrl, boolean test);
}
