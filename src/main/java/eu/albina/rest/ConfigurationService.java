package eu.albina.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.JSONObject;

import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;

@Path("/configuration")
@Api(value = "/configuration")
public class ConfigurationService {

	// private static Logger logger =
	// LoggerFactory.getLogger(ConfigurationService.class);

	@Context
	UriInfo uri;

	@POST
	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setConfigurationParameter(String configuration) {
		try {
			JSONObject configurationJson = new JSONObject(configuration);
			GlobalVariables.setConfigurationParameters(configurationJson);
			return Response.ok().build();
		} catch (ConfigurationException e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getConfigurationParameters() {
		return Response.ok(GlobalVariables.getConfigProperties().toString(), MediaType.APPLICATION_JSON).build();
	}
}