package eu.albina.controller;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;

public class AuthenticationControllerTest {

	private static Logger logger = LoggerFactory.getLogger(AuthenticationControllerTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void credentialsHashTest() {
		String pw_hash = BCrypt.hashpw("ijieGohx", BCrypt.gensalt());
		logger.debug(pw_hash);
	}

	@Ignore
	@Test
	public void getSnowProfilesTest() throws AlbinaException {
		// TODO implement snow profile test
	}
}
