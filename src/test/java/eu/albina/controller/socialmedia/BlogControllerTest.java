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
import java.time.Instant;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.GlobalVariables;

import static org.junit.Assert.assertTrue;

public class BlogControllerTest {

	private static Logger logger = LoggerFactory.getLogger(BlogControllerTest.class);

	@Test
	public void testBlogPosts() throws Exception {
		BlogController.getInstance().lastFetch.put(GlobalVariables.blogIds.get(GlobalVariables.codeTyrol, LanguageCode.de), Instant.ofEpochMilli(0L));
		List<Blogger.Item> blogPosts = BlogController.getInstance().getBlogPosts(GlobalVariables.codeTyrol, LanguageCode.de);
		assertTrue("size=" + blogPosts.size(), blogPosts.size() > 5);
		assertTrue("one blog has image", blogPosts.stream().anyMatch(item -> item.images != null && !item.images.isEmpty()));
	}

	@Test
	public void testBlogPost() throws Exception {
		String blogPost = BlogController.getInstance().getBlogPost("1227558273754407795", GlobalVariables.codeTyrol, LanguageCode.de);
		assertTrue(blogPost, blogPost.contains("Lawinenabgänge, Rissbildungen und Setzungsgeräusche sind eindeutige Alarmsignale"));
	}

	@Ignore
	@Test
	public void sendBlogPostsTest() throws KeyManagementException, CertificateException, NoSuchAlgorithmException,
			KeyStoreException, IOException {
		BlogController.getInstance().sendNewBlogPosts(GlobalVariables.codeTyrol, LanguageCode.de);
	}

	@Ignore
	@Test
	public void testTicket150() throws Exception {
		final String blog = "{\n" + "  \"replies\": {},\n" + "  \"kind\": \"blogger#post\",\n"
				+ "  \"author\": {},\n"
				+ "  \"etag\": \"\\\"dGltZXN0YW1wOiAxNTc1NTYxNTg5NzM2Cm9mZnNldDogMz YwMDAwMAo\\\"\",\n"
				+ "  \"id\": \"4564885875858452565\",\n" + "  \"published\": \"2019-12-05T16:59:00+01:00\",\n"
				+ "  \"blog\": {\"id\": \"1263754381945501754\"},\n" + "  \"title\": \"Sonnige Woche\",\n"
				+ "  \"updated\": \"2019-12-05T16:59:49+01:00\",\n"
				+ "  \"url\": \"http ://lawinensuedtirol.blogspot.com/2019/12/sonnige-woche.html\",\n"
				+ "  \"content\": \"\",\n"
				+ "  \"selfLink\": \"https://www.googleapis.com/blogger/v3/blogs/1263754381945501754/posts/4564885875858452565\"\n"
				+ "}\n";

		Blogger.Item item = new CommonProcessor().fromJson(blog, Blogger.Item.class);
		logger.debug(item.title);
	}
}
