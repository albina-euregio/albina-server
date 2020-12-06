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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONObject;

import eu.albina.exception.AlbinaException;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.messengerpeople.MessengerPeopleNewsletterHistory;
import eu.albina.model.messengerpeople.MessengerPeopleTargets;
import eu.albina.model.messengerpeople.MessengerPeopleUser;
import eu.albina.model.messengerpeople.MessengerPeopleUserData;
import eu.albina.model.socialmedia.MessengerPeopleConfig;
import eu.albina.model.socialmedia.Shipment;
import eu.albina.util.GlobalVariables;

public class MessengerPeopleProcessorController extends CommonProcessor {
	private static Logger logger = LoggerFactory.getLogger(MessengerPeopleProcessorController.class);

	private static MessengerPeopleProcessorController instance = null;

	public static MessengerPeopleProcessorController getInstance() {
		if (instance == null) {
			instance = new MessengerPeopleProcessorController();
		}
		return instance;
	}

	// private static String
	// apikey="a1e6d5387c979b039040447af4a4d20a_11513_9fc5a49fc674b5b2750ad90a7";
	private final String baseUrl = "https://rest.messengerpeople.com/api/v12";
	ObjectMapper objectMapper = new ObjectMapper();
	int MESSENGER_PEOPLE_CONNECTION_TIMEOUT = 10000;
	int MESSENGER_PEOPLE_SOCKET_TIMEOUT = 10000;

	public MessengerPeopleProcessorController() {
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		objectMapper.registerModule(new JtsModule());
	}

