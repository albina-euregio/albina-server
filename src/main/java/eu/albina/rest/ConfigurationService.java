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

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.hibernate.HibernateException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.albina.controller.socialmedia.MessengerPeopleProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.enumerations.Role;
import eu.albina.model.socialmedia.Channel;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.rest.filter.Secured;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;

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
	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setRegionConfiguration(String json) {
		MessengerPeopleProcessorController ct = MessengerPeopleProcessorController.getInstance();
		RegionConfigurationController rc = RegionConfigurationController.getInstance();
		try {
			RegionConfiguration regionConfiguration = ct.fromJson(json, RegionConfiguration.class);
			rc.saveRegionConfiguration(regionConfiguration);
			return Response.ok().build();
		} catch (HibernateException | JSONException | IOException | AlbinaException e) {
			logger.warn("Error subscribe", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.getMessage()).build();
		}

	}

	@GET
	@Path("/region")
	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getRegionConfiguration(@QueryParam("regionId") String regionId)
			throws JsonProcessingException, AlbinaException {
		MessengerPeopleProcessorController ct = MessengerPeopleProcessorController.getInstance();
		RegionConfigurationController rcc = RegionConfigurationController.getInstance();
		RegionConfiguration regionConfiguration = rcc.getRegionConfiguration(regionId);
		return Response.ok(ct.toJson(regionConfiguration), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/channels")
	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChannels() throws AlbinaException, JsonProcessingException {
		RegionConfigurationController ct = RegionConfigurationController.getInstance();
		List<Channel> channelList = ct.getChannels();
		return Response.ok(ct.toJson(channelList), MediaType.APPLICATION_JSON).build();
	}
}
