package eu.albina.rest;

import eu.albina.controller.socialmedia.MessengerPeopleProcessorController;
import eu.albina.model.Region;
import eu.albina.model.messengerpeople.MessengerPeopleNewsLetter;
import eu.albina.model.messengerpeople.MessengerPeopleNewsletterHistory;
import eu.albina.model.messengerpeople.MessengerPeopleTargets;
import eu.albina.model.messengerpeople.MessengerPeopleUser;
import eu.albina.model.socialmedia.RegionConfiguration;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

@Path("/social-media")
@Api(value = "/social-media")
public class SocialMediaService {

	private static Logger logger = LoggerFactory.getLogger(SocialMediaService.class);

	@Context
	UriInfo uri;

	// --------------------------------------
	// MESSAGE PEOPLE CALLS - BEGIN
	// --------------------------------------
	@GET
	@Path("/targets")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response targets() throws IOException {
		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
		MessengerPeopleTargets targets=ct.getTargets();
		return Response.ok(ct.toJson(targets), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/newsletter-history")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response newsLetterHistory() throws IOException {
		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
		MessengerPeopleNewsletterHistory targets=ct.getNewsLetterHistory(50);
		return Response.ok(ct.toJson(targets), MediaType.APPLICATION_JSON).build();

	}

	@POST
	@Path("/newsletter")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response newsLetterSend(
			@PathParam("content") @ApiParam("Message content")
			String content,
			@PathParam("attachmentUrl") @ApiParam("Attachment url")
			String attachmentUrl,
			@PathParam("targetId") @ApiParam("targetId on Messenger People")
			String targetId) throws IOException {
		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
		MessengerPeopleNewsLetter sendResult=ct.sendNewsLetter(targetId,content,attachmentUrl);
		return Response.ok(ct.toJson(sendResult), MediaType.APPLICATION_JSON).build();

	}

	@GET
	@Path("/users")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response users() throws IOException {
		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
		List<MessengerPeopleUser> targets=ct.getUsers(50,0);
		return Response.ok(ct.toJson(targets), MediaType.APPLICATION_JSON).build();
	}

	// --------------------------------------
	// MESSAGE PEOPLE CALLS - BEGIN
	// --------------------------------------

	// --------------------------------------
	// REGIONS -BEGIN
	// --------------------------------------

	@GET
	@Path("/configuration/regionConfiguration")
	@ApiOperation(value = "get list of regions configuration", notes = "List of regions", response = Region.class, tags={  })
	@ApiResponses(value = {
//			@ApiResponse(code = 2XX, message = "OK", response = Region.class),
//			@ApiResponse(code = 5XX, message = "Server error", response = Void.class)
	})
	@Produces(MediaType.APPLICATION_JSON)
	public Response regionsConfigurationsGet() {
		return Response.ok().entity("magic!").build();
	}


	@POST
	@Path("/configuration/regionConfiguration")
	@ApiOperation(value = "create a new regionConfiguration", notes = "regionConfiguration configuration", response = RegionConfiguration.class, tags={  })
	@ApiResponses(value = {
//			@ApiResponse(code = 2XX, message = "OK", response = RegionConfiguration.class),
//			@ApiResponse(code = 5XX, message = "Server error", response = Void.class)
	})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response regionsConfigurationsPost(@Valid RegionConfiguration body) {
		return Response.ok().entity("magic!").build();
	}


	@GET
	@Path("/configuration/regionConfiguration/{regionId}")
	@ApiOperation(value = "get regionConfiguration", notes = "", response = RegionConfiguration.class, tags={  })
	@ApiResponses(value = {
//			@ApiResponse(code = 2XX, message = "OK", response = RegionConfiguration.class),
//			@ApiResponse(code = 5XX, message = "Server error", response = Void.class)
	})
	@Produces(MediaType.APPLICATION_JSON)
	public Response regionsConfigurationsRegionIdGet(@PathParam("regionId") @ApiParam("Numeric ID of the user to get") String regionId) {
		return Response.ok().entity("magic!").build();
	}


	@PUT
	@Path("/configuration/regionConfiguration/{regionId}")
	@ApiOperation(value = "update regionConfiguration", notes = "", response = RegionConfiguration.class, tags={  })
	@ApiResponses(value = {
//			@ApiResponse(code = 2XX, message = "OK", response = RegionConfiguration.class),
//			@ApiResponse(code = 5XX, message = "Server error", response = Void.class)
	})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response regionsConfigurationsRegionIdPut(@Valid RegionConfiguration body,@PathParam("regionId") @ApiParam("Numeric ID of the user to put") String regionId) {
		return Response.ok().entity("magic!").build();
	}

	// --------------------------------------
	// REGIONS - END
	// --------------------------------------

//
//	public static void main(String args[]){
//		String pwd = BCrypt.hashpw("clesio", BCrypt.gensalt());
//		System.out.print(pwd);
//	}
}
