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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequestDestination;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequestPostFile;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsResponse;
import eu.albina.model.publication.rapidmail.recipientlist.RapidMailRecipientListResponse;
import eu.albina.model.publication.rapidmail.recipientlist.RapidMailRecipientListResponseItem;
import eu.albina.model.publication.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.util.HibernateUtil;
import eu.albina.util.HttpClientUtil;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.hibernate.HibernateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface RapidMailController {
	Logger logger = LoggerFactory.getLogger(RapidMailController.class);
	String baseUrl = "https://apiv3.emailsys.net";
	Client client = HttpClientUtil.newClientBuilder().register(JacksonFeature.class).build();

	static Optional<RapidMailConfiguration> getConfiguration(Region region, LanguageCode languageCode, String subjectMatter) {
		Objects.requireNonNull(region, "region");
		Objects.requireNonNull(region.getId(), "region.getId()");
		Objects.requireNonNull(languageCode, "languageCode");

		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<RapidMailConfiguration> select = criteriaBuilder.createQuery(RapidMailConfiguration.class);
			Root<RapidMailConfiguration> root = select.from(RapidMailConfiguration.class);
			select.where(
				criteriaBuilder.equal(root.get("region"), region),
				criteriaBuilder.equal(root.get("lang"), languageCode),
				subjectMatter == null
					? criteriaBuilder.isNull(root.get("subjectMatter"))
					: criteriaBuilder.equal(root.get("subjectMatter"), subjectMatter)
			);
			try {
				return Optional.ofNullable(entityManager.createQuery(select).getSingleResult());
			} catch (PersistenceException e) {
				return Optional.empty();
			}
		});
	}

	private static String calcBasicAuth(RapidMailConfiguration config) {
		String auth = config.getUsername() + ":" + config.getPassword();
		return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
	}

	static RapidMailRecipientListResponse getRecipientsList(RapidMailConfiguration config) throws IOException, HibernateException {

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Recipientlists#/Recipientlists/get_recipientlists
		Response response = client.target(baseUrl + "/recipientlists")
			.request()
			.header("Authorization", calcBasicAuth(config))
			.header("Accept", MediaType.APPLICATION_JSON)
			.get();
		logger.info("Retrieving recipients -> {}", response.getStatusInfo());
		return response.readEntity(RapidMailRecipientListResponse.class);
	}

	static void createRecipient(RapidMailConfiguration config, PostRecipientsRequest recipient)
		throws AlbinaException, IOException, HibernateException {

		if (recipient.getRecipientlistId() == null) {
			int recipientListId = getRecipientId(config);
			recipient.setRecipientlistId(recipientListId);
		}
		client.target(baseUrl + "/recipients")
			.queryParam("send_activationmail", "yes")
			.request()
			.header("Authorization", calcBasicAuth(config))
			.header("Accept", MediaType.APPLICATION_JSON)
			.post(Entity.entity(recipient, MediaType.APPLICATION_JSON));
	}

	static void sendMessage(PostMailingsRequest mailingsPost, RapidMailConfiguration config)
		throws AlbinaException, IOException, HibernateException {

		if (mailingsPost.getDestinations() == null) {
			int recipientListId = getRecipientId(config);
			mailingsPost.setDestinations(Collections.singletonList(
				new PostMailingsRequestDestination().id(recipientListId).type("recipientlist").action("include")));
		}

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Mailings#/Mailings/post_mailings
		logger.info("Sending {} ...", mailingsPost);
		Response response = client.target(baseUrl + "/mailings")
			.request()
			.header("Authorization", calcBasicAuth(config))
			.header("Accept", MediaType.APPLICATION_JSON)
			.post(Entity.entity(mailingsPost, MediaType.APPLICATION_JSON));
		logger.info("... returned {}", response);
		PostMailingsResponse entity = response.readEntity(PostMailingsResponse.class);
		logger.info("... returned {}", entity);
	}

	static int getRecipientId(RapidMailConfiguration config) throws AlbinaException, HibernateException, IOException {
		String recipientName = config.getMailinglistName();
		logger.info("Retrieving recipient for {} ...", recipientName);
		RapidMailRecipientListResponse recipientListResponse = getRecipientsList(config);
		return recipientListResponse.getEmbedded().getRecipientlists().stream()
			.filter(x -> recipientName.equalsIgnoreCase(x.getName()))
			.mapToInt(RapidMailRecipientListResponseItem::getId)
			.findFirst()
			.orElseThrow(() -> new AlbinaException("Invalid recipientList name '" + recipientName + "'. Please check configuration"));
	}

	static String createZipFile(String htmlContent, String textContent) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			 ZipOutputStream out = new ZipOutputStream(baos)) {
			if (htmlContent != null) {
				ZipEntry e = new ZipEntry("content.html");
				out.putNextEntry(e);
				byte[] data = htmlContent.getBytes(StandardCharsets.UTF_8);
				out.write(data, 0, data.length);
				out.closeEntry();
			}
			if (textContent != null) {
				ZipEntry e = new ZipEntry("content.txt");
				out.putNextEntry(e);
				byte[] data = textContent.getBytes(StandardCharsets.UTF_8);
				out.write(data, 0, data.length);
				out.closeEntry();
			}
			out.close();
			byte[] zipData = baos.toByteArray();
			return Base64.getEncoder().encodeToString(zipData);
		}
	}

	static void sendEmail(RapidMailConfiguration config, String emailHtml, String subject) throws IOException, AlbinaException {
		Objects.requireNonNull(config, "config");
		logger.info("Sending [{}] email for {} ({} bytes)...", subject, config, emailHtml.getBytes(StandardCharsets.UTF_8).length);
		LanguageCode lang = config.getLang();
		Region region = config.getRegion();
		PostMailingsRequestPostFile file = new PostMailingsRequestPostFile()
			.description("mail-content.zip")
			.type("application/zip")
			.content(createZipFile(emailHtml, null));
		final String fromEmail = lang.getBundleString("email", region);
		final String fromName = lang.getBundleString("website.name", region);
		PostMailingsRequest request = new PostMailingsRequest()
			.fromEmail(fromEmail)
			.fromName(fromName)
			.subject(subject)
			.status("scheduled")
			.file(file);
		sendMessage(request, config);
	}
}
