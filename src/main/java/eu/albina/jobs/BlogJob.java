// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.RegionController;
import eu.albina.controller.publication.BlogController;
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
public class BlogJob {

	private static final Logger logger = LoggerFactory.getLogger(BlogJob.class);

	/**
	 * Execute all necessary tasks to publish new blog posts.
	 */
	public void execute() {
		for (Region region : RegionController.getInstance().getPublishBlogRegions()) {
			logger.info("Blog job triggered for {}!", region.getId());
			for (LanguageCode lang : region.getEnabledLanguages()) {
				try {
					BlogController.sendNewBlogPosts(region, lang);
				} catch (IOException | InterruptedException e) {
					logger.warn("Blog job failed", e);
				}
			}
		}
		try {
			BlogController.sendNewBlogPosts(BlogConfiguration.TECH_BLOG_ID, RapidMailConfiguration.TECH_SUBJECT_MATTER, new Region("AT-07"));
		} catch (IOException | InterruptedException e) {
			logger.warn("Blog job failed", e);
		}
	}

}
