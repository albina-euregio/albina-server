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

import eu.albina.model.ServerInstance;
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
		ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
		if (!serverInstance.isPublishAt5PM()) {
			return;
		}
		ZonedDateTime today = LocalDate.now().atStartOfDay(AlbinaUtil.localZone());
		Instant startDate = today.plusDays(1).toInstant();
		Instant endDate = today.plusDays(2).toInstant();
		Instant publicationDate = AlbinaUtil.getInstantNowNoNanos();
		logger.info("Publication/update job triggered startDate={} endDate={} publicationDate={}", startDate, endDate, publicationDate);

		List<Region> regions = RegionController.getInstance().getPublishBulletinRegions();
		if (regions.isEmpty()) {
			logger.info("No bulletins to publish/update.");
			return;
		}
		try {
			String userName = serverInstance.getUserName();
			User user = userName != null ? UserController.getInstance().getUser(userName) : null;
			Map<String, AvalancheBulletin> publishedBulletins = AvalancheBulletinController.getInstance()
					.publishBulletins(startDate, endDate, regions, publicationDate, user);
			if (publishedBulletins.values() == null || publishedBulletins.values().isEmpty()) {
				return;
			}
			List<AvalancheBulletin> result = publishedBulletins.values().stream()
				.filter(avalancheBulletin -> avalancheBulletin.getPublishedRegions() != null
					&& !avalancheBulletin.getPublishedRegions().isEmpty())
				.collect(Collectors.toList());
			if (result == null || result.isEmpty()) {
				return;
			}
			PublicationController.getInstance().publish(result, regions, user, publicationDate, startDate, false);
		} catch (AlbinaException e) {
			logger.error("Error publishing/updating bulletins", e);
		}
	}
}
