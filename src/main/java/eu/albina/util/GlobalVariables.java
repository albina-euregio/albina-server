// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import eu.albina.model.LocalServerInstance;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.util.Objects;

@Singleton
public class GlobalVariables {

	@Value("${albina.conf.git.version}")
	public String version;
	@Value("${albina.conf.publishAt5PM}")
	boolean publishAt5PM;
	@Value("${albina.conf.publishAt8AM}")
	boolean publishAt8AM;
	@Value("${albina.conf.pdfDirectory}")
	String pdfDirectory;
	@Value("${albina.conf.htmlDirectory}")
	String htmlDirectory;
	@Value("${albina.conf.mapsPath}")
	String mapsPath;
	@Value("${albina.conf.mediaPath}")
	String mediaPath;
	@Value("${albina.conf.incidentsPath}")
	String incidentsPath;
	@Value("${albina.conf.mapProductionUrl}")
	String mapProductionUrl;
	@Value("${albina.conf.webauthnRpId}")
	String webauthnRpId;
	@Value("${albina.conf.webauthnRpName}")
	String webauthnRpName;
	@Value("${albina.conf.webauthnOrigin}")
	String webauthnOrigin;

	public String getIncidentsPath() {
		return Objects.requireNonNull(incidentsPath, "incidentsPath");
	}

	/** The WebAuthn Relying Party ID: a registrable domain suffix of every origin passkeys are used from. */
	public String getWebauthnRpId() {
		return Objects.requireNonNull(webauthnRpId, "webauthnRpId");
	}

	public String getWebauthnRpName() {
		return Objects.requireNonNullElse(webauthnRpName, getWebauthnRpId());
	}

	/** The origin (scheme + host + port) this server instance's frontend is deployed at. */
	public String getWebauthnOrigin() {
		return Objects.requireNonNull(webauthnOrigin, "webauthnOrigin");
	}

	public LocalServerInstance getLocalServerInstance() {
		return new LocalServerInstance(
			publishAt5PM,
			publishAt8AM,
			Objects.requireNonNull(mapsPath, "mapsPath"),
			Objects.requireNonNull(mapProductionUrl, "mapProductionUrl"),
			Objects.requireNonNull(pdfDirectory, "pdfDirectory"),
			Objects.requireNonNull(htmlDirectory, "htmlDirectory"),
			Objects.requireNonNull(mediaPath, "mediaPath")
		);
	}

	public LocalServerInstance getLocalServerInstance(String pdfDirectory, String mapsPath) {
		return new LocalServerInstance(
			publishAt5PM,
			publishAt8AM,
			Objects.requireNonNullElse(mapsPath, this.mapsPath),
			Objects.requireNonNull(mapProductionUrl, "mapProductionUrl"),
			Objects.requireNonNullElse(pdfDirectory, this.pdfDirectory),
			Objects.requireNonNull(htmlDirectory, "htmlDirectory"),
			Objects.requireNonNull(mediaPath, "mediaPath")
		);
	}
}
