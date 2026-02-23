// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import eu.albina.controller.AvalancheBulletinController.AvalancheBulletinRepository;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionRepository;
import eu.albina.controller.publication.PublicationController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.LocalServerInstance;
import eu.albina.model.Region;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.util.GlobalVariables;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
	AvalancheBulletinRepository avalancheBulletinRepository;

	@Inject
	RegionRepository regionRepository;

	@Inject
	private GlobalVariables globalVariables;

	/**
	 * Execute all necessary tasks to publish the bulletins at 5PM, depending
	 * on the current settings.
	 */
	public synchronized void execute(PublicationStrategy strategy) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		LocalServerInstance serverInstance = globalVariables.getLocalServerInstance();
		if (!strategy.isEnabled(serverInstance)) {
			return;
		}
		Clock system = Clock.system(PublicationStrategy.localZone());
		Instant startDate = strategy.getStartDate(system);
		Instant endDate = strategy.getEndDate(system);
		Instant publicationDate = ZonedDateTime.now().withNano(0).toInstant();
		logger.info("{} triggered startDate={} endDate={} publicationDate={}", getClass().getSimpleName(), startDate, endDate, publicationDate);

		List<Region> publishBulletinRegions = regionRepository.getPublishBulletinRegions();
		List<Region> regions = Objects.requireNonNullElse(strategy.getRegions(), publishBulletinRegions).stream()
			.filter(region -> {
				AvalancheReport report = avalancheReportController.getInternalReport(startDate, region);
				BulletinStatus status = report != null ? report.getStatus() : null;
				logger.info("Internal status for region {} is {}", region.getId(), status);
				return status == BulletinStatus.submitted || status == BulletinStatus.resubmitted;
			}).collect(Collectors.toList());
		if (regions.isEmpty()) {
			logger.info("No bulletins to publish/update/change.");
			return;
		}

		final List<AvalancheBulletin> allBulletins = avalancheBulletinRepository.findByValidFromOrValidUntil(startDate, endDate);

		// publish all regions which have to be published
		for (Region region : regions) {
			logger.info("Publish bulletins for region {}", region.getId());

			for (AvalancheBulletin bulletin : allBulletins) {
				// select bulletins within the region
				if (!bulletin.affectsRegionWithoutSuggestions(region)) {
					continue;
				}
				// publish all saved regions
				Set<String> savedRegions = bulletin.getSavedRegions().stream()
					.filter(entry -> entry.startsWith(region.getId()))
					.collect(Collectors.toSet());
				if (savedRegions.isEmpty()) {
					continue;
				}
				bulletin.getSavedRegions().removeAll(savedRegions);
				bulletin.getPublishedRegions().addAll(savedRegions);
				bulletin.setPublicationDate(publicationDate.atZone(ZoneOffset.UTC));
				avalancheBulletinRepository.save(bulletin);
			}

			List<AvalancheBulletin> regionBulletins = allBulletins.stream()
				.filter(bulletin -> bulletin.affectsRegionOnlyPublished(region))
				.sorted()
				.collect(Collectors.toList());

			if (regionBulletins.isEmpty()) {
				logger.info("No published bulletins found for region {}.", region);
				return;
			}

			logger.info("Publishing region {} with bulletins {} and publication time {}", region, regionBulletins, publicationDate);

			avalancheReportController.publishReport(regionBulletins, startDate, region, publicationDate);
		}

		logger.info("Publication phase 1 done after {}", stopwatch);

		List<AvalancheReport> publishedReports = publishBulletinRegions.stream()
			.map(region -> {
				AvalancheReport avalancheReport = avalancheReportController.getPublicReport(startDate, region);
				if (avalancheReport == null) {
					logger.info("Skipping region {} since report {} is null", regions, avalancheReport);
					return null;
				}
				if (!BulletinStatus.isPublishedOrRepublished(avalancheReport.getStatus())) {
					// maybe another region was not published at all
					logger.info("Skipping region {} since report {} has status {}", region, avalancheReport, avalancheReport.getStatus());
					return null;
				}
				return avalancheReport;
			})
			.filter(Objects::nonNull)
			.toList();

		// all bulletins (aka. globalBulletins) are needed to create complete maps
		List<AvalancheBulletin> globalBulletins = avalancheReportController.mergeOrSplitBulletins(publishedReports.stream());

		for (AvalancheReport avalancheReport : publishedReports) {
			Region region = avalancheReport.getRegion();
			List<AvalancheBulletin> regionBulletins = globalBulletins.stream().filter(b -> b.affectsRegionOnlyPublished(region)).toList();
			logger.info("Load region {} with report {} and its bulletins {}", region, avalancheReport, regionBulletins);
			avalancheReport.setBulletins(regionBulletins, globalBulletins);
			avalancheReport.setServerInstance(serverInstance);
		}

		// starting from here, everything should be run async in executor, method should return
		Thread.startVirtualThread(() -> {
			Queue<Runnable> tasksAfterDirectoryUpdate = new ConcurrentLinkedDeque<>();

			Collection<Thread> phase2 = new ArrayList<>();
			for (AvalancheReport avalancheReport : publishedReports) {
				if (avalancheReport.getBulletins().isEmpty()) {
					logger.info("Skipping region {} since bulletins {} is empty", avalancheReport, avalancheReport.getBulletins());
					continue;
				}
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

			Set<Region> superRegions = publishBulletinRegions.stream().flatMap(r -> r.getSuperRegions().stream()).collect(Collectors.toSet());
			for (Region superRegion : superRegions) {
				// update all super regions (even if 'regions' is not part of the super region an aggregated warning region can affect the super region)
				phase2.add(Thread.startVirtualThread(() -> {
					List<AvalancheBulletin> regionBulletins = globalBulletins.stream()
						.filter(bulletin -> bulletin.affectsRegionOnlyPublished(superRegion)).collect(Collectors.toList());
					logger.info("Publishing super region {} with bulletins {} and publication time {}", superRegion, regionBulletins, publicationDate);
					AvalancheReport report = AvalancheReport.of(regionBulletins, superRegion, serverInstance);
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

			publicationController.createSymbolicLinks(AvalancheReport.of(globalBulletins, null, serverInstance));

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

}
