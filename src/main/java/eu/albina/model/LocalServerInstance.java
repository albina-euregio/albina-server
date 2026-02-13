// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

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
}
