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

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.controller.socialmedia.TelegramChannelProcessorController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.model.socialmedia.TelegramConfig;

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
	public void sendBulletinNewsletter(String message, LanguageCode lang, List<String> regions, String attachmentUrl, String bulletinUrl, boolean test) {
		TelegramChannelProcessorController ctTc = TelegramChannelProcessorController.getInstance();
		for (String region : regions) {
			try {
				RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
				Set<TelegramConfig> telegramConfigs = rc.getTelegramConfigs();
				TelegramConfig config = telegramConfigs.stream()
					.filter(telegramConfig -> telegramConfig.getLanguageCode().equals(lang))
					.findFirst()
					.orElseThrow(() -> new AlbinaException("No configuration for telegram channel found (" + region + ", " + lang + ")"));
				logger.info("Publishing report on telegram channel for {} in {}", config.getRegionConfiguration().getRegion().getId(), lang);
				ctTc.trySendPhoto(config, message, attachmentUrl, test, 3);
			} catch (Exception e) {
				logger.error("Error while sending bulletin newsletter to telegram channel in " + lang + " for region "
						+ region, e);
			}
		}
	}
}
