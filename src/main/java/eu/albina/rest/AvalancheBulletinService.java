// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.MoreObjects;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;

import eu.albina.caaml.Caaml;
import eu.albina.caaml.CaamlVersion;
import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionRepository;
import eu.albina.controller.UserRepository;
import eu.albina.exception.AlbinaException;
import eu.albina.jobs.PublicationJob;
import eu.albina.jobs.PublicationStrategy;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.LocalServerInstance;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.util.GlobalVariables;
import eu.albina.util.JsonUtil;
import eu.albina.util.PdfUtil;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.server.types.files.SystemFile;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Controller("/bulletins")
@Tag(name = "bulletins")
@OpenAPIDefinition(info = @Info(
	title = "albina-server",
	version = "0.0",
	description = "Server component to compose and publish multilingual avalanche bulletins",
	license = @License(name = "GNU Affero General Public License v3.0", url = "https://gitlab.com/albina-euregio/albina-server/-/blob/master/LICENSE"),
	contact = @Contact(name = "avalanche.report", url = "https://avalanche.report/", email = "info@avalanche.report")
), servers = {@Server(url = "/albina/api")})
public class AvalancheBulletinService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinService.class);

	@Inject
	Caaml caaml;

	@Inject
	private AvalancheBulletinController avalancheBulletinController;

	@Inject
	private AvalancheReportController avalancheReportController;

	@Inject
	RegionRepository regionRepository;

	@Inject
	private GlobalVariables globalVariables;

	@Inject
	private UserRepository userRepository;

	@Inject
	PublicationJob publicationJob;

	@Get("/edit")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@ApiResponse(description = "bulletins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))))
	@Operation(summary = "Get bulletins for date")
	@JsonView(JsonUtil.Views.Internal.class)
	public List<AvalancheBulletin> getJSONBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("regions") List<String> regionIds) {

		List<Region> regions = new ArrayList<>();
		regionIds.forEach(regionId -> {
			try {
				Region region = regionRepository.findById(regionId).orElseThrow();
				regions.add(region);
			} catch (NoSuchElementException e) {
				logger.warn("No region with ID: " + regionId);
			}
		});
		return getJSONBulletins0(date, regions);
	}

	@Get("/edit/caaml/json")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get bulletins for date as CAAML JSON")
	public String getCaamlJsonBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("regions") List<String> regionIds,
		@QueryValue("lang") LanguageCode language,
		@QueryValue("version") CaamlVersion version) {

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		Instant endDate = startDate.plus(1, ChronoUnit.DAYS);
		List<Region> regions = regionRepository.getRegionsOrBulletinRegions(regionIds);
		ZonedDateTime publicationDate = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		try {
			List<AvalancheBulletin> bulletins = avalancheBulletinController.getBulletins(startDate, endDate, regions);
			bulletins.forEach(b -> b.setPublicationDate(publicationDate));
			bulletins.forEach(b -> b.setPublishedRegions(b.getPublishedAndSavedRegions()));
			AvalancheReport avalancheReport = AvalancheReport.of(bulletins, null, globalVariables.getLocalServerInstance());
			avalancheReport.setStatus(BulletinStatus.draft);
			return makeCAAML(avalancheReport, language, MoreObjects.firstNonNull(version, CaamlVersion.V6_JSON));
		} catch (RuntimeException e) {
			logger.warn("Error loading bulletins", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	private List<AvalancheBulletin> getJSONBulletins0(String date, List<Region> regions) {
		logger.debug("GET JSON bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

		if (regions.isEmpty()) {
			logger.warn("No region defined.");
		}

		List<AvalancheBulletin> bulletins = avalancheBulletinController.getBulletins(startDate, endDate, regions);
		Collections.sort(bulletins);
		return bulletins;
	}

	@Get
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Produces(MediaType.APPLICATION_XML)
	@Operation(deprecated = true)
	public String getPublishedXMLBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		@QueryValue("lang") LanguageCode language,
		@QueryValue("version") CaamlVersion version) {
		List<String> regionIds = regionId != null ? Collections.singletonList(regionId) : Collections.emptyList();
		return getPublishedCaamlBulletins(date, regionIds, language, version);
	}

	private String makeCAAML(AvalancheReport avalancheReport, LanguageCode language, CaamlVersion version) {
		return this.caaml.createCaaml(avalancheReport, MoreObjects.firstNonNull(language, LanguageCode.en), MoreObjects.firstNonNull(version, CaamlVersion.V5));
	}

	@Get("/caaml")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Produces(MediaType.APPLICATION_XML)
	@Operation(summary = "Get published bulletins for date as CAAML XML")
	public String getPublishedCaamlBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("regions") List<String> regionIds,
		@QueryValue("lang") LanguageCode language,
		@QueryValue("version") CaamlVersion version) {
		logger.debug("GET published XML bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		List<Region> regions = regionRepository.getRegionsOrBulletinRegions(regionIds);

		try {
			AvalancheReport avalancheReport = AvalancheReport.of(
				avalancheReportController.getPublishedBulletins(startDate, regions), null,
				globalVariables.getLocalServerInstance());
			return makeCAAML(avalancheReport, language, version);
		} catch (RuntimeException e) {
			logger.warn("Error loading bulletins", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get("/caaml/json")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Get published bulletins for date as CAAML JSON")
	public String getPublishedCaamlJsonBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("regions") List<String> regionIds,
		@QueryValue("lang") LanguageCode language,
		@QueryValue("version") CaamlVersion version) {
		return getPublishedCaamlBulletins(date, regionIds, language, MoreObjects.firstNonNull(version, CaamlVersion.V6_JSON));
	}

	@Serdeable
	public record LatestBulletin(Instant date) {
	}

	@Get("/latest")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Get latest bulletin date")
	public LatestBulletin getLatest() {
		logger.debug("GET latest date");
		try {
			return new LatestBulletin(avalancheReportController.getLatestDate());
		} catch (AlbinaException e) {
			logger.warn("Error loading latest date", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get
	@ApiResponse(description = "bulletins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))))
	@Operation(deprecated = true)
	public List<AvalancheBulletin> getPublishedJSONBulletins0(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("regions") List<String> regionIds, @QueryValue("lang") LanguageCode language) {
		return getPublishedJSONBulletins(date, regionIds, language);
	}

	private final RateLimiter pdfRateLimiter = RateLimiter.create(2.0); // allow 2 PDFs per second

	@Get("/pdf")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Produces(MediaType.APPLICATION_PDF)
	@Operation(summary = "Get published bulletins as PDF")
	public SystemFile getPublishedBulletinsAsPDF(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		@QueryValue("grayscale") boolean grayscale,
		@QueryValue("lang") LanguageCode language) {

		Stopwatch stopwatch = Stopwatch.createStarted();
		logger.info("Get published bulletins as PDF {}", regionId);
		try {
			pdfRateLimiter.acquire();
			Instant startDate = DateControllerUtil.parseDateOrToday(date);
			Region region = regionRepository.findById(regionId).orElseThrow();
			List<AvalancheBulletin> bulletins = avalancheReportController.getPublishedBulletins(startDate, List.of(region));
			LocalServerInstance serverInstance = globalVariables.getLocalServerInstance(resolveTmpPath(globalVariables.tmpOverride).toString(), null);
			AvalancheReport avalancheReport = AvalancheReport.of(bulletins, region, serverInstance);
			Path pdf = new PdfUtil(avalancheReport, language, grayscale).createPdf();
			return new SystemFile(pdf.toFile());
		} catch (Exception e) {
			logger.warn("Error creating PDF", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		} finally {
			logger.info("Get published bulletin as PDF {} took {}", regionId, stopwatch);
		}
	}

	@Get("/{bulletinId}/pdf")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Produces(MediaType.APPLICATION_PDF)
	@Operation(summary = "Get published bulletin as PDF")
	public SystemFile getPublishedBulletinAsPDF(
		@PathVariable("bulletinId") String bulletinId,
		@QueryValue("region") String regionId,
		@QueryValue("grayscale") boolean grayscale,
		@QueryValue("lang") LanguageCode language) {

		Stopwatch stopwatch = Stopwatch.createStarted();
		logger.info("Get published bulletin as PDF {}", bulletinId);
		try {
			pdfRateLimiter.acquire();
			AvalancheBulletin bulletin = avalancheBulletinController.getBulletin(bulletinId);
			Region region = regionRepository.findById(regionId).orElseThrow();
			LocalServerInstance serverInstance = globalVariables.getLocalServerInstance(resolveTmpPath(globalVariables.tmpOverride).toString(), null);
			AvalancheReport avalancheReport = AvalancheReport.of(List.of(bulletin), region, serverInstance);
			Path pdf = new PdfUtil(avalancheReport, language, grayscale).createPdf();
			return new SystemFile(pdf.toFile());
		} catch (Exception e) {
			logger.warn("Error creating PDF", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		} finally {
			logger.info("Get published bulletin as PDF {} took {}", bulletinId, stopwatch);
		}
	}

	@Get("/json")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@ApiResponse(description = "bulletins", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvalancheBulletin.class))))
	@Operation(summary = "Get published bulletins for date")
	@JsonView(JsonUtil.Views.Public.class)
	public List<AvalancheBulletin> getPublishedJSONBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("regions") List<String> regionIds, @QueryValue("lang") LanguageCode language) {
		logger.debug("GET published JSON bulletins");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		List<Region> regions = regionRepository.getRegionsOrBulletinRegions(regionIds);
		return avalancheReportController.getPublishedBulletins(startDate, regions);
	}

	@Serdeable
	public record Highest(DangerRating dangerRating) {
	}

	@Get("/highest")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@ApiResponse(description = "latest", content = @Content(schema = @Schema(implementation = Highest.class)))
	@Operation(summary = "Get highest danger rating")
	public Highest getHighestDangerRating(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("regions") List<String> regionIds) {
		logger.debug("GET highest danger rating");

		Instant startDate = DateControllerUtil.parseDateOrToday(date);
		List<Region> regions = regionRepository.getRegionsOrBulletinRegions(regionIds);

		try {
			DangerRating highestDangerRating = avalancheBulletinController
				.getHighestDangerRating(startDate, regions);

			return new Highest(highestDangerRating);
		} catch (AlbinaException e) {
			logger.warn("Error loading highest danger rating", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/preview")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_PDF)
	@Operation(summary = "Get bulletin preview as PDF")
	public SystemFile getPreviewPdf(
		@Body AvalancheBulletin[] bulletinsArray,
		@QueryValue("region") String regionId,
		@QueryValue("lang") LanguageCode language) {

		logger.debug("POST PDF preview {}", regionId);

		try {
			Region region = regionRepository.findById(regionId).orElseThrow();
			ZonedDateTime publicationDate = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
			List<AvalancheBulletin> bulletins = Arrays.stream(bulletinsArray)
				.filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region))
				.sorted()
				.collect(Collectors.toList());
			bulletins.forEach(b -> b.setPublicationDate(publicationDate));

			LocalServerInstance serverInstance = globalVariables.getLocalServerInstance(
					resolveTmpPath(globalVariables.tmpOverride).toString(),
					resolveTmpPath(globalVariables.tmpOverride).toString());
			AvalancheReport avalancheReport = AvalancheReport.of(bulletins, region, serverInstance);
			avalancheReport.setStatus(BulletinStatus.draft); // preview

			MapUtil.createMapyrusMaps(avalancheReport);

			final Path pdf = new PdfUtil(avalancheReport, language, false).createPdf();

			return new SystemFile(pdf.toFile()).attach();
		} catch (Exception e) {
			logger.warn("Error creating PDFs", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	private Path resolveTmpPath(String override) throws Exception {
		Path path = (override == null || override.isBlank())
			? Path.of(StandardSystemProperty.JAVA_IO_TMPDIR.value())
			: Path.of(override);
		Files.createDirectories(path);
		return path;
	}

	@Get("/{bulletinId}")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@ApiResponse(description = "bulletin", content = @Content(schema = @Schema(implementation = AvalancheBulletin.class)))
	@Operation(summary = "Get bulletin by ID")
	@JsonView(JsonUtil.Views.Internal.class)
	@Transactional
	public AvalancheBulletin getJSONBulletin(@PathVariable("bulletinId") String bulletinId) {
		logger.debug("GET JSON bulletin: {}", bulletinId);

		try {
			AvalancheBulletin bulletin = avalancheBulletinController.getBulletin(bulletinId);
			if (bulletin == null) {
				throw new HttpStatusException(HttpStatus.NOT_FOUND, new AlbinaException("Bulletin not found for ID: " + bulletinId).toJSON());
			}
			return bulletin;
		} catch (RuntimeException e) {
			logger.warn("Error loading bulletin", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.toString());
		}
	}

	@Post("/{bulletinId}")
	@Secured({Role.Str.FORECASTER, Role.Str.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update bulletin")
	@JsonView(JsonUtil.Views.Internal.class)
	@Transactional
	public List<AvalancheBulletin> updateJSONBulletin(
		@PathVariable("bulletinId") String bulletinId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@Body AvalancheBulletin bulletin,
		@QueryValue("region") String regionId,
		Principal principal) {

		synchronized (regionId.intern()) {
			logger.info("POST JSON bulletin {} from date {}", bulletinId, date);
			try {
				Instant startDate = DateControllerUtil.parseDateOrThrow(date);
				Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

				User user = userRepository.findByIdOrElseThrow(principal);
				Region region = regionRepository.findById(regionId).orElseThrow();
				List<Region> regions = regionRepository.findAll();

				if (user.hasPermissionForRegion(region.getId())) {
					loadUser(bulletin);
					avalancheBulletinController.updateBulletin(bulletin, startDate, endDate, region, user);
				} else
					throw new AlbinaException("User is not authorized for this region!");

				return getJSONBulletins0(date, regions);
			} catch (AlbinaException e) {
				logger.warn("Error creating bulletin", e);
				throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.toJSON());
			}
		}
	}

	private void loadUser(AvalancheBulletin bulletin) {
		if (bulletin.getUser() != null && bulletin.getUser().getEmail() != null) {
			bulletin.setUser(userRepository.findById(bulletin.getUser().getEmail()).orElseThrow());
		}
	}

	@Put
	@Secured({Role.Str.FORECASTER, Role.Str.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create bulletin")
	@JsonView(JsonUtil.Views.Internal.class)
	@Transactional
	public List<AvalancheBulletin> createJSONBulletin(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@Body AvalancheBulletin bulletin,
		@QueryValue("region") String regionId,
		Principal principal) {

		synchronized (regionId.intern()) {
			logger.info("PUT JSON bulletin from date {}", date);
			try {
				Instant startDate = DateControllerUtil.parseDateOrThrow(date);
				Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

				User user = userRepository.findByIdOrElseThrow(principal);
				Region region = regionRepository.findById(regionId).orElseThrow();
				List<Region> regions = regionRepository.findAll();

				if (user.hasPermissionForRegion(region.getId())) {
					loadUser(bulletin);
					Map<String, AvalancheBulletin> avalancheBulletins = avalancheBulletinController
						.createBulletin(bulletin, startDate, endDate, region);
					avalancheReportController.saveReport(avalancheBulletins, startDate, region, user);
				} else
					throw new AlbinaException("User is not authorized for this region!");

				return getJSONBulletins0(date, regions);
			} catch (AlbinaException e) {
				logger.warn("Error creating bulletin", e);
				throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.toJSON());
			}
		}
	}

	@Delete("/{bulletinId}")
	@Secured({Role.Str.FORECASTER, Role.Str.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete bulletin")
	@JsonView(JsonUtil.Views.Internal.class)
	@Transactional
	public List<AvalancheBulletin> deleteJSONBulletin(
		@PathVariable("bulletinId") String bulletinId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		Principal principal) {

		synchronized (regionId.intern()) {
			logger.info("DELETE JSON bulletin {} from date {}", bulletinId, date);
			try {
				Instant startDate = DateControllerUtil.parseDateOrThrow(date);
				Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

				User user = userRepository.findByIdOrElseThrow(principal);
				Region region = regionRepository.findById(regionId).orElseThrow();
				List<Region> regions = regionRepository.findAll();

				if (user.hasPermissionForRegion(region.getId())) {
					avalancheBulletinController.deleteBulletin(bulletinId, startDate, endDate, region, user);

				} else
					throw new AlbinaException("User is not authorized for this region!");

				return getJSONBulletins0(date, regions);
			} catch (AlbinaException e) {
				logger.warn("Error creating bulletin", e);
				throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.toJSON());
			}
		}
	}

	@Post
	@Secured({Role.Str.FORECASTER, Role.Str.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create bulletins")
	@JsonView(JsonUtil.Views.Internal.class)
	@Transactional
	public List<AvalancheBulletin> createJSONBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@Body AvalancheBulletin[] bulletinsArray,
		@QueryValue("region") String regionId,
		Principal principal) {

		synchronized (regionId.intern()) {
			logger.debug("POST JSON bulletins for date {}", date);
			try {
				Instant startDate = DateControllerUtil.parseDateOrThrow(date);
				Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

				User user = userRepository.findByIdOrElseThrow(principal);
				Region region = regionRepository.findById(regionId).orElseThrow();

				if (user.hasPermissionForRegion(region.getId())) {
					List<AvalancheBulletin> bulletins = List.of(bulletinsArray);
					bulletins.forEach(this::loadUser);
					avalancheBulletinController.saveBulletins(bulletins, startDate, endDate, region, user);
				} else
					throw new AlbinaException("User is not authorized for this region!");

				List<String> regionIDs = regionRepository.findAll().stream().map(Region::getId).collect(Collectors.toList());
				return getJSONBulletins(date, regionIDs);
			} catch (AlbinaException e) {
				logger.warn("Error creating bulletin", e);
				throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.toJSON());
			}
		}
	}

	@Post("/change")
	@Secured(Role.Str.FORECASTER)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Change bulletins")
	@Transactional
	public void changeBulletins(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@Body AvalancheBulletin[] bulletinsArray,
		@QueryValue("region") String regionId,
		Principal principal) {
		logger.debug("POST JSON bulletins change");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = userRepository.findByIdOrElseThrow(principal);
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (user.hasPermissionForRegion(region.getId())) {
				BulletinStatus status = avalancheReportController.getInternalStatusForDay(startDate, region);

				if ((status != BulletinStatus.submitted) && (status != BulletinStatus.resubmitted)) {
					List<AvalancheBulletin> bulletins = List.of(bulletinsArray);

					avalancheBulletinController.saveBulletins(bulletins, startDate, endDate, region, user);

					// eu.albina.model.AvalancheReport.timestamp has second precision due to MySQL's datatype datetime
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}

					List<AvalancheBulletin> allBulletins = avalancheBulletinController.submitBulletins(startDate,
						endDate, region, user);
					List<AvalancheBulletin> regionBulletins = allBulletins.stream()
						.filter(bulletin -> bulletin.affectsRegion(region))
						.collect(Collectors.toList());
					avalancheReportController.submitReport(regionBulletins, startDate, region, user);

					// eu.albina.model.AvalancheReport.timestamp has second precision due to MySQL's datatype datetime
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}

				List<Region> regions = List.of(region);
				publicationJob.execute(PublicationStrategy.change(startDate, regions));
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error creating bulletin", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/submit")
	@Secured(Role.Str.FORECASTER)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Submit bulletins")
	@Transactional
	public void submitBulletins(@QueryValue("region") String regionId,
									@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
										   Principal principal) {
		logger.debug("POST submit bulletins");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = userRepository.findByIdOrElseThrow(principal);
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (regionId != null && user.hasPermissionForRegion(regionId)) {
				List<AvalancheBulletin> allBulletins = avalancheBulletinController.submitBulletins(startDate,
					endDate, region, user);

				List<AvalancheBulletin> regionBulletins = allBulletins.stream()
					.filter(bulletin -> bulletin.affectsRegion(region))
					.collect(Collectors.toList());

				avalancheReportController.submitReport(regionBulletins, startDate, region, user);
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error submitting bulletins", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get("/check")
	@Secured({Role.Str.FORECASTER, Role.Str.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Check bulletins")
	public Set<String> checkBulletins(@QueryValue("region") String regionId,
									  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
									  Principal principal) {
		logger.debug("GET check bulletins");

		try {
			if (regionId == null)
				throw new AlbinaException("No region defined!");

			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			Region region = regionRepository.findById(regionId).orElseThrow();
			User user = userRepository.findByIdOrElseThrow(principal);

			if (user.hasPermissionForRegion(region.getId())) {
				return avalancheBulletinController.checkBulletins(startDate, endDate, region);
			} else
				throw new AlbinaException("User is not authorized for this region!");
		} catch (AlbinaException e) {
			logger.warn("Error loading bulletins", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}
