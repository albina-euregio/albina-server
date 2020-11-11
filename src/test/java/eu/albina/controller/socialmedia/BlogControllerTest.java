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
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.github.openjson.JSONObject;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;
import eu.albina.util.XMLResourceBundleControl;

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
		BlogController.getInstance().sendNewBlogPosts(GlobalVariables.codeTyrol, LanguageCode.de);
	}

	@Ignore
	@Test
	public void testTicket150() {
		final JSONObject blog = new JSONObject("{\n" + "  \"replies\": {},\n" + "  \"kind\": \"blogger#post\",\n"
				+ "  \"author\": {},\n"
				+ "  \"etag\": \"\\\"dGltZXN0YW1wOiAxNTc1NTYxNTg5NzM2Cm9mZnNldDogMz YwMDAwMAo\\\"\",\n"
				+ "  \"id\": \"4564885875858452565\",\n" + "  \"published\": \"2019-12-05T16:59:00+01:00\",\n"
				+ "  \"blog\": {\"id\": \"1263754381945501754\"},\n" + "  \"title\": \"Sonnige Woche\",\n"
				+ "  \"updated\": \"2019-12-05T16:59:49+01:00\",\n"
				+ "  \"url\": \"http ://lawinensuedtirol.blogspot.com/2019/12/sonnige-woche.html\",\n"
				+ "  \"content\": \"\",\n"
				+ "  \"selfLink\": \"https://www.googleapis.com/blogger/v3/blogs/1263754381945501754/posts/4564885875858452565\"\n"
				+ "}\n");

		ResourceBundle messages = ResourceBundle.getBundle("i18n.MessagesBundle", LanguageCode.de.getLocale(),
				new XMLResourceBundleControl());

		BlogController.getInstance().sendNewBlogPostToMessengerpeople(blog, "IT-32-BZ", LanguageCode.de, messages);
	}
}
