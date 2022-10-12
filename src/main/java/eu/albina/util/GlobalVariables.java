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

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalVariables {

	private static final Logger logger = LoggerFactory.getLogger(GlobalVariables.class);

	public static String version;

	public static String propertiesFilePath = "META-INF/config.properties";

	// TODO use schema from caaml.org
	public static String albinaXmlSchemaUrl = "https://api.avalanche.report/caaml/albina.xsd";

	public static String tmpDirectory = System.getProperty("java.io.tmpdir");

	public static String getTmpMapsPath() {
		return tmpDirectory;
	}

	public static String getTmpPdfDirectory() {
		return tmpDirectory;
	}

	public static int[] getRGB(final String hex) {
		final int[] ret = new int[3];
		for (int i = 0; i < 3; i++)
		{
			ret[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
		}
		return ret;
	}

	public static void loadConfigProperties() {
		Configurations configs = new Configurations();
		Configuration config;
		try {
			config = configs.properties(propertiesFilePath);
			if (config.containsKey("gitVersion"))
				version = config.getString("gitVersion");
			logger.info("Configuration file loaded!");
		} catch (ConfigurationException e) {
			logger.error("Configuration file could not be loaded!", e);
		}
	}

}
