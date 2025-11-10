// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;
import eu.albina.controller.RegionRepository;
import eu.albina.controller.ServerInstanceRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.publication.PublicationController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.util.AlbinaUtil;

/**
 * A job handling all the tasks and logic necessary to
 * automatically publish the bulletins at 5PM.
 *
 * @author Norbert Lanzanasto
 */
@Singleton
public class PublicationJob {

	private static final Logger logger = LoggerFactory.getLogger(PublicationJob.class);

	@Inject
	PublicationController publicationController;

	@Inject
	AvalancheReportController avalancheReportController;

	@Inject
	AvalancheBulletinController avalancheBulletinController;

	@Inject
	RegionRepository regionRepository;

	@Inject
	ServerInstanceRepository serverInstanceRepository;

	/**
	 * Execute all necessary tasks to publish the bulletins at 5PM, depending
	 * on the current settings.
	 */
	public synchronized void execute(PublicationStrategy strategy) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		ServerInstance serverInstance = serverInstanceRepository.getLocalServerInstance();
		if (!strategy.isEnabled(serverInstance)) {
			return;
		}
		Clock system = Clock.system(AlbinaUtil.localZone());
		Instant startDate = strategy.getStartDate(system);
		Instant endDate = strategy.getEndDate(system);
		Instant publicationDate = AlbinaUtil.getInstantNowNoNanos();
		logger.info("{} triggered startDate={} endDate={} publicationDate={}", getClass().getSimpleName(), startDate, endDate, publicationDate);

		List<Region> regions = Objects.requireNonNullElseGet(strategy.getRegions(), () -> regionRepository.getPublishBulletinRegions()).stream()
			.filter(region -> {
				BulletinStatus status = avalancheReportController.getInternalStatusForDay(startDate, region);
				logger.info("Internal status for region {} is {}", region.getId(), status);
				return status == BulletinStatus.submitted || status == BulletinStatus.resubmitted;
			}).collect(Collectors.toList());
		if (regions.isEmpty()) {
			logger.info("No bulletins to publish/update/change.");
			return;
		}

		for (Region region : regions) {
			logger.info("Publish bulletins for region {}", region.getId());
			BulletinStatus internalStatus = avalancheReportController.getInternalStatusForDay(startDate, region);

			logger.info("Internal status for region {} is {}", region.getId(), internalStatus);

			if (internalStatus == BulletinStatus.submitted || internalStatus == BulletinStatus.resubmitted) {
				avalancheBulletinController.publishBulletins(startDate, endDate, region, publicationDate, serverInstance.getUserName());
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
		String validityDateString = AvalancheReport.of(publishedBulletins, null, serverInstance).getValidityDateString();
		String publicationTimeString = AvalancheReport.of(publishedBulletins, null, serverInstance).getPublicationTimeString();

		Collections.sort(publishedBulletins);

		// publish all regions which have to be published
		for (Region region : regions) {
			List<AvalancheBulletin> regionBulletins = publishedBulletins.stream()
				.filter(bulletin -> bulletin.affectsRegionOnlyPublished(region))
				.collect(Collectors.toList());
			logger.info("Publishing region {} with bulletins {} and publication time {}", region, regionBulletins, publicationTimeString);

			avalancheReportController.publishReport(regionBulletins, startDate, region, serverInstance.getUserName(), publicationDate);
		}

		// get all published bulletins
		// FIXME set publicationDate for all bulletins (somehow a hack)
		publishedBulletins = avalancheReportController.getPublishedBulletins(startDate, regionRepository.getPublishBulletinRegions());
		publishedBulletins.forEach(bulletin -> bulletin.setPublicationDate(publicationDate.atZone(ZoneId.of("UTC"))));
		List<AvalancheBulletin> publishedBulletins0 = publishedBulletins;

		logger.info("Publication phase 1 done after {}", stopwatch);

		// update all regions to create complete maps
		List<AvalancheReport> allRegions = regionRepository.getPublishBulletinRegions().stream().flatMap(region -> {
			List<AvalancheBulletin> regionBulletins = publishedBulletins0.stream()
				.filter(bulletin -> bulletin.affectsRegionOnlyPublished(region)).collect(Collectors.toList());
			logger.info("Load region {} with bulletins {} and publication time {}", region, regionBulletins, publicationTimeString);
			if (regionBulletins.isEmpty()) {
				logger.info("Skipping region {} since bulletins {} is empty", region, regionBulletins);
				return Stream.empty();
			}
			AvalancheReport avalancheReport = avalancheReportController.getPublicReport(startDate, region);
			logger.info("Load region {} with report {}", region, avalancheReport);
			if (avalancheReport == null) {
				logger.info("Skipping region {} since report {} is null", region, avalancheReport);
				return Stream.empty();
			}
			BulletinStatus status = avalancheReport.getStatus();
			if (status != BulletinStatus.published && status != BulletinStatus.republished) {
				// maybe another region was not published at all
				logger.info("Skipping region {} since report {} has status {}", region, avalancheReport, status);
				return Stream.empty();
			}
			avalancheReport.setBulletins(regionBulletins, publishedBulletins0);
			avalancheReport.setServerInstance(serverInstance);
			return Stream.of(avalancheReport);
		}).toList();

		// starting from here, everything should be run async in executor, method should return

		Thread.startVirtualThread(() -> {
			Queue<Runnable> tasksAfterDirectoryUpdate = new ConcurrentLinkedDeque<>();

			Collection<Thread> phase2 = new ArrayList<>();
			for (AvalancheReport avalancheReport : allRegions) {
				phase2.add(Thread.startVirtualThread(() -> {
					logger.info("Creating resources for {}", avalancheReport);
					publicationController.createRegionResources(avalancheReport.getRegion(), avalancheReport);

					if (strategy.isChange()) {
						logger.info("Skipping sendToAllChannels since publication isChange");
						return;
					}
					if (!regions.contains(avalancheReport.getRegion())) {
						logger.info("Skipping sendToAllChannels since report {} is not part of {}", avalancheReport, regions);
						return;
					}
					// send notifications only for updated regions after all maps were created
					logger.info("Scheduling sendToAllChannels for {}", avalancheReport);
					tasksAfterDirectoryUpdate.add(() -> publicationController.sendToAllChannels(avalancheReport));
				}));
			}
			for (Region superRegion : getSuperRegions(regions)) {
				// update all super regions
				phase2.add(Thread.startVirtualThread(() -> {
					logger.info("Publishing super region {} with bulletins {} and publication time {}", superRegion, publishedBulletins0, publicationTimeString);
					AvalancheReport report = AvalancheReport.of(publishedBulletins0, superRegion, serverInstance);
					publicationController.createRegionResources(superRegion, report);
				}));
			}

			for (Thread thread : phase2) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			logger.info("Publication phase 2 done after {}", stopwatch);

			publicationController.createSymbolicLinks(AvalancheReport.of(publishedBulletins0, null, serverInstance));

			Collection<Thread> phase3 = new ArrayList<>();
			for (Runnable runnable : tasksAfterDirectoryUpdate) {
				phase3.add(Thread.startVirtualThread(runnable));
			}

			for (Thread thread : phase3) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			logger.info("Publication phase 3 done after {}", stopwatch);
		});
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

}
