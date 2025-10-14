// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.util.Optional;
import java.util.List;

import eu.albina.controller.RegionRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.albina.exception.AlbinaException;
import eu.albina.util.JsonUtil;
import jakarta.inject.Inject;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/regions")
@Tag(name = "regions")
public class RegionService {

	private static final Logger logger = LoggerFactory.getLogger(RegionService.class);

	@Inject
	RegionRepository regionRepository;

	@Get
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all regions")
	@ApiResponse(description = "regions", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Region.class))))
	public HttpResponse<?> getRegions() {
		logger.debug("GET JSON regions");

		// TODO check if user has ADMIN rights for this region

		try {
			List<Region> regions = regionRepository.findAll();
			return HttpResponse.ok(regions);
		} catch (HibernateException he) {
			logger.warn("Error loading regions", he);
			return HttpResponse.badRequest().body(he.toString());
		}
	}

	@Get("/region")
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get region for ID")
	@ApiResponse(description = "region", content = @Content(schema = @Schema(implementation = Region.class)))
	public HttpResponse<?> getRegion(@QueryValue("region") String regionId) {
		logger.debug("GET JSON region");

		// TODO check if user has ADMIN rights for this region

		try {
			Region region = regionRepository.findById(regionId).orElseThrow();
			return HttpResponse.ok(region);
		} catch (HibernateException he) {
			logger.warn("Error loading region", he);
			return HttpResponse.badRequest().body(he.toString());
		}
	}

	@Put
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update region")
	public HttpResponse<?> updateRegion(
		@Body @Parameter(schema = @Schema(implementation = Region.class)) String regionString) {
		logger.debug("PUT JSON region");

		// TODO check if user has ADMIN rights for this region (UserRegionRoleLinks.class)

		try {
			String id = new Region(regionString, Region::new).getId();
			Optional<Region> optionalRegion = regionRepository.findById(id);
			if (optionalRegion.isPresent()) {
				Region existing = optionalRegion.get();
				// Avoid overwriting fields that are not contained in the JSON object sent by the frontend.
				// This happens whenever new fields are added to the backend but not yet to the frontend.
				JsonUtil.ALBINA_OBJECT_MAPPER.readerForUpdating(existing).readValue(regionString);
				existing.fixLanguageConfigurations();
				regionRepository.update(existing);
				return HttpResponse.ok(existing.toJSON());
			} else {
				String message = "Error updating region - Region does not exist";
				logger.warn(message);
				return HttpResponse.badRequest().body(new AlbinaException(message).toJSON());
			}
		} catch (HibernateException e) {
			logger.warn("Error updating region", e);
			return HttpResponse.badRequest().body(e.toString());
		} catch (JsonProcessingException e) {
			logger.warn("Error deserializing region", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Post
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create region")
	public HttpResponse<?> createRegion(
		@Body @Parameter(schema = @Schema(implementation = Region.class)) String regionString) {
		logger.debug("POST JSON region");
		try {
			Region region = new Region(regionString, Region::new);

			// check if id already exists
			if (regionRepository.findById(region.getId()).isEmpty()) {
				region.fixLanguageConfigurations();
				regionRepository.save(region);
				return HttpResponse.created(region);
			} else {
				String message = "Error creating region - Region already exists";
				logger.warn(message);
				return HttpResponse.badRequest(new AlbinaException(message).toJSON());
			}
		} catch (JsonProcessingException e) {
			logger.warn("Error deserializing region", e);
			return HttpResponse.badRequest(e.toString());
		}
	}

	@Get("/locked")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public HttpResponse<?> getLockedRegions(@QueryValue("region") String region) {
		return HttpResponse.serverError();
	}
}
