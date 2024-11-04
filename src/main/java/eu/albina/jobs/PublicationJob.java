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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
 */
public class PublicationJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(PublicationJob.class);
	private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("publication-pool-%d").build());

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
		RegionController regionController = RegionController.getInstance();
		ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();

		if (!isEnabled(serverInstance)) {
			return;
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

			avalancheReportController.publishReport(regionBulletins, startDate, region, user, publicationDate);
		}

		// get all published bulletins
		// FIXME set publicationDate for all bulletins (somehow a hack)
		publishedBulletins = avalancheReportController.getPublishedBulletins(startDate, regionController.getPublishBulletinRegions());
		publishedBulletins.forEach(bulletin -> bulletin.setPublicationDate(publicationDate.atZone(ZoneId.of("UTC"))));
		List<AvalancheBulletin> publishedBulletins0 = publishedBulletins;

		// update all regions to create complete maps
		regionController.getPublishBulletinRegions().stream().map(region -> CompletableFuture.runAsync(() -> {
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

			PublicationController publicationController = PublicationController.getInstance();
			publicationController.createRegionResources(region, avalancheReport);

			// send notifications only for updated regions after all maps were created
			if (regions.contains(region) && !isChange()) {
				publicationController.sendToAllChannels(avalancheReport);
			}
		}, executor)).forEach(future -> {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException ex) {
				Throwables.throwIfUnchecked(ex);
				throw new RuntimeException(ex);
			}
		});

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
		return RegionController.getInstance().getPublishBulletinRegions();
	}

}
