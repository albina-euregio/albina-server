package org.avalanches.controller;

import org.avalanches.ais.exception.AvalancheInformationSystemException;
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
	public void getSnowProfilesTest() throws AvalancheInformationSystemException {
		// TODO implement snow profile test
	}
}
