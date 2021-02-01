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

import eu.albina.controller.ObservationController;
import eu.albina.model.Observation;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.time.OffsetDateTime;
import java.util.List;

@Path("/observations")
@Api(value = "/observations")
public class ObservationService {

	private static final Logger logger = LoggerFactory.getLogger(ObservationService.class);

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	public List<Observation> getObservations(
		@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("startDate") String start,
		@ApiParam(value = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("endDate") String end) {

		OffsetDateTime startDate = OffsetDateTime.parse(start);
		OffsetDateTime endDate = OffsetDateTime.parse(end);
		return ObservationController.get(startDate, endDate);
	}

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@Path("/{id}")
	public Observation getObservation(@PathParam("id") long id) {
		return ObservationController.get(id);
	}

	@POST
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	public Observation postObservation(Observation observation) {
		observation.setId(null);
		logger.info("Creating observation {}", observation);
		return ObservationController.create(observation);
	}

	@PUT
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@Path("/{id}")
	public Observation putObservation(@PathParam("id") long id, Observation observation) {
		observation.setId(id);
		logger.info("Updating observation {}", observation);
		return ObservationController.update(observation);
	}

	@DELETE
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@Path("/{id}")
	public void deleteObservation(@PathParam("id") long id) {
		logger.info("Deleting observation {}", id);
		ObservationController.delete(id);
	}

}
