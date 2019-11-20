/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
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
package eu.albina.controller.socialmedia;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.HibernateUtil;

public class BlogControllerTest {

	// private static Logger logger =
	// LoggerFactory.getLogger(BlogControllerTest.class);

	@Before
	public void setUp() throws Exception {
		HibernateUtil.getInstance().setUp();
	}

	@After
	public void shutDown() {
		HibernateUtil.getInstance().shutDown();
	}

	@Ignore
	@Test
	public void sendBlogPostsTest() throws KeyManagementException, CertificateException, NoSuchAlgorithmException,
			KeyStoreException, IOException {
		BlogController.getInstance().sendNewBlogPosts("AT-07", LanguageCode.de);
	}
}
