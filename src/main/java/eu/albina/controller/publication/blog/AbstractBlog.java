package eu.albina.controller.publication.blog;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import eu.albina.model.publication.BlogConfiguration;

public interface AbstractBlog {
	List<BlogItem> searchBlogPosts(BlogConfiguration config, String searchText, String searchCategory, Instant startDate, Instant endDate) throws IOException, InterruptedException;

	List<BlogItem> getBlogPosts(BlogConfiguration config) throws IOException, InterruptedException;

	BlogItem getLatestBlogPost(BlogConfiguration config) throws IOException, InterruptedException;

	BlogItem getBlogPost(BlogConfiguration config, String blogPostId) throws IOException, InterruptedException;
}
