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

import org.apache.http.HttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.albina.controller.socialmedia.RapidMailProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.controller.socialmedia.ShipmentController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.enumerations.Role;
import eu.albina.model.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.model.socialmedia.Shipment;
import eu.albina.rest.filter.Secured;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path("/social-media")
@Api(value = "/social-media")
public class SocialMediaService {

	@Context
	UriInfo uri;

	// --------------------------------------
	// COMMON USAGE - BEGIN
	// --------------------------------------
	@POST
	@Path("/rapidmail/send-message/{region-id}/{language}")
	@Secured({ Role.ADMIN })
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
	@Secured({ Role.ADMIN })
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
	@Secured({ Role.ADMIN })
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
	@Secured({ Role.ADMIN })
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
	@Secured({ Role.ADMIN })
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

	@GET
	@Path("/shipments")
	@Secured({ Role.ADMIN })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getShipmentsList() throws AlbinaException, JsonProcessingException {
		ShipmentController ctSp = ShipmentController.getInstance();
		List<Shipment> shipmentsList = ShipmentController.getInstance().shipmentsList();
		return Response.ok(ctSp.toJson(shipmentsList), MediaType.APPLICATION_JSON).build();
	}

	// --------------------------------------
	// COMMON USAGE - END
	// --------------------------------------
}
