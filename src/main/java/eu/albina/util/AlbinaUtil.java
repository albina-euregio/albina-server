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

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.enumerations.DangerRating;

public interface AlbinaUtil {

	Logger logger = LoggerFactory.getLogger(AlbinaUtil.class);

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

	static Instant getInstantNowNoNanos() {
		return ZonedDateTime.now().withNano(0).toInstant();
	}

	static ZonedDateTime getZonedDateTimeNowNoNanos() {
		return AlbinaUtil.getInstantNowNoNanos().atZone(ZoneId.of("UTC"));
	}

	static ZonedDateTime getZonedDateTimeUtc(Instant instant) {
		return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
	}

}
