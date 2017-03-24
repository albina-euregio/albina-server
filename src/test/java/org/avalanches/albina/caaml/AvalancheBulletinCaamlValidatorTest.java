package org.avalanches.albina.caaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class AvalancheBulletinCaamlValidatorTest {

	private static Logger logger = LoggerFactory.getLogger(AvalancheBulletinCaamlValidatorTest.class);

	private String validBulletinStringFromResource;
	private String invalidBulletinStringFromResource;
	private String unclearBulletinStringFromResource;

	@Before
	public void setUp() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		// Load valid avalanche bulletin JSON from resources
		InputStream is = classloader.getResourceAsStream("validBulletin.xml");
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
		is = classloader.getResourceAsStream("invalidBulletin.xml");

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

		// Load invalid avalanche bulletin JSON from resources
		is = classloader.getResourceAsStream("unclearBulletin.xml");

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

		unclearBulletinStringFromResource = bulletinStringBuilder.toString();
	}

	@Test
	public void testValidateAvalancheBulletinCaamlValid() {
		try {
			CaamlValidator.validateCaamlBulletin(validBulletinStringFromResource);
		} catch (SAXException e) {
			Assert.fail();
		} catch (IOException e) {
			Assert.fail();
		}
	}

	@Test(expected = SAXException.class)
	public void testValidateAvalancheBulletinCaamlInvalid() throws SAXException, IOException {
		CaamlValidator.validateCaamlBulletin(invalidBulletinStringFromResource);
	}

	@Test
	public void testValidateAvalancheBulletinCaamlUnclear() {
		try {
			CaamlValidator.validateCaamlBulletin(unclearBulletinStringFromResource);
		} catch (SAXException e) {
			Assert.fail();
		} catch (IOException e) {
			Assert.fail();
		}
	}

}
