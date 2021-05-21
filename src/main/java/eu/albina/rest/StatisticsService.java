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

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.OffsetDateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.StatisticsController;
import eu.albina.model.enumerations.LanguageCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path("/statistics")
@Api(value = "/statistics")
public class StatisticsService {

	private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

	@Context
	UriInfo uri;

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getBulletinCsv(
			@ApiParam(value = "Start date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("startDate") String startDate,
			@ApiParam(value = "End date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("endDate") String endDate,
			@QueryParam("lang") LanguageCode language, @QueryParam("extended") boolean extended,
			@QueryParam("duplicate") boolean duplicate) {
		logger.debug("GET CSV bulletins");

		Instant start = null;
		Instant end = null;

		if (startDate != null)
			start = OffsetDateTime.parse(startDate).toInstant();
		else
			return Response.notAcceptable(null).build();
		if (endDate != null)
			end = OffsetDateTime.parse(endDate).toInstant();
		else
			return Response.notAcceptable(null).build();

		String statistics = StatisticsController.getInstance().getDangerRatingStatistics(start, end, language, extended,
				duplicate);
		return Response.ok(new ByteArrayInputStream(statistics.getBytes()), MediaType.APPLICATION_OCTET_STREAM).build();
	}
}
