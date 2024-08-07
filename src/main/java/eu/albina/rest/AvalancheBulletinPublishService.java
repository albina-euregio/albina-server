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
package eu.albina.rest;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.RegionController;
import eu.albina.controller.ServerInstanceController;
import eu.albina.controller.UserController;
import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.exception.AlbinaException;
import eu.albina.jobs.PublicationJob;
import eu.albina.jobs.UpdateJob;
import eu.albina.model.AbstractPersistentObject;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.User;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.PdfUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/bulletins/publish")
@Tag(name = "bulletins/publish")
public class AvalancheBulletinPublishService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinPublishService.class);

	/**
	 * Publish a major update to an already published bulletin (not at 5PM nor 8AM).
	 *
	 * @param regionId
	 *            The region to publish the bulletins for.
	 * @param date
	 *            The date to publish the bulletins for.
	 * @param securityContext
	 * @return
	 */
	@POST
	@Secured({ Role.FORECASTER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response publishBulletins(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST publish bulletins");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			List<Region> regions = Stream.concat(
				Stream.of(region),
				region.getSuperRegions().stream().filter(Region::isPublishBulletins)
			).distinct().collect(Collectors.toList());

			if (user.hasPermissionForRegion(region.getId())) {
				new UpdateJob() {
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
				}.execute(null);

				return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error publishing bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	/**
	 * Publish a major update to an already published bulletin (not at 5PM nor 8AM).
	 *
	 * @param date
	 *            The date to publish the bulletins for.
	 * @param securityContext
	 * @return
	 */
	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response publishAllBulletins(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST publish all bulletins");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			new Thread(() -> {
				new PublicationJob() {
					@Override
					protected boolean isEnabled(ServerInstance serverInstance) {
						return true;
					}

					@Override
					protected Instant getStartDate(Clock clock) {
						return startDate;
					}
				}.execute(null);
				}, "publishAllBulletins").start();
			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error publishing bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/pdf")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPdf(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create PDF [{}]", date);

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			List<Region> publishBulletinRegions = RegionController.getInstance().getPublishBulletinRegions();
			Collection<AvalancheBulletin> result = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, publishBulletinRegions);

			List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
			for (AvalancheBulletin b : result)
				bulletins.add(b);

			Collections.sort(bulletins);

			String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTimeString = AlbinaUtil.getPublicationDateDirectory(bulletins);
			ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

			// update all super regions
			Set<Region> regions = new HashSet<Region>(publishBulletinRegions);
			for (Region region : publishBulletinRegions) {
				for (Region superRegion : region.getSuperRegions()) {
					if (regions.stream().noneMatch(updateRegion -> updateRegion.getId().equals(superRegion.getId())))
						regions.add(superRegion);
				}
			}
			for (Region region : regions) {
				try {
					logger.info("PDF production for {} started", region.getId());
					List<AvalancheBulletin> regionBulletins = bulletins.stream().filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region)).collect(Collectors.toList());
					AvalancheReport avalancheReport = AvalancheReport.of(regionBulletins, region, localServerInstance);
					PdfUtil.createRegionPdfs(avalancheReport);
				} finally {
					logger.info("PDF production {} finished", region.getId());
				}
			}

			// copy files
			AlbinaUtil.runUpdatePdfsScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestPdfsScript(validityDateString);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating PDFs", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/html")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createHtml(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create HTML [{}]", date);

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			List<Region> publishBulletinRegions = RegionController.getInstance().getPublishBulletinRegions();
			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, publishBulletinRegions);

			// update all super regions
			Set<Region> regions = new HashSet<Region>(publishBulletinRegions);
			for (Region region : publishBulletinRegions) {
				for (Region superRegion : region.getSuperRegions()) {
					if (regions.stream().noneMatch(updateRegion -> updateRegion.getId().equals(superRegion.getId())))
						regions.add(superRegion);
				}
			}
			for (Region region : regions) {
				AvalancheReport avalancheReport = AvalancheReportController.getInstance().getInternalReport(startDate, region);
				List<AvalancheBulletin> regionBulletins = bulletins.stream().filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region)).collect(Collectors.toList());
				avalancheReport.setBulletins(regionBulletins);
				avalancheReport.setServerInstance(ServerInstanceController.getInstance().getLocalServerInstance());
				PublicationController.getInstance().createSimpleHtml(avalancheReport);
			}

			// copy files
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestHtmlsScript(AlbinaUtil.getValidityDateString(bulletins));

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating HTMLs", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/map")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createMap(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create map [{}]", date);

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			List<Region> publishBulletinRegions = RegionController.getInstance().getPublishBulletinRegions();
			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, publishBulletinRegions);

			String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTimeString = AlbinaUtil.getPublicationDateDirectory(bulletins);
			ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

			// update all super regions
			Set<Region> regions = new HashSet<Region>(publishBulletinRegions);
			for (Region region : publishBulletinRegions) {
				for (Region superRegion : region.getSuperRegions()) {
					if (regions.stream().noneMatch(updateRegion -> updateRegion.getId().equals(superRegion.getId())))
						regions.add(superRegion);
				}
			}
			for (Region region: regions) {
				try {
					AvalancheReport avalancheReport = AvalancheReportController.getInstance().getPublicReport(startDate, region);
					List<AvalancheBulletin> regionBulletins = bulletins.stream().filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region)).collect(Collectors.toList());
					avalancheReport.setBulletins(regionBulletins, bulletins);
					avalancheReport.setServerInstance(localServerInstance);
					PublicationController.getInstance().createMaps(avalancheReport);
				} catch (Exception e1) {
					logger.error("Error during map production for " + region.getId(), e1);
				}
			}

			// copy files
			AlbinaUtil.runUpdateMapsScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestMapsScript(validityDateString);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating maps", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/caaml")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createCaaml(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@Context SecurityContext securityContext) {
		logger.debug("POST create caaml [{}]", date);

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			List<Region> publishBulletinRegions = RegionController.getInstance().getPublishBulletinRegions();
			ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
					.getPublishedBulletins(startDate, publishBulletinRegions);

			String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
			String publicationTimeString = AlbinaUtil.getPublicationDateDirectory(bulletins);
			ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

			// update all super regions
			Set<Region> regions = new HashSet<Region>(publishBulletinRegions);
			for (Region region : publishBulletinRegions) {
				for (Region superRegion : region.getSuperRegions()) {
					if (regions.stream().noneMatch(updateRegion -> updateRegion.getId().equals(superRegion.getId())))
						regions.add(superRegion);
				}
			}
			for (Region region: regions) {

				List<AvalancheBulletin> regionBulletins = bulletins.stream().filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region)).collect(Collectors.toList());
				logger.info("Creating CAAML for region {} with bulletins {}", region.getId(), regionBulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()));

				AvalancheReport avalancheReport = AvalancheReportController.getInstance().getPublicReport(startDate, region);
				avalancheReport.setBulletins(regionBulletins, bulletins);
				avalancheReport.setServerInstance(localServerInstance);

				if (regionBulletins.isEmpty()) {
					continue;
				}

				PublicationController.getInstance().createCaamlV5(avalancheReport);
				PublicationController.getInstance().createCaamlV6(avalancheReport);
				PublicationController.getInstance().createJson(avalancheReport);
			}

			// copy files
			AlbinaUtil.runUpdateCaamlsScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestCaamlsScript(validityDateString);

			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error creating CAAML", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/email")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendEmail(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("lang") LanguageCode language,
			@Context SecurityContext securityContext) {
		try {
			logger.debug("POST send emails for {} in {} [{}]", regionId, language, date);
			for (MultichannelMessage posting : getMultichannelMessage(regionId, date, language)) {
				posting.sendMails();
			}
			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error sending emails", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		} catch (Exception e) {
			logger.warn("Error sending emails", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/telegram")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response triggerTelegramChannel(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("lang") LanguageCode language,
			@Context SecurityContext securityContext) {
		try {
			logger.debug("POST trigger telegram channel for {} in {} [{}]", regionId, language, date);
			for (MultichannelMessage posting : getMultichannelMessage(regionId, date, language)) {
				posting.sendTelegramMessage();
			}
			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error triggering telegram channel", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		} catch (Exception e) {
			logger.warn("Error triggering telegram channel", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/push")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response triggerPushNotifications(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("lang") LanguageCode language,
			@Context SecurityContext securityContext) {
		try {
			logger.debug("POST trigger push notifications for {} in {} [{}]", regionId, language, date);
			for (MultichannelMessage posting : getMultichannelMessage(regionId, date, language)) {
				posting.sendPushNotifications();
			}
			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error triggering push notifications", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	private static List<MultichannelMessage> getMultichannelMessage(String regionId, String date, LanguageCode language) throws AlbinaException {
		Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
		Instant startDate = DateControllerUtil.parseDateOrThrow(date);
		ArrayList<AvalancheBulletin> bulletins = AvalancheReportController.getInstance()
			.getPublishedBulletins(startDate, Collections.singletonList(region));
		AvalancheReport avalancheReport = AvalancheReportController.getInstance().getInternalReport(startDate, region);
		avalancheReport.setBulletins(bulletins);
		avalancheReport.setServerInstance(ServerInstanceController.getInstance().getLocalServerInstance());
		return (language != null ? Collections.singleton(language) : LanguageCode.ENABLED).stream()
			.map(lang -> MultichannelMessage.of(avalancheReport, lang))
			.collect(Collectors.toList());
	}

}
