package org.avalanches.albina.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.avalanches.albina.model.enumerations.Aspect;
import org.avalanches.albina.model.enumerations.AvalancheProblem;
import org.avalanches.albina.model.enumerations.AvalancheType;
import org.avalanches.albina.model.enumerations.CountryCode;
import org.avalanches.albina.model.enumerations.DangerRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;

@Path("/incidents")
@Api(value = "/incidents")
public class AvalancheIncidentService {

	private static Logger logger = LoggerFactory.getLogger(AvalancheIncidentService.class);

	@Context
	UriInfo uri;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIncidents(@QueryParam("country") CountryCode country, @QueryParam("region") String region,
			@QueryParam("subregion") String subregion, @QueryParam("from") String from, @QueryParam("to") String to,
			@QueryParam("above") String above, @QueryParam("below") String below,
			@QueryParam("angleFrom") int angleFrom, @QueryParam("angleTo") int angleTo,
			@QueryParam("aspects") List<Aspect> aspects, @QueryParam("size") String size,
			@QueryParam("avalancheType") AvalancheType type, @QueryParam("remote") String remote,
			@QueryParam("avalancheProblems") List<AvalancheProblem> avalancheProblems,
			@QueryParam("dangerPatterns") List<Integer> dangerPatterns,
			@QueryParam("dangerRatings") List<DangerRating> dangerRatings, @QueryParam("fatal") boolean isFatal) {
		logger.debug("GET incidents");

		// TODO implement getIncidents()

		return Response.status(501).build();
	}

	@GET
	@Path("/{incidentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIncident(@PathParam("incidentId") String incidentId) {
		logger.debug("GET incident");

		// TODO implement getIncident()

		return Response.status(501).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createIncident() {
		logger.debug("POST incident");

		// TODO implement createIncident()

		return Response.status(501).build();
	}

	@PUT
	@Path("/{incidentId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateIncident(@PathParam("incidentId") String incidentId) {
		logger.debug("PUT incident");

		// TODO implement updateIncident()

		return Response.status(501).build();
	}

	@DELETE
	@Path("/{incidentId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteIncident(@PathParam("incidentId") String incidentId) {
		logger.debug("DELETE incident");

		// TODO implement deleteIncident()

		return Response.status(501).build();
	}
}
