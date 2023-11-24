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
package eu.albina.jobs;

import eu.albina.controller.publication.BlogItem;
import eu.albina.model.publication.BlogConfiguration;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.RegionController;
import eu.albina.controller.publication.BlogController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;

import java.io.IOException;
import java.util.List;

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
			for (LanguageCode lang : LanguageCode.ENABLED) {
				sendNewBlogPosts(region, lang);
			}
		}
	}

	private static void sendNewBlogPosts(Region region, LanguageCode lang) {
		if (!region.isPublishBlogs()) {
			logger.debug("Publishing blogs is disabled for region {}", region);
			return;
		}

		BlogConfiguration config = BlogController.getConfiguration(region, lang).orElse(null);
		if (config == null) {
			logger.debug("No blog configuration found for region {} and lang {}", region, lang);
			return;
		}

		List<? extends BlogItem> blogPosts;
		try {
			blogPosts = BlogController.getBlogPosts(config);
		} catch (IOException e) {
			logger.warn("Blog posts could not be retrieved: " + region.getId() + ", " + lang.toString(), e);
			return;
		}

		for (BlogItem object : blogPosts) {
			BlogController.sendBlogPost(config, object);
		}
	}
}
