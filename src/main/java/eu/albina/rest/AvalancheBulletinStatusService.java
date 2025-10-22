// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionRepository;
import eu.albina.exception.AlbinaException;
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
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Controller("/bulletins/status")
@Tag(name = "bulletins/status")
public class AvalancheBulletinStatusService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinStatusService.class);

	@Inject
	private AvalancheReportController avalancheReportController;

	@Inject
	RegionRepository regionRepository;

	@Serdeable
	public record Status(Instant date, BulletinStatus status, AvalancheReport report) {
	}

	@Get
	public List<Status> getStatus(@QueryValue("region") String regionId,
									 @QueryValue("timezone") String timezone,
									 @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
									 @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) {

		Instant startDate = DateControllerUtil.parseDateOrToday(start);
		Instant endDate = DateControllerUtil.parseDateOrToday(end);
		ZoneId zoneId = DateControllerUtil.parseTimezoneOrLocal(timezone);

		try {
			Map<Instant, BulletinStatus> status;
			if (regionId == null || regionId.isEmpty()) {
				status = avalancheReportController.getStatus(startDate, endDate,
						regionRepository.getPublishBulletinRegions());
			} else {
				status = avalancheReportController.getStatus(startDate, endDate, regionRepository.findById(regionId).orElseThrow());
			}

			return status.entrySet().stream()
				.map(entry -> new Status(entry.getKey(), entry.getValue(), null))
				.toList();
		} catch (Exception e) {
			logger.warn("Error loading status", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get("/internal")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public List<Status> getInternalStatus(@QueryValue("region") String regionId,
										  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
										  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) {

		try {
			Region region = regionRepository.findById(regionId).orElseThrow();
			Instant startDate = DateControllerUtil.parseDateOrToday(start);
			Instant endDate = DateControllerUtil.parseDateOrNull(end);

			return avalancheReportController
				.getInternalStatus(startDate, endDate, region).entrySet().stream()
				.map(entry -> new Status(entry.getKey(), entry.getValue(), null))
				.toList();
		} catch (Exception e) {
			logger.warn("Error loading status for " + regionId, e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get("/publications")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public List<Status> getPublicationsStatus(@QueryValue("region") @NotBlank String regionId,
											  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
											  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) {

		Instant startDate = DateControllerUtil.parseDateOrToday(start);
		Instant endDate = DateControllerUtil.parseDateOrNull(end);

		Region region = regionRepository.findById(regionId).orElseThrow();
		return avalancheReportController
			.getPublicationStatus(startDate, endDate, region)
			.entrySet().stream()
			.map(entry -> new Status(entry.getKey(), null, entry.getValue()))
			.toList();
	}

	@Get("/publication")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public Status getPublicationStatus(@QueryValue("region") String regionId,
										 @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date) {

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		Instant endDate = startDate;

		try {
			Map<Instant, AvalancheReport> status = avalancheReportController
					.getPublicationStatus(startDate, endDate, regionRepository.findById(regionId).orElseThrow());

			if (status.size() > 1)
				logger.warn("More than one report found!");
			else if (status.isEmpty())
				throw new AlbinaException("No publication found!");

			Entry<Instant, AvalancheReport> entry = status.entrySet().iterator().next();
			return new Status(entry.getKey(), null, entry.getValue());
		} catch (Exception e) {
			logger.warn("Error loading status", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}
