package eu.albina.model;

import eu.albina.model.enumerations.LanguageCode;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static eu.albina.util.AlbinaUtil.localZone;

public interface HasPublicationDate {

	ZonedDateTime getPublicationDate();

	default String getPublicationDate(LanguageCode lang) {
		ZonedDateTime dateTime = getPublicationDate().withZoneSameInstant(localZone()).truncatedTo(ChronoUnit.MINUTES);
		return lang.getDateTime(dateTime);
	}

	default String getPublicationTimeString() {
		DateTimeFormatter formatterPublicationTime = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.of("UTC"));
		return getPublicationDate().withZoneSameInstant(ZoneId.of("UTC")).format(formatterPublicationTime);
	}

	default boolean isUpdate() {
		LocalTime localTime = getPublicationDate().withZoneSameInstant(localZone()).toLocalTime();
		return !LocalTime.of(17, 0).equals(localTime);
	}
}
