package org.avalanches.albina.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.avalanches.albina.controller.RegionController;
import org.avalanches.albina.exception.AlbinaException;
import org.avalanches.albina.model.Region;
import org.avalanches.albina.util.AlbinaUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
				JSONObject json = AlbinaUtil.createRegionHeaderJson();
				JSONArray features = new JSONArray();
				for (Region entry : regions) {
					features.put(entry.toJSON());
				}
				json.put("features", features);
				return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error loading regions - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
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
					Document doc = AlbinaUtil.createXmlError("message", "No regions found!");
					return Response.status(Response.Status.NOT_FOUND).entity(AlbinaUtil.convertDocToString(doc))
							.build();
				} else {
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element rootElement = AlbinaUtil.createRegionHeaderCaaml(doc);
					Element locations = doc.createElement("locations");

					for (Region entry : regions)
						locations.appendChild(entry.toCAAML(doc));

					rootElement.appendChild(locations);

					return Response.ok(AlbinaUtil.convertDocToString(doc), MediaType.APPLICATION_XML).build();
				}
			} catch (AlbinaException e) {
				logger.warn("Error loading region: " + e.getMessage());
				return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toXML()).build();
			}
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(ex.getMessage().toString()).build();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.getMessage().toString()).build();
		}
	}

	@GET
	@Path("/{regionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonRegiong(@PathParam("regionId") String regionId) {
		logger.debug("GET JSON region: " + regionId);

		try {
			Region region = RegionController.getInstance().getRegion(regionId);
			if (region == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("message", "Region not found for ID: " + regionId);
				return Response.status(Response.Status.NOT_FOUND).entity(jsonObject.toString()).build();
			} else {
				JSONObject json = AlbinaUtil.createRegionHeaderJson();
				JSONArray features = new JSONArray();
				features.put(region.toJSON());
				json.put("features", features);
				return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error loading region: " + e.getMessage());
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
					Document doc = AlbinaUtil.createXmlError("message", "Region not found for ID: " + regionId);
					return Response.status(Response.Status.NOT_FOUND).entity(AlbinaUtil.convertDocToString(doc))
							.build();
				} else {
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element rootElement = AlbinaUtil.createRegionHeaderCaaml(doc);
					Element locations = doc.createElement("locations");

					locations.appendChild(region.toCAAML(doc));

					rootElement.appendChild(locations);

					return Response.ok(AlbinaUtil.convertDocToString(doc), MediaType.APPLICATION_XML).build();
				}
			} catch (AlbinaException e) {
				logger.warn("Error loading region: " + e.getMessage());
				return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toXML()).build();
			}
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(ex.getMessage().toString()).build();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.getMessage().toString()).build();
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
				JSONObject json = AlbinaUtil.createRegionHeaderJson();
				JSONArray features = new JSONArray();
				for (Region entry : regions) {
					features.put(entry.toJSON());
				}
				json.put("features", features);
				return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error loading regions - " + e.getMessage());
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
					Document doc = AlbinaUtil.createXmlError("message", "No subregions found for region: " + regionId);
					return Response.status(Response.Status.NOT_FOUND).entity(AlbinaUtil.convertDocToString(doc))
							.build();
				} else {
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element rootElement = AlbinaUtil.createRegionHeaderCaaml(doc);
					Element locations = doc.createElement("locations");

					for (Region entry : regions)
						locations.appendChild(entry.toCAAML(doc));

					rootElement.appendChild(locations);

					return Response.ok(AlbinaUtil.convertDocToString(doc), MediaType.APPLICATION_XML).build();
				}
			} catch (AlbinaException e) {
				logger.warn("Error loading region: " + e.getMessage());
				return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.toXML()).build();
			}
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(ex.getMessage().toString()).build();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return Response.status(400).type(MediaType.APPLICATION_XML).entity(e.getMessage().toString()).build();
		}
	}
}
