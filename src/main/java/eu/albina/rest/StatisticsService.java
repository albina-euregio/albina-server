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
import eu.albina.exception.AlbinaException;
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
			@ApiParam(value = "Starttime in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("startDate") String startDate,
			@ApiParam(value = "Starttime in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("endDate") String endDate,
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

			String statistics = StatisticsController.getInstance().getDangerRatingStatistics(start, end, language);
			return Response.ok(new ByteArrayInputStream(statistics.getBytes()), MediaType.APPLICATION_OCTET_STREAM)
					.build();
		} catch (UnsupportedEncodingException e) {
			logger.warn("Error creating CSV - " + e.getMessage());
			return Response.status(400).build();
		} catch (AlbinaException e) {
			logger.warn("Error creating CSV - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}
}
