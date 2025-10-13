// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class GlobalVariables {

	private static final Logger logger = LoggerFactory.getLogger(GlobalVariables.class);

	public static String version;

	public static String propertiesFilePath = "META-INF/config.properties";

	public static void loadConfigProperties() {
		try (InputStream inputStream = Resources.getResource(propertiesFilePath).openStream()) {
			Properties properties = new Properties();
			properties.load(inputStream);
			version = properties.getProperty("gitVersion");
			logger.info("Configuration file loaded!");
		} catch (Exception e) {
			logger.error("Configuration file could not be loaded!", e);
		}
	}

}
