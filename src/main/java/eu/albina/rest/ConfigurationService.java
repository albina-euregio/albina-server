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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONObject;

import eu.albina.controller.ServerInstanceController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.annotations.Api;

@Path("/configuration")
@Api(value = "/configuration")
public class ConfigurationService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

	@Context
	UriInfo uri;

	@POST
	@Secured({ Role.SUPERADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setConfigurationParameter(String serverInstance) {
		try {
			JSONObject serverInstanceJson = new JSONObject(serverInstance);
			ServerInstanceController.getInstance().updateServerInstance(new ServerInstance(serverInstanceJson));
			return Response.ok().build();
		} catch (AlbinaException e) {
			logger.warn("Error updating local server configuration", e);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Secured({ Role.SUPERADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getConfigurationParameters() {
		return Response.ok(ServerInstanceController.getInstance().getLocalServerInstance().toJSON(), MediaType.APPLICATION_JSON).build();
	}
}
