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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Path("/observations")
@Api(value = "/observations")
public class ObservationService {

	private static final Logger logger = LoggerFactory.getLogger(ObservationService.class);

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	public List<Observation> getObservations(
		@ApiParam(value = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("startDate") String start,
		@ApiParam(value = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("endDate") String end) {

		LocalDateTime startDate = OffsetDateTime.parse(start).toLocalDateTime();
		LocalDateTime endDate = OffsetDateTime.parse(end).toLocalDateTime();
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

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN })
	@Path("/export")
	@Produces("text/csv")
	public Response getBulletinCsv(
			@ApiParam(value = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("startDate") String startDate,
			@ApiParam(value = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("endDate") String endDate
			) {
		logger.debug("GET CSV observations");

		LocalDateTime start = null;
		LocalDateTime end = null;

		if (startDate != null)
			start = OffsetDateTime.parse(startDate).toLocalDateTime();
		else
			return Response.notAcceptable(null).build();
		if (endDate != null)
			end = OffsetDateTime.parse(endDate).toLocalDateTime();
		else
			return Response.notAcceptable(null).build();

		String statistics = ObservationController.getCsv(start, end);

		StringBuilder sbFilename = new StringBuilder();
		sbFilename.append("observations_");
		sbFilename.append(OffsetDateTime.parse(startDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		sbFilename.append("_");
		sbFilename.append(OffsetDateTime.parse(endDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		String filename = sbFilename.toString();

		try {
			File tmpFile = File.createTempFile(filename.toString(), ".csv");
			FileWriter writer = new FileWriter(tmpFile);
			writer.write(statistics);
			writer.close();

			return Response.ok(tmpFile).header(HttpHeaders.CONTENT_DISPOSITION,
			"attachment; filename=\"" + filename.toString() + ".csv\"").header(HttpHeaders.CONTENT_TYPE, "text/csv").build();
		} catch (IOException e) {
			logger.warn("Error creating statistics", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}
}
