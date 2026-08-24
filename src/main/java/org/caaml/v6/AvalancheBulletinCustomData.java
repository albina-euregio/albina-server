// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import io.micronaut.core.annotation.Nullable;
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
		@Nullable TendencyProgression tendencyProgression,
		@Nullable @JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "bulletinPhoto")
		List<BulletinPhoto> bulletinPhotos
	) {
	}

	@Serdeable
	public record TendencyProgression(
		List<LocalDate> dates,
		Map<String, List<DangerRatingValue>> dangerRatings) {
	}


	@Serdeable
	public record LwdTyrol(
		@Nullable @JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "dangerPatterns")
		List<String> dangerPatterns
	) {
	}

	@Serdeable
	public record BulletinPhoto(
		String url,
		@Nullable String copyright,
		@Nullable LocalDate date,
		@Nullable String microRegionId,
		@Nullable String locationName,
		@Nullable Double latitude,
		@Nullable Double longitude
	) {
		public BulletinPhoto(String url) {
			this(url, null, null, null, null, null, null);
		}
	}
}
