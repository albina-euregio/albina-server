// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.UserRepository;
import eu.albina.exception.AlbinaException;
import eu.albina.jobs.ChannelStatusJob;
import eu.albina.model.Region;
import eu.albina.model.StatusInformation;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

import java.security.Principal;
import java.util.List;

@Controller("/status")
@Tag(name = "status")
public class StatusService {

	@Inject
	private ChannelStatusJob channelStatusJob;

	@Inject
	private UserRepository userRepository;

	@Get("/channels")
	@Secured({Role.Str.SUPERADMIN, Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get status of publication channels that are associated with the current user's regions")
	public List<StatusInformation> getStatus(Principal principal) throws AlbinaException {
		// obtain all regions for which this user has permissions
		String username = principal.getName();
		User user = userRepository.findById(username).orElseThrow();
		List<StatusInformation> result = new java.util.ArrayList<>();
		for (Region region : user.getRegions()) {
			result.addAll(channelStatusJob.getOrTriggerStatusForRegion(region.getId()));
		}
		return result;
	}

	@Post("/channels")
	@Secured({Role.Str.SUPERADMIN, Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Trigger status checks for all publication channels that are associated with the current user's regions")
	public List<StatusInformation> triggerStatusChecks(Principal principal) throws AlbinaException {
		// obtain all regions for which this user has permissions
		String username = principal.getName();
		User user = userRepository.findById(username).orElseThrow();
		List<StatusInformation> result = new java.util.ArrayList<>();
		for (Region region : user.getRegions()) {
			result.addAll(channelStatusJob.triggerStatusChecks(region));
		}
		return result;
	}
}
