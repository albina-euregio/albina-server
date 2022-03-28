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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.User;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.GlobalVariables;

/**
 * A {@code org.quartz.Job} handling all the tasks and logic necessary to
 * automatically publish the Avalanche.report at 5PM.
 *
 * @author Norbert Lanzanasto
 *
 */
public class PublicationJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(PublicationJob.class);

	/**
	 * Execute all necessary tasks to publish the Avalanche.report at 5PM, depending
	 * on the current settings.
	 *
	 * @param arg0
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("Publication job triggered!");

		List<String> regions = GlobalVariables.getPublishRegions();
		if (!regions.isEmpty()) {
			try {
				User user = UserController.getInstance().getUser(GlobalVariables.avalancheReportUsername);

				ZonedDateTime today = LocalDate.now().atStartOfDay(AlbinaUtil.localZone());
				Instant startDate = today.plusDays(1).toInstant();
				Instant endDate = today.plusDays(2).toInstant();

                logger.debug("Start date: {}", startDate.toString());
                logger.debug("End date: {}", endDate.toString());

				Instant publicationDate = Instant.now();

                logger.debug("Publication date: {}", publicationDate.toString());

				// Set publication date
				Map<String, AvalancheBulletin> publishedBulletins = AvalancheBulletinController.getInstance()
						.publishBulletins(startDate, endDate, regions, publicationDate, user);

				if (publishedBulletins.values() != null && !publishedBulletins.values().isEmpty()) {
					List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
					for (AvalancheBulletin avalancheBulletin : publishedBulletins.values()) {
						if (avalancheBulletin.getPublishedRegions() != null
								&& !avalancheBulletin.getPublishedRegions().isEmpty())
							result.add(avalancheBulletin);
					}
					if (result != null && !result.isEmpty()) {
						PublicationController.getInstance().publishAutomatically(result);
					} else {
						logger.debug("No bulletins to publish!");
					}
				}

				List<String> avalancheReportIds = new ArrayList<String>();
				for (String region : regions) {
					String avalancheReportId = AvalancheReportController.getInstance()
							.publishReport(publishedBulletins.values(), startDate, region, user, publicationDate);
					avalancheReportIds.add(avalancheReportId);
				}
			} catch (AlbinaException e) {
				logger.error("Error publishing bulletins", e);
			}
		} else {
			logger.info("No bulletins to publish.");
		}
	}
}
