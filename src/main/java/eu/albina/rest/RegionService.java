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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import com.github.openjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.RegionController;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;

@Path("/regions")
@Api(value = "/regions")
public class RegionService {

	private static final Logger logger = LoggerFactory.getLogger(RegionService.class);

	@Context
	UriInfo uri;

	@GET
	@Secured({ Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER })
	@Path("/locked")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getLockedRegions(@QueryParam("region") String region) {
		logger.debug("GET JSON locked regions");

		JSONArray json = new JSONArray();
		for (DateTime date : RegionController.getInstance().getLockedRegions(region))
			json.put(date.toString(GlobalVariables.formatterDateTime));
		return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
	}
}
