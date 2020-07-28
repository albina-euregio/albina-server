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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.controller.socialmedia.TelegramChannelProcessorController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.model.socialmedia.TelegramConfig;

public class TelegramChannelUtil {

	private static TelegramChannelUtil instance = null;

	private static final Logger logger = LoggerFactory.getLogger(TelegramChannelUtil.class);

	public static TelegramChannelUtil getInstance() throws IOException, URISyntaxException {
		if (instance == null) {
			instance = new TelegramChannelUtil();
		}
		return instance;
	}

	public void sendBulletinNewsletters(List<AvalancheBulletin> bulletins, List<String> regions, boolean update) {
		for (LanguageCode lang : GlobalVariables.languages) {
			Locale currentLocale = new Locale(lang.toString());
			ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", currentLocale);

			DateTime date = AlbinaUtil.getDate(bulletins);
			String message = GlobalVariables.getSocialMediaText(date, update, messages);
			String validityDate = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
			sendBulletinNewsletter(message, bulletins, validityDate, publicationTime, lang, regions, messages);
		}
	}

	private void sendBulletinNewsletter(String message, List<AvalancheBulletin> bulletins, String validityDate,
			String publicationTime, LanguageCode lang, List<String> regions, ResourceBundle messages) {
		TelegramChannelProcessorController ctTc = TelegramChannelProcessorController.getInstance();
		for (String region : regions) {
			try {
				String attachmentUrl = GlobalVariables.getMapsUrl(messages) + "/" + validityDate + "/" + publicationTime
						+ "/" + AlbinaUtil.getRegionOverviewMapFilename("");
				RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
				Set<TelegramConfig> telegramConfigs = rc.getTelegramConfigs();
				TelegramConfig config = null;
				for (TelegramConfig telegramConfig : telegramConfigs) {
					if (telegramConfig.getLanguageCode().equals(lang)) {
						config = telegramConfig;
						break;
					}
				}

				if (config != null) {
					ctTc.sendNewsletter(config, message, attachmentUrl);
				} else {
					throw new AlbinaException(
							"No configuration for telegram channel found (" + region + ", " + lang + ")");
				}
			} catch (IOException | AlbinaException e) {
				logger.error("Error while sending bulletin newsletter in " + lang + " for region " + region, e);
			}
		}
	}
}
