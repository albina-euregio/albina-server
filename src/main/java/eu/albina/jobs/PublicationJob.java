// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	private final ExecutorService executor = Executors.newCachedThreadPool(Thread.ofPlatform().name("publication-pool-").factory());

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

		List<Runnable> tasksAfterDirectoryUpdate = new ArrayList<>();

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

		Stream<CompletableFuture<Void>> futures1 = allRegions.stream().map(avalancheReport -> CompletableFuture.runAsync(() -> {
			publicationController.createRegionResources(avalancheReport.getRegion(), avalancheReport);

			// send notifications only for updated regions after all maps were created
			if (regions.contains(avalancheReport.getRegion()) && !strategy.isChange()) {
				tasksAfterDirectoryUpdate.add(() -> publicationController.sendToAllChannels(avalancheReport));
			}
		}, executor));

		// update all super regions
		Stream<CompletableFuture<Void>> futures2 = getSuperRegions(regions).stream().map(superRegion -> CompletableFuture.runAsync(() -> {
			logger.info("Publishing super region {} with bulletins {} and publication time {}", superRegion, publishedBulletins0, publicationTimeString);
			AvalancheReport report = AvalancheReport.of(publishedBulletins0, superRegion, serverInstance);
			publicationController.createRegionResources(superRegion, report);
		}, executor));

		CompletableFuture<Void> phase2 = CompletableFuture.allOf(Stream.concat(futures1, futures2).toArray(CompletableFuture[]::new));
		phase2.thenRunAsync(() -> logger.info("Publication phase 2 done after {}", stopwatch), executor);
		CompletableFuture<Void> directoryUpdate = phase2.thenRunAsync(() -> {
			createSymbolicLinks(AvalancheReport.of(publishedBulletins0, null, serverInstance));
		}, executor);

		Stream<CompletableFuture<Void>> futures3 = tasksAfterDirectoryUpdate.stream().map(taskAfterDirectoryUpdate -> directoryUpdate.thenRunAsync(taskAfterDirectoryUpdate, executor));
		CompletableFuture<Void> phase3 = CompletableFuture.allOf(futures3.toArray(CompletableFuture[]::new));
		phase3.thenRunAsync(() -> logger.info("Publication phase 3 done after {}", stopwatch), executor);

	}

	void createSymbolicLinks(AvalancheReport avalancheReport) {
		ServerInstance serverInstance = avalancheReport.getServerInstance();
		String validityDateString = avalancheReport.getValidityDateString();
		String publicationTimeString = avalancheReport.getPublicationTimeString();
		try {
			createSymbolicLinks(
				Paths.get(serverInstance.getPdfDirectory(), validityDateString, publicationTimeString),
				Paths.get(serverInstance.getPdfDirectory(), validityDateString)
			);
			if (avalancheReport.isLatest()) {
				createSymbolicLinks(
					Paths.get(serverInstance.getPdfDirectory(), validityDateString, publicationTimeString),
					Paths.get(serverInstance.getPdfDirectory(), "latest")
				);
				stripDateFromFilenames(Paths.get(serverInstance.getPdfDirectory(), "latest"), validityDateString);
				createSymbolicLinks(
					Paths.get(serverInstance.getHtmlDirectory(), validityDateString),
					Paths.get(serverInstance.getHtmlDirectory())
				);
			}
		} catch (IOException e) {
			logger.error("Failed to create symbolic links", e);
			throw new UncheckedIOException(e);
		}
	}

	void createSymbolicLinks(Path fromDirectory, Path toDirectory) throws IOException {
		// clean target directory
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(toDirectory)) {
			for (Path path : stream) {
				if (Files.isDirectory(path)) {
					continue;
				}
				logger.info("Removing existing file {}", path);
				Files.delete(path);
			}
		}
		// create symbolic links
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(fromDirectory)) {
			for (Path path : stream) {
				if (Files.isDirectory(path)) {
					continue;
				}
				Path link = toDirectory.resolve(path.getFileName());
				Path target = toDirectory.relativize(path);
				logger.info("Creating symbolic link {} to {}", link, target);
				Files.createSymbolicLink(link, target);
			}
		}
	}

	void stripDateFromFilenames(Path directory, String validityDateString) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, validityDateString + "*")) {
			for (Path path : stream) {
				Path target = path.resolveSibling(path.getFileName().toString().substring(validityDateString.length() + 1));
				logger.info("Renaming file {} to {}", path, target);
				Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
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
