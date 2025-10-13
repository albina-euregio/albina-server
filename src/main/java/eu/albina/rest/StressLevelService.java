// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.RegionController;
import eu.albina.controller.StressLevelController;
import eu.albina.controller.UserController;
import eu.albina.model.StressLevel;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.model.Region;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller("/user/stress-level")
@Tag(name = "user")
public class StressLevelService {

	private static final Logger logger = LoggerFactory.getLogger(StressLevelService.class);

	@Inject
	RegionController regionController;

	@Inject
	private UserController userController;

	@Get
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "List stress level entries of user")
	public HttpResponse<?> getStressLevels(
			Principal principal,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) {

		LocalDate startDate = OffsetDateTime.parse(start).toLocalDate();
		LocalDate endDate = OffsetDateTime.parse(end).toLocalDate();
		User user = userController.getUser(principal.getName());
		Set<User> users = Collections.singleton(user);
		List<StressLevel> stressLevels = StressLevelController.get(users, startDate, endDate);
		return HttpResponse.ok(stressLevels);
	}

	@Get("/team")
	@Secured(Role.Str.FORECASTER)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "List stress level entries of team")
	public HttpResponse<?> getTeamStressLevels(
			Principal principal,
			@QueryValue("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) {

		LocalDate startDate = OffsetDateTime.parse(start).toLocalDate();
		LocalDate endDate = OffsetDateTime.parse(end).toLocalDate();
		User user = userController.getUser(principal.getName());
		try {
			// check that user is member of requested region
			Region region = regionController.getRegion(regionId);
			if (!user.hasPermissionForRegion(region.getId())) {
				return HttpResponse.status(HttpStatus.FORBIDDEN);
			}
			List<User> users = userController.getUsers().stream()
					.filter(u -> !u.isDeleted())
					.filter(u -> u.hasRole(Role.FORECASTER) || u.hasRole(Role.FOREMAN))
					.filter(u -> user.getRoles().stream().anyMatch(u::hasRole))
					.filter(u -> u.hasPermissionForRegion(region.getId()))
					.collect(Collectors.toList());
			Map<UUID, List<StressLevel>> stressLevels = StressLevel.randomizeUsers(StressLevelController.get(users, startDate, endDate));
			return HttpResponse.ok(stressLevels);
		} catch (Exception e) {
			logger.warn("Failed to get stress levels for region: " + regionId, e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Post
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create stress level entry")
	public HttpResponse<?> postStressLevel(
			Principal principal,
			@Body StressLevel stressLevel) {

		User user = userController.getUser(principal.getName());
		stressLevel.setUser(user);
		stressLevel = StressLevelController.create(stressLevel);
		logger.info("Creating stress level {}", stressLevel);
		return HttpResponse.ok(stressLevel);
	}

}
