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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.github.openjson.JSONObject;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.controller.ServerInstanceController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Role;
import eu.albina.rest.filter.Secured;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.EmailUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Path("/media")
@Api(value = "/media")
public class MediaFileService {

	private static final Logger logger = LoggerFactory.getLogger(MediaFileService.class);

	@Context
	UriInfo uri;

	@POST
	@Secured({ Role.ADMIN, Role.FORECASTER})
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	public Response saveMediaFile(
		@ApiParam(value = "Start date in the format yyyy-MM-dd'T'HH:mm:ssZZ") @QueryParam("date") String dateString,
		@QueryParam("region") String regionId,
		@QueryParam("lang") LanguageCode language,
		@FormDataParam("text") String mediaText,
        @FormDataParam("file") InputStream uploadedInputStream,
		@Context SecurityContext securityContext) {
		try {
			
			if (regionId == null) {
				logger.warn("Region for media file not defined!");
				return Response.status(400).type(MediaType.APPLICATION_JSON).entity("Region for media file not defined!").build();
			}
			
			Region region = RegionController.getInstance().getRegion(regionId);
			User user = UserController.getInstance().getUser(securityContext.getUserPrincipal().getName());

			if (region == null || !user.hasPermissionForRegion(region.getId())) {
				logger.warn("User is not authorized for this region!");
				throw new AlbinaException("User is not authorized for this region!");
			}

			Instant date = DateControllerUtil.parseDateOrThrow(dateString);

			String fileLocation = ServerInstanceController.getInstance().getLocalServerInstance().getMediaPath() + "/" + region + "/";

			// save mp3 file
			String mp3FileName = AlbinaUtil.getMediaFileName(dateString, user, language, ".mp3");
			File mp3File = new File(fileLocation + mp3FileName);
			mp3File.getParentFile().mkdir();
			FileOutputStream out = new FileOutputStream(mp3File);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
			logger.debug(mp3FileName + " successfully uploaded to: " + fileLocation);

			// save text file
			String txtFileName = AlbinaUtil.getMediaFileName(dateString, user, language, ".txt");
			File txtFile = new File(fileLocation + txtFileName);
			txtFile.getParentFile().mkdir();
			FileOutputStream outputStream = new FileOutputStream(txtFile);
			byte[] strToBytes = mediaText.getBytes();
			outputStream.write(strToBytes);
			outputStream.close();
			logger.debug(txtFileName + " successfully uploaded to " + fileLocation);

			// send emails
			EmailUtil.getInstance().sendMediaEmails(mediaText, date, region, user.getName(), false, language);

			// set publication flag
			AvalancheReportController.getInstance().setMediaFileFlag(date, region);

			return Response.status(200).type(MediaType.APPLICATION_JSON).entity(new JSONObject().append("file", fileLocation)).build();
		} catch (AlbinaException e) {
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toJSON()).build();
		} catch (FileNotFoundException e) {
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		} catch (IOException e) {
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		} catch (URISyntaxException e) {
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		} catch (Exception e) {
			return Response.status(400).type(MediaType.APPLICATION_JSON).entity(e.toString()).build();
		}
	}
}
