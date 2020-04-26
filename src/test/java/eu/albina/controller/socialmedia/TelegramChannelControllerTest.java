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
package eu.albina.controller.socialmedia;

import java.io.IOException;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.albina.exception.AlbinaException;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.model.socialmedia.TelegramConfig;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;

public class TelegramChannelControllerTest {

	// private static Logger logger =
	// LoggerFactory.getLogger(TelegramChannelControllerTest.class);

	@Before
	public void setUp() throws Exception {
		HibernateUtil.getInstance().setUp();
	}

	@After
	public void shutDown() {
		HibernateUtil.getInstance().shutDown();
	}

	@Ignore
	@Test
	public void sendMessageTest() throws IOException, AlbinaException {
		TelegramChannelProcessorController tcc = TelegramChannelProcessorController.getInstance();
		RegionConfigurationController rcc = RegionConfigurationController.getInstance();

		String attachmentUrl = "https://avalanche.report/albina_files_dev/2020-01-26/fd_albina_map.jpg";
		String message;

		for (String region : GlobalVariables.regionsEuregio) {
			RegionConfiguration regionConfiguration = rcc.getRegionConfiguration(region);
			Set<TelegramConfig> telegramConfigs = regionConfiguration.getTelegramConfigs();
			for (TelegramConfig telegramConfig : telegramConfigs) {
				message = region + " - " + telegramConfig.getLanguageCode();
				tcc.sendNewsletter(telegramConfig, message, attachmentUrl);
			}
		}
	}
}
