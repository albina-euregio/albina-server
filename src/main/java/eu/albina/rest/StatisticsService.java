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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.StatisticsController;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path("/statistics")
@Api(value = "/statistics")
public class StatisticsService {

	private static Logger logger = LoggerFactory.getLogger(StatisticsService.class);

	@Context
	UriInfo uri;

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getBulletinCsv(
			@ApiParam(value = "Start date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("startDate") String startDate,
			@ApiParam(value = "End date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("endDate") String endDate,
			@QueryParam("lang") LanguageCode language) {
		logger.debug("GET CSV bulletins");

		try {
			DateTime start = null;
			DateTime end = null;

			if (startDate != null)
				start = DateTime.parse(URLDecoder.decode(startDate, StandardCharsets.UTF_8.name()),
						GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
			else
				return Response.notAcceptable(null).build();
			if (endDate != null)
				end = DateTime.parse(URLDecoder.decode(endDate, StandardCharsets.UTF_8.name()),
						GlobalVariables.parserDateTime).toDateTime(DateTimeZone.UTC);
			else
				return Response.notAcceptable(null).build();

			String statistics = StatisticsController.getInstance().getDangerRatingStatistics(start, end, language,
					false);
			return Response.ok(new ByteArrayInputStream(statistics.getBytes()), MediaType.APPLICATION_OCTET_STREAM)
					.build();
		} catch (UnsupportedEncodingException e) {
			logger.warn("Error creating CSV - " + e.getMessage());
			return Response.status(400).build();
		}
	}
}
