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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.controller.RegionController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.XmlUtil;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;

@Path("/regions")
@Api(value = "/regions")
public class RegionService {

	private static Logger logger = LoggerFactory.getLogger(RegionService.class);

	@Context
	UriInfo uri;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getJsonRegions() {
		logger.debug("GET JSON top level regions");

		try {
			List<Region> regions = RegionController.getInstance().getRegions();
			if (regions == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("message", "No regions found!");
				return Response.status(Response.Status.NOT_FOUND).entity(jsonObject.toString()).build();
			} else {
				JSONObject json = createRegionHeaderJson();
				JSONArray features = new JSONArray();
				for (Region entry : regions) {
					features.put(entry.toJSON());
				}
				json.put("features", features);
				return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error loading regions", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

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

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response getCaamlRegions() {
		logger.debug("GET XML top level regions");

		try {
			try {
				List<Region> regions = RegionController.getInstance().getRegions();
				if (regions == null) {
					Document doc = XmlUtil.createXmlError("message", "No regions found!");
					return Response.status(Response.Status.NOT_FOUND).entity(XmlUtil.convertDocToString(doc))
							.build();
				} else {
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element rootElement = XmlUtil.createRegionHeaderCaaml(doc);
					Element locations = doc.createElement("locations");

					for (Region entry : regions)
						locations.appendChild(entry.toCAAML(doc));

					rootElement.appendChild(locations);

					return Response.ok(XmlUtil.convertDocToString(doc), MediaType.APPLICATION_XML).build();
				}
			} catch (AlbinaException e) {
				logger.warn("Error loading region", e);
				return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toXML()).build();
			}
		} catch (TransformerException | ParserConfigurationException ex) {
			logger.warn("Error serializing regions", ex);
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(ex.getMessage().toString()).build();
		}
	}

	@GET
	@Path("/{regionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonRegion(@PathParam("regionId") String regionId) {
		logger.debug("GET JSON region: " + regionId);

		try {
			Region region = RegionController.getInstance().getRegion(regionId);
			if (region == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("message", "Region not found for ID: " + regionId);
				return Response.status(Response.Status.NOT_FOUND).entity(jsonObject.toString()).build();
			} else {
				JSONObject json = createRegionHeaderJson();
				JSONArray features = new JSONArray();
				features.put(region.toJSON());
				json.put("features", features);
				return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error loading region", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}

	@GET
	@Path("/{regionId}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getCaamlRegion(@PathParam("regionId") String regionId) {
		logger.debug("GET XML region: " + regionId);

		try {
			try {
				Region region = RegionController.getInstance().getRegion(regionId);
				if (region == null) {
					Document doc = XmlUtil.createXmlError("message", "Region not found for ID: " + regionId);
					return Response.status(Response.Status.NOT_FOUND).entity(XmlUtil.convertDocToString(doc))
							.build();
				} else {
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element rootElement = XmlUtil.createRegionHeaderCaaml(doc);
					Element locations = doc.createElement("locations");

					locations.appendChild(region.toCAAML(doc));

					rootElement.appendChild(locations);

					return Response.ok(XmlUtil.convertDocToString(doc), MediaType.APPLICATION_XML).build();
				}
			} catch (AlbinaException e) {
				logger.warn("Error loading region", e);
				return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toXML()).build();
			}
		} catch (TransformerException | ParserConfigurationException ex) {
			logger.warn("Error serializing regions", ex);
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(ex.getMessage().toString()).build();
		}
	}

	@GET
	@Path("/{regionId}/subregions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonSubregions(@PathParam("regionId") String regionId) {
		logger.debug("GET JSON subregions");

		try {
			List<Region> regions = RegionController.getInstance().getRegions(regionId);
			if (regions == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("message", "No subregions found for region: " + regionId);
				return Response.status(Response.Status.NOT_FOUND).entity(jsonObject.toString()).build();
			} else {
				JSONObject json = createRegionHeaderJson();
				JSONArray features = new JSONArray();
				for (Region entry : regions) {
					features.put(entry.toJSON());
				}
				json.put("features", features);
				return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error loading regions", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@GET
	@Path("/{regionId}/subregions")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getXmlSubregions(@PathParam("regionId") String regionId) {
		logger.debug("GET XML subregions");

		try {
			try {
				List<Region> regions = RegionController.getInstance().getRegions(regionId);
				if (regions == null) {
					Document doc = XmlUtil.createXmlError("message", "No subregions found for region: " + regionId);
					return Response.status(Response.Status.NOT_FOUND).entity(XmlUtil.convertDocToString(doc))
							.build();
				} else {
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element rootElement = XmlUtil.createRegionHeaderCaaml(doc);
					Element locations = doc.createElement("locations");

					for (Region entry : regions)
						locations.appendChild(entry.toCAAML(doc));

					rootElement.appendChild(locations);

					return Response.ok(XmlUtil.convertDocToString(doc), MediaType.APPLICATION_XML).build();
				}
			} catch (AlbinaException e) {
				logger.warn("Error loading region", e);
				return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toXML()).build();
			}
		} catch (TransformerException | ParserConfigurationException ex) {
			logger.warn("Error serializing regions", ex);
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(ex.getMessage().toString()).build();
		}
	}

	private static JSONObject createRegionHeaderJson() {
		JSONObject json = new JSONObject();
		json.put("type", "FeatureCollection");
		JSONObject crs = new JSONObject();
		crs.put("type", "name");
		JSONObject properties = new JSONObject();
		properties.put("name", GlobalVariables.referenceSystemUrn);
		crs.put("properties", properties);
		json.put("crs", crs);
		return json;
	}
}
