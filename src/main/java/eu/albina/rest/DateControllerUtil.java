package eu.albina.rest;

import com.google.common.base.Strings;
import eu.albina.exception.AlbinaException;
import eu.albina.util.AlbinaUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

interface DateControllerUtil {
	String DATE_FORMAT_DESCRIPTION = "Date in the format yyyy-MM-dd'T'HH:mm:ssZZ";

	static Instant parseDateOrToday(String date) {
		if (date != null) {
			return ZonedDateTime.parse(date).toInstant();
		} else {
			return AlbinaUtil.getInstantStartOfDay();
		}
	}

	static Instant parseDateOrThrow(String date) throws AlbinaException {
		if (date != null) {
			return ZonedDateTime.parse(date).toInstant();
		} else {
			throw new AlbinaException("No date!");
		}
	}

	static Instant parseDateOrNull(String date) {
		if (date != null) {
			return ZonedDateTime.parse(date).toInstant();
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
