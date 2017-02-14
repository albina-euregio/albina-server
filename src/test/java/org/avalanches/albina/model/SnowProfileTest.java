package org.avalanches.albina.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.avalanches.albina.model.SnowProfile;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnowProfileTest {

	private static Logger logger = LoggerFactory.getLogger(SnowProfileTest.class);
	private String validSnowProfileJsonString;

	@Before
	public void setUp() throws Exception {
		// Load JSON from resources
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("validSnowProfile.json");

		StringBuilder profileStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				profileStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing profile!");
		}

		validSnowProfileJsonString = profileStringBuilder.toString();
	}

	@Ignore
	@Test
	public void testCreateObjectFromJSONAndBack() {
		JSONObject data = new JSONObject(validSnowProfileJsonString);
		SnowProfile profile = new SnowProfile(data);
		JSONAssert.assertEquals(validSnowProfileJsonString, profile.toJSON(), JSONCompareMode.NON_EXTENSIBLE);
	}
}
