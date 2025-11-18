// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import eu.albina.controller.RegionRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.publication.blog.BlogController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.model.publication.RapidMailConfiguration;

import java.io.IOException;

/**
 * A job handling all the tasks and logic necessary to
 * automatically publish blog posts.
 *
 * @author Norbert Lanzanasto
 *
 */
@Singleton
public class BlogJob {

	private static final Logger logger = LoggerFactory.getLogger(BlogJob.class);

	@Inject
	RegionRepository regionRepository;

	@Inject
	BlogController blogController;

	/**
	 * Execute all necessary tasks to publish new blog posts.
	 */
	@Scheduled(cron = "0 0/10 * * * ?")
	public void execute() {
		for (Region region : regionRepository.getPublishBlogRegions()) {
			logger.info("Blog job triggered for {}!", region.getId());
			for (LanguageCode lang : region.getEnabledLanguages()) {
				try {
					blogController.sendNewBlogPosts(region, lang);
				} catch (IOException | InterruptedException e) {
					logger.warn("Blog job failed", e);
				}
			}
		}
		try {
			blogController.sendNewBlogPosts(BlogConfiguration.TECH_BLOG_ID, RapidMailConfiguration.TECH_SUBJECT_MATTER, BlogConfiguration.TECH_BLOG_REGION_OVERRIDE);
		} catch (IOException | InterruptedException e) {
			logger.warn("Blog job failed", e);
		}
	}

}
