/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
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
import java.util.ResourceBundle;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.socialmedia.MessengerPeopleProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.socialmedia.RegionConfiguration;

public class MessengerPeopleUtil {

	private static MessengerPeopleUtil instance = null;

	private static final Logger logger = LoggerFactory.getLogger(MessengerPeopleUtil.class);

	public static MessengerPeopleUtil getInstance() throws IOException, URISyntaxException {
		if (instance == null) {
			instance = new MessengerPeopleUtil();
		}
		return instance;
	}

	public void sendBulletinNewsletters(List<AvalancheBulletin> bulletins, List<String> regions, boolean update) {
		DateTime date = AlbinaUtil.getDate(bulletins);
		String validityDate = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTime = AlbinaUtil.getPublicationTime(bulletins);

		for (LanguageCode lang : GlobalVariables.socialMediaLanguages) {
			ResourceBundle messages = ResourceBundle.getBundle("i18n.MessagesBundle", lang.getLocale(),
					new XMLResourceBundleControl());
			String message = GlobalVariables.getSocialMediaText(date, update, messages);
			sendBulletinNewsletter(message, bulletins, validityDate, publicationTime, lang, regions, messages);
		}
	}

	private void sendBulletinNewsletter(String message, List<AvalancheBulletin> bulletins, String validityDate,
			String publicationTime, LanguageCode lang, List<String> regions, ResourceBundle messages) {
		MessengerPeopleProcessorController ctMp = MessengerPeopleProcessorController.getInstance();
		for (String region : regions) {
			try {
				String attachmentUrl = GlobalVariables.getMapsUrl(messages) + "/" + validityDate + "/" + publicationTime
						+ "/" + AlbinaUtil.getRegionOverviewMapFilename("", "jpg");
				RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
				ctMp.sendNewsLetter(rc.getMessengerPeopleConfig(), lang, message, attachmentUrl);
			} catch (IOException | AlbinaException e) {
				logger.error("Error while sending bulletin newsletter in " + lang + " for region " + region, e);
			}
		}
	}
}
