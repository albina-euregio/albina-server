// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.http.MediaType;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Singleton
public class RapidMailController {
	private static final Logger logger = LoggerFactory.getLogger(RapidMailController.class);
	private static final String baseUrl = "https://apiv3.emailsys.net";

	@Inject
	ObjectMapper objectMapper;

	@Inject
	HttpClient client;

	@Inject
	RapidMailConfigurationRepository rapidMailConfigurationRepository;

	@Repository
	public interface RapidMailConfigurationRepository extends CrudRepository<RapidMailConfiguration, Long> {
		Optional<RapidMailConfiguration> findByRegionAndLanguageCodeAndSubjectMatter(Region region, LanguageCode languageCode, String subjectMatter);
	}

	public Optional<RapidMailConfiguration> getConfiguration(Region region, LanguageCode languageCode, String subjectMatter) {
		return rapidMailConfigurationRepository.findByRegionAndLanguageCodeAndSubjectMatter(region, languageCode, subjectMatter);
	}

	private static String calcBasicAuth(RapidMailConfiguration config) {
		String auth = config.getUsername() + ":" + config.getPassword();
		return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
	}

	private RapidMailRecipientListResponse getRecipientsList(RapidMailConfiguration config) throws IOException, InterruptedException {

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Recipientlists#/Recipientlists/get_recipientlists
		HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/recipientlists"))
			.header("Authorization", calcBasicAuth(config))
			.header("Accept", MediaType.APPLICATION_JSON)
			.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		logger.info("Retrieving recipients -> {}", response.statusCode());
		return objectMapper.readValue(response.body(), RapidMailRecipientListResponse.class);
	}

	public void createRecipient(RapidMailConfiguration config, PostRecipientsRequest recipient)
		throws AlbinaException, IOException, InterruptedException {

		if (recipient.getRecipientlistId() == null) {
			int recipientListId = getRecipientId(config);
			recipient.setRecipientlistId(recipientListId);
		}
		HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/recipients?send_activationmail=yes"))
			.header("Authorization", calcBasicAuth(config))
			.header("Accept", MediaType.APPLICATION_JSON)
			.header("Content-Type", MediaType.APPLICATION_JSON)
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(recipient)))
			.build();
		client.send(request, HttpResponse.BodyHandlers.discarding());
	}

	private void sendMessage(PostMailingsRequest mailingsPost, RapidMailConfiguration config)
		throws AlbinaException, IOException, InterruptedException {

		if (mailingsPost.getDestinations() == null) {
			int recipientListId = getRecipientId(config);
			mailingsPost.setDestinations(Collections.singletonList(
				new PostMailingsRequestDestination().id(recipientListId).type("recipientlist").action("include")));
		}

		// https://developer.rapidmail.wiki/documentation.html?urls.primaryName=Mailings#/Mailings/post_mailings
		logger.info("Sending {} ...", mailingsPost);
		HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/mailings"))
			.header("Authorization", calcBasicAuth(config))
			.header("Accept", MediaType.APPLICATION_JSON)
			.header("Content-Type", MediaType.APPLICATION_JSON)
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(mailingsPost)))
			.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		logger.info("... returned {}", response);
		PostMailingsResponse entity = objectMapper.readValue(response.body(), PostMailingsResponse.class);
		logger.info("... returned {}", entity);
	}

	private int getRecipientId(RapidMailConfiguration config) throws AlbinaException, IOException, InterruptedException {
		String recipientName = config.getMailinglistName();
		logger.info("Retrieving recipient for {} ...", recipientName);
		RapidMailRecipientListResponse recipientListResponse = getRecipientsList(config);
		return recipientListResponse.getEmbedded().getRecipientlists().stream()
			.filter(x -> recipientName.equalsIgnoreCase(x.getName()))
			.mapToInt(RapidMailRecipientListResponseItem::getId)
			.findFirst()
			.orElseThrow(() -> new AlbinaException("Invalid recipientList name '" + recipientName + "'. Please check configuration"));
	}

	private String createZipFile(String htmlContent, String textContent) throws IOException {
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

	public void sendEmail(RapidMailConfiguration config, String emailHtml, String subject) throws IOException, AlbinaException, InterruptedException {
		Objects.requireNonNull(config, "config");
		logger.info("Sending [{}] email for {} ({} bytes)...", subject, config, emailHtml.getBytes(StandardCharsets.UTF_8).length);
		LanguageCode lang = config.getLanguageCode();
		Region region = config.getRegion();
		PostMailingsRequestPostFile file = new PostMailingsRequestPostFile()
			.description("mail-content.zip")
			.type("application/zip")
			.content(createZipFile(emailHtml, null));
		final String fromEmail = region.getWarningServiceEmail(lang);
		final String fromName = region.getWarningServiceName(lang);
		PostMailingsRequest request = new PostMailingsRequest()
			.fromEmail(fromEmail)
			.fromName(fromName)
			.subject(subject)
			.status("scheduled")
			.file(file);
		sendMessage(request, config);
	}
}
