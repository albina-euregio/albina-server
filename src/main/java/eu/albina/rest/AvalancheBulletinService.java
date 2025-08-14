// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;
import eu.albina.controller.ServerInstanceController;
import eu.albina.model.ServerInstance;
import eu.albina.caaml.Caaml;
import eu.albina.util.HibernateUtil;
import eu.albina.util.JsonUtil;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.persistence.EntityManager;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.common.base.MoreObjects;

import eu.albina.caaml.CaamlVersion;
import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.jobs.ChangeJob;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.BulletinLock;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.GlobalVariables;
import eu.albina.map.MapUtil;
import eu.albina.util.PdfUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@Path("/bulletins")
@Tag(name = "bulletins")
@OpenAPIDefinition(info = @Info(
	title = "albina-server",
	version = "0.0",
	description = "Server component to compose and publish multilingual avalanche bulletins",
	license = @License(name = "GNU General Public License v3.0", url = "https://gitlab.com/albina-euregio/albina-server/-/blob/master/LICENSE"),
	contact = @Contact(name = "avalanche.report", url = "https://avalanche.report/", email = "info@avalanche.report")
), servers = {@Server(url = "/albina/api")})
public class AvalancheBulletinService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/edit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))))
	@Operation(summary = "Get bulletins for date")
	public Response getJSONBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("regions") List<String> regionIds) {

		List<Region> regions = new ArrayList<Region>();
		regionIds.forEach(regionId -> {
			try {
				Region region = RegionController.getInstance().getRegion(regionId);
				regions.add(region);
			} catch (HibernateException e) {
				logger.warn("No region with ID: " + regionId);
			}
		});
		return HibernateUtil.getInstance().run(entityManager -> getJSONBulletins(date, regions, entityManager));
	}

	@GET
	@Secured({Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/edit/caaml/json")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get bulletins for date as CAAML JSON")
	public Response getCaamlJsonBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("regions") List<String> regionIds,
		@QueryParam("lang") LanguageCode language,
		@QueryParam("version") CaamlVersion version) {

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		Instant endDate = startDate.plus(1, ChronoUnit.DAYS);
		List<Region> regions = RegionController.getInstance().getRegionsOrBulletinRegions(regionIds);
		ZonedDateTime publicationDate = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		try {
			List<AvalancheBulletin> bulletins = HibernateUtil.getInstance().runTransaction(entityManager ->
				AvalancheBulletinController.getInstance().getBulletins(startDate, endDate, regions, entityManager));
			bulletins.forEach(b -> b.setPublicationDate(publicationDate));
			bulletins.forEach(b -> b.setPublishedRegions(b.getPublishedAndSavedRegions()));
			AvalancheReport avalancheReport = AvalancheReport.of(bulletins, null, ServerInstanceController.getInstance().getLocalServerInstance());
			avalancheReport.setStatus(BulletinStatus.draft);
			return makeCAAML(avalancheReport, language, MoreObjects.firstNonNull(version, CaamlVersion.V6_JSON));
		} catch (RuntimeException e) {
			logger.warn("Error loading bulletins", e);
			return Response.noContent().build();
		}
	}

	private Response getJSONBulletins(String date, List<Region> regions, EntityManager entityManager) {
		logger.debug("GET JSON bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

		if (regions.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance().getBulletins(startDate, endDate, regions, entityManager);
		Collections.sort(bulletins);
		String jsonResult = JsonUtil.writeValueUsingJackson(bulletins, JsonUtil.Views.Internal.class);
		return Response.ok(jsonResult, MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Operation(deprecated = true)
	public Response getPublishedXMLBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("region") String regionId,
		@QueryParam("lang") LanguageCode language,
		@QueryParam("version") CaamlVersion version) {
		List<String> regionIds = regionId != null ? Collections.singletonList(regionId) : Collections.emptyList();
		return getPublishedCaamlBulletins(date, regionIds, language, version);
	}

	private static Response makeCAAML(AvalancheReport avalancheReport, LanguageCode language, CaamlVersion version) {
		String caaml = Caaml.createCaaml(avalancheReport, MoreObjects.firstNonNull(language, LanguageCode.en), MoreObjects.firstNonNull(version, CaamlVersion.V5));
		if (caaml != null) {
			final String type = version != CaamlVersion.V6_JSON ? MediaType.APPLICATION_XML : MediaType.APPLICATION_JSON;
			return Response.ok(caaml, type).build();
		} else {
			logger.debug("No bulletins for this request.");
			return Response.noContent().build();
		}
	}

	@GET
	@Path("/caaml")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Operation(summary = "Get published bulletins for date as CAAML XML")
	public Response getPublishedCaamlBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("regions") List<String> regionIds,
		@QueryParam("lang") LanguageCode language,
		@QueryParam("version") CaamlVersion version) {
		logger.debug("GET published XML bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		List<Region> regions = RegionController.getInstance().getRegionsOrBulletinRegions(regionIds);

		try {
			AvalancheReport avalancheReport = AvalancheReport.of(
				AvalancheReportController.getInstance().getPublishedBulletins(startDate, regions), null,
				ServerInstanceController.getInstance().getLocalServerInstance());
			return makeCAAML(avalancheReport, language, version);
		} catch (RuntimeException e) {
			logger.warn("Error loading bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_XML).build();
		}
	}

	@GET
	@Path("/caaml/json")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get published bulletins for date as CAAML JSON")
	public Response getPublishedCaamlJsonBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("regions") List<String> regionIds,
		@QueryParam("lang") LanguageCode language,
		@QueryParam("version") CaamlVersion version) {
		return getPublishedCaamlBulletins(date, regionIds, language, MoreObjects.firstNonNull(version, CaamlVersion.V6_JSON));
	}

	static class LatestBulletin {
		public Instant date;
	}

	@GET
	@Path("/latest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "latest", content = @Content(schema = @Schema(implementation = LatestBulletin.class)))
	@Operation(summary = "Get latest bulletin date")
	public Response getLatest() {
		logger.debug("GET latest date");
		try {
			LatestBulletin json = new LatestBulletin();
			json.date = AvalancheReportController.getInstance().getLatestDate();

			return Response.ok(json, MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading latest date", e);
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toString()).build();
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))))
	@Operation(deprecated = true)
	public Response getPublishedJSONBulletins0(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("regions") List<String> regionIds, @QueryParam("lang") LanguageCode language) {
		return getPublishedJSONBulletins(date, regionIds, language);
	}

	private final RateLimiter pdfRateLimiter = RateLimiter.create(2.0); // allow 2 PDFs per second

	@GET
	@Path("/pdf")
	@Produces(PdfUtil.MEDIA_TYPE)
	@Operation(summary = "Get published bulletins as PDF")
	public Response getPublishedBulletinsAsPDF(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("region") String regionId,
		@QueryParam("grayscale") boolean grayscale,
		@QueryParam("lang") LanguageCode language) {

		Stopwatch stopwatch = Stopwatch.createStarted();
		logger.info("Get published bulletins as PDF {}", regionId);
		try {
			pdfRateLimiter.acquire();
			Instant startDate = DateControllerUtil.parseDateOrToday(date);
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			List<AvalancheBulletin> bulletins = AvalancheReportController.getInstance().getPublishedBulletins(startDate, List.of(region));
			ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
			serverInstance.setPdfDirectory(GlobalVariables.getTmpPdfDirectory());
			AvalancheReport avalancheReport = AvalancheReport.of(bulletins, region, serverInstance);
			java.nio.file.Path pdf = new PdfUtil(avalancheReport, language, grayscale).createPdf();
			return Response.ok(pdf.toFile(), PdfUtil.MEDIA_TYPE).build();
		} catch (Exception e) {
			logger.warn("Error creating PDF", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		} finally {
			logger.info("Get published bulletin as PDF {} took {}", regionId, stopwatch);
		}
	}

	@GET
	@Path("/{bulletinId}/pdf")
	@Produces(PdfUtil.MEDIA_TYPE)
	@Operation(summary = "Get published bulletin as PDF")
	public Response getPublishedBulletinAsPDF(
		@PathParam("bulletinId") String bulletinId,
		@QueryParam("region") String regionId,
		@QueryParam("grayscale") boolean grayscale,
		@QueryParam("lang") LanguageCode language) {

		Stopwatch stopwatch = Stopwatch.createStarted();
		logger.info("Get published bulletin as PDF {}", bulletinId);
		try {
			pdfRateLimiter.acquire();
			AvalancheBulletin bulletin = AvalancheBulletinController.getInstance().getBulletin(bulletinId);
			Region region = RegionController.getInstance().getRegion(regionId);
			ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
			serverInstance.setPdfDirectory(GlobalVariables.getTmpPdfDirectory());
			AvalancheReport avalancheReport = AvalancheReport.of(List.of(bulletin), region, serverInstance);
			java.nio.file.Path pdf = new PdfUtil(avalancheReport, language, grayscale).createPdf();
			return Response.ok(pdf.toFile(), PdfUtil.MEDIA_TYPE).build();
		} catch (Exception e) {
			logger.warn("Error creating PDF", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		} finally {
			logger.info("Get published bulletin as PDF {} took {}", bulletinId, stopwatch);
		}
	}

	@GET
	@Path("/json")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))))
	@Operation(summary = "Get published bulletins for date")
	public Response getPublishedJSONBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("regions") List<String> regionIds, @QueryParam("lang") LanguageCode language) {
		logger.debug("GET published JSON bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		List<Region> regions = RegionController.getInstance().getRegionsOrBulletinRegions(regionIds);
		List<AvalancheBulletin> bulletins = AvalancheReportController.getInstance().getPublishedBulletins(startDate, regions);
		String json = JsonUtil.writeValueUsingJackson(bulletins, JsonUtil.Views.Public.class);
		return Response.ok(json, MediaType.APPLICATION_JSON).build();
	}

	static class Highest {
		public DangerRating dangerRating;
	}

	@GET
	@Path("/highest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "latest", content = @Content(schema = @Schema(implementation = Highest.class)))
	@Operation(summary = "Get highest danger rating")
	public Response getHighestDangerRating(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("regions") List<String> regionIds) {
		logger.debug("GET highest danger rating");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);

		if (regionIds.isEmpty()) {
			logger.warn("No region defined.");
			return Response.noContent().build();
		}

		List<Region> regions = new ArrayList<Region>();
		regionIds.forEach(regionId -> regions.add(RegionController.getInstance().getRegion(regionId)));

		try {
			DangerRating highestDangerRating = AvalancheBulletinController.getInstance()
				.getHighestDangerRating(startDate, regions);

			Highest jsonResult = new Highest();
			jsonResult.dangerRating = highestDangerRating;
			return Response.ok(jsonResult, MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading highest danger rating", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@POST
	@Secured({Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/preview")
	@Produces(PdfUtil.MEDIA_TYPE)
	@Operation(summary = "Get bulletin preview as PDF")
	public Response getPreviewPdf(
		@Parameter(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))) String bulletinsString,
		@QueryParam("region") String regionId,
		@QueryParam("lang") LanguageCode language) {

		logger.debug("POST PDF preview {}", regionId);

		try {
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			ZonedDateTime publicationDate = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
			List<AvalancheBulletin> bulletins = Arrays.stream(JsonUtil.parseUsingJackson(bulletinsString, AvalancheBulletin[].class))
				.filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region))
				.sorted()
				.collect(Collectors.toList());
			bulletins.forEach(b -> b.setPublicationDate(publicationDate));

			ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
			serverInstance.setMapsPath(GlobalVariables.getTmpPdfDirectory());
			serverInstance.setPdfDirectory(GlobalVariables.getTmpPdfDirectory());
			AvalancheReport avalancheReport = AvalancheReport.of(bulletins, region, serverInstance);
			avalancheReport.setStatus(BulletinStatus.draft); // preview

			MapUtil.createMapyrusMaps(avalancheReport);

			final java.nio.file.Path pdf = new PdfUtil(avalancheReport, language, false).createPdf();

			return Response.ok(pdf.toFile())
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdf.getFileName() + "\"")
				.header(HttpHeaders.CONTENT_TYPE, PdfUtil.MEDIA_TYPE).build();
		} catch (AlbinaException e) {
			logger.warn("Error creating PDFs", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		} catch (Exception e) {
			logger.warn("Error creating PDFs", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@GET
	@Secured({Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{bulletinId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "bulletin", content = @Content(schema = @Schema(implementation = AvalancheBulletin.class)))
	@Operation(summary = "Get bulletin by ID")
	public Response getJSONBulletin(@PathParam("bulletinId") String bulletinId) {
		logger.debug("GET JSON bulletin: {}", bulletinId);

		try {
			AvalancheBulletin bulletin = AvalancheBulletinController.getInstance().getBulletin(bulletinId);
			if (bulletin == null) {
				return Response.status(Response.Status.NOT_FOUND).entity(new AlbinaException("Bulletin not found for ID: " + bulletinId).toJSON()).build();
			}
			String json = JsonUtil.writeValueUsingJackson(bulletin, JsonUtil.Views.Internal.class);
			return Response.ok(json, MediaType.APPLICATION_JSON).build();
		} catch (HibernateException e) {
			logger.warn("Error loading bulletin", e);
			return Response.status(400).type(MediaType.TEXT_PLAIN).entity(e.toString()).build();
		}
	}

	@POST
	@Secured({Role.FORECASTER, Role.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{bulletinId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update bulletin")
	public Response updateJSONBulletin(
		@PathParam("bulletinId") String bulletinId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@Parameter(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin[].class))) String bulletinString,
		@QueryParam("region") String regionId,
		@Context SecurityContext securityContext) {

		synchronized (regionId.intern()) {
			logger.info("POST JSON bulletin {} from date {}", bulletinId, date);
			return HibernateUtil.getInstance().runTransaction(entityManager -> {
				try {
					Instant startDate = DateControllerUtil.parseDateOrThrow(date);
					Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

					User user = entityManager.find(User.class, securityContext.getUserPrincipal().getName());
					Region region = entityManager.find(Region.class, regionId);
					List<Region> regions = RegionController.getInstance().getRegions(entityManager);

					if (region != null && user != null && user.hasPermissionForRegion(region.getId())) {
						AvalancheBulletin bulletin = JsonUtil.parseUsingJackson(bulletinString, AvalancheBulletin.class);
						loadUser(entityManager, bulletin);
						AvalancheBulletinController.getInstance().updateBulletin(bulletin, startDate, endDate, region, user, entityManager);
					} else
						throw new AlbinaException("User is not authorized for this region!");

					return getJSONBulletins(date, regions, entityManager);
				} catch (AlbinaException e) {
					logger.warn("Error creating bulletin", e);
					return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
				}
			});
		}
	}

	private static void loadUser(EntityManager entityManager, AvalancheBulletin bulletin) {
		if (bulletin.getUser() != null && bulletin.getUser().getEmail() != null) {
			bulletin.setUser(entityManager.find(User.class, bulletin.getUser().getEmail()));
		}
	}

	@PUT
	@Secured({Role.FORECASTER, Role.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create bulletin")
	public Response createJSONBulletin(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@Parameter(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin[].class))) String bulletinString,
		@QueryParam("region") String regionId,
		@Context SecurityContext securityContext) {

		synchronized (regionId.intern()) {
			logger.info("PUT JSON bulletin from date {}", date);
			return HibernateUtil.getInstance().runTransaction(entityManager -> {
				try {
					Instant startDate = DateControllerUtil.parseDateOrThrow(date);
					Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

					User user = entityManager.find(User.class, securityContext.getUserPrincipal().getName());
					Region region = entityManager.find(Region.class, regionId);
					List<Region> regions = RegionController.getInstance().getRegions(entityManager);

					if (region != null && user != null && user.hasPermissionForRegion(region.getId())) {
						AvalancheBulletin bulletin = JsonUtil.parseUsingJackson(bulletinString, AvalancheBulletin.class);
						loadUser(entityManager, bulletin);
						Map<String, AvalancheBulletin> avalancheBulletins = AvalancheBulletinController.getInstance()
							.createBulletin(bulletin, startDate, endDate, region, entityManager);
						AvalancheReportController.getInstance().saveReport(avalancheBulletins, startDate, region, user, entityManager);

						// save report for super regions
						for (Region superRegion : region.getSuperRegions()) {
							AvalancheReportController.getInstance().saveReport(avalancheBulletins, startDate, superRegion, user, entityManager);
						}
					} else
						throw new AlbinaException("User is not authorized for this region!");

					return getJSONBulletins(date, regions, entityManager);
				} catch (AlbinaException e) {
					logger.warn("Error creating bulletin", e);
					return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
				}
			});
		}
	}

	@DELETE
	@Secured({Role.FORECASTER, Role.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{bulletinId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Delete bulletin")
	public Response deleteJSONBulletin(
		@PathParam("bulletinId") String bulletinId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@QueryParam("region") String regionId,
		@Context SecurityContext securityContext) {

		synchronized (regionId.intern()) {
			logger.info("DELETE JSON bulletin {} from date {}", bulletinId, date);
			return HibernateUtil.getInstance().runTransaction(entityManager -> {
				try {
					Instant startDate = DateControllerUtil.parseDateOrThrow(date);
					Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

					User user = entityManager.find(User.class, securityContext.getUserPrincipal().getName());
					Region region = entityManager.find(Region.class, regionId);
					List<Region> regions = RegionController.getInstance().getRegions(entityManager);

					if (region != null && user != null && user.hasPermissionForRegion(region.getId())) {
						AvalancheBulletinController.getInstance().deleteBulletin(bulletinId, startDate, endDate, region, user, entityManager);

					} else
						throw new AlbinaException("User is not authorized for this region!");

					return getJSONBulletins(date, regions, entityManager);
				} catch (AlbinaException e) {
					logger.warn("Error creating bulletin", e);
					return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
				}
			});
		}
	}

	@POST
	@Secured({Role.FORECASTER, Role.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create bulletins")
	public Response createJSONBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@Parameter(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin[].class))) String bulletinsString,
		@QueryParam("region") String regionId,
		@Context SecurityContext securityContext) {

		synchronized (regionId.intern()) {
			logger.debug("POST JSON bulletins for date {}", date);
			try {
				Instant startDate = DateControllerUtil.parseDateOrThrow(date);
				Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

				User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
				Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

				if (region != null && user.hasPermissionForRegion(region.getId())) {
					List<AvalancheBulletin> bulletins = List.of(JsonUtil.parseUsingJackson(bulletinsString, AvalancheBulletin[].class));
					HibernateUtil.getInstance().run(entityManager -> {
						bulletins.forEach(b -> loadUser(entityManager, b));
						return null;
					});
					AvalancheBulletinController.getInstance().saveBulletins(bulletins, startDate, endDate, region, user);
				} else
					throw new AlbinaException("User is not authorized for this region!");

				List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
				return getJSONBulletins(date, regionIDs);
			} catch (AlbinaException e) {
				logger.warn("Error creating bulletin", e);
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
			}
		}
	}

	@POST
	@Secured({Role.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/change")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Change bulletins")
	public Response changeBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
		@Parameter(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))) String bulletinsString,
		@QueryParam("region") String regionId,
		@Context SecurityContext securityContext) {
		logger.debug("POST JSON bulletins change");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				BulletinStatus status = AvalancheReportController.getInstance().getInternalStatusForDay(startDate, region);

				if ((status != BulletinStatus.submitted) && (status != BulletinStatus.resubmitted)) {
					List<AvalancheBulletin> bulletins = List.of(JsonUtil.parseUsingJackson(bulletinsString, AvalancheBulletin[].class));

					AvalancheBulletinController.getInstance().saveBulletins(bulletins, startDate, endDate, region, user);

					// eu.albina.model.AvalancheReport.timestamp has second precision due to MySQL's datatype datetime
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}

					List<AvalancheBulletin> allBulletins = AvalancheBulletinController.getInstance().submitBulletins(startDate,
						endDate, region, user);
					List<AvalancheBulletin> regionBulletins = allBulletins.stream()
						.filter(bulletin -> bulletin.affectsRegion(region))
						.collect(Collectors.toList());
					AvalancheReportController.getInstance().submitReport(regionBulletins, startDate, region, user);
					// submit report for super regions
					for (Region superRegion : region.getSuperRegions()) {
						List<AvalancheBulletin> superRegionBulletins = allBulletins.stream()
							.filter(bulletin -> bulletin.affectsRegion(superRegion))
							.collect(Collectors.toList());
						AvalancheReportController.getInstance().submitReport(superRegionBulletins, startDate, superRegion, user);
					}

					// eu.albina.model.AvalancheReport.timestamp has second precision due to MySQL's datatype datetime
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}

				List<Region> regions = Stream.concat(
					Stream.of(region),
					region.getSuperRegions().stream().filter(Region::isPublishBulletins)
				).distinct().collect(Collectors.toList());

				new Thread(() -> {
					new ChangeJob() {
						@Override
						protected Instant getStartDate(Clock clock) {
							return startDate;
						}

						@Override
						protected List<Region> getRegions() {
							return regions;
						}
					}.execute(null);
				}, "changeBulletins").start();

				return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@POST
	@Secured({Role.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/submit")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Submit bulletins")
	public Response submitBulletins(@QueryParam("region") String regionId,
									@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
									@Context SecurityContext securityContext) {
		logger.debug("POST submit bulletins");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (regionId != null && user.hasPermissionForRegion(regionId)) {
				List<AvalancheBulletin> allBulletins = AvalancheBulletinController.getInstance().submitBulletins(startDate,
					endDate, region, user);

				List<AvalancheBulletin> regionBulletins = allBulletins.stream()
					.filter(bulletin -> bulletin.affectsRegion(region))
					.collect(Collectors.toList());

				AvalancheReportController.getInstance().submitReport(regionBulletins, startDate, region, user);

				// submit report for super regions
				for (Region superRegion : region.getSuperRegions()) {
					List<AvalancheBulletin> superRegionBulletins = allBulletins.stream()
						.filter(bulletin -> bulletin.affectsRegion(superRegion))
						.collect(Collectors.toList());

					AvalancheReportController.getInstance().submitReport(superRegionBulletins, startDate, superRegion, user);
				}

				return Response.ok(MediaType.APPLICATION_JSON).entity("{}").build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error submitting bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@GET
	@Secured({Role.FORECASTER, Role.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Check bulletins")
	public Response checkBulletins(@QueryParam("region") String regionId,
								   @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
								   @Context SecurityContext securityContext) {
		logger.debug("GET check bulletins");

		try {
			if (regionId == null)
				throw new AlbinaException("No region defined!");

			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				JSONArray result = AvalancheBulletinController.getInstance().checkBulletins(startDate, endDate, region);
				return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@GET
	@Secured({Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/locked")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLockedBulletins(@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date, @Context SecurityContext securityContext) {
		logger.debug("GET JSON locked bulletins");

		try {
			JSONArray json = new JSONArray();
			for (BulletinLock bulletinLock : AvalancheBulletinController.getInstance().getLockedBulletins(DateControllerUtil.parseDateOrThrow(date)))
				json.put(bulletinLock);
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletin locks", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}
}
