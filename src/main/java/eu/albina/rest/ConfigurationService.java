package eu.albina.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.albina.controller.socialmedia.MessengerPeopleProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.socialmedia.*;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.hibernate.HibernateException;
import org.json.JSONException;
import org.json.JSONObject;

import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Path("/configuration")
@Api(value = "/configuration")
public class ConfigurationService {

	private static Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

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


	@POST
	@Path("/region")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setRegionConfiguration(
			String json) {
		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
		RegionConfigurationController rc= RegionConfigurationController.getInstance();
		try{
			RegionConfiguration regionConfiguration=ct.fromJson(json,RegionConfiguration.class);
			rc.saveRegionConfiguration(regionConfiguration);
			return Response.ok().build();
		}
		catch (HibernateException | JSONException | IOException | AlbinaException e) {
			logger.warn("Error subscribe - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build();
		}

	}

	@GET
	@Path("/region")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getRegionConfiguration(@QueryParam("regionId") String regionId) throws JsonProcessingException, AlbinaException {
		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
		RegionConfigurationController rcc=RegionConfigurationController.getInstance();
		RegionConfiguration regionConfiguration= rcc.getRegionConfiguration(regionId);
		return Response.ok(ct.toJson(regionConfiguration), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/channels")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChannels() throws AlbinaException, JsonProcessingException {
		RegionConfigurationController ct=RegionConfigurationController.getInstance();
		List<Channel> channelList=ct.getChannels();
		return Response.ok(ct.toJson(channelList), MediaType.APPLICATION_JSON).build();
	}

}