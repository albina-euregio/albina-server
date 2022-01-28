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

import com.github.openjson.JSONObject;

import eu.albina.caaml.CaamlVersion;
import eu.albina.model.enumerations.LanguageCode;

public class GlobalVariables {

	private static final Logger logger = LoggerFactory.getLogger(GlobalVariables.class);

	public static String version;

	/*---- Defined in configuration file -----*/
	private static boolean createMaps = false;
	static String vapidPublicKey;
	static String vapidPrivateKey;
	private static boolean publishAt5PM = false;
	private static boolean publishAt8AM = false;
	/*---- Defined in configuration file -----*/

	static String serverImagesUrl = "https://admin.avalanche.report/images/";
	static String serverMapsUrl = "";
	static String serverPdfUrl = "";
	static String serverSimpleHtmlUrl = "";
	static String serverWebsiteUrl = "";

	public static String pdfDirectory = "/mnt/albina_files_local";
	public static String htmlDirectory = "/mnt/simple_local";
	public static String mapsPath = "/mnt/albina_files_local";
	public static String mapProductionUrl = "";

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

	public static String avalancheReportUsername = "info@avalanche.report";
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

	public static boolean isCreateMaps() {
		return createMaps;
	}

	public static void setCreateMaps(boolean createMaps) throws ConfigurationException {
		GlobalVariables.createMaps = createMaps;
		setConfigProperty("createMaps", createMaps);
	}

	public static String getVapidPublicKey() {
		return vapidPublicKey;
	}

	public static String getVapidPrivateKey() {
		return vapidPrivateKey;
	}

	public static boolean isPublishAt5PM() {
		return publishAt5PM;
	}

	public static void setPublishAt5PM(boolean publishAt5PM) throws ConfigurationException {
		GlobalVariables.publishAt5PM = publishAt5PM;
		setConfigProperty("publishAt5PM", publishAt5PM);
	}

	public static boolean isPublishAt8AM() {
		return publishAt8AM;
	}

	public static void setPublishAt8AM(boolean publishAt8AM) throws ConfigurationException {
		GlobalVariables.publishAt8AM = publishAt8AM;
		setConfigProperty("publishAt8AM", publishAt8AM);
	}

	public static String getPdfDirectory() {
		return pdfDirectory;
	}

	public static String getTmpPdfDirectory() {
		return tmpDirectory;
	}

	public static void setPdfDirectory(String pdfDirectory) throws ConfigurationException {
		GlobalVariables.pdfDirectory = pdfDirectory;
		setConfigProperty("pdfDirectory", pdfDirectory);
	}

	public static String getHtmlDirectory() {
		return htmlDirectory;
	}

	public static void setHtmlDirectory(String htmlDirectory) throws ConfigurationException {
		GlobalVariables.htmlDirectory = htmlDirectory;
		setConfigProperty("htmlDirectory", htmlDirectory);
	}

	public static String getServerImagesUrl() {
		return serverImagesUrl;
	}

	public static void setServerImagesUrl(String serverImagesUrl) throws ConfigurationException {
		GlobalVariables.serverImagesUrl = serverImagesUrl;
		setConfigProperty("serverImagesUrl", serverImagesUrl);
	}

	public static String getMapsPath() {
		return mapsPath;
	}

	public static String getTmpMapsPath() {
		return tmpDirectory;
	}

	public static void setMapsPath(String mapsPath) throws ConfigurationException {
		GlobalVariables.mapsPath = mapsPath;
		setConfigProperty("mapsPath", mapsPath);
	}

	public static String getMapProductionUrl() {
		return mapProductionUrl;
	}

	public static void setMapProductionUrl(String mapProductionUrl) throws ConfigurationException {
		GlobalVariables.mapProductionUrl = mapProductionUrl;
		setConfigProperty("mapProductionUrl", mapProductionUrl);
	}

	public static String getAvalancheReportUsername() {
		return avalancheReportUsername;
	}

	public static void setAvalancheReportUsername(String avalancheReportUsername) throws ConfigurationException {
		GlobalVariables.avalancheReportUsername = avalancheReportUsername;
		setConfigProperty("avalancheReportUsername", avalancheReportUsername);
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
			if (config.containsKey("pdfDirectory"))
				pdfDirectory = config.getString("pdfDirectory");
			if (config.containsKey("htmlDirectory"))
				htmlDirectory = config.getString("htmlDirectory");
			if (config.containsKey("serverImagesUrl"))
				serverImagesUrl = config.getString("serverImagesUrl");
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
			if (config.containsKey("mapsPath"))
				mapsPath = config.getString("mapsPath");
			if (config.containsKey("mapProductionUrl"))
				mapProductionUrl = config.getString("mapProductionUrl");
			if (config.containsKey("createMaps"))
				createMaps = config.getBoolean("createMaps");
			if (config.containsKey("publishAt5PM"))
				publishAt5PM = config.getBoolean("publishAt5PM");
			if (config.containsKey("publishAt8AM"))
				publishAt8AM = config.getBoolean("publishAt8AM");
			if (config.containsKey("vapidPublicKey"))
				vapidPublicKey = config.getString("vapidPublicKey");
			if (config.containsKey("vapidPrivateKey"))
				vapidPrivateKey = config.getString("vapidPrivateKey");
			logger.info("Configuration file loaded!");
		} catch (ConfigurationException e) {
			logger.error("Configuration file could not be loaded!", e);
		}
	}

	public static JSONObject getConfigProperties() {
		JSONObject json = new JSONObject();

		if (pdfDirectory != null)
			json.put("pdfDirectory", pdfDirectory);
		if (htmlDirectory != null)
			json.put("htmlDirectory", htmlDirectory);
		if (serverImagesUrl != null)
			json.put("serverImagesUrl", serverImagesUrl);
		if (mapsPath != null)
			json.put("mapsPath", mapsPath);
		if (mapProductionUrl != null)
			json.put("mapProductionUrl", mapProductionUrl);
		json.put("createMaps", createMaps);
		json.put("publishAt5PM", publishAt5PM);
		json.put("publishAt8AM", publishAt8AM);

		return json;
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

	public static void setConfigurationParameters(JSONObject configuration) throws ConfigurationException {
		if (configuration.has("pdfDirectory"))
			setPdfDirectory(configuration.getString("pdfDirectory"));
		if (configuration.has("htmlDirectory"))
			setHtmlDirectory(configuration.getString("htmlDirectory"));
		if (configuration.has("serverImagesUrl"))
			setServerImagesUrl(configuration.getString("serverImagesUrl"));
		if (configuration.has("mapsPath"))
			setMapsPath(configuration.getString("mapsPath"));
		if (configuration.has("mapProductionUrl"))
			setMapProductionUrl(configuration.getString("mapProductionUrl"));
		if (configuration.has("createMaps"))
			setCreateMaps(configuration.getBoolean("createMaps"));
		if (configuration.has("publishAt5PM"))
			setPublishAt5PM(configuration.getBoolean("publishAt5PM"));
		if (configuration.has("publishAt8AM"))
			setPublishAt8AM(configuration.getBoolean("publishAt8AM"));
	}
}
