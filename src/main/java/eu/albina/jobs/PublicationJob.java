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
		AvalancheBulletinController avalancheBulletinController = AvalancheBulletinController.getInstance();
		AvalancheReportController avalancheReportController = AvalancheReportController.getInstance();
		PublicationController publicationController = PublicationController.getInstance();
		RegionController regionController = RegionController.getInstance();
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
				BulletinStatus status = avalancheReportController.getInternalStatusForDay(startDate, region);
				logger.info("Internal status for region {} is {}", region.getId(), status);
				return status == BulletinStatus.submitted || status == BulletinStatus.resubmitted;
			}).collect(Collectors.toList());
		if (regions.isEmpty()) {
			logger.info("No bulletins to publish/update/change.");
			return;
		}
		User user = getUser(serverInstance);

		for (Region region : regions) {
			logger.info("Publish bulletins for region {}", region.getId());
			BulletinStatus internalStatus = avalancheReportController.getInternalStatusForDay(startDate, region);

			logger.info("Internal status for region {} is {}", region.getId(), internalStatus);

			if (internalStatus == BulletinStatus.submitted || internalStatus == BulletinStatus.resubmitted) {
				avalancheBulletinController.publishBulletins(startDate, endDate, region, publicationDate, user);
			}
		}
		List<AvalancheBulletin> publishedBulletins = avalancheBulletinController.getAllBulletins(startDate, endDate);
		if (publishedBulletins.isEmpty()) {
			return;
		}

		publishedBulletins = publishedBulletins.stream()
			.filter(avalancheBulletin -> avalancheBulletin.getPublishedRegions() != null
				&& !avalancheBulletin.getPublishedRegions().isEmpty())
			.collect(Collectors.toList());
		if (publishedBulletins.isEmpty()) {
			logger.info("No published regions found in bulletins.");
			return;
		}
		logger.info("Publishing bulletins with publicationDate={} startDate={}", publicationDate, startDate);
		// TODO check if we can use startDate instead
		String validityDateString = AlbinaUtil.getValidityDateString(publishedBulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(publicationDate);

		Collections.sort(publishedBulletins);

		// publish all regions which have to be published
		for (Region region : regions) {
			List<AvalancheBulletin> regionBulletins = publishedBulletins.stream()
				.filter(bulletin -> bulletin.affectsRegionOnlyPublished(region))
				.collect(Collectors.toList());
			logger.info("Publishing region {} with bulletins {} and publication time {}", region.getId(),
				regionBulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()),
				publicationTimeString);

			avalancheReportController.publishReport(regionBulletins, startDate, region, user, publicationDate);
		}

		Map<Region, AvalancheReport> reportMap = new HashMap<>();

		// get all published bulletins
		// FIXME set publicationDate for all bulletins (somehow a hack)
		publishedBulletins = avalancheReportController.getPublishedBulletins(startDate, regionController.getPublishBulletinRegions());
		publishedBulletins.forEach(bulletin -> bulletin.setPublicationDate(publicationDate.atZone(ZoneId.of("UTC"))));

		// update all regions to create complete maps
		for (Region region : regionController.getPublishBulletinRegions()) {
			List<AvalancheBulletin> regionBulletins = publishedBulletins.stream()
				.filter(bulletin -> bulletin.affectsRegionOnlyPublished(region)).collect(Collectors.toList());
			logger.info("Load region {} with bulletins {} and publication time {}", region.getId(),
				regionBulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()), publicationTimeString);
			AvalancheReport avalancheReport = avalancheReportController.getPublicReport(startDate, region);

			if (avalancheReport == null || regionBulletins.isEmpty()) {
				continue;
			}

			avalancheReport.setBulletins(regionBulletins, publishedBulletins);
			avalancheReport.setServerInstance(serverInstance);

			// maybe another region was not published at all
			BulletinStatus status = avalancheReport.getStatus();
			if (status != BulletinStatus.published && status != BulletinStatus.republished) {
				continue;
			}

			publicationController.createRegionResources(region, avalancheReport);

			if (regions.contains(region)) {
				reportMap.put(region, avalancheReport);
			}
		}

		// update all super regions
		Set<Region> superRegions = getSuperRegions(regions);
		for (Region region : superRegions) {
			logger.info("Publishing super region {} with bulletins {} and publication time {}", region.getId(),
				publishedBulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()), publicationTimeString);
			AvalancheReport avalancheReport = AvalancheReport.of(publishedBulletins, region, serverInstance);
			publicationController.createRegionResources(region, avalancheReport);
		}

		// copy files
		AlbinaUtil.runUpdateFilesScript(validityDateString, publicationTimeString);
		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(publishedBulletins))) {
			AlbinaUtil.runUpdateLatestFilesScript(validityDateString);
		}

		// send notifications only for updated regions after all maps were created
		if (isChange()) {
			return;
		}
		for (AvalancheReport avalancheReport : reportMap.values()) {
			publicationController.sendMessages(avalancheReport);
		}

	}

	protected User getUser(ServerInstance serverInstance) {
		String userName = serverInstance.getUserName();
		return userName != null ? UserController.getInstance().getUser(userName) : null;
	}

	private static Set<Region> getSuperRegions(List<Region> regions) {
		Set<Region> superRegions = new HashSet<>();
		for (Region region : regions) {
			for (Region superRegion : region.getSuperRegions()) {
				if (superRegions.stream().noneMatch(updateRegion -> updateRegion.getId().equals(superRegion.getId()))) {
					superRegions.add(superRegion);
				}
			}
		}
		return superRegions;
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
