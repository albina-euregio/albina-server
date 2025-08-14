// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.RegionController;
import eu.albina.controller.publication.BlogController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.model.publication.RapidMailConfiguration;

/**
 * A {@code org.quartz.Job} handling all the tasks and logic necessary to
 * automatically publish blog posts.
 *
 * @author Norbert Lanzanasto
 *
 */
public class BlogJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(BlogJob.class);

	/**
	 * Execute all necessary tasks to publish new blog posts.
	 *
	 * @param arg0
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		for (Region region : RegionController.getInstance().getPublishBlogRegions()) {
			logger.info("Blog job triggered for {}!", region.getId());
			for (LanguageCode lang : region.getEnabledLanguages()) {
				BlogController.sendNewBlogPosts(region, lang);
			}
		}
		BlogController.sendNewBlogPosts(BlogConfiguration.TECH_BLOG_ID, RapidMailConfiguration.TECH_SUBJECT_MATTER, new Region("AT-07"));
	}

}
