// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionRepository;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.Role;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/bulletins/status")
@Tag(name = "bulletins/status")
public class AvalancheBulletinStatusService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinStatusService.class);

	@Inject
	private AvalancheReportController avalancheReportController;

	@Inject
	private AvalancheReportController.AvalancheReportRepository avalancheReportRepository;

	@Inject
	RegionRepository regionRepository;

	@Serdeable
	public record Status(Instant date, Instant timestamp, BulletinStatus status) {
	}

	@Get
	public Collection<Status> getStatus(@QueryValue("region") String regionId,
									 @QueryValue("timezone") String timezone,
									 @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
									 @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) {

		Instant startDate = DateControllerUtil.parseDateOrToday(start);
		Instant endDate = DateControllerUtil.parseDateOrToday(end);

		try {
			List<Region> regions = regionId == null || regionId.isEmpty()
				? regionRepository.getPublishBulletinRegions()
				: List.of(regionRepository.findById(regionId).orElseThrow());
			return regions.stream()
				.flatMap(region -> avalancheReportController.getPublicReports(startDate, endDate, region).stream())
				.map(r -> new Status(r.getDate().toInstant(), r.getTimestamp().toInstant(), r.getStatus()))
				.collect(Collectors.toMap(Status::date, r -> r,
					(s1, s2) -> Stream.of(s1, s2).max(Comparator.comparing(Status::status, BulletinStatus::comparePublicationStatus)).orElseThrow()
				))
				.values();
		} catch (Exception e) {
			logger.warn("Error loading status", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get("/internal")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public Collection<Status> getInternalStatus(@QueryValue("region") String regionId,
										  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
										  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) {

		try {
			Region region = regionRepository.findById(regionId).orElseThrow();
			Instant startDate = DateControllerUtil.parseDateOrToday(start);
			Instant endDate = DateControllerUtil.parseDateOrNull(end);

			return avalancheReportRepository.findByDateBetweenAndRegion(
				startDate.atZone(ZoneOffset.UTC),
				endDate.atZone(ZoneOffset.UTC),
				region
			).stream()
				.sorted(Comparator.comparing(AvalancheReport::getTimestamp))
				.map(r -> new Status(r.getDate().toInstant(), r.getTimestamp().toInstant(), r.getStatus()))
				.collect(Collectors.toMap(Status::date, r -> r, (s1, s2) -> s2))
				.values();
		} catch (Exception e) {
			logger.warn("Error loading status for " + regionId, e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get("/publication")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public Status getPublicationStatus(@QueryValue("region") String regionId,
										 @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date) {

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		Instant endDate = startDate;

		Region region = regionRepository.findById(regionId).orElseThrow();

		Collection<AvalancheReport> reports = avalancheReportController.getPublicReports(startDate, endDate, region);

		AvalancheReport report = reports.iterator().next();
		return new Status(report.getDate().toInstant(), report.getTimestamp().toInstant(), report.getStatus());
	}
}
