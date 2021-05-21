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
import java.time.temporal.ChronoUnit;
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
 * automatically update the Avalanche.report at 8AM.
 *
 * @author Norbert Lanzanasto
 *
 */
public class UpdateJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(UpdateJob.class);

	/**
	 * Execute all necessary tasks to update the Avalanche.report at 8AM, depending
	 * on the current settings.
	 *
	 * @param arg0
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("Update job triggered!");

		try {
			User user = UserController.getInstance().getUser(GlobalVariables.avalancheReportUsername);

			Instant startDate = AlbinaUtil.getInstantStartOfDay();
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			List<String> changedRegions = new ArrayList<String>();
			if (GlobalVariables.isPublishBulletinsTyrol()
					&& AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeTyrol))
				changedRegions.add(GlobalVariables.codeTyrol);
			if (GlobalVariables.isPublishBulletinsSouthTyrol()
					&& AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeSouthTyrol))
				changedRegions.add(GlobalVariables.codeSouthTyrol);
			if (GlobalVariables.isPublishBulletinsTrentino()
					&& AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeTrentino))
				changedRegions.add(GlobalVariables.codeTrentino);
			if (GlobalVariables.isPublishBulletinsAran()
					&& AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeAran))
				changedRegions.add(GlobalVariables.codeAran);

			Instant publicationDate = AlbinaUtil.getInstantNow();

			if (!changedRegions.isEmpty()) {
				Map<String, AvalancheBulletin> publishedBulletins = AvalancheBulletinController.getInstance()
						.publishBulletins(startDate, endDate, changedRegions, publicationDate, user);
				if (publishedBulletins.values() != null && !publishedBulletins.values().isEmpty()) {
					List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
					for (AvalancheBulletin avalancheBulletin : publishedBulletins.values()) {
						if (avalancheBulletin.getPublishedRegions() != null
								&& !avalancheBulletin.getPublishedRegions().isEmpty())
							result.add(avalancheBulletin);
					}
					if (result != null && !result.isEmpty())
						PublicationController.getInstance().updateAutomatically(result, changedRegions);
				}

				AvalancheReportController.getInstance().publishReport(publishedBulletins.values(), startDate,
						changedRegions, user, publicationDate);
			} else {
				logger.info("No bulletins to update.");
			}
		} catch (AlbinaException e) {
			logger.error("Error publishing bulletins", e);
		}
	}
}
