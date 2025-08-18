// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import static eu.albina.util.AlbinaUtil.localZone;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.albina.model.enumerations.LanguageCode;

public interface HasPublicationDate {

	ZonedDateTime getPublicationDate();

	default String getPublicationDate(LanguageCode lang) {
		ZonedDateTime dateTime = getPublicationDate().withZoneSameInstant(localZone()).truncatedTo(ChronoUnit.MINUTES);
		return lang.getDateTime(dateTime);
	}

	@JsonIgnore
	default String getPublicationTimeString() {
		DateTimeFormatter formatterPublicationTime = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.of("UTC"));
		return getPublicationDate().withZoneSameInstant(ZoneId.of("UTC")).format(formatterPublicationTime);
	}

	@JsonIgnore
	default boolean isUpdate() {
		LocalTime localTime = getPublicationDate().withZoneSameInstant(localZone()).toLocalTime();
		return !LocalTime.of(17, 0).equals(localTime);
	}
}
