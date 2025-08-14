// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import eu.albina.exception.AlbinaException;
import eu.albina.util.AlbinaUtil;

interface DateControllerUtil {
	String DATE_FORMAT_DESCRIPTION = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ";

	static Instant parseDate(String date) {
		return parseDateString(date).toInstant();
	}

	private static ZonedDateTime parseDateString(String date) {
		Objects.requireNonNull(date, "date");
		return date.length() == "2006-01-02".length()
			? LocalDate.parse(date).atStartOfDay(AlbinaUtil.localZone())
			: ZonedDateTime.parse(date);
	}

	static Instant parseDateOrToday(String date) {
		if (date != null) {
			return parseDate(date);
		} else {
			return LocalDate.now().atStartOfDay(AlbinaUtil.localZone()).toInstant();
		}
	}

	static Instant parseDateOrThrow(String date) throws AlbinaException {
		if (date != null) {
			return parseDate(date);
		} else {
			throw new AlbinaException("No date!");
		}
	}

	static Instant parseDateOrNull(String date) {
		if (date != null) {
			return parseDate(date);
		} else {
			return null;
		}
	}

	static ZoneId parseTimezoneOrLocal(String timezone) {
		if (Strings.isNullOrEmpty(timezone)) {
			return AlbinaUtil.localZone();
		} else {
			return ZoneId.of(timezone);
		}
	}

	static OffsetDateTime getStartOfHydrologicalYear(OffsetDateTime date) {
		OffsetDateTime startDate = date;
		if (startDate.getMonthValue() < 10) {
			startDate = startDate.minusYears(1);
		}
		return startDate.withMonth(10).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
	}

	static Range<Instant> parseHydrologicalYearInstantRange(String date) {
		OffsetDateTime endDate = OffsetDateTime.parse(date);
		OffsetDateTime startDate = DateControllerUtil.getStartOfHydrologicalYear(endDate);
		Instant startInstant = startDate.toInstant();
		Instant endInstant = endDate.toInstant();
		return Range.closed(startInstant, endInstant);
	}

	static Range<Instant> parseInstantRange(String date) {
		OffsetDateTime startDate = OffsetDateTime.parse(date);
		OffsetDateTime endDate = startDate.plusDays(1);
		Instant startInstant = startDate.toInstant();
		Instant endInstant = endDate.toInstant();
		return Range.closed(startInstant, endInstant);
	}
}
