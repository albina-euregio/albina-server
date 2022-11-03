/*******************************************************************************
 * Copyright (C) 2021 albina
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

import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.HttpClientUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Path("/observations/lwdkip")
@Hidden
public class ObservationLwdKipService {

	private static final String ARCGIS_API = "https://gis.tirol.gv.at/arcgis/";
	private static final Logger logger = LoggerFactory.getLogger(ObservationLwdKipService.class);
	private static Token token;
	private final Client client = HttpClientUtil.newClientBuilder()
		.register(PatchedJacksonJsonProvider.class)
		.build();

	static class Token {
		public String token;
		public long expires;

		boolean isExpired() {
			return expires < System.currentTimeMillis() + 5_000;
		}
	}

	static class PatchedJacksonJsonProvider extends JacksonJsonProvider {
		@Override
		public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
			// fix for MessageBodyReader not found for media type=text/plain;charset=utf-8
			return super.isReadable(type, genericType, annotations, mediaType)
				|| super.isReadable(type, genericType, annotations, MediaType.APPLICATION_JSON_TYPE);
		}
	}

	@GET
	@Secured({Role.ADMIN, Role.FORECASTER, Role.FOREMAN, Role.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{layer}")
	public Response get(@PathParam("layer") String layer, @Context UriInfo uriInfo) throws Exception {
		if (token == null || token.isExpired()) {
			try {
				Form form = new Form();
				form.param("username", System.getenv("ALBINA_LWDKIP_USERNAME"));
				form.param("password", System.getenv("ALBINA_LWDKIP_PASSWORD"));
				form.param("client", "requestip");
				form.param("f", "json");
				Entity<Form> entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
				WebTarget target = client.target(ARCGIS_API + "tokens/");
				token = target.request().post(entity, Token.class);
			} catch (Exception ex) {
				logger.warn("Failed to obtain ArcGis token", ex);
				return Response.serverError().build();
			}
		}

		try {
			WebTarget target = client.target(ARCGIS_API + "rest/services/APPS_DVT/lwdkip/mapserver/" + layer + "/query");
			target = target.queryParam("token", ObservationLwdKipService.token.token);
			for (Map.Entry<String, List<String>> queryParameter : uriInfo.getQueryParameters().entrySet()) {
				target = target.queryParam(queryParameter.getKey(), queryParameter.getValue().toArray());
			}
			Object json = target.request().get(String.class);
			return Response.ok(json, MediaType.APPLICATION_JSON).build();
		} catch (Exception ex) {
			logger.warn("Failed to perform ArcGis query", ex);
			return Response.serverError().build();
		}
	}

}
