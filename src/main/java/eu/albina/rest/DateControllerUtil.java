package eu.albina.rest;

import com.google.common.base.Strings;
import eu.albina.exception.AlbinaException;
import eu.albina.util.AlbinaUtil;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

interface DateControllerUtil {
	String DATE_FORMAT_DESCRIPTION = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ";

	static Instant parseDate(String date) {
		Objects.requireNonNull(date, "date");
		return date.length() == "2006-01-02".length()
			? LocalDate.parse(date).atStartOfDay(AlbinaUtil.localZone()).toInstant()
			: ZonedDateTime.parse(date).toInstant();
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
}
