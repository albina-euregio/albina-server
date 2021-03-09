/*******************************************************************************
 * Copyright (C) 2021 albina-euregio
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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import io.swagger.annotations.Api;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/observations/lwdkip")
@Api(value = "/observations/lwdkip")
public class ObservationLwdKipService {

	private static final String ARCGIS_API = "https://gis.tirol.gv.at/arcgis/";
	private static final Logger logger = LoggerFactory.getLogger(ObservationLwdKipService.class);
	private static Token token;

	static class Token {
		public String token;
		public long expires;

		boolean isExpired() {
			return expires < System.currentTimeMillis() + 5_000;
		}
	}

	@GET
	@Secured({Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{layer}")
	public Response get(@PathParam("layer") String layer, @Context UriInfo uriInfo) throws Exception {
		if (token == null || token.isExpired()) {
			try {
				Request tokenRequest = Request.Post(ARCGIS_API + "tokens/").bodyForm(
					new BasicNameValuePair("username", System.getenv("ALBINA_LWDKIP_USERNAME")),
					new BasicNameValuePair("password", System.getenv("ALBINA_LWDKIP_PASSWORD")),
					new BasicNameValuePair("client", "requestip"),
					new BasicNameValuePair("f", "json"));
				String tokenJson = tokenRequest.execute().returnContent().asString();
				token = new ObjectMapper().readValue(tokenJson, Token.class);
			} catch (Exception ex) {
				logger.warn("Failed to obtain ArcGis token", ex);
				return Response.serverError().build();
			}
		}

		try {
			UriBuilder uriBuilder = UriBuilder.fromUri(ARCGIS_API + "rest/services/APPS_DVT/lwdkip/mapserver/" + layer + "/query");
			uriBuilder.queryParam("token", token.token);
			uriInfo.getQueryParameters().forEach((key, values) -> uriBuilder.queryParam(key, values.toArray()));
			URI uri = uriBuilder.build();
			String json = Request.Get(uri).execute().returnContent().asString();
			return Response.ok(json).build();
		} catch (Exception ex) {
			logger.warn("Failed to perform ArcGis query", ex);
			return Response.serverError().build();
		}
	}

}
