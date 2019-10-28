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
		for (LanguageCode lang : GlobalVariables.languages) {
			try {
				DateTime date = AlbinaUtil.getDate(bulletins);
				String message = GlobalVariables.getMessengerPeopleText(lang, date, update);
				String validityDate = AlbinaUtil.getValidityDateString(bulletins);
				sendBulletinNewsletter(message, bulletins, validityDate, lang, regions);
			} catch (IOException | AlbinaException e) {
				logger.error("Bulletin newsletter could not be sent: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void sendBulletinNewsletter(String message, List<AvalancheBulletin> bulletins, String validityDate,
			LanguageCode lang, List<String> regions) throws AlbinaException, IOException {
		MessengerPeopleProcessorController ctMp = MessengerPeopleProcessorController.getInstance();
		for (String region : regions) {
			// ArrayList<AvalancheBulletin> regionBulletins = new
			// ArrayList<AvalancheBulletin>();
			// for (AvalancheBulletin avalancheBulletin : bulletins) {
			// if (avalancheBulletin.affectsRegionOnlyPublished(region))
			// regionBulletins.add(avalancheBulletin);
			// }
			// String attachmentUrl;
			// if (AlbinaUtil.hasDaytimeDependency(bulletins) &&
			// !AlbinaUtil.hasDaytimeDependency(regionBulletins))
			// attachmentUrl = GlobalVariables.getMapsPath() + validityDate + "/"
			// + AlbinaUtil.getRegionOverviewMapFilename("", false);
			// else
			// attachmentUrl = GlobalVariables.getMapsPath() + validityDate + "/"
			// + AlbinaUtil.getRegionOverviewMapFilename("");
			String attachmentUrl = GlobalVariables.getMapsPath() + validityDate + "/"
					+ AlbinaUtil.getRegionOverviewMapFilename("");
			RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
			ctMp.sendNewsLetter(rc.getMessengerPeopleConfig(), lang.toString(), message, attachmentUrl);
		}
	}
}
