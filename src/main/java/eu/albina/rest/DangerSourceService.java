// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.albina.controller.DangerSourceRepository;
import eu.albina.controller.DangerSourceVariantRepository;
import eu.albina.controller.RegionRepository;
import eu.albina.controller.UserRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;

import jakarta.inject.Inject;
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
	public HttpResponse<List<DangerSource>> getDangerSources(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId) {
		logger.debug("GET JSON danger sources");

		Range<Instant> instantRange = DateControllerUtil.parseHydrologicalYearInstantRange(date);

		List<DangerSource> dangerSources = dangerSourceRepository
			.findByCreationDateBetweenAndOwnerRegion(instantRange.lowerEndpoint(), instantRange.upperEndpoint(), regionId);
		return HttpResponse.ok(dangerSources);
	}

	@Post("/{dangerSourceId}")
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update danger source")
	public void updateDangerSource(
		@PathVariable("dangerSourceId") String dangerSourceId,
			@Body DangerSource dangerSource) {
		logger.debug("POST JSON danger source");

		dangerSourceRepository.update(dangerSource);
	}

	@Get("/edit")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@ApiResponse(description = "danger-sources", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DangerSourceVariant.class))))
	@Operation(summary = "Get danger source variants for date")
	public HttpResponse<List<DangerSourceVariant>> getVariants(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId) {
		logger.debug("GET JSON danger source variants");

		Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);
		Region region = regionRepository.findById(regionId).orElseThrow();
		List<DangerSourceVariant> variants = dangerSourceVariantRepository
			.findByCreationDateBetweenAndOwnerRegion(instantRange.lowerEndpoint(), instantRange.upperEndpoint(), region.getId());
		return HttpResponse.ok(variants);
	}

	@Get("/status")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public HttpResponse<List<DangerSourceVariantsStatus>> getInternalStatus(@QueryValue("region") String regionId,
																				   @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String startDate,
																				   @Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String endDate) {

		Range<Instant> instantRangeStart = DateControllerUtil.parseInstantRange(startDate);
		Range<Instant> instantRangeEnd = DateControllerUtil.parseInstantRange(endDate);
		Region region = regionRepository.findById(regionId).orElseThrow();

		List<DangerSourceVariantsStatus> status = dangerSourceVariantRepository
			.getDangerSourceVariantsStatus(instantRangeStart, instantRangeEnd, region);
		return HttpResponse.ok(status);
	}

	@Get("/{dangerSourceId}/edit")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@ApiResponse(description = "danger-sources", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DangerSourceVariant.class))))
	@Operation(summary = "Get danger source variants for danger source and date")
	public HttpResponse<List<DangerSourceVariant>> getVariantsForDangerSource(
		@PathVariable("dangerSourceId") String dangerSourceId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("regions") List<String> regionIds) {
		logger.debug("GET JSON danger source variants for danger source");

		Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);
		List<Region> regions = regionIds.stream().map(regionRepository::findById)
			.map(Optional::orElseThrow)
			.collect(Collectors.toList());
		List<DangerSourceVariant> variants = dangerSourceVariantRepository.getDangerSourceVariants(
			instantRange.lowerEndpoint(), instantRange.upperEndpoint(), regions, dangerSourceId);
		return HttpResponse.ok(variants);
	}

	@Post
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create variants")
	public HttpResponse<List<DangerSourceVariant>> createVariants(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		Principal principal,
			@Body DangerSourceVariant[] variants) {
		logger.debug("POST JSON variants");

		try {
			Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);
			User user = userRepository.findById(principal.getName()).orElseThrow();
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (user.hasPermissionForRegion(region.getId())) {
				for (DangerSourceVariant variant : variants) {
					variant.setTextcat(dangerSourceVariantTextController.getTextForDangerSourceVariant(
						variant,
						dangerSourceVariantTextController.getDangerSourceVariantText(variant)));
				}
				dangerSourceVariantRepository.saveDangerSourceVariants(Arrays.asList(variants),
					instantRange.lowerEndpoint(), instantRange.upperEndpoint(), region);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			return getVariants(date, regionId);
		} catch (AlbinaException e) {
			logger.warn("Error creating variants", e);
			return getVariants(date, regionId);
		}
	}

	@Get("/variants/{variantId}")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@ApiResponse(description = "variant", content = @Content(schema = @Schema(implementation = DangerSourceVariant.class)))
	@Operation(summary = "Get variant by ID")
	public HttpResponse<DangerSourceVariant> getVariantById(
		@PathVariable("variantId") String variantId
	) {
		logger.debug("GET JSON danger source variant: {}", variantId);
		DangerSourceVariant variant = dangerSourceVariantRepository.findById(variantId).orElseThrow();
		return HttpResponse.ok(variant);
	}

	@Post("/variants/{variantId}")
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update danger source variant")
	public HttpResponse<List<DangerSourceVariant>> updateDangerSource(
		@PathVariable("variantId") String variantId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		Principal principal,
			@Body DangerSourceVariant variant) {
		logger.debug("POST JSON danger source variant");

		try {
			variant.setTextcat(dangerSourceVariantTextController.getTextForDangerSourceVariant(variant,
				dangerSourceVariantTextController.getDangerSourceVariantText(variant)));

			Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);

			User user = userRepository.findById(principal.getName()).orElseThrow();
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (user.hasPermissionForRegion(region.getId())) {
				dangerSourceVariantRepository.updateDangerSourceVariant(variant,
					instantRange.lowerEndpoint(), instantRange.upperEndpoint(), region);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			return getVariants(date, regionId);
		} catch (AlbinaException e) {
			logger.warn("Error creating danger source variant", e);
			return getVariants(date, regionId);
		}
	}

	@Delete("/variants/{variantId}")
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete danger source variant")
	public HttpResponse<List<DangerSourceVariant>> deleteVariant(
		@PathVariable("variantId") String variantId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		Principal principal) {
		logger.debug("DELETE JSON danger source variant");

		try {
			User user = userRepository.findById(principal.getName()).orElseThrow();
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (user.hasPermissionForRegion(region.getId())) {
				dangerSourceVariantRepository.deleteById(variantId);
			} else {
				throw new AlbinaException("User is not authorized for this region!");
			}

			return getVariants(date, regionId);
		} catch (AlbinaException e) {
			logger.warn("Error deleting variant", e);
			return getVariants(date, regionId);
		}
	}

	@Put("/variants")
	@Secured({ Role.Str.FORECASTER, Role.Str.FOREMAN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create danger source variant")
	public HttpResponse<List<DangerSourceVariant>> createVariant(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
		@QueryValue("region") String regionId,
		Principal principal,
			@Body DangerSourceVariant variant) {
		logger.debug("PUT JSON danger source variant");

		try {
			variant.setTextcat(dangerSourceVariantTextController.getTextForDangerSourceVariant(variant,
				dangerSourceVariantTextController.getDangerSourceVariantText(variant)));

			Range<Instant> instantRange = DateControllerUtil.parseInstantRange(date);

			User user = userRepository.findById(principal.getName()).orElseThrow();
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (user.hasPermissionForRegion(region.getId())) {
				dangerSourceVariantRepository.createDangerSourceVariant(variant,
					instantRange.lowerEndpoint(), instantRange.upperEndpoint(), region);
			} else
				throw new AlbinaException("User is not authorized for this region!");

			return getVariants(date, regionId);
		} catch (AlbinaException e) {
			logger.warn("Error creating danger source variant", e);
			return getVariants(date, regionId);
		}
	}
}
