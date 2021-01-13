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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.socialmedia.MessengerPeopleProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.socialmedia.RegionConfiguration;

public class MessengerPeopleUtil implements SocialMediaUtil {

	private static MessengerPeopleUtil instance = null;

	private static final Logger logger = LoggerFactory.getLogger(MessengerPeopleUtil.class);

	public static MessengerPeopleUtil getInstance() throws IOException, URISyntaxException {
		if (instance == null) {
			instance = new MessengerPeopleUtil();
		}
		return instance;
	}

	@Override
	public void sendBulletinNewsletter(String message, LanguageCode lang, List<String> regions, String attachmentUrl) {
		MessengerPeopleProcessorController ctMp = MessengerPeopleProcessorController.getInstance();
		for (String region : regions) {
			try {
				logger.info("Attachment URL: " + attachmentUrl);
				RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
				ctMp.sendNewsLetter(rc.getMessengerPeopleConfig(), lang, message, attachmentUrl);
			} catch (IOException | AlbinaException e) {
				logger.error("Error while sending bulletin newsletter in " + lang + " for region " + region, e);
			}
		}
	}
}
