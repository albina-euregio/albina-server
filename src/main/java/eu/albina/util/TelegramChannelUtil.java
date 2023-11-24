/*******************************************************************************
 * Copyright (C) 2020 Norbert Lanzanasto
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

import eu.albina.model.publication.TelegramConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.publication.TelegramController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;

public class TelegramChannelUtil implements SocialMediaUtil {

	private static TelegramChannelUtil instance = null;

	private static final Logger logger = LoggerFactory.getLogger(TelegramChannelUtil.class);

	public static TelegramChannelUtil getInstance() {
		if (instance == null) {
			instance = new TelegramChannelUtil();
		}
		return instance;
	}

	@Override
	public void sendBulletinNewsletter(String message, LanguageCode lang, Region region, String attachmentUrl, String bulletinUrl) {
		if (region.isSendTelegramMessages()) {
			try {
				logger.info("Publishing report on telegram channel for {} in {}", region.getId(), lang);
				TelegramConfiguration config = TelegramController.getConfiguration(region, lang).orElseThrow();
				TelegramController.trySendPhoto(config, message, attachmentUrl, 3);
			} catch (Exception e) {
				logger.error("Error while sending bulletin newsletter to telegram channel in " + lang + " for region "
						+ region.getId(), e);
			}
		}
	}
}
