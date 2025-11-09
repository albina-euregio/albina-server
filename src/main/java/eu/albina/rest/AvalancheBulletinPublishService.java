// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.publication.PublicationController;
import eu.albina.controller.RegionRepository;
import eu.albina.controller.ServerInstanceRepository;
import eu.albina.controller.UserRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.exception.AlbinaException;
import eu.albina.jobs.PublicationJob;
import eu.albina.jobs.UpdateJob;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.User;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/bulletins/publish")
@Tag(name = "bulletins/publish")
public class AvalancheBulletinPublishService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinPublishService.class);

	@Inject
	PublicationController publicationController;

	@Inject
	AvalancheReportController avalancheReportController;

	@Inject
	AvalancheBulletinController avalancheBulletinController;

	@Inject
	RegionRepository regionRepository;

	@Inject
	private ServerInstanceRepository serverInstanceRepository;

	@Inject
	private UserRepository userRepository;

	@PersistenceContext
	EntityManager entityManager;

	/**
	 * Publish a major update to an already published bulletin (not at 5PM nor 8AM).
	 */
	@Post
	@Secured(Role.Str.FORECASTER)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Transactional
	public void publishBulletins(@QueryValue("region") String regionId,
											@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
											Principal principal) {

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);

			User user = userRepository.findByIdOrElseThrow(principal);
			Region region = regionRepository.findById(regionId).orElseThrow();
			List<Region> regions = Stream.concat(
				Stream.of(region),
				region.getSuperRegions().stream().filter(Region::isPublishBulletins)
			).distinct().collect(Collectors.toList());

			if (user.hasPermissionForRegion(region.getId())) {
				new UpdateJob(publicationController, avalancheReportController, avalancheBulletinController, regionRepository, serverInstanceRepository.getLocalServerInstance(), entityManager) {
					@Override
					protected boolean isEnabled(ServerInstance serverInstance) {
						return true;
					}

					@Override
					protected Instant getStartDate(Clock clock) {
						return startDate;
					}

					@Override
					protected List<Region> getRegions() {
						return regions;
					}
				}.execute();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error publishing bulletins", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	/**
	 * Publish a major update to an already published bulletin (not at 5PM nor 8AM).
	 */
	@Post("/all")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Transactional
	public void publishAllBulletins(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
			@QueryValue("change") boolean change) {

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			new Thread(() -> {
				new PublicationJob(publicationController, avalancheReportController, avalancheBulletinController, regionRepository, serverInstanceRepository.getLocalServerInstance(), entityManager) {
					@Override
					protected boolean isEnabled(ServerInstance serverInstance) {
						return true;
					}

					@Override
					protected Instant getStartDate(Clock clock) {
						return startDate;
					}

					@Override
					protected boolean isChange() {
						return change;
					}
				}.execute();
				}, "publishAllBulletins").start();
		} catch (AlbinaException e) {
			logger.warn("Error publishing bulletins", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/email")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Transactional
	public void sendEmail(@QueryValue("region") String regionId,
							  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
							  @QueryValue("lang") LanguageCode language) {
		try {
			for (MultichannelMessage posting : getMultichannelMessage(regionId, date, language)) {
				posting.sendMails(publicationController);
			}
		} catch (Exception e) {
			logger.warn("Error sending emails", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/telegram")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Transactional
	public void triggerTelegramChannel(@QueryValue("region") String regionId,
										   @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
										   @QueryValue("lang") LanguageCode language) {
		try {
			for (MultichannelMessage posting : getMultichannelMessage(regionId, date, language)) {
				posting.sendTelegramMessage(publicationController);
			}
		} catch (Exception e) {
			logger.warn("Error triggering telegram channel", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/whatsapp")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Transactional
	public void triggerWhatsAppChannel(@QueryValue("region") String regionId,
										   @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
										   @QueryValue("lang") LanguageCode language) {
		try {
			for (MultichannelMessage posting : getMultichannelMessage(regionId, date, language)) {
				posting.sendWhatsAppMessage(publicationController);
			}
		} catch (Exception e) {
			logger.warn("Error triggering whatsapp channel", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/push")
	@Secured(Role.Str.ADMIN)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Transactional
	public void triggerPushNotifications(@QueryValue("region") String regionId,
											 @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
											 @QueryValue("lang") LanguageCode language) {
		try {
			for (MultichannelMessage posting : getMultichannelMessage(regionId, date, language)) {
				posting.sendPushNotifications(publicationController);
			}
		} catch (Exception e) {
			logger.warn("Error triggering push notifications", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	private List<MultichannelMessage> getMultichannelMessage(String regionId, String date, LanguageCode language) throws AlbinaException {
		Region region = regionRepository.findById(regionId).orElseThrow();
		Instant startDate = DateControllerUtil.parseDateOrThrow(date);
		ArrayList<AvalancheBulletin> bulletins = avalancheReportController
			.getPublishedBulletins(startDate, Collections.singletonList(region));
		AvalancheReport avalancheReport = avalancheReportController.getInternalReport(startDate, region);
		avalancheReport.setBulletins(bulletins);
		avalancheReport.setServerInstance(serverInstanceRepository.getLocalServerInstance());
		return (language != null ? Collections.singleton(language) : region.getEnabledLanguages()).stream()
			.map(lang -> MultichannelMessage.of(avalancheReport, lang))
			.collect(Collectors.toList());
	}

}
