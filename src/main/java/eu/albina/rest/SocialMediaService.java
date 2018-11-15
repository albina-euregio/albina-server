package eu.albina.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.albina.controller.socialmedia.*;
import eu.albina.exception.AlbinaException;
import eu.albina.model.messengerpeople.MessengerPeopleNewsLetter;
import eu.albina.model.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.rapidmail.mailings.PostMailingsResponse;
import eu.albina.model.rapidmail.recipientlist.RapidMailRecipientListResponse;
import eu.albina.model.rapidmail.recipients.get.GetRecipientsResponse;
import eu.albina.model.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.model.rapidmail.recipients.post.PostRecipientsResponse;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.model.socialmedia.Shipment;
import io.swagger.annotations.*;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.TwitterException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

@Path("/social-media")
@Api(value = "/social-media")
public class SocialMediaService {

	private static Logger logger = LoggerFactory.getLogger(SocialMediaService.class);

	@Context
	UriInfo uri;

//	// --------------------------------------
//	// MESSAGE PEOPLE CALLS - BEGIN
//	// --------------------------------------
//	@GET
//	@Path("/messenger-people/targets")
////	@Secured({ Role.ADMIN })
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response targets() throws IOException {
//		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
//		MessengerPeopleTargets targets=ct.getTargets();
//		return Response.ok(ct.toJson(targets), MediaType.APPLICATION_JSON).build();
//	}
//
//	@GET
//	@Path("/messenger-people/newsletter-history")
////	@Secured({ Role.ADMIN })
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response newsLetterHistory() throws IOException {
//		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
//		MessengerPeopleNewsletterHistory newsletterHistory=ct.getNewsLetterHistory(50);
//		return Response.ok(ct.toJson(newsletterHistory), MediaType.APPLICATION_JSON).build();
//
//	}
//
//	@POST
//	@Path("/messenger-people/newsletter")
////	@Secured({ Role.ADMIN })
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response newsLetterSend(
//			@PathParam("content") @ApiParam("Message content")
//			String content,
//			@PathParam("attachmentUrl") @ApiParam("Attachment url")
//			String attachmentUrl,
//			@PathParam("targetId") @ApiParam("targetId on Messenger People")
//			String targetId) throws IOException {
//		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
//		MessengerPeopleNewsLetter sendResult=ct.sendNewsLetter(targetId,content,attachmentUrl);
//		return Response.ok(ct.toJson(sendResult), MediaType.APPLICATION_JSON).build();
//
//	}
//
//	@GET
//	@Path("/messenger-people/users")
////	@Secured({ Role.ADMIN })
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response users() throws IOException {
//		MessengerPeopleProcessorController ct=MessengerPeopleProcessorController.getInstance();
//		List<MessengerPeopleUser> targets=ct.getUsers(50,0);
//		return Response.ok(ct.toJson(targets), MediaType.APPLICATION_JSON).build();
//	}
////
////	 --------------------------------------
////	 MESSAGE PEOPLE CALLS - END
////	 --------------------------------------

	// --------------------------------------
	// COMMON USAGE - BEGIN
	// --------------------------------------
	@POST
	@Path("/rapidmail/send-message/{region-id}/{language}")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendRapidMail(
			@PathParam("region-id") @ApiParam("Region id")
			String regionId,
			@PathParam("language") @ApiParam("Language id")
			String language,
			@ApiParam("Send message content")
			String content
	) throws IOException, AlbinaException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		RapidMailProcessorController ctRm=RapidMailProcessorController.getInstance();
		PostMailingsRequest mailingsPost= ctRm.fromJson(content, PostMailingsRequest.class);
		RegionConfigurationController ctRc=RegionConfigurationController.getInstance();
		RegionConfiguration rc=ctRc.getRegionConfiguration(regionId);
		HttpResponse response=ctRm.sendMessage(rc.getRapidMailConfig(),language, mailingsPost);
		return Response
				.status(response.getStatusLine().getStatusCode())
				.entity(response.getEntity().getContent())
				.header("Content-Type",response.getEntity().getContentType())
				.build();
	}

	@GET
	@Path("/rapidmail/recipient-list/{region-id}")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRecipientList(
			@PathParam("region-id") @ApiParam("Region id")
			String regionId
	) throws AlbinaException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		RegionConfigurationController ctRc=RegionConfigurationController.getInstance();
		RegionConfiguration rc=ctRc.getRegionConfiguration(regionId);
		RapidMailProcessorController ctRm=RapidMailProcessorController.getInstance();
		HttpResponse response=ctRm.getRecipientsList(rc.getRapidMailConfig());
		return Response
				.status(response.getStatusLine().getStatusCode())
				.entity(response.getEntity().getContent())
				.header("Content-Type",response.getEntity().getContentType())
				.build();
	}

	@POST
	@Path("/rapidmail/recipients/{region-id}")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response addRecipient(
			@PathParam("region-id") @ApiParam("Region id")
			String regionId,
			@ApiParam("Recipient data")
			String content
	) throws AlbinaException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		RegionConfigurationController ctRc=RegionConfigurationController.getInstance();
		RegionConfiguration rc=ctRc.getRegionConfiguration(regionId);
		RapidMailProcessorController ctRm=RapidMailProcessorController.getInstance();
		HttpResponse response=ctRm.createRecipient(rc.getRapidMailConfig(),ctRm.fromJson(content, PostRecipientsRequest.class));
		return Response
				.status(response.getStatusLine().getStatusCode())
				.entity(response.getEntity().getContent())
				.header("Content-Type",response.getEntity().getContentType())
				.build();
	}

	@DELETE
	@Path("/rapidmail/recipients/{region-id}/{recipient-id}")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteRecipient(
			@PathParam("region-id") @ApiParam("Region id")
					String regionId,
			@PathParam("recipient-id") @ApiParam("Recipient id")
					Integer recipientId,
			@ApiParam("Send message content")
					String content
	) throws AlbinaException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		RegionConfigurationController ctRc=RegionConfigurationController.getInstance();
		RegionConfiguration rc=ctRc.getRegionConfiguration(regionId);
		RapidMailProcessorController ctRm=RapidMailProcessorController.getInstance();
		HttpResponse response=ctRm.deleteRecipient(rc.getRapidMailConfig(),recipientId);
		return Response
				.status(response.getStatusLine().getStatusCode())
				.entity(response.getEntity().getContent())
				.header("Content-Type",response.getEntity().getContentType())
				.build();
	}

