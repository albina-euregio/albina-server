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
package eu.albina.socialmedia;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.socialmedia.MessengerPeopleProcessorController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.messengerpeople.MessengerPeopleNewsletterHistory;
import eu.albina.model.messengerpeople.MessengerPeopleTargets;
import eu.albina.model.messengerpeople.MessengerPeopleUser;
import eu.albina.model.socialmedia.MessengerPeopleConfig;
import eu.albina.model.socialmedia.RegionConfiguration;

public class MessengerPeopleProcessorTest {

	private static Logger logger = LoggerFactory.getLogger(MessengerPeopleProcessorTest.class);
	private MessengerPeopleConfig messengerPeopleConfig;

	@Before
	public void setUp() throws Exception {
		messengerPeopleConfig = new MessengerPeopleConfig();
		// Set api key
		messengerPeopleConfig.setApiKey("");
		RegionConfiguration regionConfiguration = new RegionConfiguration();
		Region region = new Region();
		region.setId("Test");
		regionConfiguration.setRegion(region);
		messengerPeopleConfig.setRegionConfiguration(regionConfiguration);
	}

	@Ignore
	@Test
	public void getUsersTest() throws IOException {
		List<MessengerPeopleUser> users = MessengerPeopleProcessorController.getInstance()
				.getUsers(messengerPeopleConfig, 10000, 0);
		logger.info(users.toString());
	}

	@Ignore
	@Test
	public void getTargetsTest() throws IOException {
		MessengerPeopleTargets targets = MessengerPeopleProcessorController.getInstance()
				.getTargets(messengerPeopleConfig);
		logger.info(targets.toString());
	}

	@Ignore
	@Test
	public void getNewsletterHistoryTest() throws IOException {
		MessengerPeopleNewsletterHistory newsLetterHistory = MessengerPeopleProcessorController.getInstance()
				.getNewsLetterHistory(messengerPeopleConfig, 10);
		logger.info(newsLetterHistory.toString());
	}

	@Ignore
	@Test
	public void getUsersStatsTest() throws IOException {
		HttpResponse usersStats = MessengerPeopleProcessorController.getInstance().getUsersStats(messengerPeopleConfig);
		logger.info(usersStats.toString());
	}

	@Ignore
	@Test
	public void sendNewsletterTest() throws IOException, AlbinaException {
		HttpResponse response = MessengerPeopleProcessorController.getInstance().sendNewsLetter(messengerPeopleConfig,
				LanguageCode.de, "Test message", null);
		logger.info(response.toString());
	}
}
