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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.albina.model.Subscriber;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.publication.rapidmail.recipientlist.RapidMailRecipientListResponse;
import eu.albina.model.publication.rapidmail.recipients.post.PostRecipientsRequest;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.http.MediaType;
import io.micronaut.serde.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.RapidMailConfiguration;

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
		Optional<RapidMailConfiguration> findByRegionAndLanguageCodeAndSubjectMatter(Region region, LanguageCode languageCode, @Nonnull String subjectMatter);
		Optional<RapidMailConfiguration> findByRegionAndLanguageCodeAndSubjectMatterIsNull(Region region, LanguageCode languageCode);
	}

	public Optional<RapidMailConfiguration> getConfiguration(Region region, LanguageCode languageCode, @Nonnull String subjectMatter) {
		return rapidMailConfigurationRepository.findByRegionAndLanguageCodeAndSubjectMatter(region, languageCode, subjectMatter);
	}

	public Optional<RapidMailConfiguration> getConfiguration(Region region, LanguageCode languageCode) {
		return rapidMailConfigurationRepository.findByRegionAndLanguageCodeAndSubjectMatterIsNull(region, languageCode);
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

	public void createRecipient(RapidMailConfiguration config, Subscriber subscriber)
		throws AlbinaException, IOException, InterruptedException {

		PostRecipientsRequest recipient = new PostRecipientsRequest(subscriber.getEmail(), getRecipientId(config));
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
		logger.info("... returned {}", response.body());
	}

	private int getRecipientId(RapidMailConfiguration config) throws AlbinaException, IOException, InterruptedException {
		String recipientName = config.getMailinglistName();
		logger.info("Retrieving recipient for {} ...", recipientName);
		RapidMailRecipientListResponse recipientListResponse = getRecipientsList(config);
		return recipientListResponse.embedded().recipientlists().stream()
			.filter(x -> recipientName.equalsIgnoreCase(x.name()))
			.mapToInt(RapidMailRecipientListResponse.Item::id)
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
		PostMailingsRequest.File file = new PostMailingsRequest.File(
			"mail-content.zip",
			"application/zip",
			createZipFile(emailHtml, null)
		);
		PostMailingsRequest.Destination destination = new PostMailingsRequest.Destination("include", getRecipientId(config), "recipientlist");
		PostMailingsRequest request = new PostMailingsRequest(
			List.of(destination),
			file,
			region.getWarningServiceEmail(lang),
			region.getWarningServiceName(lang),
			null,
			null,
			null,
			subject
		);
		sendMessage(request, config);
	}
}
