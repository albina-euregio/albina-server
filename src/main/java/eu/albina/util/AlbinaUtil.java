// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface AlbinaUtil {

	Logger logger = LoggerFactory.getLogger(AlbinaUtil.class);

	static LocalTime validityStart() {
		return LocalTime.of(17, 0);
	}

}