	/**
	 *
	 * @param limit
	 * @param offset
	 * @return
	 */
	public List<MessengerPeopleUser> getUsers(MessengerPeopleConfig config, Integer limit, Integer offset)
			throws IOException {
		String jsonString = Request
				.Get(baseUrl + "/user"
						+ String.format("?apikey=%s&limit=%s&offset=%s", config.getApiKey(), limit, offset))
				.connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT).socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
				.execute().returnContent().asString();
		JSONObject json = new JSONObject(jsonString);
		List<MessengerPeopleUser> users = objectMapper.readValue(json.get("user").toString(),
				new TypeReference<List<MessengerPeopleUser>>() {
				});
		return users;
	}

	public void setUserDetails(MessengerPeopleConfig config, String id, MessengerPeopleUserData messengerPeopleUserData)
			throws IOException {
		String json = URLEncoder.encode(objectMapper.writeValueAsString(messengerPeopleUserData),
				StandardCharsets.UTF_8.toString());
		Request.Put(baseUrl + "/user" + String.format("?apikey=%s&fields=%s", config.getApiKey(), json))
				.connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT).socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
				.execute();
	}

	/**
	 * Do not use this cause doesn't return array. Or use a map from jackson
	 *
	 * @return
	 */
	public MessengerPeopleTargets getTargets(MessengerPeopleConfig config) throws IOException {
		String json = Request.Get(baseUrl + "/newsletter/targeting" + String.format("?apikey=%s", config.getApiKey()))
				.connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT).socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
				.execute().returnContent().asString();
		MessengerPeopleTargets targets = objectMapper.readValue(json, MessengerPeopleTargets.class);
		return targets;
	}

	// LANG: only languages on messengerpeople
	// REGION: only regions on messengerpeople
	private int getTargeting(String region, LanguageCode lang) {
		switch (region) {
		case GlobalVariables.codeTyrol:
			switch (lang) {
			case de:
				return GlobalVariables.targetingTyrolDe;
			case it:
				return GlobalVariables.targetingTyrolIt;
			case en:
				return GlobalVariables.targetingTyrolEn;
			default:
				return -1;
			}
		case GlobalVariables.codeSouthTyrol:
			switch (lang) {
			case de:
				return GlobalVariables.targetingSouthTyrolDe;
			case it:
				return GlobalVariables.targetingSouthTyrolIt;
			case en:
				return GlobalVariables.targetingSouthTyrolEn;
			default:
				return -1;
			}
		case GlobalVariables.codeTrentino:
			switch (lang) {
			case de:
				return GlobalVariables.targetingTrentinoDe;
			case it:
				return GlobalVariables.targetingTrentinoIt;
			case en:
				return GlobalVariables.targetingTrentinoEn;
			default:
				return -1;
			}
		case "Test":
			return GlobalVariables.targetingTest;
		default:
			return -1;
		}
	}

	public CloseableHttpClient sslHttpClient() {
		// Trust own CA and all self-signed certs
		return HttpClients.custom().build();
	}

	public HttpResponse sendNewsLetter(MessengerPeopleConfig config, LanguageCode language, String message,
			String attachmentUrl) throws IOException, AlbinaException {
		Integer targeting = getTargeting(config.getRegionConfiguration().getRegion().getId(), language);
		if (targeting > 0) {
			StringBuilder data = new StringBuilder();
			data.append("{ \"text\": \"");
			data.append(message);
			if (attachmentUrl != null) {
				data.append("\", \"media\": \"");
				data.append(attachmentUrl);
			}
			data.append("\", \"notification\": \"");
			data.append(message);
			data.append("\", \"targeting\": \"");
			data.append(targeting);
			data.append("\", \"apikey\": \"");
			data.append(config.getApiKey());
			data.append("\" }");

			logger.info("Publishing report on messengerpeople for "
					+ config.getRegionConfiguration().getRegion().getId() + " in " + language);

			String url = baseUrl + "/content";
			Request request = Request.Post(url).addHeader("Content-Type", "application/json")
					.bodyString(data.toString(), ContentType.APPLICATION_JSON)
					.connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT).socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT);
			HttpResponse response = request.execute().returnResponse();

			// Go ahead only if success
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.warn("Error publishing report on messengerpeople for "
						+ config.getRegionConfiguration().getRegion().getId() + " in " + language + " (error code "
						+ response.getStatusLine().getStatusCode() + ")");

				String body = response.getEntity() != null
						? IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8)
						: null;
				ShipmentController.getInstance().saveShipment(createActivityRow(config, language.toString(),
						"message=" + message + ", attachmentUrl=" + attachmentUrl, body, null));
				return response;
			}
			return response;
		} else {
			throw new AlbinaException("No targeting on messengerpeople!");
		}
	}

	public MessengerPeopleNewsletterHistory getNewsLetterHistory(MessengerPeopleConfig config, Integer limit)
			throws IOException {
		String json = Request
				.Get(baseUrl + "/newsletter" + String.format("?apikey=%s&limit=%d", config.getApiKey(), limit))
				.connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT).socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
				.execute().returnContent().asString();
		MessengerPeopleNewsletterHistory newsletterHistory = objectMapper.readValue(json,
				MessengerPeopleNewsletterHistory.class);
		return newsletterHistory;
	}

	public HttpResponse getUsersStats(MessengerPeopleConfig config) throws IOException {
		HttpResponse response = Request
				.Get(baseUrl + "/stats/user" + String.format("?apikey=%s", config.getApiKey())
						+ "&days=1&hours=0&start=" + Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond() + "&end=")
				.connectTimeout(MESSENGER_PEOPLE_CONNECTION_TIMEOUT).socketTimeout(MESSENGER_PEOPLE_SOCKET_TIMEOUT)
				.execute().returnResponse();
		return response;
	}

	private Shipment createActivityRow(MessengerPeopleConfig config, String language, String request, String response,
			String idMp) {
		Shipment shipment = new Shipment().date(ZonedDateTime.now())
				.name(config.getRegionConfiguration().getRegion().getNameEn()).language(language).idMp(idMp).idRm(null)
				.idTw(null).request(request).response(response).region(config.getRegionConfiguration())
				.provider(config.getProvider());
		return shipment;
	}
}
