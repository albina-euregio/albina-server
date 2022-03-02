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
package eu.albina.controller.publication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Collections;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequestDestination;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsResponse;
import eu.albina.model.publication.rapidmail.recipientlist.RapidMailRecipientListResponse;
import eu.albina.model.publication.rapidmail.recipientlist.RapidMailRecipientListResponseItem;
import eu.albina.model.publication.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.util.HibernateUtil;
import eu.albina.util.HttpClientUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.hibernate.HibernateException;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RapidMailController {
	private static final Logger logger = LoggerFactory.getLogger(RapidMailController.class);
	private static RapidMailController instance = null;
	private final String baseUrl = "https://apiv3.emailsys.net";
	private final Client client = HttpClientUtil.newClientBuilder().register(JacksonFeature.class).build();

	public static RapidMailController getInstance() throws CertificateException, NoSuchAlgorithmException,
			KeyStoreException, IOException, KeyManagementException {
		if (instance == null) {
			instance = new RapidMailController();
		}
		return instance;
	}

	private RapidMailConfiguration getConfiguration(Region region) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			RapidMailConfiguration result = null;
			if (region != null && !Strings.isNullOrEmpty(region.getId())) {
				result = (RapidMailConfiguration) entityManager.createQuery(HibernateUtil.queryGetRapidMailConfiguration)
				.setParameter("region", region).getSingleResult();
			} else {
				throw new HibernateException("No region defined!");
			}
			if (result != null)
				return result;
			else
				throw new HibernateException("No rapid mail configuration found for " + region.getId());
		});
	}

	private String calcBasicAuth(String user, String pass) {
		return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
	}

	public RapidMailRecipientListResponse getRecipientsList(Region region) throws IOException, HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Recipientlists#/Recipientlists/get_recipientlists
		Response response = client.target(baseUrl + "/recipientlists")
			.request()
			.header("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
			.header("Accept", MediaType.APPLICATION_JSON)
			.get();
		logger.info("Retrieving recipients -> {}", response.getStatusInfo());
		return response.readEntity(RapidMailRecipientListResponse.class);
	}

	public Response createRecipient(Region region, PostRecipientsRequest recipient,
			String sendActivationmail, LanguageCode language) throws AlbinaException, IOException, HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);

		if (recipient.getRecipientlistId() == null) {
			String recipientName = getRecipientName(region, language, false);
			Integer recipientListId = getRecipientId(region, recipientName);
			recipient.setRecipientlistId(recipientListId);
		}
		return client.target(baseUrl + "/recipients")
			.queryParam("send_activationmail", MoreObjects.firstNonNull(sendActivationmail, "yes"))
			.request()
			.header("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
			.header("Accept", MediaType.APPLICATION_JSON)
			.post(Entity.entity(recipient, MediaType.APPLICATION_JSON));
	}

	public Response deleteRecipient(Region region, Integer recipientId) throws IOException, HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Recipientlists#/Recipientlists/delete_recipientlists__recipientlist_id_
		return client.target(baseUrl + "/recipients/" + recipientId)
			.request()
			.header("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
			.header("Accept", MediaType.APPLICATION_JSON)
			.delete();
	}

	public PostMailingsResponse sendMessage(Region region, LanguageCode language, PostMailingsRequest mailingsPost, boolean test, boolean media)
			throws AlbinaException, IOException, HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);

		if (mailingsPost.getDestinations() == null) {
			String recipientName = test ? "TEST" : getRecipientName(region, language, media);
			logger.info("Retrieving recipient for {} ...", recipientName);
			int recipientListId = getRecipientId(region, recipientName);
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

 	private String getRecipientName(Region region, LanguageCode language, boolean media) throws HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);
		if (media)
			return config.getRegion().getId() + "_" + language.name().toUpperCase() + "_media";
		else
			return config.getRegion().getId() + "_" + language.name().toUpperCase();
	}

	public int getRecipientId(Region region, String recipientName) throws AlbinaException, HibernateException, IOException {
		RapidMailRecipientListResponse recipientListResponse = getRecipientsList(region);
		return recipientListResponse.getEmbedded().getRecipientlists().stream()
			.filter(x -> recipientName.equalsIgnoreCase(x.getName()))
			.mapToInt(RapidMailRecipientListResponseItem::getId)
			.findFirst()
			.orElseThrow(() -> new AlbinaException("Invalid recipientList name '" + recipientName + "'. Please check configuration"));
	}

}
