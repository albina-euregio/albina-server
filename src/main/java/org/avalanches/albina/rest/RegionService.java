package org.avalanches.albina.rest;

import java.io.StringWriter;

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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.avalanches.albina.controller.RegionController;
import org.avalanches.albina.exception.AlbinaException;
import org.avalanches.albina.model.Region;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.swagger.annotations.Api;

@Path("/locationCatalogs")
@Api(value = "/locationCatalogs")
public class RegionService {

	private static Logger logger = LoggerFactory.getLogger(RegionService.class);

	@Context
	UriInfo uri;

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getCaamlRegionCatalog(@PathParam("regionId") String regionId) {
		logger.debug("GET XML region: " + regionId);

		try {
			try {
				Region region = RegionController.getInstance().getRegion(regionId);
				if (region == null) {
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder;
					docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element rootElement = doc.createElement("message");
					rootElement.appendChild(doc.createTextNode("Region not found for ID: " + regionId));
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					StreamResult result = new StreamResult(new StringWriter());
					DOMSource source = new DOMSource(doc);
					transformer.transform(source, result);
					return Response.status(Response.Status.NOT_FOUND).entity(result.getWriter().toString()).build();
				}
				String caaml = region.toCAAML().toString();
				return Response.ok(caaml, MediaType.APPLICATION_XML).build();
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJsonRegionCatalog(@PathParam("regionId") String regionId) {
		logger.debug("GET JSON region: " + regionId);

		try {
			Region region = RegionController.getInstance().getRegion(regionId);
			if (region == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("message", "Region not found for ID: " + regionId);
				return Response.status(Response.Status.NOT_FOUND).entity(jsonObject.toString()).build();
			}
			String json = region.toJSON().toString();
			return Response.ok(json, MediaType.APPLICATION_JSON).build();
		} catch (AlbinaException e) {
			logger.warn("Error loading region: " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON().toString()).build();
		}
	}
}
