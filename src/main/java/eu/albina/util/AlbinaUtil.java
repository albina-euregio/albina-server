// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.time.LocalTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface AlbinaUtil {

	Logger logger = LoggerFactory.getLogger(AlbinaUtil.class);

	static ZoneId localZone() {
		return ZoneId.of("Europe/Vienna");
	}

	static LocalTime validityStart() {
		return LocalTime.of(17, 0);
	}

}
