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

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

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
