/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public interface AlbinaUtil {

	Logger logger = LoggerFactory.getLogger(AlbinaUtil.class);
	DateTimeFormatter formatterPublicationTime = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.of("UTC"));

	static ZoneId localZone() {
		return ZoneId.of("Europe/Vienna");
	}

	static LocalTime validityStart() {
		return LocalTime.of(17, 0);
	}

	static String getWarningLevelId(AvalancheBulletinDaytimeDescription avalancheBulletinDaytimeDescription) {
		if (avalancheBulletinDaytimeDescription.isHasElevationDependency())
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingBelow()) + "_"
				+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
		else
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove()) + "_"
				+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
	}

	static String getTendencyDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		LocalDate date = getValidityDate(bulletins).plusDays(1);
		return lang.getBundleString("tendency.binding-word").strip() + " " + lang.getLongDate(date.atStartOfDay(localZone()));
	}

	static Instant getInstantNowNoNanos() {
		return ZonedDateTime.now().withNano(0).toInstant();
	}

	static ZonedDateTime getZonedDateTimeNowNoNanos() {
		return AlbinaUtil.getInstantNowNoNanos().atZone(ZoneId.of("UTC"));
	}

	static ZonedDateTime getZonedDateTimeUtc(Instant instant) {
		return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
	}

	static String getDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		LocalDate date = getValidityDate(bulletins);
		return lang.getLongDate(date.atStartOfDay(localZone()));
	}

	static String getValidityDateString(List<AvalancheBulletin> bulletins) {
		return getValidityDate(bulletins).toString();
	}

	static LocalDate getValidityDate(List<AvalancheBulletin> bulletins) {
		return bulletins.stream()
			.map(AvalancheBulletin::getValidityDate)
			.max(Comparator.naturalOrder())
			.orElseThrow();
	}

	static String getValidityDateString(List<AvalancheBulletin> bulletins, Period offset) {
		return getValidityDate(bulletins).plus(offset).toString();
	}

	static String getValidityDateString(List<AvalancheBulletin> bulletins, Period offset, LanguageCode lang) {
		LocalDate date = getValidityDate(bulletins).plus(offset);
		return lang.getDate(date.atStartOfDay(localZone()));
	}

	static String getPreviousValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		return getValidityDateString(bulletins, Period.ofDays(-1), lang);
	}

	static String getNextValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		return getValidityDateString(bulletins, Period.ofDays(1), lang);
	}

	static boolean isUpdate(List<AvalancheBulletin> bulletins) {
		Instant instant = getPublicationDate(bulletins);
		LocalTime localTime = instant.atZone(localZone()).toLocalTime();
		return !LocalTime.of(17, 0).equals(localTime);
	}

	static Instant getPublicationDate(List<AvalancheBulletin> bulletins) {
		return bulletins.stream()
			.map(AvalancheBulletin::getPublicationDate)
			.filter(Objects::nonNull)
			.map(ZonedDateTime::toInstant)
			.max(Comparator.naturalOrder())
			.orElse(null);

	}

	static String getPublicationDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		Instant instant = getPublicationDate(bulletins);
		if (instant == null) {
			return "";
		}
		ZonedDateTime dateTime = instant.atZone(localZone()).truncatedTo(ChronoUnit.MINUTES);
		return lang.getDateTime(dateTime);
	}

	static String getPublicationDateDirectory(List<AvalancheBulletin> bulletins) {
		Instant instant = getPublicationDate(bulletins);
		if (instant == null) {
			// PDF preview, for instance
			return "";
		}
		return getPublicationDateDirectory(instant);
	}

	static String getPublicationDateDirectory(Instant publicationTime) {
		return publicationTime.atZone(ZoneId.of("UTC")).format(formatterPublicationTime);
	}

	static boolean isLatest(List<AvalancheBulletin> bulletins) {
		return isLatest(bulletins, Clock.system(localZone()));
	}

	static boolean isLatest(List<AvalancheBulletin> bulletins, Clock clock) {
		LocalDate date = getValidityDate(bulletins);
		ZonedDateTime now = ZonedDateTime.now(clock);

		if (now.getHour() >= 17) {
			return date.equals(now.toLocalDate().plusDays(1));
		} else {
			return date.equals(now.toLocalDate());
		}
	}

}
