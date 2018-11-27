package eu.albina.rest;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.albina.controller.socialmedia.MessengerPeopleProcessorController;
import eu.albina.controller.socialmedia.RapidMailProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.controller.socialmedia.ShipmentController;
import eu.albina.controller.socialmedia.TwitterProcessorController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.messengerpeople.MessengerPeopleNewsLetter;
import eu.albina.model.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.model.socialmedia.Shipment;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import twitter4j.Status;
import twitter4j.TwitterException;

@Path("/social-media")
@Api(value = "/social-media")
public class SocialMediaService {
	private static Logger logger = LoggerFactory.getLogger(SocialMediaService.class);

	@Context
	UriInfo uri;

	// --------------------------------------
	// COMMON USAGE - BEGIN
	// --------------------------------------
	@POST
	@Path("/rapidmail/send-message/{region-id}/{language}")
	// @Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendRapidMail(@PathParam("region-id") @ApiParam("Region id") String regionId,
			@PathParam("language") @ApiParam("Language id") String language,
			@ApiParam("Send message content") String content) throws Exception {
		RapidMailProcessorController ctRm = RapidMailProcessorController.getInstance();
		PostMailingsRequest mailingsPost = ctRm.fromJson(content, PostMailingsRequest.class);
		RegionConfigurationController ctRc = RegionConfigurationController.getInstance();
		RegionConfiguration rc = ctRc.getRegionConfiguration(regionId);
		HttpResponse response = ctRm.sendMessage(rc.getRapidMailConfig(), language, mailingsPost);
		return Response.status(response.getStatusLine().getStatusCode()).entity(response.getEntity().getContent())
				.header(response.getEntity().getContentType().getName(),
						response.getEntity().getContentType().getValue())
				.build();
	}

	@GET
	@Path("/rapidmail/recipient-list/{region-id}")
	// @Secured({ Role.ADMIN })
	@Produces("application/hal+json")
	public Response getRecipientList(@PathParam("region-id") @ApiParam("Region id") String regionId)
			throws AlbinaException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException {
		RegionConfigurationController ctRc = RegionConfigurationController.getInstance();
		RegionConfiguration rc = ctRc.getRegionConfiguration(regionId);
		RapidMailProcessorController ctRm = RapidMailProcessorController.getInstance();
		HttpResponse response = ctRm.getRecipientsList(rc.getRapidMailConfig(), regionId);
		return Response.status(response.getStatusLine().getStatusCode()).entity(response.getEntity().getContent())
				.header(response.getEntity().getContentType().getName(),
						response.getEntity().getContentType().getValue())
				.build();
	}

	@POST
	@Path("/rapidmail/recipients/{region-id}/{language}")
	// @Secured({ Role.ADMIN })
	@Produces("application/hal+json")
	public Response addRecipient(@PathParam("region-id") @ApiParam("Region id") String regionId,
			@PathParam("language") @ApiParam("Language id") String language,
			@QueryParam("send_activationmail") String sendActivationmail, @ApiParam("Recipient data") String content)
			throws Exception {
		RegionConfigurationController ctRc = RegionConfigurationController.getInstance();
		RegionConfiguration rc = ctRc.getRegionConfiguration(regionId);
		RapidMailProcessorController ctRm = RapidMailProcessorController.getInstance();
		HttpResponse response = ctRm.createRecipient(rc.getRapidMailConfig(),
				ctRm.fromJson(content, PostRecipientsRequest.class), sendActivationmail, language);
		return Response.status(response.getStatusLine().getStatusCode()).entity(response.getEntity().getContent())
				.header(response.getEntity().getContentType().getName(),
						response.getEntity().getContentType().getValue())
				.build();
	}

	@DELETE
	@Path("/rapidmail/recipients/{region-id}/{recipient-id}")
	// @Secured({ Role.ADMIN })
	@Produces("application/hal+json")
	public Response deleteRecipient(@PathParam("region-id") @ApiParam("Region id") String regionId,
			@PathParam("recipient-id") @ApiParam("Recipient id") Integer recipientId,
			@ApiParam("Send message content") String content) throws AlbinaException, IOException, CertificateException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		RegionConfigurationController ctRc = RegionConfigurationController.getInstance();
		RegionConfiguration rc = ctRc.getRegionConfiguration(regionId);
		RapidMailProcessorController ctRm = RapidMailProcessorController.getInstance();
		HttpResponse response = ctRm.deleteRecipient(rc.getRapidMailConfig(), recipientId);
		return Response.status(response.getStatusLine().getStatusCode()).entity(response.getEntity().getContent())
				.header(response.getEntity().getContentType().getName(),
						response.getEntity().getContentType().getValue())
				.build();
	}

	@GET
	@Path("/rapidmail/recipients/{region-id}/{recipient-list-id}")
	// @Secured({ Role.ADMIN })
	@Produces("application/hal+json")
	public Response getRecipients(@PathParam("region-id") @ApiParam("Region id") String regionId,
			@PathParam("recipient-list-id") @ApiParam("Recipient id") String recipientListId,
			@ApiParam("Send message content") String content) throws AlbinaException, IOException, CertificateException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		RegionConfigurationController ctRc = RegionConfigurationController.getInstance();
		RegionConfiguration rc = ctRc.getRegionConfiguration(regionId);
		RapidMailProcessorController ctRm = RapidMailProcessorController.getInstance();
		HttpResponse response = ctRm.getRecipients(rc.getRapidMailConfig(), recipientListId);
		return Response.status(response.getStatusLine().getStatusCode()).entity(response.getEntity().getContent())
				.header(response.getEntity().getContentType().getName(),
						response.getEntity().getContentType().getValue())
				.build();
	}

	@POST
	@Path("/twitter/send-message/{region-id}/{language}")
	// @Secured({ Role.ADMIN })
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.TEXT_HTML)
	public Response sendTwitter(@PathParam("region-id") @ApiParam("Region id") String regionId,
			@PathParam("language") @ApiParam("Language id") String language, @QueryParam("previous_id") Long previousId,
			@ApiParam("Send message content") String status) throws IOException, AlbinaException, TwitterException {

		TwitterProcessorController ctTw = TwitterProcessorController.getInstance();
		RegionConfigurationController ctRc = RegionConfigurationController.getInstance();
		RegionConfiguration rc = ctRc.getRegionConfiguration(regionId);
		Status response = ctTw.createTweet(rc.getTwitterConfig(), language, status, previousId);
		return Response.ok(ctTw.toJson(response), MediaType.TEXT_HTML).build();
	}

	@POST
	@Path("/messenger-people/send-message/{region-id}/{language}")
	// @Secured({ Role.ADMIN })
	@Produces(MediaType.WILDCARD)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessengerPeople(@PathParam("region-id") @ApiParam("Region id") String regionId,
			@PathParam("language") @ApiParam("Language id") String language,
			@ApiParam("Send message content") @QueryParam("message") String message,
			@ApiParam("Send message content") @QueryParam("attachment") String attachmentUrl)
			throws IOException, AlbinaException {
		MessengerPeopleProcessorController ctMp = MessengerPeopleProcessorController.getInstance();
		RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(regionId);
		MessengerPeopleNewsLetter response = ctMp.sendNewsLetter(rc.getMessengerPeopleConfig(), language, message,
				attachmentUrl);
		return Response.ok(ctMp.toJson(response), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/messenger-people/stats-user/{region-id}")
	// @Secured({ Role.ADMIN })
	@Produces(MediaType.WILDCARD)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserStats(@PathParam("region-id") @ApiParam("region-id") String regionId)
			throws AlbinaException, IOException {
		MessengerPeopleProcessorController ctMp = MessengerPeopleProcessorController.getInstance();
		RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(regionId);
        HttpResponse response = ctMp.getUsersStats(rc.getMessengerPeopleConfig());
        return Response.status(response.getStatusLine().getStatusCode())
                .entity(IOUtils.toString(response.getEntity().getContent(),"UTF-8"))
                .header(response.getEntity().getContentType().getName(),
                        response.getEntity().getContentType().getValue())
                .build();
	}

	@GET
	@Path("/shipments")
	// @Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getShipmentsList() throws AlbinaException, JsonProcessingException {
		ShipmentController ctSp = ShipmentController.getInstance();
		List<Shipment> shipmentsList = ShipmentController.getInstance().shipmentsList();
		return Response.ok(ctSp.toJson(shipmentsList), MediaType.APPLICATION_JSON).build();
	}

	// --------------------------------------
	// COMMON USAGE - END
	// --------------------------------------

	// TODO: remove it
	// @GET
	// @Path("/bulletin-email-send")
	// @Secured({ Role.ADMIN })
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response bes() throws AlbinaException, IOException, URISyntaxException
	// {
	// EmailUtil emSp=EmailUtil.getInstance();
	// HttpResponse a=emSp.sendBulletinEmail(null, LanguageCode.it,null);
	// ShipmentController ctSp=ShipmentController.getInstance();
	// return Response.ok(ctSp.toJson(a), MediaType.APPLICATION_JSON).build();
	// }

}
