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
package eu.albina.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class StatisticsControllerTest {

	private static Logger logger = LoggerFactory.getLogger(RegionControllerTest.class);

	private List<AvalancheBulletin> bulletins;
	private List<AvalancheBulletin> bulletinsAmPm;

	@Before
	public void setUp() throws Exception {
		// HibernateUtil.getInstance().setUp();

		// Load valid avalanche bulletin JSON from resources
		bulletins = new ArrayList<AvalancheBulletin>();
		bulletinsAmPm = new ArrayList<AvalancheBulletin>();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("2030-02-16_1.json");
		StringBuilder bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		String validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin);

		is = classloader.getResourceAsStream("2030-02-16_2.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin2 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin2);

		is = classloader.getResourceAsStream("2030-02-16_3.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin3 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin3);

		is = classloader.getResourceAsStream("2030-02-16_4.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin4 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin4);

		is = classloader.getResourceAsStream("2030-02-16_5.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin5 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin5);

		is = classloader.getResourceAsStream("2030-02-16_6.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin6 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletinsAmPm.add(bulletin6);

		is = classloader.getResourceAsStream("2030-02-16_7.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin7 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletinsAmPm.add(bulletin7);
	}

	@After
	public void shutDown() {
		// HibernateUtil.getInstance().shutDown();
	}

	@Ignore
	@Test
	public void getCsv() {
		String csvString = StatisticsController.getInstance().getCsvString(LanguageCode.de, bulletinsAmPm, false);
		System.out.println(csvString);
	}

	@Ignore
	@Test
	public void getExtendedCsv() {
		String csvString = StatisticsController.getInstance().getCsvString(LanguageCode.de, bulletinsAmPm, true);
		System.out.println(csvString);
	}
}
