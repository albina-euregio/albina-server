/*******************************************************************************
 * Copyright (C) 2021 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.controller;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.User;
import eu.albina.util.HibernateUtil;

public class UserControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

	@Before
	public void setUp() throws Exception {
		HibernateUtil.getInstance().setUp();
	}

	@Test
	public void getUsersTest() throws AlbinaException {
		List<User> users = UserController.getInstance().getUsers();
		for (User user : users) {
			logger.info(user.getEmail());
		}
	}

	@Test
	public void getUserTest() throws AlbinaException {
		User user = UserController.getInstance().getUser("info@avalanche.report");
		logger.info(user.getEmail());
	}
}
