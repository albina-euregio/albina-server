// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationControllerTest.class);

	@BeforeEach
	public void setUp() throws Exception {
	}

	@Test
	public void testAccessToken() {
		String token = AuthenticationController.getInstance().issueAccessToken("foobar");
		Assertions.assertNotNull(token);
	}

	@Test
	public void credentialsHashTest() {
		String pw_hash = BCrypt.hashpw("ijieGohx", BCrypt.gensalt());
		logger.debug(pw_hash);
	}
}
