package eu.albina.rest;

import java.io.Serializable;
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

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.NewsController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.News;
import eu.albina.rest.filter.Secured;
import eu.albina.util.GlobalVariables;
import io.swagger.annotations.Api;

@Path("/news")
@Api(value = "/news")
public class NewsService {

	private static Logger logger = LoggerFactory.getLogger(NewsService.class);

	@Context
	UriInfo uri;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getJsonNews(@QueryParam("from") String from, @QueryParam("until") String until) {
		logger.debug("GET JSON news");

		DateTime startDate = null;
		DateTime endDate = null;

		if (from != null)
			startDate = DateTime.parse(from, GlobalVariables.formatterDateTime);
		if (until != null)
			endDate = DateTime.parse(until, GlobalVariables.formatterDateTime);

		try {
			List<News> news = NewsController.getInstance().getNews(startDate, endDate);
			if (news == null) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("message", "No news found!");
				return Response.status(Response.Status.NOT_FOUND).entity(jsonObject.toString()).build();
			} else {
				JSONArray json = new JSONArray();
				for (News entry : news) {
					json.put(entry.toJSON());
				}
				return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
			}
		} catch (AlbinaException e) {
			logger.warn("Error loading news - " + e.getMessage());
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		}
	}

	@POST
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJsonNews(String newsString) {
		logger.debug("POST JSON news");

		JSONObject newsJson = new JSONObject(newsString);

		JSONObject validationResult = eu.albina.json.JsonValidator.validateNews(newsString);
		if (validationResult.length() == 0) {
			News news = new News(newsJson);
			try {
				Serializable newsId = NewsController.getInstance().saveNews(news);
				if (newsId == null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.append("message", "News not saved!");
					return Response.status(400).type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("newsId", newsId);
					return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(newsId)).build())
							.type(MediaType.APPLICATION_JSON).entity(jsonObject.toString()).build();
				}
			} catch (AlbinaException e) {
				logger.warn("Error creating news - " + e.getMessage());
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
			}
		} else
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(validationResult.toString()).build();
	}
}
