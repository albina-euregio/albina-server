// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.model.enumerations.Role;
import eu.albina.util.DeleteTempDirectoryOnClose;
import eu.albina.util.GlobalVariables;

import com.google.common.io.MoreFiles;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import org.caaml.v6.AvalancheBulletinCustomData.BulletinPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Controller("/bulletins/photo")
@Tag(name = "bulletins/photo")
public class AvalancheBulletinPhotoService {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinPhotoService.class);

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG, MediaType.IMAGE_WEBP);

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
		MediaType contentType = file0.getContentType().orElse(null);
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.getName())) {
			throw new HttpStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
				"Only %s images are allowed".formatted(ALLOWED_CONTENT_TYPES));
		}
		try (DeleteTempDirectoryOnClose dir = DeleteTempDirectoryOnClose.of("photo")) {
			String extension = MoreFiles.getFileExtension(Path.of(file0.getFilename()));
			Path file = dir.tempDirectory().resolve(uuid + "." + extension);
			Files.copy(file0.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
			logger.info("POST upload bulletin photo to {} ({} bytes) done", file, Files.size(file));
			Path mediaPath = Path.of(globalVariables.getLocalServerInstance().mediaPath());
			Path webpFile = mediaPath.resolve(regionId).resolve(uuid + ".webp");
			Files.createDirectories(webpFile.getParent());
			logger.info("POST upload bulletin photo: converting {} to {}", file, webpFile);
			new ProcessBuilder("cwebp",
				// Resize the source to a rectangle with size width x height. If either (but not both) of the width or height parameters is 0, the value will be calculated preserving the aspect-ratio.
				"-resize", "1600", "0",
				file.toString(), "-o", webpFile.toString()
			).inheritIO().start().waitFor();
			String url = webpFile.toUri().toString().replace("file:///var/www/", "https://"); // FIXME
			return new BulletinPhoto(url);
		} catch (Exception e) {
			logger.error("POST upload bulletin error", e);
			throw new RuntimeException(e);
		}
	}

}
