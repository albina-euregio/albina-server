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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.albina.controller.DangerSourceController;
import eu.albina.controller.DangerSourceVariantController;
import eu.albina.controller.RegionController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.DangerSource;
import eu.albina.model.DangerSourceVariant;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.JsonUtil;
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

@Path("/danger-sources")
@Tag(name = "danger-sources")
@OpenAPIDefinition(info = @Info(
	title = "albina-server",
	version = "0.0",
	description = "Server component to compose and publish multilingual avalanche bulletins",
	license = @License(name = "GNU General Public License v3.0", url = "https://gitlab.com/albina-euregio/albina-server/-/blob/master/LICENSE"),
	contact = @Contact(name = "avalanche.report", url = "https://avalanche.report/", email = "info@avalanche.report")
), servers = {@Server(url = "/albina/api")})
public class DangerSourceService {

	private static final Logger logger = LoggerFactory.getLogger(DangerSourceService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "danger-sources", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DangerSource.class))))
	@Operation(summary = "Get danger sources for season")
	public List<DangerSource> getDangerSources(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("regions") List<String> regionIds,
			@Context SecurityContext securityContext) {
		logger.debug("GET JSON danger sources");

		OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
		LocalDateTime endDate = offsetDateTime.toLocalDateTime();
		LocalDateTime startDate = DateControllerUtil.getStartOfHydrologicalYear(endDate);

		if (regionIds.isEmpty()) {
			logger.warn("No region defined.");
			return new ArrayList<DangerSource>();
		}

		List<Region> regions = new ArrayList<Region>();
		regionIds.forEach(regionId -> {
			try {
				Region region = RegionController.getInstance().getRegion(regionId);
				regions.add(region);
			} catch (HibernateException e) {
				logger.warn("No region with ID: " + regionId);
			}
		});

		return DangerSourceController.getInstance().getDangerSources(startDate.toInstant(offsetDateTime.getOffset()), endDate.toInstant(offsetDateTime.getOffset()));
	}

	@POST
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{dangerSourceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update danger source")
	public void updateDangerSource(
			@PathParam("dangerSourceId") String dangerSourceId,
			@Context SecurityContext securityContext,
			String json) {
		logger.debug("POST JSON danger source");

		try {
			DangerSource dangerSource = JsonUtil.parseUsingJackson(json, DangerSource.class);
			DangerSourceController.getInstance().updateDangerSource(dangerSource);
		} catch (JsonProcessingException e) {
			logger.warn("Error creating danger source variant", e);
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/edit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "danger-sources", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DangerSourceVariant.class))))
	@Operation(summary = "Get danger source variants for date")
	public List<DangerSourceVariant> getVariants(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("regions") List<String> regionIds,
			@Context SecurityContext securityContext) {
		logger.debug("GET JSON danger source variants");

		OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
		LocalDateTime startDate = offsetDateTime.toLocalDateTime();
		LocalDateTime endDate = startDate.plusDays(1);

		if (regionIds.isEmpty()) {
			logger.warn("No region defined.");
			return new ArrayList<DangerSourceVariant>();
		}

		List<Region> regions = new ArrayList<Region>();
		regionIds.forEach(regionId -> {
			try {
				Region region = RegionController.getInstance().getRegion(regionId);
				regions.add(region);
			} catch (HibernateException e) {
				logger.warn("No region with ID: " + regionId);
			}
		});

		return DangerSourceVariantController.getInstance().getDangerSourceVariants(startDate.toInstant(offsetDateTime.getOffset()), endDate.toInstant(offsetDateTime.getOffset()), regions);
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/{dangerSourceId}/edit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "danger-sources", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DangerSourceVariant.class))))
	@Operation(summary = "Get danger source variants for danger source and date")
	public List<DangerSourceVariant> getVariantsForDangerSource(
			@PathParam("dangerSourceId") String dangerSourceId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("regions") List<String> regionIds) {
		logger.debug("GET JSON danger source variants for danger source");

		OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
		LocalDateTime startDate = offsetDateTime.toLocalDateTime();
		LocalDateTime endDate = startDate.plusDays(1);

		if (regionIds.isEmpty()) {
			logger.warn("No region defined.");
			return new ArrayList<DangerSourceVariant>();
		}

		List<Region> regions = new ArrayList<Region>();
		regionIds.forEach(regionId -> {
			try {
				Region region = RegionController.getInstance().getRegion(regionId);
				regions.add(region);
			} catch (HibernateException e) {
				logger.warn("No region with ID: " + regionId);
			}
		});

		return DangerSourceVariantController.getInstance().getDangerSourceVariants(startDate.toInstant(offsetDateTime.getOffset()), endDate.toInstant(offsetDateTime.getOffset()), regions, dangerSourceId);
	}

	@POST
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create variants")
	public List<DangerSourceVariant> createVariants(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("region") String regionId,
			@Context SecurityContext securityContext,
			String json) {
		logger.debug("POST JSON variants");

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			Instant endDate = startDate.plus(1, ChronoUnit.DAYS);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				DangerSourceVariant[] variants = JsonUtil.parseUsingJackson(json, DangerSourceVariant[].class);
				DangerSourceVariantController.getInstance().saveDangerSourceVariants(Arrays.asList(variants), startDate, endDate, region);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getVariants(date, regionIDs, securityContext);
		} catch (AlbinaException | JsonProcessingException e) {
			logger.warn("Error creating variants", e);
			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getVariants(date, regionIDs, securityContext);
		}
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/variants/{variantId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(description = "variant", content = @Content(schema = @Schema(implementation = DangerSourceVariant.class)))
	@Operation(summary = "Get variant by ID")
	public DangerSourceVariant getVariantById(
		@PathParam("variantId") String variantId,
		@Context SecurityContext securityContext
	) {
		logger.debug("GET JSON danger source variant: {}", variantId);
		return DangerSourceVariantController.getInstance().getDangerSourceVariant(variantId);
	}

	@POST
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/variants/{variantId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update danger source variant")
	public List<DangerSourceVariant> updateDangerSource(
			@PathParam("variantId") String variantId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("region") String regionId,
			@Context SecurityContext securityContext,
			String json) {
		logger.debug("POST JSON danger source variant");

		try {
			DangerSourceVariant variant = JsonUtil.parseUsingJackson(json, DangerSourceVariant.class);
			OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
			LocalDateTime startDate = offsetDateTime.toLocalDateTime();
			LocalDateTime endDate = startDate.plusDays(1);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				DangerSourceVariantController.getInstance().updateDangerSourceVariant(variant, startDate.toInstant(offsetDateTime.getOffset()), endDate.toInstant(offsetDateTime.getOffset()), region);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getVariants(date, regionIDs, securityContext);
		} catch (AlbinaException | JsonProcessingException e) {
			logger.warn("Error creating danger source variant", e);
			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getVariants(date, regionIDs, securityContext);
		}
	}

	@DELETE
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/variants/{variantId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Delete danger source variant")
	public List<DangerSourceVariant> deleteVariant(
			@PathParam("variantId") String variantId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("region") String regionId,
			@Context SecurityContext securityContext) {
		logger.debug("DELETE JSON danger source variant");

		try {
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			
			if (region != null && user.hasPermissionForRegion(region.getId())) {
				DangerSourceVariantController.getInstance().deleteDangerSourceVariant(variantId);				
			} else {
				throw new AlbinaException("User is not authorized for this region!");
			}
			
			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getVariants(date, regionIDs, securityContext);
		} catch (AlbinaException e) {
			logger.warn("Error deleting variant", e);
			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getVariants(date, regionIDs, securityContext);
		}
	}

	@PUT
	@Secured({ Role.FORECASTER, Role.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Path("/variants")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create danger source variant")
	public List<DangerSourceVariant> createVariant(
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String date,
			@QueryParam("region") String regionId,
			@Context SecurityContext securityContext,
			String json) {
		logger.debug("PUT JSON danger source variant");

		try {
			DangerSourceVariant variant = JsonUtil.parseUsingJackson(json, DangerSourceVariant.class);
			OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
			LocalDateTime startDate = offsetDateTime.toLocalDateTime();
			LocalDateTime endDate = startDate.plusDays(1);

			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);

			if (region != null && user.hasPermissionForRegion(region.getId())) {
				DangerSourceVariantController.getInstance().createDangerSourceVariant(variant, startDate.toInstant(offsetDateTime.getOffset()), endDate.toInstant(offsetDateTime.getOffset()), region);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getVariants(date, regionIDs, securityContext);
		} catch (AlbinaException | JsonProcessingException e) {
			logger.warn("Error creating danger source variant", e);
			List<String> regionIDs = RegionController.getInstance().getRegions().stream().map(Region::getId).collect(Collectors.toList());
			return getVariants(date, regionIDs, securityContext);
		}
	}
}
