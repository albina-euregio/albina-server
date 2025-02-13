package eu.albina.model;

import eu.albina.model.enumerations.LanguageCode;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;

import static eu.albina.util.AlbinaUtil.localZone;

public interface HasValidityDate {
	LocalDate getValidityDate();

	default String getValidityDateString() {
		return getValidityDate().toString();
	}

	default String getTendencyDate(LanguageCode lang) {
		LocalDate date = getValidityDate().plusDays(1);
		return lang.getBundleString("tendency.binding-word").strip() + " " + lang.getLongDate(date.atStartOfDay(localZone()));
	}

	default String getDate(LanguageCode lang) {
		LocalDate date = getValidityDate();
		return lang.getLongDate(date.atStartOfDay(localZone()));
	}

	default String getValidityDateString(Period offset) {
		return getValidityDate().plus(offset).toString();
	}

	default String getValidityDateString(Period offset, LanguageCode lang) {
		LocalDate date = getValidityDate().plus(offset);
		return lang.getDate(date.atStartOfDay(localZone()));
	}

	default String getPreviousValidityDateString(LanguageCode lang) {
		return getValidityDateString(Period.ofDays(-1), lang);
	}

	default String getNextValidityDateString(LanguageCode lang) {
		return getValidityDateString(Period.ofDays(1), lang);
	}

	default boolean isLatest() {
		return isLatest(Clock.system(localZone()));
	}

	default boolean isLatest(Clock clock) {
		LocalDate date = getValidityDate();
		ZonedDateTime now = ZonedDateTime.now(clock);

		if (now.getHour() >= 17) {
			return date.equals(now.toLocalDate().plusDays(1));
		} else {
			return date.equals(now.toLocalDate());
		}
	}
}
