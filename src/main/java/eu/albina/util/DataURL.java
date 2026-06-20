package eu.albina.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import com.google.common.io.Resources;
import com.google.common.net.MediaType;

public interface DataURL {

	/**
	 * Returns a data URL for the given image.
	 *
	 * @param mediaType the media type of the image
	 * @param bytes     the image bytes
	 * @return data URL
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc2397">RFC 2397: The "data" URL scheme</a>
	 * @see <a href="https://developer.mozilla.org/en-US/docs/web/http/basics_of_http/data_urls">Data URLs - HTTP | MDN</a>
	 */
	static String of(MediaType mediaType, byte[] bytes) {
		return "data:" + mediaType + ";base64," + Base64.getEncoder().encodeToString(bytes);
	}

	static String ofPath(Path path) throws IOException {
		String contentType = Files.probeContentType(path);
		MediaType mediaType = contentType != null ? MediaType.parse(contentType) : MediaType.OCTET_STREAM;
		return of(mediaType, Files.readAllBytes(path));
	}

	static String ofResource(String resource) {
		try {
			Path path = Path.of(Resources.getResource(resource).toURI());
			return ofPath(path);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
