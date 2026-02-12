// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.io.IOException;
import java.util.Optional;
import java.util.List;

import eu.albina.controller.RegionRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;

import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.Region;
import eu.albina.model.enumerations.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/regions")
@Tag(name = "regions")
public class RegionService {

	private static final Logger logger = LoggerFactory.getLogger(RegionService.class);

	@Inject
	RegionRepository regionRepository;

	@Inject
	private ObjectMapper objectMapper;

	@Get
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get all regions")
	public List<Region> getRegions() {
		// TODO check if user has ADMIN rights for this region
		return regionRepository.findAll();
	}

	@Get("/region")
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get region for ID")
	public Region getRegion(@QueryValue("region") String regionId) {
		// TODO check if user has ADMIN rights for this region
		return regionRepository.findById(regionId).orElseThrow();
	}

	@Post
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create or update region")
	public Region saveRegion(
		@Body @Parameter(schema = @Schema(implementation = Region.class)) String regionString) {
		// TODO check if user has ADMIN rights for this region (UserRegionRoleLinks.class)

		try {
			String id = objectMapper.readValue(regionString, Region.class).getId();
			Optional<Region> optionalRegion = regionRepository.findById(id);
			if (optionalRegion.isPresent()) {
				Region existing = optionalRegion.get();
				// Avoid overwriting fields that are not contained in the JSON object sent by the frontend.
				// This happens whenever new fields are added to the backend but not yet to the frontend.
				existing.updateFromJSON(regionString, objectMapper);
				existing.fixLanguageConfigurations();
				regionRepository.update(existing);
				return existing;
			} else {
				Region region = objectMapper.readValue(regionString, Region.class);
				region.fixLanguageConfigurations();
				regionRepository.update(region); // with `save` we get  "detached entity passed to persist: eu.albina.model.ServerInstance"
				return region;
			}
		} catch (IOException | PersistenceException e) {
			logger.warn("Error updating region", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}
