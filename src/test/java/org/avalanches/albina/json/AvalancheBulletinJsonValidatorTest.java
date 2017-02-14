package org.avalanches.albina.json;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.avalanches.albina.json.JsonValidator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvalancheBulletinJsonValidatorTest {

	private static Logger logger = LoggerFactory.getLogger(AvalancheBulletinJsonValidatorTest.class);

	private String validBulletinStringFromResource;
	private String invalidBulletinStringFromResource;

	@Before
	public void setUp() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		// Load valid avalanche bulletin JSON from resources
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
		validBulletinStringFromResource = bulletinStringBuilder.toString();

		// Load invalid avalanche bulletin JSON from resources
		is = classloader.getResourceAsStream("invalidBulletin.json");

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

		invalidBulletinStringFromResource = bulletinStringBuilder.toString();
	}

	@Test
	public void testValidateAvalancheBulletinJSONValid() {
		assertEquals(0, JsonValidator.validateAvalancheBulletin(validBulletinStringFromResource).length());
	}

	@Test
	public void testValidateAvalancheBulletinJSONInvalid() {
		assertEquals(1, JsonValidator.validateAvalancheBulletin(invalidBulletinStringFromResource).length());
	}

}
