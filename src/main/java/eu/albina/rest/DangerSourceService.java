// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.albina.controller.DangerSourceRepository;
import eu.albina.controller.DangerSourceVariantRepository;
import eu.albina.controller.RegionRepository;
import eu.albina.controller.UserRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import eu.albina.controller.DangerSourceVariantTextController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.DangerSource;
import eu.albina.model.DangerSourceVariant;
import eu.albina.model.DangerSourceVariantsStatus;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/danger-sources")
@Tag(name = "danger-sources")
@Transactional
public class DangerSourceService {

	private static final Logger logger = LoggerFactory.getLogger(DangerSourceService.class);

	@Inject
	DangerSourceRepository dangerSourceRepository;

	@Inject
	DangerSourceVariantRepository dangerSourceVariantRepository;

	@Inject
	DangerSourceVariantTextController dangerSourceVariantTextController;

	@Inject
	RegionRepository regionRepository;

	@Inject
	private UserRepository userRepository;

	@Get
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@ApiResponse(description = "danger-sources", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DangerSource.class))))
	@Operation(summary = "Get danger sources for season")
	public List<DangerSource> getDangerSources(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId) {

		Range<Instant> instantRange = DateControllerUtil.parseHydrologicalYearInstantRange(date);
		return dangerSourceRepository
			.findByCreationDateBetweenAndOwnerRegion(instantRange.lowerEndpoint(), instantRange.upperEndpoint(), regionId);
	}

	@Post("/{dangerSourceId}")
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update danger source")
	public void updateDangerSource(
		@PathVariable("dangerSourceId") String dangerSourceId,
			@Body DangerSource dangerSource) {

		dangerSourceRepository.update(dangerSource);
	}

	@Get("/edit")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@ApiResponse(description = "danger-sources", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DangerSourceVariant.class))))
	@Operation(summary = "Get danger source variants for date")
	public List<DangerSourceVariant> getVariants(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId) {

		Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);
		Region region = regionRepository.findById(regionId).orElseThrow();
		return dangerSourceVariantRepository
			.findByCreationDateBetweenAndOwnerRegion(instantRange.lowerEndpoint(), instantRange.upperEndpoint(), region.getId());
	}

	@Get("/status")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public List<DangerSourceVariantsStatus> getInternalStatus(@QueryValue("region") String regionId,
															  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String startDate,
															  @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String endDate) {

		Range<Instant> instantRangeStart = DateControllerUtil.parseInstantRange(startDate);
		Range<Instant> instantRangeEnd = DateControllerUtil.parseInstantRange(endDate);
		Region region = regionRepository.findById(regionId).orElseThrow();

		return dangerSourceVariantRepository
			.getDangerSourceVariantsStatus(instantRangeStart, instantRangeEnd, region);
	}

	@Get("/{dangerSourceId}/edit")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@ApiResponse(description = "danger-sources", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DangerSourceVariant.class))))
	@Operation(summary = "Get danger source variants for danger source and date")
	public List<DangerSourceVariant> getVariantsForDangerSource(
		@PathVariable("dangerSourceId") String dangerSourceId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("regions") List<String> regionIds) {

		Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);
		List<Region> regions = regionIds.stream().map(regionRepository::findById)
			.map(Optional::orElseThrow)
			.toList();
		return dangerSourceVariantRepository.getDangerSourceVariants(
			instantRange.lowerEndpoint(), instantRange.upperEndpoint(), regions, dangerSourceId);
	}

	@Post
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create variants")
	public List<DangerSourceVariant> createVariants(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		Principal principal,
			@Body DangerSourceVariant[] variants) {

		try {
			Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);
			User user = userRepository.findByIdOrElseThrow(principal);
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (user.hasPermissionForRegion(region.getId())) {
				for (DangerSourceVariant variant : variants) {
					variant.setTextcat(dangerSourceVariantTextController.getTextForDangerSourceVariant(variant));
				}
				dangerSourceVariantRepository.saveDangerSourceVariants(Arrays.asList(variants),
					instantRange.lowerEndpoint(), instantRange.upperEndpoint(), region);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			return getVariants(date, regionId);
		} catch (Exception e) {
			logger.warn("Error creating variants", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get("/variants/{variantId}")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@ApiResponse(description = "variant", content = @Content(schema = @Schema(implementation = DangerSourceVariant.class)))
	@Operation(summary = "Get variant by ID")
	public DangerSourceVariant getVariantById(
		@PathVariable("variantId") String variantId
	) {
		return dangerSourceVariantRepository.findById(variantId).orElseThrow();
	}

	@Post("/variants/{variantId}")
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update danger source variant")
	public List<DangerSourceVariant> updateDangerSource(
		@PathVariable("variantId") String variantId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		Principal principal,
			@Body DangerSourceVariant variant) {

		try {
			variant.setTextcat(dangerSourceVariantTextController.getTextForDangerSourceVariant(variant));

			Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);

			User user = userRepository.findByIdOrElseThrow(principal);
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (user.hasPermissionForRegion(region.getId())) {
				dangerSourceVariantRepository.updateDangerSourceVariant(variant,
					instantRange.lowerEndpoint(), instantRange.upperEndpoint(), region);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			return getVariants(date, regionId);
		} catch (Exception e) {
			logger.warn("Error creating danger source variant", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Delete("/variants/{variantId}")
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete danger source variant")
	public List<DangerSourceVariant> deleteVariant(
		@PathVariable("variantId") String variantId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		Principal principal) {

		try {
			User user = userRepository.findByIdOrElseThrow(principal);
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (user.hasPermissionForRegion(region.getId())) {
				dangerSourceVariantRepository.deleteById(variantId);
			} else {
				throw new AlbinaException("User is not authorized for this region!");
			}

			return getVariants(date, regionId);
		} catch (Exception e) {
			logger.warn("Error deleting variant", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Put("/variants")
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create danger source variant")
	public List<DangerSourceVariant> createVariant(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		Principal principal,
			@Body DangerSourceVariant variant) {

		try {
			variant.setTextcat(dangerSourceVariantTextController.getTextForDangerSourceVariant(variant));

			Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);

			User user = userRepository.findByIdOrElseThrow(principal);
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (user.hasPermissionForRegion(region.getId())) {
				dangerSourceVariantRepository.createDangerSourceVariant(variant,
					instantRange.lowerEndpoint(), instantRange.upperEndpoint(), region);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			return getVariants(date, regionId);
		} catch (AlbinaException e) {
			logger.warn("Error creating danger source variant", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}
