// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;

import eu.albina.exception.AlbinaException;
import eu.albina.model.User;
import eu.albina.util.HibernateUtil;

public class UserControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

	@BeforeEach
	public void setUp() throws Exception {
		HibernateUtil.getInstance().setUp();
	}

	@AfterEach
	public void shutDown() {
		HibernateUtil.getInstance().shutDown();
	}

	@Test
	@Disabled
	public void getUsersTest() throws AlbinaException {
		List<User> users = UserController.getInstance().getUsers();
		for (User user : users) {
			logger.info(user.getEmail());
		}
	}

	@Test
	@Disabled
	public void getUserTest() throws AlbinaException {
		User user = UserController.getInstance().getUser("info@avalanche.report");
		logger.info(user.getEmail());
	}

	@Test
	@Disabled
	public void getRolesTest() throws AlbinaException {
		JSONArray rolesJson = UserController.getInstance().getRolesJson();
		logger.info(rolesJson.toString());
	}
}
