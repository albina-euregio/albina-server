package eu.albina.json;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnowProfileJsonValidatorTest {

	private static Logger logger = LoggerFactory.getLogger(SnowProfileJsonValidatorTest.class);

	private String validSnowProfileStringFromResource;
	private String invalidSnowProfileStringFromResource;

	@Before
	public void setUp() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		// Load valid snow profile JSON from resources
		InputStream is = classloader.getResourceAsStream("validSnowProfile.json");
		StringBuilder snowProfileStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				snowProfileStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing snow profile!");
		}
		validSnowProfileStringFromResource = snowProfileStringBuilder.toString();

		// Load invalid snow profile JSON from resources
		is = classloader.getResourceAsStream("invalidSnowProfile.json");

		snowProfileStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				snowProfileStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing snow profile!");
		}

		invalidSnowProfileStringFromResource = snowProfileStringBuilder.toString();
	}

	@Ignore
	@Test
	public void testValidateSnowProfileJSONValid() {
		assertEquals(0, JsonValidator.validateSnowProfile(validSnowProfileStringFromResource).length());
	}

	@Ignore
	@Test
	public void testValidateSnowProfileJSONInvalid() {
		assertEquals(1, JsonValidator.validateSnowProfile(invalidSnowProfileStringFromResource).length());
	}

}
