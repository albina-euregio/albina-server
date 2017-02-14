package org.avalanches.ais.rest;

import java.io.FileNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.avalanches.ais.util.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;

@Path("/schemas")
@Api(value = "/schemas")
public class JsonSchemaService {

	private static Logger logger = LoggerFactory.getLogger(JsonSchemaService.class);

	@Context
	UriInfo uri;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getJSONSchema(@PathParam("type") String type) {
		logger.debug("GET JSON schema");

		try {
			String json = GlobalVariables.getFileString(type);
			return Response.ok(json, MediaType.APPLICATION_JSON).build();
		} catch (FileNotFoundException e) {
			return Response.status(404).type(MediaType.APPLICATION_JSON).build();
		}
	}

}
