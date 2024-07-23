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

import eu.albina.controller.StressLevelController;
import eu.albina.controller.UserController;
import eu.albina.model.StressLevel;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Path("/user/stress-level")
@Tag(name = "user")
public class StressLevelService {

	private static final Logger logger = LoggerFactory.getLogger(StressLevelService.class);

	@GET
	@Secured({Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "List stress level entries of user")
	public List<StressLevel> getObservations(
		@Context SecurityContext securityContext,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("startDate") String start,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("endDate") String end) {

		User user = new User(securityContext.getUserPrincipal().getName());
		LocalDate startDate = OffsetDateTime.parse(start).toLocalDate();
		LocalDate endDate = OffsetDateTime.parse(end).toLocalDate();
		return StressLevelController.get(user, startDate, endDate);
	}

	@POST
	@Secured({Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create stress level entry")
	public Response postObservation(
		@Context SecurityContext securityContext,
		StressLevel stressLevel) {

		User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());
		stressLevel.setUser(user);
		StressLevelController.create(stressLevel);
		logger.info("Creating stress level {}", stressLevel);
		return Response.ok().build();
	}

}
