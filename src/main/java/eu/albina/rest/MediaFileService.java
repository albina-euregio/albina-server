// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.security.annotation.Secured;
import jakarta.servlet.annotation.MultipartConfig;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.controller.ServerInstanceController;
import eu.albina.controller.UserController;
import eu.albina.controller.publication.RapidMailController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
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


	@Post
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Operation(summary = "Save media file")
	public HttpResponse<?> saveMediaFile(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String dateString,
		@QueryValue("region") String regionId,
		@QueryValue("lang") LanguageCode language,
		@QueryValue("important") boolean important,
		String mediaText, // FIXME
		StreamingFileUpload file, // FIXME
		Principal principal) {
		try {
			logger.info("Saving media file: {} (size={}, type={})", file.getFilename(), 0, file.getContentType());

			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			User user = UserController.getInstance().getUser(principal.getName());

			if (region == null || !user.hasPermissionForRegion(region.getId())) {
				logger.warn("User is not authorized for this region!");
				throw new AlbinaException("User is not authorized for this region!");
			}

			Instant date = DateControllerUtil.parseDateOrThrow(dateString);
			ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

			Path fileLocation = getMediaPath(localServerInstance, region, language);
			Files.createDirectories(fileLocation);

			// save mp3 file
			String mp3FileName = getMediaFileName(dateString, user, language, ".mp3");
			Path mp3File = fileLocation.resolve(mp3FileName);
			Files.copy(file.asInputStream(), mp3File, StandardCopyOption.REPLACE_EXISTING);
			logger.info("{} successfully uploaded to: {}", mp3FileName, mp3File);

			// save text file
			String txtFileName = getMediaFileName(dateString, user, language, ".txt");
			Path txtFile = fileLocation.resolve(txtFileName);
			Files.write(txtFile, mediaText.getBytes());
			logger.info("{} successfully uploaded to {}", txtFileName, txtFile);

			// send emails
			ZonedDateTime localDate = date.atZone(AlbinaUtil.localZone());
			String formattedDate = language.getLongDate(localDate);

			String mp3FileUrl = getMediaFileUrl(language, region, localServerInstance) + "/" + mp3FileName;

			String subject = MessageFormat.format(language.getBundleString("email.media.subject"), region.getWebsiteName(language), formattedDate, user.getName());
			String text = language.getBundleString("email.media.link.mp3");
			String emailHtml = String.format("%s<br><br>%s<br><br>%s",
				mediaText.replace("\n", "<br>"),
				String.format("<a href=\"%s\">%s</a>", mp3FileUrl, text),
				MessageFormat.format(language.getBundleString("email.media.text"), user.getName()));
			RapidMailConfiguration config = RapidMailController.getConfiguration(region, language, "media").orElseThrow();
			RapidMailController.sendEmail(config, emailHtml, subject);

			if (important) {
				subject = MessageFormat.format(language.getBundleString("email.media.important.subject"), region.getWebsiteName(language), formattedDate, user.getName());
				config = RapidMailController.getConfiguration(region, language, "media+").orElseThrow();
				RapidMailController.sendEmail(config, emailHtml, subject);
			}

			// set publication flag
			AvalancheReportController.getInstance().setMediaFileFlag(date, region);

			return HttpResponse.created(Map.of("file", fileLocation));
		} catch (AlbinaException e) {
			logger.warn("Failed to save media file", e);
			return HttpResponse.badRequest().body(e.toJSON());
		} catch (Exception e) {
			logger.warn("Failed to save media file", e);
			return HttpResponse.badRequest().body(e.toString());
		}
	}

	@Get("/rss")
	@Produces(MediaType.APPLICATION_XML)
	@Operation(summary = "Get media files as RSS feed")
	public HttpResponse<?> getRssFeed(
		@QueryValue(value = "region", defaultValue = "AT-07") String regionId,
		@QueryValue(value = "lang", defaultValue = "de") LanguageCode language
	) throws Exception {
		final ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
		final Region region = new Region(regionId);
		final String websiteName = region.getWebsiteName(language);
		final String rss = RssUtil.getRss(
			language,
			region,
			getMediaPath(serverInstance, region, language));
		return HttpResponse.ok(rss).contentType(MediaType.APPLICATION_XML);
	}

	public static Path getMediaPath(ServerInstance serverInstance, Region region, LanguageCode lang) {
		Path mediaPath = Paths.get(serverInstance.getMediaPath());
		return mediaPath
			.resolve(region.getId())
			.resolve(lang.name());
	}

	public static String getMediaFileUrl(LanguageCode lang, Region region, ServerInstance serverInstance) {
		String mediaFileDirectory = Paths.get(serverInstance.getMediaPath()).getFileName().toString();
		return String.format("%s/%s/%s/%s", region.getStaticUrl(), mediaFileDirectory, region.getId(), lang);
	}

	public static String getMediaFileName(String date, User user, LanguageCode language, String fileExtension) {
		String stringDate = OffsetDateTime.parse(date).toLocalDate().toString();
		return stringDate + "_" + language.getBundleString("media-file.name") + "_" + user.getName().toLowerCase().replace(" ", "-") + fileExtension;
	}

}
