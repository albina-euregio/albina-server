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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.caaml.CaamlVersion;
import eu.albina.model.enumerations.LanguageCode;

public class GlobalVariables {

	private static final Logger logger = LoggerFactory.getLogger(GlobalVariables.class);

	public static String version;

	static String serverMapsUrl = "";
	static String serverPdfUrl = "";
	static String serverSimpleHtmlUrl = "";
	static String serverWebsiteUrl = "";

	public static DateTimeFormatter formatterPublicationTime = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.of("UTC"));

	@Deprecated
	public final static String codeTrentino = "IT-32-TN";
	@Deprecated
	public final static String codeSouthTyrol = "IT-32-BZ";
	@Deprecated
	public final static String codeTyrol = "AT-07";
	@Deprecated
	public final static String codeEuregio = "EUREGIO";
	@Deprecated
	public final static String codeAran = "ES-CT-L";

	public static String propertiesFilePath = "META-INF/config.properties";
	public static String albinaXmlSchemaUrl = "https://api.avalanche.report/caaml/albina.xsd";
	public static String csvDeliminator = ";";
	public static String csvLineBreak = "\n";

	public static int[] getRGB(final String hex) {
		final int[] ret = new int[3];
		for (int i = 0; i < 3; i++)
		{
			ret[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
		}
		return ret;
	}

	public static String tmpDirectory = System.getProperty("java.io.tmpdir");

	public static final String tokenEncodingIssuer = "albina";

	public static long accessTokenExpirationDuration = 1000 * 60 * 60 * 24;
	public static long refreshTokenExpirationDuration = 1000 * 60 * 60 * 24 * 7;
	public static long confirmationTokenExpirationDuration = 1000 * 60 * 60 * 24 * 3;

	public static String referenceSystemUrn = "urn:ogc:def:crs:OGC:1.3:CRS84";
	// public static String referenceSystemUrn = "EPSG:32632";
	public static String bulletinCaamlSchemaFileString = CaamlVersion.V5.schemaLocation();

	private static final String emailEncoding = "UTF-8";

	public static String notAvailableString = "N/A";

	public static String getTmpMapsPath() {
		return tmpDirectory;
	}

	public static String getTmpPdfDirectory() {
		return tmpDirectory;
	}

	public static long getAccessTokenExpirationDuration() {
		return accessTokenExpirationDuration;
	}

	public static void setAccessTokenExpirationDuration(long accessTokenExpirationDuration)
			throws ConfigurationException {
		GlobalVariables.accessTokenExpirationDuration = accessTokenExpirationDuration;
		setConfigProperty("accessTokenExpirationDuration", accessTokenExpirationDuration);
	}

	public static long getConfirmationTokenExpirationDuration() {
		return confirmationTokenExpirationDuration;
	}

	public static void setConfirmationTokenExpirationDuration(long confirmationTokenExpirationDuration)
			throws ConfigurationException {
		GlobalVariables.confirmationTokenExpirationDuration = confirmationTokenExpirationDuration;
		setConfigProperty("confirmationTokenExpirationDuration", confirmationTokenExpirationDuration);
	}

	public static String getBulletinCaamlSchemaFileString() {
		return bulletinCaamlSchemaFileString;
	}

	public static void setBulletinCaamlSchemaFileString(String bulletinCaamlSchemaFileString)
			throws ConfigurationException {
		GlobalVariables.bulletinCaamlSchemaFileString = bulletinCaamlSchemaFileString;
		setConfigProperty("bulletinCaamlSchemaFileString", bulletinCaamlSchemaFileString);
	}

	public static String getEuregioLogoPath(boolean grayscale) {
		if (grayscale)
			return "logo/grey/euregio.png";
		else
			return "logo/color/euregio.png";
	}

	public static String getAvalancheReportLogoPath(LanguageCode lang) {
		switch (lang) {
			case de:
				return "images/logo/color/lawinen_report.png";
			case it:
				return "images/logo/color/valanghe_report.png";
			case en:
				return "images/logo/color/avalanche_report.png";
			default:
				return "images/logo/color/avalanche_report.png";
		}
	}

	public static String getCopyrightText(LanguageCode lang) {
		// return AlbinaUtil.getYear(bulletins) + lang.getBundleString("copyright");
		return "";
	}

	public static String getEmailEncoding() {
		return emailEncoding;
	}

	public static void loadConfigProperties() {
		Configurations configs = new Configurations();
		Configuration config;
		try {
			config = configs.properties(propertiesFilePath);
			if (config.containsKey("gitVersion"))
				version = config.getString("gitVersion");
			if (config.containsKey("serverMapsUrl"))
				serverMapsUrl = config.getString("serverMapsUrl");
			if (config.containsKey("serverMapsUrl"))
				serverMapsUrl = config.getString("serverMapsUrl");
			if (config.containsKey("serverSimpleHtmlUrl"))
				serverSimpleHtmlUrl = config.getString("serverSimpleHtmlUrl");
			if (config.containsKey("serverPdfUrl"))
				serverPdfUrl = config.getString("serverPdfUrl");
			if (config.containsKey("serverWebsiteUrl"))
				serverWebsiteUrl = config.getString("serverWebsiteUrl");
			logger.info("Configuration file loaded!");
		} catch (ConfigurationException e) {
			logger.error("Configuration file could not be loaded!", e);
		}
	}

	private static void setConfigProperty(String key, Object value) throws ConfigurationException {
		Configurations configs = new Configurations();
		FileBasedConfigurationBuilder<PropertiesConfiguration> propertiesBuilder = configs
				.propertiesBuilder(propertiesFilePath);
		PropertiesConfiguration configuration;
		try {
			configuration = propertiesBuilder.getConfiguration();
			configuration.setProperty(key, value);
			propertiesBuilder.save();
			logger.info("[Configuration saved] {}: {}", key, value);
		} catch (ConfigurationException e) {
			logger.error("Configuration could not be saved!", e);
			throw e;
		}
	}
}
