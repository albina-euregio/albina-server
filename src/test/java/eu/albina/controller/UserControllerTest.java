// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.util.List;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.User;

public class UserControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

	@Inject
	UserRepository userRepository;

	@Test
	@Disabled
	public void getUsersTest() {
		List<User> users = userRepository.findAll();
		for (User user : users) {
			logger.info(user.getEmail());
		}
	}

	@Test
	@Disabled
	public void getUserTest() {
		User user = userRepository.findById("info@avalanche.report").orElseThrow();
		logger.info(user.getEmail());
	}
}
