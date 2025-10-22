// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.controller.StressLevelRepository;
import eu.albina.controller.UserRepository;
import eu.albina.exception.AlbinaException;
import eu.albina.model.StressLevel;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
	private UserRepository userRepository;

	@Inject
	private StressLevelRepository stressLevelRepository;

	@Get
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "List stress level entries of user")
	public List<StressLevel> getStressLevels(
			Principal principal,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) throws AlbinaException {

		LocalDate startDate = OffsetDateTime.parse(start).toLocalDate();
		LocalDate endDate = OffsetDateTime.parse(end).toLocalDate();
		User user = userRepository.findByIdOrElseThrow(principal);
		Set<User> users = Collections.singleton(user);
		return stressLevelRepository.findByUserInAndDateBetween(users, startDate, endDate);
	}

	@Get("/team")
	@Secured(Role.Str.FORECASTER)
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "List stress level entries of team")
	public Map<UUID, List<StressLevel>> getTeamStressLevels(
			Principal principal,
			@QueryValue("region") String regionId,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) throws AlbinaException {

		LocalDate startDate = OffsetDateTime.parse(start).toLocalDate();
		LocalDate endDate = OffsetDateTime.parse(end).toLocalDate();
		User user = userRepository.findByIdOrElseThrow(principal);
		try {
			// check that user is member of requested region
			if (!user.hasPermissionForRegion(regionId)) {
				throw new HttpStatusException(HttpStatus.FORBIDDEN, regionId);
			}
			List<User> users = userRepository.findAll().stream()
					.filter(u -> !u.isDeleted())
					.filter(u -> u.hasRole(Role.FORECASTER) || u.hasRole(Role.FOREMAN))
					.filter(u -> user.getRoles().stream().anyMatch(u::hasRole))
					.filter(u -> u.hasPermissionForRegion(regionId))
					.collect(Collectors.toList());
			return StressLevel.randomizeUsers(stressLevelRepository.findByUserInAndDateBetween(users, startDate, endDate));
		} catch (Exception e) {
			logger.warn("Failed to get stress levels for region: " + regionId, e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create stress level entry")
	@Transactional
	public StressLevel postStressLevel(
			Principal principal,
			@Body StressLevel stressLevel) throws AlbinaException {

		User user = userRepository.findByIdOrElseThrow(principal);
		stressLevel.setUser(user);
		stressLevel = stressLevelRepository.updateOrSave(stressLevel);
		logger.info("Creating stress level {}", stressLevel);
		return stressLevel;
	}

}
