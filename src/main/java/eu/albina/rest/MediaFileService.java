/*******************************************************************************
 * Copyright (C) 2022 Norbert Lanzanasto
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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONObject;

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
import eu.albina.rest.filter.Secured;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.RssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@MultipartConfig
@Path("/media")
@Tag(name = "media")
public class MediaFileService {

	private static final Logger logger = LoggerFactory.getLogger(MediaFileService.class);

	@Context
	UriInfo uri;

	@POST
	@Secured({Role.ADMIN, Role.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Produces({MediaType.APPLICATION_JSON})
	@Operation(summary = "Save media file")
	public Response saveMediaFile(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryParam("date") String dateString,
		@QueryParam("region") String regionId,
		@QueryParam("lang") LanguageCode language,
		@QueryParam("important") boolean important,
		@FormDataParam("text") String mediaText,
		@FormDataParam("file") InputStream uploadedInputStream,
		@FormDataParam("file") FormDataContentDisposition fileDetail,
		@Context SecurityContext securityContext) {
		try {
			logger.info("Saving media file: {} (size={}, type={})", fileDetail.getFileName(), fileDetail.getSize(), fileDetail.getType());

			Region region = RegionController.getInstance().getRegionOrThrowAlbinaException(regionId);
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region == null || !user.hasPermissionForRegion(region.getId())) {
				logger.warn("User is not authorized for this region!");
				throw new AlbinaException("User is not authorized for this region!");
			}

			Instant date = DateControllerUtil.parseDateOrThrow(dateString);
			ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

			java.nio.file.Path fileLocation = getMediaPath(localServerInstance, region, language);
			Files.createDirectories(fileLocation);

			// save mp3 file
			String mp3FileName = getMediaFileName(dateString, user, language, ".mp3");
			java.nio.file.Path mp3File = fileLocation.resolve(mp3FileName);
			Files.copy(uploadedInputStream, mp3File, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			logger.info("{} successfully uploaded to: {}", mp3FileName, mp3File);

			// save text file
			String txtFileName = getMediaFileName(dateString, user, language, ".txt");
			java.nio.file.Path txtFile = fileLocation.resolve(txtFileName);
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

			return Response.status(200).type(MediaType.APPLICATION_JSON).entity(new JSONObject().append("file", fileLocation)).build();
		} catch (AlbinaException e) {
			logger.warn("Failed to save media file", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		} catch (Exception e) {
			logger.warn("Failed to save media file", e);
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}

	@GET
	@Path("/rss")
	@Produces(MediaType.APPLICATION_XML)
	@Operation(summary = "Get media files as RSS feed")
	public Response getRssFeed(
		@QueryParam("region") @DefaultValue("AT-07") String regionId,
		@QueryParam("lang") @DefaultValue("de") LanguageCode language
	) throws Exception {
		final ServerInstance serverInstance = ServerInstanceController.getInstance().getLocalServerInstance();
		final Region region = new Region(regionId);
		final String websiteName = region.getWebsiteName(language);
		final String rss = RssUtil.getRss(
			language,
			region,
			getMediaPath(serverInstance, region, language));
		return Response.ok(rss, MediaType.APPLICATION_XML).build();
	}

	public static java.nio.file.Path getMediaPath(ServerInstance serverInstance, Region region, LanguageCode lang) {
		java.nio.file.Path mediaPath = Paths.get(serverInstance.getMediaPath());
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
