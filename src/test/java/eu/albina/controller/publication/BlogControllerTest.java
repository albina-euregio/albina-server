// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import eu.albina.RegionTestUtils;
import eu.albina.controller.publication.blog.BlogController;
import eu.albina.controller.publication.blog.BlogItem;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.model.publication.RapidMailConfiguration;

@MicronautTest
public class BlogControllerTest {

	@Inject
    BlogController blogController;

	@Inject
	RegionTestUtils regionTestUtils;

	@Test
	void testWordpress() throws Exception {
		BlogConfiguration config = new BlogConfiguration();
		config.setBlogApiUrl("https://blog.avalanche.report/at-07/wp-json/wp/v2/");
		config.setLanguageCode(LanguageCode.de);
		config.setLastPublishedTimestamp(OffsetDateTime.parse("2023-01-01T00:00:00Z"));
		config.setRegion(regionTestUtils.regionTyrol());
		List<? extends BlogItem> blogPosts = blogController.getBlogPosts(config);
		assertTrue(blogPosts.size() > 5, "size=" + blogPosts.size());
		assertTrue(blogPosts.stream().anyMatch(item -> item.getAttachmentUrl() != null), "one blog has image");
		assertTrue(blogController.getBlogPost(config, blogPosts.getFirst().getId()).getContent().length() > 100, "blog has >100 chars");

		config.setBlogApiUrl("https://blog.avalanche.report/it-32-bz/wp-json/wp/v2/");
		assertEquals("Inizio dell’inverno in montagna", blogController.getBlogPost(config, "1851").getTitle());
		assertEquals(OffsetDateTime.parse("2023-11-30T14:20:44Z"), blogController.getBlogPost(config, "2084").getPublished());

		config.setBlogApiUrl("https://blog.avalanche.report/it-32-tn/wp-json/wp/v2/");
		assertNull(blogController.getBlogPost(config, "495").getAttachmentUrl());
	}

	@Disabled
	@Test
	public void testBlogPosts() throws Exception {
		BlogConfiguration config = blogController.getConfiguration(regionTestUtils.regionTyrol(), LanguageCode.de).orElseThrow();
		config.setLastPublishedTimestamp(OffsetDateTime.parse("2023-01-01T00:00:00Z"));
		List<? extends BlogItem> blogPosts = blogController.getBlogPosts(config);
		assertTrue(blogPosts.size() > 5, "size=" + blogPosts.size());
		assertTrue(blogPosts.stream().anyMatch(item -> item.getAttachmentUrl() != null), "one blog has image");
		blogController.updateConfigurationLastPublished(config, blogPosts.getFirst());
	}

	@Disabled
	@Test
	public void testLatestBlogPost() throws Exception {
		BlogConfiguration config = blogController.getConfiguration(regionTestUtils.regionTyrol(), LanguageCode.de).orElseThrow();
		BlogItem blogItem = blogController.getLatestBlogPost(config);
		assertTrue(blogItem.getContent().length() > 100, "blog has >100 chars");
	}

	@Disabled
	@Test
	public void testBlogPost() throws Exception {
        BlogConfiguration configuration = blogController.getConfiguration(regionTestUtils.regionTyrol(), LanguageCode.de).orElseThrow();
		String blogPost = blogController.getBlogPost(configuration, "1227558273754407795").getContent();
		assertTrue(blogPost.contains("Lawinenabgänge, Rissbildungen und Setzungsgeräusche sind eindeutige Alarmsignale"));
	}

	@Disabled
	@Test
	public void testTicket150() throws Exception {
		BlogConfiguration config = blogController.getConfiguration(regionTestUtils.regionSouthTyrol(), LanguageCode.de).orElseThrow();
		String blogPost = blogController.getBlogPost(config, "4564885875858452565").getContent();
		assertTrue(blogPost.contains("In dieser Woche sorgte das Wetter für traumhafte Verhältnisse in den Bergen mit milden Temperaturen und schwachem Wind."));
	}

	@Disabled
	@Test
	public void testTicket280() throws Exception {
		BlogConfiguration config = new BlogConfiguration();
		config.setBlogApiUrl("https://www.googleapis.com/blogger/v3/blogs/");
		config.setBlogId("5267718003722031964");
		config.setApiKey("xxx");
		BlogItem blogPost = blogController.getBlogPost(config, "576660231960838111");
		assertEquals("2023-11-22T08:44-08:00", blogPost.getPublished().toString());
		assertEquals("2023-11-22T16:44:00Z", blogPost.getPublished().toInstant().toString());
		assertEquals("Boletín de Aludes para el Jueves 23/11/2023", blogPost.getTitle());
		config.setLastPublishedTimestamp(blogPost.getPublished());
		Assumptions.assumeTrue(Instant.now().isBefore(Instant.parse("2023-11-24T12:00:00Z")));
		assertEquals(Collections.emptyList(), blogController.getBlogPosts(config));
	}

	@Disabled
	@Test
	public void testTechBlog() throws Exception {
		blogController.sendNewBlogPosts(BlogConfiguration.TECH_BLOG_ID, RapidMailConfiguration.TECH_SUBJECT_MATTER, "AT-07");
	}
}
