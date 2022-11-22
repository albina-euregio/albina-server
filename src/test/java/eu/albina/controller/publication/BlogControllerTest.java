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
package eu.albina.controller.publication;

import java.time.Instant;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.GoogleBloggerConfiguration;
import eu.albina.util.HibernateUtil;

import static eu.albina.RegionTestUtils.regionSouthTyrol;
import static eu.albina.RegionTestUtils.regionTyrol;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class BlogControllerTest {


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
	public void testBlogPosts() throws Exception {
		GoogleBloggerConfiguration config = BlogController.getInstance().getConfiguration(regionTyrol, LanguageCode.de);
		BlogController.getInstance().lastFetch.put(config.getBlogId(), Instant.ofEpochMilli(0L));
		List<Blogger.Item> blogPosts = BlogController.getInstance().getBlogPosts(config);
		assertTrue("size=" + blogPosts.size(), blogPosts.size() > 5);
		assertTrue("one blog has image", blogPosts.stream().anyMatch(item -> item.images != null && !item.images.isEmpty()));
	}

	@Ignore
	@Test
	public void testLatestBlogPost() throws Exception {
		GoogleBloggerConfiguration config = BlogController.getInstance().getConfiguration(regionTyrol, LanguageCode.de);
		Blogger.Item blogPost = BlogController.getInstance().getLatestBlogPost(config);
		assertTrue("blog has >100 chars", blogPost.content.length() > 100);
	}

	@Ignore
	@Test
	public void testBlogPost() throws Exception {
		String blogPost = BlogController.getInstance().getBlogPost("1227558273754407795", regionTyrol, LanguageCode.de);
		assertThat(blogPost, containsString("Lawinenabgänge, Rissbildungen und Setzungsgeräusche sind eindeutige Alarmsignale"));
	}

	@Ignore
	@Test
	public void sendBlogPostsTest() {
		HibernateUtil.getInstance().setUp();
		BlogController.getInstance().sendNewBlogPosts(regionTyrol, LanguageCode.de);
		HibernateUtil.getInstance().shutDown();
	}

	@Ignore
	@Test
	public void testTicket150() throws Exception {
		String blogPost = BlogController.getInstance().getBlogPost("4564885875858452565", regionSouthTyrol, LanguageCode.de);
		assertThat(blogPost, containsString("In dieser Woche sorgte das Wetter für traumhafte Verhältnisse in den Bergen mit milden Temperaturen und schwachem Wind."));
	}
}
