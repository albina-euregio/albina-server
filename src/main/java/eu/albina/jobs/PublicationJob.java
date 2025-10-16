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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.albina.controller.RegionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.publication.PublicationController;
import eu.albina.model.AbstractPersistentObject;
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
public class PublicationJob {

	private static final Logger logger = LoggerFactory.getLogger(PublicationJob.class);
	private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("publication-pool-%d").build());
	private final PublicationController publicationController;
	private final AvalancheReportController avalancheReportController;
	private final AvalancheBulletinController avalancheBulletinController;
	protected final RegionRepository regionRepository;
	private final ServerInstance serverInstance;

	public PublicationJob(PublicationController publicationController, AvalancheReportController avalancheReportController, AvalancheBulletinController avalancheBulletinController, RegionRepository regionRepository, ServerInstance serverInstance) {
		this.publicationController = publicationController;
		this.avalancheReportController = avalancheReportController;
		this.avalancheBulletinController = avalancheBulletinController;
		this.regionRepository = regionRepository;
		this.serverInstance = serverInstance;
	}

	@Transactional
	private List<Runnable> execute0() {
		if (!isEnabled(serverInstance)) {
			return null;
		}
		Clock system = Clock.system(AlbinaUtil.localZone());
		Instant startDate = getStartDate(system);
		Instant endDate = getEndDate(system);
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
			return null;
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
			return null;
		}

		publishedBulletins = publishedBulletins.stream()
			.filter(avalancheBulletin -> avalancheBulletin.getPublishedRegions() != null
				&& !avalancheBulletin.getPublishedRegions().isEmpty())
			.collect(Collectors.toList());
		if (publishedBulletins.isEmpty()) {
			logger.info("No published regions found in bulletins.");
			return null;
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
			logger.info("Publishing region {} with bulletins {} and publication time {}", region.getId(),
				regionBulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()),
				publicationTimeString);

			avalancheReportController.publishReport(regionBulletins, startDate, region, serverInstance.getUserName(), publicationDate);
		}

		// get all published bulletins
		// FIXME set publicationDate for all bulletins (somehow a hack)
		publishedBulletins = avalancheReportController.getPublishedBulletins(startDate, regionRepository.getPublishBulletinRegions());
		publishedBulletins.forEach(bulletin -> bulletin.setPublicationDate(publicationDate.atZone(ZoneId.of("UTC"))));
		List<AvalancheBulletin> publishedBulletins0 = publishedBulletins;

		List<Runnable> tasksAfterDirectoryUpdate = new ArrayList<>();

		// update all regions to create complete maps
		Stream<CompletableFuture<Void>> futures1 = regionRepository.getPublishBulletinRegions().stream().map(region -> CompletableFuture.runAsync(() -> {
			List<AvalancheBulletin> regionBulletins = publishedBulletins0.stream()
				.filter(bulletin -> bulletin.affectsRegionOnlyPublished(region)).collect(Collectors.toList());
			logger.info("Load region {} with bulletins {} and publication time {}", region.getId(),
				regionBulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()), publicationTimeString);
			AvalancheReport avalancheReport = avalancheReportController.getPublicReport(startDate, region);

			if (avalancheReport == null || regionBulletins.isEmpty()) {
				return;
			}

			avalancheReport.setBulletins(regionBulletins, publishedBulletins0);
			avalancheReport.setServerInstance(serverInstance);

			// maybe another region was not published at all
			BulletinStatus status = avalancheReport.getStatus();
			if (status != BulletinStatus.published && status != BulletinStatus.republished) {
				return;
			}

			publicationController.createRegionResources(region, avalancheReport);

			// send notifications only for updated regions after all maps were created
			if (regions.contains(region) && !isChange()) {
				tasksAfterDirectoryUpdate.add(() -> publicationController.sendToAllChannels(avalancheReport));
			}
		}, executor));

		// update all super regions
		Stream<CompletableFuture<Void>> futures2 = getSuperRegions(regions).stream().map(superRegion -> CompletableFuture.runAsync(() -> {
			logger.info("Publishing super region {} with bulletins {} and publication time {}", superRegion.getId(),
				publishedBulletins0.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()), publicationTimeString);
			AvalancheReport report = AvalancheReport.of(publishedBulletins0, superRegion, serverInstance);
			publicationController.createRegionResources(superRegion, report);
		}, executor));

		Stream.concat(futures1, futures2).forEach(PublicationJob::await);

		try {
			createSymbolicLinks(
				Paths.get(serverInstance.getPdfDirectory(), validityDateString, publicationTimeString),
				Paths.get(serverInstance.getPdfDirectory(), validityDateString)
			);
			if (AvalancheReport.of(publishedBulletins, null, null).isLatest()) {
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
		return tasksAfterDirectoryUpdate;
	}

	/**
	 * Execute all necessary tasks to publish the bulletins at 5PM, depending
	 * on the current settings.
	 */
	public void execute() {
		List<Runnable> tasksAfterDirectoryUpdate;

		synchronized (PublicationJob.class) {
			tasksAfterDirectoryUpdate = execute0();
			if (tasksAfterDirectoryUpdate == null) {
				return;
			}
		}

		tasksAfterDirectoryUpdate.stream()
			.map(CompletableFuture::runAsync)
			.forEach(PublicationJob::await);
	}

	private static void await(CompletableFuture<Void> future) {
		try {
			future.get();
		} catch (InterruptedException | ExecutionException ex) {
			Throwables.throwIfUnchecked(ex);
			throw new RuntimeException(ex);
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

	protected boolean isEnabled(ServerInstance serverInstance) {
		return serverInstance.isPublishAt5PM();
	}

	protected boolean isChange() {
		return false;
	}

	protected Instant getStartDate(Clock clock) {
		return ZonedDateTime.of(
			LocalDate.now(clock),
			AlbinaUtil.validityStart(),
			clock.getZone()
		).toInstant();
	}

	protected Instant getEndDate(Clock clock) {
		return getStartDate(clock).atZone(clock.getZone()).plusDays(1).toInstant();
	}

	protected List<Region> getRegions() {
		return regionRepository.getPublishBulletinRegions();
	}

}
