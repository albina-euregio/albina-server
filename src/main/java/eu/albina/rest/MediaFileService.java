// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.UserRepository;
import eu.albina.model.LocalServerInstance;
import eu.albina.util.GlobalVariables;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.MultipartConfig;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.publication.rapidmail.RapidMailController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.RssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@MultipartConfig
@Controller("/media")
@Tag(name = "media")
public class MediaFileService {

	private static final Logger logger = LoggerFactory.getLogger(MediaFileService.class);

	@Inject
	private AvalancheReportController avalancheReportController;

	@Inject
	RegionRepository regionRepository;

	@Inject
	private GlobalVariables globalVariables;

	@Inject
	private UserRepository userRepository;

	@Inject
	private RapidMailController rapidMailController;

	@Post
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Operation(summary = "Save media file")
	public void saveMediaFile(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String dateString,
		@QueryValue("region") String regionId,
		@QueryValue("lang") LanguageCode language,
		@QueryValue("important") boolean important,
		@Part("text") String text,
		@Part("file") CompletedFileUpload file,
		Principal principal) {
		try {
			logger.info("Saving media file: {} (size={}, type={})", file.getFilename(), 0, file.getContentType());

			Region region = regionRepository.findById(regionId).orElseThrow();
			User user = userRepository.findByIdOrElseThrow(principal);

			if (!user.hasPermissionForRegion(region.getId())) {
				logger.warn("User is not authorized for this region!");
				throw new AlbinaException("User is not authorized for this region!");
			}

			Instant date = DateControllerUtil.parseDateOrThrow(dateString);
			LocalServerInstance localServerInstance = globalVariables.getLocalServerInstance();

			Path fileLocation = getMediaPath(localServerInstance, region, language);
			Files.createDirectories(fileLocation);

			// save mp3 file
			String mp3FileName = getMediaFileName(dateString, user, language, ".mp3");
			Path mp3File = fileLocation.resolve(mp3FileName);
			Files.write(mp3File, file.getBytes());
			logger.info("{} successfully uploaded to: {}", mp3FileName, mp3File);

			// save text file
			String txtFileName = getMediaFileName(dateString, user, language, ".txt");
			Path txtFile = fileLocation.resolve(txtFileName);
			Files.write(txtFile, text.getBytes());
			logger.info("{} successfully uploaded to {}", txtFileName, txtFile);

			// send emails
			LocalDate localDate = date.atZone(AlbinaUtil.localZone()).toLocalDate();
			String formattedDate = language.getLongDate(localDate);

			String mp3FileUrl = getMediaFileUrl(language, region, localServerInstance) + "/" + mp3FileName;

			String subject = MessageFormat.format(language.getBundleString("email.media.subject"), region.getWebsiteName(language), formattedDate, user.getName());
			String emailHtml = String.format("%s<br><br>%s<br><br>%s",
				text.replace("\n", "<br>"),
				String.format("<a href=\"%s\">%s</a>", mp3FileUrl, language.getBundleString("email.media.link.mp3")),
				MessageFormat.format(language.getBundleString("email.media.text"), user.getName()));
			RapidMailConfiguration config = rapidMailController.getConfiguration(region, language, "media").orElseThrow();
			rapidMailController.sendEmail(config, emailHtml, subject);

			if (important) {
				subject = MessageFormat.format(language.getBundleString("email.media.important.subject"), region.getWebsiteName(language), formattedDate, user.getName());
				config = rapidMailController.getConfiguration(region, language, "media+").orElseThrow();
				rapidMailController.sendEmail(config, emailHtml, subject);
			}

			// set publication flag
			avalancheReportController.setMediaFileFlag(date, region);
		} catch (Exception e) {
			logger.warn("Failed to save media file", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Get("/rss")
	@Produces("application/rss+xml")
	@Operation(summary = "Get media files as RSS feed")
	public String getRssFeed(
		@QueryValue(value = "region", defaultValue = "AT-07") String regionId,
		@QueryValue(value = "lang", defaultValue = "de") LanguageCode language
	) throws Exception {
		final LocalServerInstance serverInstance = globalVariables.getLocalServerInstance();
		final Region region = new Region(regionId);
		return RssUtil.getRss(
			language,
			region,
			getMediaPath(serverInstance, region, language));
	}

	public static Path getMediaPath(LocalServerInstance serverInstance, Region region, LanguageCode lang) {
		Path mediaPath = Paths.get(serverInstance.mediaPath());
		return mediaPath
			.resolve(region.getId())
			.resolve(lang.name());
	}

	public static String getMediaFileUrl(LanguageCode lang, Region region, LocalServerInstance serverInstance) {
		String mediaFileDirectory = Paths.get(serverInstance.mediaPath()).getFileName().toString();
		return String.format("%s/%s/%s/%s", region.getStaticUrl(), mediaFileDirectory, region.getId(), lang);
	}

	public static String getMediaFileName(String date, User user, LanguageCode language, String fileExtension) {
		String stringDate = OffsetDateTime.parse(date).toLocalDate().toString();
		return stringDate + "_" + language.getBundleString("media-file.name") + "_" + user.getName().toLowerCase().replace(" ", "-") + fileExtension;
	}

}
