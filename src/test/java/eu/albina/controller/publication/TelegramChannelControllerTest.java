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
package eu.albina.controller.publication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.Resources;

import eu.albina.controller.RegionController;
import eu.albina.controller.socialmedia.TelegramChannelProcessorController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.HibernateUtil;
import eu.albina.util.TelegramChannelUtil;

// TODO test the tests
@Ignore
public class TelegramChannelControllerTest {

	private List<AvalancheBulletin> bulletins;

	@Before
	public void setUp() throws Exception {
		HibernateUtil.getInstance().setUp();

		// Load valid avalanche bulletin JSON from resources
		bulletins = new ArrayList<AvalancheBulletin>();
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_1.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_2.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_3.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_4.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_5.json")));
	}

	@After
	public void shutDown() {
		HibernateUtil.getInstance().shutDown();
	}

	@Test
	public void sendMessageTest() throws Exception {
		TelegramController telegramController = TelegramController.getInstance();

		String attachmentUrl = "https://avalanche.report/albina_files_dev/2020-01-26/fd_albina_map.jpg";
		String message;

		for (Region region : RegionController.getInstance().getPublishBulletinRegions()) {
			for (LanguageCode lang : LanguageCode.SOCIAL_MEDIA) {
				message = region + " - " + lang;
				telegramController.sendPhoto(region, lang, message, attachmentUrl, true);
			}
		}
	}

	@Test
	public void sendBulletin() throws URISyntaxException, IOException {
		Region regionTrentino = new Region("IT-32-TN");
		TelegramChannelUtil.getInstance().sendBulletinNewsletters(bulletins, regionTrentino, true, true);
	}

	@Ignore
	@Test
	public void testTrySendPhoto() throws Exception {
		TelegramController.getInstance().trySendPhoto(null, null, null, null, false, 3);
		Thread.sleep(100_000);
	}
}
