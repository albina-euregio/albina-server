// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.util.Objects;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record LocalServerInstance(
	boolean publishAt5PM,
	boolean publishAt8AM,
	String mapsPath,
	String mapProductionUrl,
	String pdfDirectory,
	String htmlDirectory,
	String mediaPath
) {
	public LocalServerInstance {
		Objects.requireNonNull(mapsPath, "mapsPath");
		Objects.requireNonNull(mapProductionUrl, "mapProductionUrl");
		Objects.requireNonNull(pdfDirectory, "pdfDirectory");
		Objects.requireNonNull(htmlDirectory, "htmlDirectory");
		Objects.requireNonNull(mediaPath, "mediaPath");
	}
}
