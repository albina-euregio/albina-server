/*******************************************************************************
 * Copyright (C) 2019 Clesius srl
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
package eu.albina.controller.socialmedia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Collections;

import com.google.common.base.MoreObjects;
import eu.albina.model.enumerations.LanguageCode;

import eu.albina.model.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.rapidmail.mailings.PostMailingsRequestDestination;
import eu.albina.model.rapidmail.mailings.PostMailingsResponse;
import eu.albina.model.rapidmail.recipientlist.RapidMailRecipientListResponse;
import eu.albina.model.rapidmail.recipientlist.RapidMailRecipientListResponseItem;
import eu.albina.model.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.model.socialmedia.RapidMailConfig;
import eu.albina.util.HttpClientUtil;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RapidMailProcessorController {
	private static final Logger logger = LoggerFactory.getLogger(RapidMailProcessorController.class);
	private static RapidMailProcessorController instance = null;
	private final String baseUrl = "https://apiv3.emailsys.net";
	private final Client client = HttpClientUtil.newClientBuilder().register(JacksonFeature.class).build();

	public static RapidMailProcessorController getInstance() throws CertificateException, NoSuchAlgorithmException,
			KeyStoreException, IOException, KeyManagementException {
		if (instance == null) {
			instance = new RapidMailProcessorController();
		}
		return instance;
	}

	private String calcBasicAuth(String user, String pass) {
		return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
	}

	public RapidMailRecipientListResponse getRecipientsList(RapidMailConfig config) throws IOException {
		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Recipientlists#/Recipientlists/get_recipientlists
		Response response = client.target(baseUrl + "/recipientlists")
			.request()
			.header("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
			.header("Accept", MediaType.APPLICATION_JSON)
			.get();
		logger.info("Retrieving recipients -> {}", response.getStatusInfo());
		return response.readEntity(RapidMailRecipientListResponse.class);
	}

	public Response createRecipient(RapidMailConfig config, PostRecipientsRequest recipient,
			String sendActivationmail, LanguageCode language) throws Exception {
		if (recipient.getRecipientlistId() == null) {
			String recipientName = getRecipientName(config, language);
			Integer recipientListId = getRecipientId(config, recipientName);
			recipient.setRecipientlistId(recipientListId);
		}
		return client.target(baseUrl + "/recipients")
			.queryParam("send_activationmail", MoreObjects.firstNonNull(sendActivationmail, "yes"))
			.request()
			.header("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
			.header("Accept", MediaType.APPLICATION_JSON)
			.post(Entity.entity(recipient, MediaType.APPLICATION_JSON));
	}

	public Response deleteRecipient(RapidMailConfig config, Integer recipientId) throws IOException {
		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Recipientlists#/Recipientlists/delete_recipientlists__recipientlist_id_
		return client.target(baseUrl + "/recipients/" + recipientId)
			.request()
			.header("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
			.header("Accept", MediaType.APPLICATION_JSON)
			.delete();
	}

	public PostMailingsResponse sendMessage(RapidMailConfig config, LanguageCode language, PostMailingsRequest mailingsPost, boolean test)
			throws Exception {
		if (mailingsPost.getDestinations() == null) {
			String recipientName = test ? "TEST" : getRecipientName(config, language);
			logger.info("Retrieving recipient for {} ...", recipientName);
			int recipientListId = getRecipientId(config, recipientName);
			logger.info("Retrieving recipient for {} -> {}", recipientName, recipientListId);
			mailingsPost.setDestinations(Collections.singletonList(
					new PostMailingsRequestDestination().id(recipientListId).type("recipientlist").action("include")));
		}

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Mailings#/Mailings/post_mailings
		logger.info("Sending {} ...", mailingsPost);
		Response response = client.target(baseUrl + "/mailings")
			.request()
			.header("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
			.header("Accept", MediaType.APPLICATION_JSON)
			.post(Entity.entity(mailingsPost, MediaType.APPLICATION_JSON));
		logger.info("... returned {}", response);
		PostMailingsResponse entity = response.readEntity(PostMailingsResponse.class);
		logger.info("... returned {}", entity);
		return entity;
	}

 	private String getRecipientName(RapidMailConfig config, LanguageCode language) {
		return config.getRegionConfiguration().getRegion().getId() + "_" + language.name().toUpperCase();
	}

	public int getRecipientId(RapidMailConfig config, String recipientName) throws Exception {
		RapidMailRecipientListResponse recipientListResponse = getRecipientsList(config);
		return recipientListResponse.getEmbedded().getRecipientlists().stream()
			.filter(x -> recipientName.equalsIgnoreCase(x.getName()))
			.mapToInt(RapidMailRecipientListResponseItem::getId)
			.findFirst()
			.orElseThrow(() -> new Exception("Invalid recipientList name '" + recipientName + "'. Please check configuration"));
	}

}
