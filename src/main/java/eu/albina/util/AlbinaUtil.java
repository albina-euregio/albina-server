// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.time.LocalTime;
import java.time.ZoneId;

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
			return DangerRating.getString(avalancheBulletinDaytimeDescription.dangerRating(false)) + "_"
				+ DangerRating.getString(avalancheBulletinDaytimeDescription.dangerRating(true));
		else
			return DangerRating.getString(avalancheBulletinDaytimeDescription.dangerRating(true)) + "_"
				+ DangerRating.getString(avalancheBulletinDaytimeDescription.dangerRating(true));
	}

}
