package eu.albina.controller.publication.blog;

import java.io.IOException;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ExecutionException;

import eu.albina.model.publication.BlogConfiguration;

public interface AbstractBlog {
	List<? extends BlogItem> getCachedBlogPosts(BlogConfiguration config, String searchText, String searchCategory, Year year) throws ExecutionException;

	BlogItem getCachedBlogPost(BlogConfiguration config, String blogPostId) throws ExecutionException;

	List<? extends BlogItem> getBlogPosts(BlogConfiguration config) throws IOException, InterruptedException;

	BlogItem getLatestBlogPost(BlogConfiguration config) throws IOException, InterruptedException;

	BlogItem getBlogPost(BlogConfiguration config, String blogPostId) throws IOException, InterruptedException;
}
