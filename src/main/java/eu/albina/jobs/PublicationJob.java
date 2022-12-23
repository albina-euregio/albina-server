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
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import eu.albina.model.AbstractPersistentObject;
import eu.albina.model.AvalancheReport;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.RegionController;
import eu.albina.controller.ServerInstanceController;
import eu.albina.controller.UserController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
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
	public void execute(JobExecutionContext arg0) {
		ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
		if (!isEnabled(serverInstance)) {
			return;
		}
		Instant startDate = getStartDate();
		Instant endDate = startDate.atZone(AlbinaUtil.localZone()).plusDays(1).toInstant();
		Instant publicationDate = AlbinaUtil.getInstantNowNoNanos();
		logger.info("{} triggered startDate={} endDate={} publicationDate={}", getClass().getSimpleName(), startDate, endDate, publicationDate);

		List<Region> regions = getRegions().stream()
			.filter(region -> {
				BulletinStatus status = AvalancheReportController.getInstance().getInternalStatusForDay(startDate, region);
				logger.info("Internal status for region {} is {}", region.getId(), status);
				return status == BulletinStatus.submitted
					|| status == BulletinStatus.resubmitted;
			}).collect(Collectors.toList());
		if (regions.isEmpty()) {
			logger.info("No bulletins to publish/update/change.");
			return;
		}
		String userName = serverInstance.getUserName();
		User user = userName != null ? UserController.getInstance().getUser(userName) : null;
		AvalancheBulletinController avalancheBulletinController = AvalancheBulletinController.getInstance();

		for (Region region1 : regions) {
			logger.info("Publish bulletins for region {}", region1.getId());
			BulletinStatus internalStatus = AvalancheReportController.getInstance().getInternalStatusForDay(startDate,
				region1);

			logger.info("Internal status for region {} is {}", region1.getId(), internalStatus);

			if (internalStatus == BulletinStatus.submitted || internalStatus == BulletinStatus.resubmitted) {
				avalancheBulletinController.publishBulletins(startDate, endDate, region1, publicationDate, user);
			}
		}
		List<AvalancheBulletin> publishedBulletins = AvalancheBulletinController.getInstance().getAllBulletins(startDate, endDate);
		if (publishedBulletins.isEmpty()) {
			return;
		}

		List<AvalancheBulletin> result = publishedBulletins.stream()
			.filter(avalancheBulletin -> avalancheBulletin.getPublishedRegions() != null
				&& !avalancheBulletin.getPublishedRegions().isEmpty())
			.collect(Collectors.toList());
		if (result == null || result.isEmpty()) {
			logger.info("No published regions found in bulletins.");
			return;
		}
		PublicationController publicationController = PublicationController.getInstance();
		boolean isChange = isChange();
		logger.info("Publishing bulletins with publicationDate={} startDate={}", publicationDate, startDate);
		// TODO check if we can use startDate instead
		String validityDateString = AlbinaUtil.getValidityDateString(result);
		String publicationTimeString = AlbinaUtil.getPublicationTime(publicationDate);
		ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

		Collections.sort(result);

		// publish all regions which have to be published
		for (Region region : regions) {
			List<AvalancheBulletin> regionBulletins = result.stream()
					.filter(bulletin -> bulletin.affectsRegionOnlyPublished(region))
					.collect(Collectors.toList());
			logger.info("Publishing region {} with bulletins {} and publication time {}", region.getId(),
					regionBulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()),
					publicationTimeString);

			AvalancheReportController.getInstance().publishReport(regionBulletins, startDate, region, user,
				publicationDate);
		}

		Map<Region, AvalancheReport> reportMap = new HashMap<Region, AvalancheReport>();

		// get all published bulletins
		// FIXME set publicationDate for all bulletins (somehow a hack)
		List<AvalancheBulletin> publishedBulletins1 = AvalancheReportController.getInstance().getPublishedBulletins(
			startDate,
				RegionController.getInstance().getPublishBulletinRegions()).stream().peek(
					bulletin -> bulletin.setPublicationDate(publicationDate.atZone(ZoneId.of("UTC")))
				).collect(Collectors.toList());

		// update all regions to create complete maps
		for (Region region : RegionController.getInstance().getPublishBulletinRegions()) {
			List<AvalancheBulletin> regionBulletins = publishedBulletins1.stream()
					.filter(bulletin -> bulletin.affectsRegionOnlyPublished(region)).collect(Collectors.toList());
			logger.info("Load region {} with bulletins {} and publication time {}", region.getId(),
					regionBulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()), publicationTimeString);
			AvalancheReport avalancheReport = AvalancheReportController.getInstance().getPublicReport(startDate,
					region);

			if (avalancheReport == null || regionBulletins.isEmpty()) {
				continue;
			}

			avalancheReport.setBulletins(regionBulletins, publishedBulletins1);
			avalancheReport.setServerInstance(localServerInstance);

			// maybe another region was not published at all
			if (avalancheReport == null || (avalancheReport.getStatus() != BulletinStatus.published
					&& avalancheReport.getStatus() != BulletinStatus.republished)) {
				continue;
			}

			publicationController.createRegionResources(region, avalancheReport);

			if (regions.contains(region)) {
				reportMap.put(region, avalancheReport);
			}
		}

		// update all super regions
		Set<Region> superRegions = new HashSet<Region>();
		for (Region region : regions) {
			for (Region superRegion : region.getSuperRegions()) {
				if (!superRegions.stream()
						.anyMatch(updateRegion -> updateRegion.getId().equals(superRegion.getId())))
					superRegions.add(superRegion);
			}
		}
		for (Region region : superRegions) {
			logger.info("Publishing super region {} with bulletins {} and publication time {}", region.getId(),
					publishedBulletins1.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()), publicationTimeString);
			AvalancheReport avalancheReport = AvalancheReport.of(publishedBulletins1, region, localServerInstance);
			publicationController.createRegionResources(region, avalancheReport);
		}

		// send notifications only for updated regions after all maps were created
		if (!isChange) {
			for (AvalancheReport avalancheReport : reportMap.values()) {
				if (!avalancheReport.getBulletins().isEmpty() && avalancheReport.getRegion().isCreateMaps()) {
					if (avalancheReport.getRegion().isSendEmails()) {
						new Thread(() -> publicationController.sendEmails(avalancheReport)).start();
					}
					if (avalancheReport.getRegion().isSendTelegramMessages()) {
						new Thread(() -> publicationController.triggerTelegramChannel(avalancheReport, null)).start();
					}
					if (avalancheReport.getRegion().isSendPushNotifications()) {
						new Thread(() -> publicationController.triggerPushNotifications(avalancheReport, null)).start();
					}
				}
			}
		}

		// copy files
		AlbinaUtil.runUpdateFilesScript(validityDateString, publicationTimeString);
		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(result)))
			AlbinaUtil.runUpdateLatestFilesScript(validityDateString);

	}

	protected boolean isEnabled(ServerInstance serverInstance) {
		return serverInstance.isPublishAt5PM();
	}

	protected boolean isChange() {
		return false;
	}

	protected Instant getStartDate() {
		return LocalDate.now().atStartOfDay(AlbinaUtil.localZone()).plusDays(1).toInstant();
	}

	protected List<Region> getRegions() {
		return RegionController.getInstance().getPublishBulletinRegions();
	}

}
