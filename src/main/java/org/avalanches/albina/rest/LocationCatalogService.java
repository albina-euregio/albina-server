package org.avalanches.albina.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;

@Path("/locationCatalogs")
@Api(value = "/locationCatalogs")
public class LocationCatalogService {

	private static Logger logger = LoggerFactory.getLogger(LocationCatalogService.class);

	@Context
	UriInfo uri;

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getCaamlRegionCatalog() {
		// TODO implement
		// query params: region, subregion
		return Response.noContent().build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonRegionCatalog() {
		// TODO implement
		return Response.noContent().build();
	}
}
