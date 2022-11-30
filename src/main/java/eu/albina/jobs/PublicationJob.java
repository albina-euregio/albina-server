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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.RegionController;
import eu.albina.controller.ServerInstanceController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.util.AlbinaUtil;

/**
 * A {@code org.quartz.Job} handling all the tasks and logic necessary to
 * automatically publish the bulletins at 5PM.
 *
 * @author Norbert Lanzanasto
 *
 */
public class PublicationJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(PublicationJob.class);

	/**
	 * Execute all necessary tasks to publish the bulletins at 5PM, depending
	 * on the current settings.
	 *
	 * @param arg0
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("Publication job triggered!");

		List<Region> regions = RegionController.getInstance().getPublishBulletinRegions();
		if (!regions.isEmpty()) {
			try {
				String userName = ServerInstanceController.getInstance().getLocalServerInstance().getUserName();
				User user = userName != null ? UserController.getInstance().getUser(userName) : null;

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
					List<AvalancheBulletin> result = publishedBulletins.values().stream()
						.filter(avalancheBulletin -> avalancheBulletin.getPublishedRegions() != null
							&& !avalancheBulletin.getPublishedRegions().isEmpty())
						.collect(Collectors.toList());
					if (result != null && !result.isEmpty()) {
						PublicationController.getInstance().publishAutomatically(result, user, publicationDate, startDate);
					} else {
						logger.debug("No bulletins to publish!");
					}
				}
			} catch (AlbinaException e) {
				logger.error("Error publishing bulletins", e);
			}
		} else {
			logger.info("No bulletins to publish.");
		}
	}
}
