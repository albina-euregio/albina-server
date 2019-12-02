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
package eu.albina.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.enumerations.LanguageCode;

public class AvalancheBulletinTest {

	private static Logger logger = LoggerFactory.getLogger(AvalancheBulletinTest.class);

	private AvalancheBulletin bulletin;
	private String bulletinJsonString;
	private String bulletin2JsonString;
	private String bulletin3JsonString;
	private String bulletin4JsonString;

	@Before
	public void setUp() throws Exception {
		// Create bulletin instance
		bulletin = new AvalancheBulletin();
		User user = new User();
		user.setName("Norbert Lanzanasto");
		user.setEmail("n.lanzanasto@gmail.com");
		bulletin.setUser(user);

		Texts avalancheSituationHighlight = new Texts();
		Text textDe = new Text();
		textDe.setLanguage(LanguageCode.de);
		textDe.setText("Am Morgen günstige Verhältnisse - dann rascher Anstieg der Lawinengefahr!");
		avalancheSituationHighlight.addText(textDe);
		Text textEn = new Text();
		textEn.setLanguage(LanguageCode.en);
		textEn.setText("Avalanche situation highlight in english.");
		avalancheSituationHighlight.addText(textEn);
		bulletin.setAvActivityHighlights(avalancheSituationHighlight);

		bulletin.setValidFrom(new DateTime(2016, 1, 05, 07, 30));
		bulletin.setValidUntil(new DateTime(2016, 1, 06, 07, 30));

		Set<String> regions = new HashSet<String>();
		regions.add("AT-07-10");
		regions.add("AT-07-11");
		regions.add("AT-07-12");
		bulletin.setSavedRegions(regions);

		// Load JSON from resources
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("validBulletin.json");

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
		bulletinJsonString = bulletinStringBuilder.toString();

		// Load JSON from resources
		is = classloader.getResourceAsStream("validBulletin2.json");

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
		bulletin2JsonString = bulletinStringBuilder.toString();

		// Load JSON from resources
		is = classloader.getResourceAsStream("validBulletin3.json");

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
		bulletin3JsonString = bulletinStringBuilder.toString();

		// Load JSON from resources
		is = classloader.getResourceAsStream("validBulletin4.json");

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
		bulletin4JsonString = bulletinStringBuilder.toString();
	}

	@Ignore
	@Test
	public void testCreateObjectFromJSONAndBack() {
		JSONObject data = new JSONObject(bulletinJsonString);
		AvalancheBulletin b = new AvalancheBulletin(data);
		JSONAssert.assertEquals(bulletinJsonString, b.toJSON(), JSONCompareMode.NON_EXTENSIBLE);
	}

	@Ignore
	@Test
	public void testSortByDangerRating() {
		List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
		JSONObject data = new JSONObject(bulletinJsonString);
		AvalancheBulletin b = new AvalancheBulletin(data);
		bulletins.add(b);
		data = new JSONObject(bulletin2JsonString);
		b = new AvalancheBulletin(data);
		bulletins.add(b);
		data = new JSONObject(bulletin3JsonString);
		b = new AvalancheBulletin(data);
		bulletins.add(b);
		data = new JSONObject(bulletin4JsonString);
		b = new AvalancheBulletin(data);
		bulletins.add(b);

		Collections.sort(bulletins);

		for (AvalancheBulletin avalancheBulletin : bulletins) {
			logger.debug("" + avalancheBulletin.getHighestDangerRatingDouble());
		}
	}
}
