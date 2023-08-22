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

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.util.HibernateUtil;

import static eu.albina.RegionTestUtils.regionSouthTyrol;
import static eu.albina.RegionTestUtils.regionTyrol;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
public class BlogControllerTest {

	@AfterEach
	public void shutDown() {
		HibernateUtil.getInstance().shutDown();
	}

	@Test
	void testWordpress() throws Exception {
		BlogConfiguration config = new BlogConfiguration();
		config.setBlogApiUrl("https://blog.avalanche.report/at-07/wp-json/wp/v2/");
		config.setLanguageCode(LanguageCode.de);
		config.setLastPublishedTimestamp(OffsetDateTime.parse("2023-01-01T00:00:00Z"));
		config.setRegion(regionTyrol);
		List<? extends BlogItem> blogPosts = BlogController.getInstance().getBlogPosts(config);
		assertTrue(blogPosts.size() > 5, "size=" + blogPosts.size());
		assertTrue(blogPosts.stream().anyMatch(item -> item.getAttachmentUrl() != null), "one blog has image");
		assertTrue(BlogController.getInstance().getBlogPost(config, blogPosts.get(0).getId()).length() > 100, "blog has >100 chars");
	}

	@Disabled
	@Test
	public void testBlogPosts() throws Exception {
		HibernateUtil.getInstance().setUp();
		BlogConfiguration config = BlogController.getInstance().getConfiguration(regionTyrol, LanguageCode.de);
		config.setLastPublishedTimestamp(OffsetDateTime.parse("2023-01-01T00:00:00Z"));
		List<? extends BlogItem> blogPosts = BlogController.getInstance().getBlogPosts(config);
		assertTrue(blogPosts.size() > 5, "size=" + blogPosts.size());
		assertTrue(blogPosts.stream().anyMatch(item -> item.getAttachmentUrl() != null), "one blog has image");
		BlogController.getInstance().updateConfigurationLastPublished(config, blogPosts.get(0));
	}

	@Disabled
	@Test
	public void testLatestBlogPost() throws Exception {
		HibernateUtil.getInstance().setUp();
		BlogConfiguration config = BlogController.getInstance().getConfiguration(regionTyrol, LanguageCode.de);
		Blogger.Item blogPost = (Blogger.Item) BlogController.getInstance().getLatestBlogPost(config);
		assertTrue(blogPost.content.length() > 100, "blog has >100 chars");
	}

	@Disabled
	@Test
	public void testBlogPost() throws Exception {
		HibernateUtil.getInstance().setUp();
		String blogPost = BlogController.getInstance().getBlogPost(BlogController.getInstance().getConfiguration(regionTyrol, LanguageCode.de), "1227558273754407795");
		assertTrue(blogPost.contains("Lawinenabgänge, Rissbildungen und Setzungsgeräusche sind eindeutige Alarmsignale"));
	}

	@Disabled
	@Test
	public void sendBlogPostsTest() {
		HibernateUtil.getInstance().setUp();
		BlogController.getInstance().sendNewBlogPosts(regionTyrol, LanguageCode.de);
	}

	@Disabled
	@Test
	public void testTicket150() throws Exception {
		BlogConfiguration config = BlogController.getInstance().getConfiguration(regionSouthTyrol, LanguageCode.de);
		String blogPost = BlogController.getInstance().getBlogPost(config, "4564885875858452565");
		assertTrue(blogPost.contains("In dieser Woche sorgte das Wetter für traumhafte Verhältnisse in den Bergen mit milden Temperaturen und schwachem Wind."));
	}
}
