// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.controller.ServerInstanceController;
import eu.albina.controller.UserController;
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
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
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
			@QueryParam("change") boolean change,
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

					@Override
					protected boolean isChange() {
						return change;
					}
				}.execute(null);
				}, "publishAllBulletins").start();
			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error publishing bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
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
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
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
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		} catch (Exception e) {
			logger.warn("Error triggering telegram channel", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@POST
	@Secured({ Role.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/whatsapp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response triggerWhatsAppChannel(@QueryParam("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("lang") LanguageCode language,
			@Context SecurityContext securityContext) {
		try {
			logger.debug("POST trigger whatsapp channel for {} in {} [{}]", regionId, language, date);
			for (MultichannelMessage posting : getMultichannelMessage(regionId, date, language)) {
				posting.sendWhatsAppMessage();
			}
			return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
		} catch (AlbinaException e) {
			logger.warn("Error triggering whatsapp channel", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		} catch (Exception e) {
			logger.warn("Error triggering whatsapp channel", e);
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
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
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
		return (language != null ? Collections.singleton(language) : region.getEnabledLanguages()).stream()
			.map(lang -> MultichannelMessage.of(avalancheReport, lang))
			.collect(Collectors.toList());
	}

}
