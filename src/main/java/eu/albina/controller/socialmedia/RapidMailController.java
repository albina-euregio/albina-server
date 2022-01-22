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

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequestDestination;
import eu.albina.model.publication.rapidmail.recipientlist.RapidMailRecipientListResponse;
import eu.albina.model.publication.rapidmail.recipientlist.RapidMailRecipientListResponseItem;
import eu.albina.model.publication.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.util.HibernateUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hibernate.HibernateException;
import org.n52.jackson.datatype.jts.JtsModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RapidMailController extends CommonProcessor {
	private static final Logger logger = LoggerFactory.getLogger(RapidMailController.class);
	private static final int RAPIDMAIL_SOCKET_TIMEOUT = 10000;
	private static final int RAPIDMAIL_CONNECTION_TIMEOUT = 10000;
	private static RapidMailController instance = null;
	private final String baseUrl = "https://apiv3.emailsys.net";
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Executor executor;

	public static RapidMailController getInstance() throws CertificateException, NoSuchAlgorithmException,
			KeyStoreException, IOException, KeyManagementException {
		if (instance == null) {
			instance = new RapidMailController();
		}
		return instance;
	}

	public RapidMailController() {
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		objectMapper.registerModule(new JtsModule());
		executor = Executor.newInstance(sslHttpClient());
	}

	private RapidMailConfiguration getConfiguration(String regionId) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			RapidMailConfiguration result = null;
			if (!Strings.isNullOrEmpty(regionId)) {
				result = (RapidMailConfiguration) entityManager.createQuery(HibernateUtil.queryGetTelegramConfiguration)
				.setParameter("regionId", regionId).getSingleResult();
			} else {
				throw new HibernateException("No region defined!");
			}
			if (result != null)
				return result;
			else
				throw new HibernateException("No rapid mail configuration found for " + regionId);
		});
	}

	public CloseableHttpClient sslHttpClient() {
		// Trust own CA and all self-signed certs
		return HttpClients.custom().build();
	}

	private String calcBasicAuth(String user, String pass) {
		return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
	}

	public RapidMailRecipientListResponse getRecipientsList(String region) throws IOException, HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Recipientlists#/Recipientlists/get_recipientlists
		Request request = Request.Get(baseUrl + "/recipientlists")
				.addHeader("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
				.addHeader("Accept", "application/hal+json").connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
				.socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT);
		HttpResponse response = executor.execute(request).returnResponse();
		return objectMapper.readValue(getResponseContent(response), RapidMailRecipientListResponse.class);
	}

	public HttpResponse createRecipient(String region, PostRecipientsRequest recipient,
			String sendActivationmail, LanguageCode language) throws Exception, HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);

		if (recipient.getRecipientlistId() == null) {
			String recipientName = getRecipientName(region, language);
			Integer recipientListId = getRecipientId(region, recipientName);
			recipient.setRecipientlistId(recipientListId);
		}
		String url = baseUrl + "/recipients";
		if (sendActivationmail == null)
			sendActivationmail = "yes";
		url += "?send_activationmail=" + sendActivationmail;
        return executor
				.execute(Request.Post(url)
						.addHeader("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
						.addHeader("Accept", "application/hal+json").addHeader("Content-Type", "application/json")
						.bodyString(toJson(recipient), ContentType.APPLICATION_JSON)
						.connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT).socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT))
				.returnResponse();
	}

	public HttpResponse deleteRecipient(String region, Integer recipientId) throws IOException, HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Recipientlists#/Recipientlists/delete_recipientlists__recipientlist_id_
		return executor.execute(Request.Delete(baseUrl + "/recipients/" + recipientId)
				.addHeader("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
				.addHeader("Accept", "application/json").connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT)
				.socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT)).returnResponse();
	}

	public HttpResponse sendMessage(String region, LanguageCode language, PostMailingsRequest mailingsPost, boolean test)
			throws Exception, HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);

		if (mailingsPost.getDestinations() == null) {
			String recipientName = test ? "TEST" : getRecipientName(region, language);
			int recipientListId = getRecipientId(region, recipientName);
			logger.info("Obtaining recipient for {} -> {}", recipientName, recipientListId);
			mailingsPost.setDestinations(Collections.singletonList(
					new PostMailingsRequestDestination().id(recipientListId).type("recipientlist").action("include")));
		}

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Mailings#/Mailings/post_mailings
		Request request = Request.Post(baseUrl + "/mailings")
				.addHeader("Authorization", calcBasicAuth(config.getUsername(), config.getPassword()))
				.addHeader("Content-Type", "application/json").addHeader("Accept", "application/hal+json")
				.bodyString(toJson(mailingsPost), ContentType.APPLICATION_JSON)
				.connectTimeout(RAPIDMAIL_CONNECTION_TIMEOUT).socketTimeout(RAPIDMAIL_SOCKET_TIMEOUT);
		logger.info("Sending {} using request {}", mailingsPost, request);
		HttpResponse response = executor.execute(request).returnResponse();
		logger.info("... returned {}", response.getStatusLine());
		logger.debug("RESPONSE: {}", response.toString());
		logger.debug("CONTENT: {}", response.getEntity().getContent().toString());
		// Go ahead only if success
		if (response.getStatusLine().getStatusCode() != 201) {
			return response;
		}
		response.getEntity().getContent().reset();
		return response;
	}

 	private String getRecipientName(String region, LanguageCode language) throws HibernateException {
		RapidMailConfiguration config = this.getConfiguration(region);
		return config.getRegion().getId() + "_" + language.name().toUpperCase();
	}

	public int getRecipientId(String region, String recipientName) throws Exception {
		RapidMailRecipientListResponse recipientListResponse = getRecipientsList(region);
		return recipientListResponse.getEmbedded().getRecipientlists().stream()
			.filter(x -> StringUtils.equalsIgnoreCase(x.getName(), recipientName))
			.mapToInt(RapidMailRecipientListResponseItem::getId)
			.findFirst()
			.orElseThrow(() -> new Exception("Invalid recipientList name '" + recipientName + "'. Please check configuration"));
	}

	private String getResponseContent(HttpResponse response) throws IOException {
		return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
	}

}
