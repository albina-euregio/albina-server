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
	@Value("${albina.conf.mapProductionUrl}")
	String mapProductionUrl;

	public LocalServerInstance getLocalServerInstance() {
		return new LocalServerInstance(
			publishAt5PM,
			publishAt8AM,
			mapsPath,
			mapProductionUrl,
			pdfDirectory,
			htmlDirectory,
			mediaPath
		);
	}

	public LocalServerInstance getLocalServerInstance(String pdfDirectory, String mapsPath) {
		return new LocalServerInstance(
			publishAt5PM,
			publishAt8AM,
			Objects.requireNonNullElse(mapsPath, this.mapsPath),
			mapProductionUrl,
			Objects.requireNonNullElse(pdfDirectory, this.pdfDirectory),
			htmlDirectory,
			mediaPath
		);
	}
}
