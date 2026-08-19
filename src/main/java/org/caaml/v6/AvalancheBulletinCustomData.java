// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@JsonPropertyOrder(alphabetic = true)
public record AvalancheBulletinCustomData(
	@JacksonXmlProperty(localName = "ALBINA")
	ALBINA ALBINA,
	@JacksonXmlProperty(localName = "LWD_Tyrol")
	LwdTyrol LWD_Tyrol
) {

	@Serdeable
	public record ALBINA(
		String mainDate,
		@JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "bulletinPhoto")
		List<BulletinPhoto> bulletinPhotos
	) {
	}

	@Serdeable
	public record LwdTyrol(
		@JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "dangerPatterns")
		List<String> dangerPatterns
	) {
	}

	@Serdeable
	public record BulletinPhoto(
		String url,
		String copyright,
		LocalDate date,
		String microRegionId,
		String locationName,
		Double latitude,
		Double longitude
	) {
		public BulletinPhoto(String url) {
			this(url, null, null, null, null, null, null);
		}
	}
}