	@GET
	@Path("/rapidmail/recipients/{region-id}/{recipient-list-id}")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRecipients(
			@PathParam("region-id") @ApiParam("Region id")
					String regionId,
			@PathParam("recipient-list-id") @ApiParam("Recipient id")
					String recipientListId,
			@ApiParam("Send message content")
					String content
	) throws AlbinaException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		RegionConfigurationController ctRc=RegionConfigurationController.getInstance();
		RegionConfiguration rc=ctRc.getRegionConfiguration(regionId);
		RapidMailProcessorController ctRm=RapidMailProcessorController.getInstance();
		HttpResponse response=ctRm.getRecipients(rc.getRapidMailConfig(),recipientListId);
		return Response
				.status(response.getStatusLine().getStatusCode())
				.entity(response.getEntity().getContent())
				.header("Content-Type",response.getEntity().getContentType())
				.build();
	}


	@POST
	@Path("/twitter/send-message/{region-id}/{language}")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.TEXT_HTML)
	public Response sendTwitter(
			@PathParam("region-id") @ApiParam("Region id")
			String regionId,
			@PathParam("language") @ApiParam("Language id")
			String language,
			@ApiParam("Send message content")
			String content
	) throws IOException, AlbinaException, TwitterException {
		TwitterProcessorController ctTw=TwitterProcessorController.getInstance();
		RegionConfigurationController ctRc=RegionConfigurationController.getInstance();
		RegionConfiguration rc=ctRc.getRegionConfiguration(regionId);
		Status response=ctTw.createTweet(rc.getTwitterConfig(),language,content);
		return Response.ok(ctTw.toJson(response), MediaType.TEXT_HTML).build();
	}


	@POST
	@Path("/messenger-people/send-message/{region-id}/{language}")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.WILDCARD)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessengerPeople(
			@PathParam("region-id") @ApiParam("Region id")
			String regionId,
			@PathParam("language") @ApiParam("Language id")
			String language,
			@ApiParam("Send message content")
			@QueryParam("message")
			String message,
			@ApiParam("Send message content")
			@QueryParam("attachmentUrl")
			String attachmentUrl
	) throws IOException, AlbinaException{
		MessengerPeopleProcessorController ctMp=MessengerPeopleProcessorController.getInstance();
		RegionConfiguration rc=RegionConfigurationController.getInstance().getRegionConfiguration(regionId);
		// TODO: how to manage target here???
		MessengerPeopleNewsLetter response=ctMp.sendNewsLetter(rc.getMessengerPeopleConfig(),language,message,attachmentUrl);
		// TODO: save into table of activity here
		return Response.ok(ctMp.toJson(response), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/shipments")
//	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getShipmentsList() throws AlbinaException, JsonProcessingException {
		ShipmentController ctSp=ShipmentController.getInstance();
		List<Shipment> shipmentsList=ShipmentController.getInstance().shipmentsList();
		return Response.ok(ctSp.toJson(shipmentsList), MediaType.APPLICATION_JSON).build();
	}


	// --------------------------------------
	// COMMON USAGE - END
	// --------------------------------------

}
