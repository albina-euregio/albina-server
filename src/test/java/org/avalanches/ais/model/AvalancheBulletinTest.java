package org.avalanches.ais.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.avalanches.ais.model.enumerations.CountryCode;
import org.avalanches.ais.model.enumerations.LanguageCode;
import org.avalanches.ais.model.enumerations.Tendency;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvalancheBulletinTest {

	private static Logger logger = LoggerFactory.getLogger(AvalancheBulletinTest.class);

	private AvalancheBulletin bulletin;
	private String bulletinJsonString;

	@Before
	public void setUp() throws Exception {
		// Create bulletin instance
		bulletin = new AvalancheBulletin();
		Author author = new Author();
		author.setName("Patrick Nairz");
		author.setEmail("patrick.nairz@tirol.gv.at");
		author.setRole("Avalanche Warning Service");
		author.setOrganization("LWDTirol");
		bulletin.setAuthor(author);

		bulletin.setTendency(Tendency.increasing);

		Texts avalancheSituationHighlight = new Texts();
		Text textDe = new Text();
		textDe.setLanguage(LanguageCode.de);
		textDe.setText("Am Morgen günstige Verhältnisse - dann rascher Anstieg der Lawinengefahr!");
		avalancheSituationHighlight.addText(textDe);
		Text textEn = new Text();
		textEn.setLanguage(LanguageCode.en);
		textEn.setText("Avalanche situation highlight in english.");
		avalancheSituationHighlight.addText(textEn);
		bulletin.setAvalancheSituationHighlight(avalancheSituationHighlight);

		bulletin.setValidFrom(new DateTime(2016, 1, 05, 07, 30));
		bulletin.setValidUntil(new DateTime(2016, 1, 06, 07, 30));

		bulletin.setCountry(CountryCode.AT);
		bulletin.setRegion("Tirol");

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
	}

	@Ignore
	@Test
	public void testCreateObjectFromJSONAndBack() {
		JSONObject data = new JSONObject(bulletinJsonString);
		AvalancheBulletin b = new AvalancheBulletin(data);
		JSONAssert.assertEquals(bulletinJsonString, b.toJSON(), JSONCompareMode.NON_EXTENSIBLE);
	}
}
