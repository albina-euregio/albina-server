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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.socialmedia.BlogController;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.GlobalVariables;

/**
 * A {@code org.quartz.Job} handling all the tasks and logic necessary to
 * automatically update the Avalanche.report at 8AM.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class BlogJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(BlogJob.class);

	// REGION
	/**
	 * Execute all necessary tasks to publish new blog posts per email and
	 * messengerpeople.
	 * 
	 * @param arg0
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		for (String region : GlobalVariables.regionsEuregio) {
			switch (region) {
			case "AT-07":
				if (GlobalVariables.isPublishBlogsTyrol()) {
					logger.info("Blog job triggered for TY!");
					for (LanguageCode lang : GlobalVariables.languages)
						(new BlogController(region, lang)).sendNewBlogPosts();
				}
				break;
			case "IT-32-BZ":
				if (GlobalVariables.isPublishBlogsSouthTyrol()) {
					logger.info("Blog job triggered for BZ!");
					for (LanguageCode lang : GlobalVariables.languages)
						(new BlogController(region, lang)).sendNewBlogPosts();
				}
				break;
			case "IT-32-TN":
				if (GlobalVariables.isPublishBlogsTrentino()) {
					logger.info("Blog job triggered for TN!");
					for (LanguageCode lang : GlobalVariables.languages)
						(new BlogController(region, lang)).sendNewBlogPosts();
				}
				break;
			default:
				break;
			}
		}
	}
}
