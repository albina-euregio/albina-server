package org.avalanches.albina.controller;

import org.avalanches.albina.exception.AlbinaException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnowProfileClassifierTest {

	private static Logger logger = LoggerFactory.getLogger(SnowProfileClassifierTest.class);

	@Before
	public void setUp() throws Exception {
		// TODO load all snow profiles from test resources
		logger.debug("SnowProfiles loading ...");

		logger.debug("SnowProfiles loaded.");
	}

	@Ignore
	@Test
	public void saveValidSnowProfileInDBTest() {
		// TODO implement snow profile test
	}

	@Ignore
	@Test
	public void getSnowProfilesTest() throws AlbinaException {
		// TODO implement snow profile test
	}
}
