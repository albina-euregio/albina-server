// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.model.enumerations.Role;
import eu.albina.util.DeleteTempDirectoryOnClose;
import eu.albina.util.GlobalVariables;

import com.google.common.io.MoreFiles;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.caaml.v6.AvalancheBulletinCustomData.BulletinPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller("/bulletins/photo")
@Tag(name = "bulletins/photo")
public class AvalancheBulletinPhotoService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinPhotoService.class);

	@Inject
	private GlobalVariables globalVariables;

	@Post
	@Secured({Role.Str.FORECASTER, Role.Str.FOREMAN})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Upload bulletin photo")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public BulletinPhoto uploadBulletinPhoto(
		@QueryValue("region") String regionId,
		@Part("file") CompletedFileUpload file0
	) {
		UUID uuid = UUID.randomUUID();
		logger.info("POST upload bulletin photo {}", file0.getFilename());
		try (DeleteTempDirectoryOnClose dir = DeleteTempDirectoryOnClose.of("photo")) {
			String extension = MoreFiles.getFileExtension(Path.of(file0.getFilename()));
			Path file = dir.tempDirectory().resolve(uuid + "." + extension);
			Files.copy(file0.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
			logger.info("POST upload bulletin photo to {} ({} bytes) done", file, Files.size(file));
			Path mediaPath = Path.of(globalVariables.getLocalServerInstance().mediaPath());
			Path webpFile = mediaPath.resolve(regionId).resolve(uuid + ".webp");
			logger.info("POST upload bulletin photo: converting {} to {}", file, webpFile);
			new ProcessBuilder("cwebp", "-resize", "1600", "1600", file.toString(), "-o", webpFile.toString()).inheritIO().start().waitFor();
			return new BulletinPhoto(webpFile.toUri().toString());
		} catch (Exception e) {
			logger.error("POST upload bulletin error", e);
			throw new RuntimeException(e);
		}
	}

}
