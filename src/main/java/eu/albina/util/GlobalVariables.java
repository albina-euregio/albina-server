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

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONObject;

import eu.albina.caaml.CaamlVersion;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;

public class GlobalVariables {

	private static final Logger logger = LoggerFactory.getLogger(GlobalVariables.class);

	public static String version;

	/*---- Defined in configuration file -----*/
	private static boolean createMaps = false;
	private static boolean createPdf = false;
	private static boolean createStaticWidget = false;
	private static boolean createSimpleHtml = false;
	private static boolean sendEmails = false;
	private static boolean publishToTelegramChannel = false;
	private static String vapidPublicKey;
	private static String vapidPrivateKey;
	private static boolean publishAt5PM = false;
	private static boolean publishAt8AM = false;
	private static String localImagesPath = "images/";
	private static String localFontsPath = "./src/main/resources/fonts/";
	// REGION
	private static boolean publishBulletinsTyrol = true;
	private static boolean publishBulletinsSouthTyrol = true;
	private static boolean publishBulletinsTrentino = true;
	private static boolean publishBulletinsAran = true;
	// REGION
	private static boolean publishBlogsTyrol = false;
	private static boolean publishBlogsSouthTyrol = false;
	private static boolean publishBlogsTrentino = false;
	/*---- Defined in configuration file -----*/

	// TODO: find better solution how to get the URL for simple html and images
	public static int directoryOffset = 5;

	public static String avalancheReportBlogUrl = "/blog/";
	public static String avalancheReportFilesUrl = "/albina_files/";
	public static String avalancheReportBulletinUrl = "/bulletin/";
	public static String avalancheReportSimpleUrl = "/simple/";

	private static final String serverMainUrl = "https://avalanche.report";
	private static String serverImagesUrl = "https://admin.avalanche.report/images/";
	private static String serverImagesUrlLocalhost = "https://admin.avalanche.report/images/";
	private static String pdfDirectory = "/mnt/albina_files_local";
	private static String htmlDirectory = "/mnt/simple_local";
	public static String mapsPath = "/mnt/albina_files_local";
	public static String mapProductionUrl = "";
	public static String scriptsPath = "/opt/local/";

	public static String blogApiUrl = "https://www.googleapis.com/blogger/v3/blogs/";
	public static String googleApiKey = "AIzaSyBN0GUcRlTfoVnarVFVpA1hUEnDcDjuXQY";
	// LANG: only languages for which a blog exists
	// REGION: only regions that have a blog
	public static final Table<String, LanguageCode, String> blogIds = ImmutableTable.<String, LanguageCode, String>builder()
		.put(GlobalVariables.codeTyrol, LanguageCode.de, "7062477220886068374")
		.put(GlobalVariables.codeTyrol, LanguageCode.it, "6964779683196470218")
		.put(GlobalVariables.codeTyrol, LanguageCode.en, "2617650030094227041")
		.put(GlobalVariables.codeSouthTyrol, LanguageCode.de, "1263754381945501754")
		.put(GlobalVariables.codeSouthTyrol, LanguageCode.it, "8922564068473612459")
		.put(GlobalVariables.codeTrentino, LanguageCode.it, "232334535081247038")
		.put("Test", LanguageCode.en, "7136072879615998197")
		.build();
	// LANG: only languages for which a blog exists
	// REGION: only regions that have a blog
	public static final Table<String, LanguageCode, String> blogUrls = ImmutableTable.<String, LanguageCode, String>builder()
		.put(GlobalVariables.codeTyrol, LanguageCode.de, "lawinenwarndienst.blogspot.com")
		.put(GlobalVariables.codeTyrol, LanguageCode.it, "servizio-valanghe-tirolo.blogspot.com")
		.put(GlobalVariables.codeTyrol, LanguageCode.en, "avalanche-warning-service-tirol.blogspot.com")
		.put(GlobalVariables.codeSouthTyrol, LanguageCode.de, "lawinensuedtirol.blogspot.com")
		.put(GlobalVariables.codeSouthTyrol, LanguageCode.it, "valanghealtoadige.blogspot.com")
		.put(GlobalVariables.codeTrentino, LanguageCode.it, "trentinovalanghe.blogspot.com")
		.build();

	// TODO shift this to social media config
	public static int targetingTyrolDe = 17519;
	public static int targetingTyrolIt = 17524;
	public static int targetingTyrolEn = 17522;
	public static int targetingSouthTyrolDe = 17533;
	public static int targetingSouthTyrolIt = 17534;
	public static int targetingSouthTyrolEn = 17536;
	public static int targetingTrentinoDe = 17539;
	public static int targetingTrentinoIt = 17537;
	public static int targetingTrentinoEn = 17541;
	public static int targetingTest = 17274;

	public static DateTimeFormatter formatterDateTime = DateTimeFormatter.ISO_DATE_TIME;
	public static DateTimeFormatter formatterDate = DateTimeFormatter.ISO_DATE;
	public static DateTimeFormatter formatterPublicationTime = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

	// REGION
	public final static String codeTrentino = "IT-32-TN";
	public final static String codeSouthTyrol = "IT-32-BZ";
	public final static String codeTyrol = "AT-07";
	public final static String codeEuregio = "EUREGIO";
	public final static String codeAran = "ES-CT-L";

	public static String propertiesFilePath = "META-INF/config.properties";
	public static String albinaXmlSchemaUrl = "https://api.avalanche.report/caaml/albina.xsd";
	public static String csvDeliminator = ";";
	public static String csvLineBreak = "\n";

	// REGION
	public static List<String> regions = new ArrayList<String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		{
			add(codeTyrol);
			add(codeSouthTyrol);
			add(codeTrentino);
			add(codeEuregio);
			add(codeAran);
		}
	};

	// REGION
	public static List<String> awsRegions = new ArrayList<String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		{
			add(codeTyrol);
			add(codeSouthTyrol);
			add(codeTrentino);
			add(codeAran);
		}
	};

	// REGION
	public static List<String> regionsEuregio = new ArrayList<String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		{
			add(codeTyrol);
			add(codeSouthTyrol);
			add(codeTrentino);
		}
	};

	// LANG
	public static List<LanguageCode> languages = new ArrayList<LanguageCode>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		{
			add(LanguageCode.de);
			add(LanguageCode.it);
			add(LanguageCode.en);
			add(LanguageCode.fr);
			add(LanguageCode.es);
			add(LanguageCode.ca);
			add(LanguageCode.oc);
		}
	};

	// LANG
	public static List<LanguageCode> socialMediaLanguages = new ArrayList<LanguageCode>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		{
			add(LanguageCode.de);
			add(LanguageCode.it);
			add(LanguageCode.en);
		}
	};

	public static String avalancheReportUsername = "info@avalanche.report";

	public static final String tokenEncodingIssuer = "albina";

	public static long accessTokenExpirationDuration = 1000 * 60 * 60 * 24;
	public static long refreshTokenExpirationDuration = 1000 * 60 * 60 * 24 * 7;
	public static long confirmationTokenExpirationDuration = 1000 * 60 * 60 * 24 * 3;

	public static String referenceSystemUrn = "urn:ogc:def:crs:OGC:1.3:CRS84";
	// public static String referenceSystemUrn = "EPSG:32632";
	public static String bulletinCaamlSchemaFileString = CaamlVersion.V5.schemaLocation();

	private static final String emailEncoding = "UTF-8";

	public static String notAvailableString = "N/A";

	public static String getAvalancheReportSimpleBaseUrl(LanguageCode lang) {
		return lang.getBundleString("avalanche-report.url") + avalancheReportSimpleUrl;
	}

	public static String getAvalancheReportFullBlogUrl(LanguageCode lang) {
		return lang.getBundleString("avalanche-report.url") + avalancheReportBlogUrl;
	}

	public static boolean isCreateMaps() {
		return createMaps;
	}

	public static void setCreateMaps(boolean createMaps) throws ConfigurationException {
		GlobalVariables.createMaps = createMaps;
		setConfigProperty("createMaps", createMaps);
	}

	public static boolean isCreatePdf() {
		return createPdf;
	}

	public static void setCreatePdf(boolean createPdf) throws ConfigurationException {
		GlobalVariables.createPdf = createPdf;
		setConfigProperty("createPdf", createPdf);
	}

	public static boolean isCreateStaticWidget() {
		return createStaticWidget;
	}

	public static void setCreateStaticWidget(boolean createStaticWidget) throws ConfigurationException {
		GlobalVariables.createStaticWidget = createStaticWidget;
		setConfigProperty("createStaticWidget", createStaticWidget);
	}

	public static boolean isSendEmails() {
		return sendEmails;
	}

	public static void setSendEmails(boolean sendEmails) throws ConfigurationException {
		GlobalVariables.sendEmails = sendEmails;
		setConfigProperty("sendEmails", sendEmails);
	}

	public static boolean isPublishToTelegramChannel() {
		return publishToTelegramChannel;
	}

	public static void setPublishToTelegramChannel(boolean publishToTelegramChannel) throws ConfigurationException {
		GlobalVariables.publishToTelegramChannel = publishToTelegramChannel;
		setConfigProperty("publishToTelegramChannel", publishToTelegramChannel);
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

	public static boolean isCreateSimpleHtml() {
		return createSimpleHtml;
	}

	public static void setCreateSimpleHtml(boolean createSimpleHtml) throws ConfigurationException {
		GlobalVariables.createSimpleHtml = createSimpleHtml;
		setConfigProperty("createSimpleHtml", createSimpleHtml);
	}

	public static String getLocalImagesPath() {
		return localImagesPath;
	}

	public static void setLocalImagesPath(String localImagesPath) throws ConfigurationException {
		GlobalVariables.localImagesPath = localImagesPath;
		setConfigProperty("localImagesPath", localImagesPath);
	}

	public static String getLocalFontsPath() {
		return localFontsPath;
	}

	public static void setLocalFontsPath(String localFontsPath) throws ConfigurationException {
		GlobalVariables.localFontsPath = localFontsPath;
		setConfigProperty("localFontsPath", localFontsPath);
	}

	public static boolean isPublishBulletinsTyrol() {
		return publishBulletinsTyrol;
	}

	public static void setPublishBulletinsTyrol(boolean publishBulletinsTyrol) throws ConfigurationException {
		GlobalVariables.publishBulletinsTyrol = publishBulletinsTyrol;
		setConfigProperty("publishBulletinsTyrol", publishBulletinsTyrol);
	}

	public static boolean isPublishBulletinsSouthTyrol() {
		return publishBulletinsSouthTyrol;
	}

	public static void setPublishBulletinsSouthTyrol(boolean publishBulletinsSouthTyrol) throws ConfigurationException {
		GlobalVariables.publishBulletinsSouthTyrol = publishBulletinsSouthTyrol;
		setConfigProperty("publishBulletinsSouthTyrol", publishBulletinsSouthTyrol);
	}

	public static boolean isPublishBulletinsTrentino() {
		return publishBulletinsTrentino;
	}

	public static void setPublishBulletinsTrentino(boolean publishBulletinsTrentino) throws ConfigurationException {
		GlobalVariables.publishBulletinsTrentino = publishBulletinsTrentino;
		setConfigProperty("publishBulletinsTrentino", publishBulletinsTrentino);
	}

	public static boolean isPublishBulletinsAran() {
		return publishBulletinsAran;
	}

	public static void setPublishBulletinsAran(boolean publishBulletinsAran) throws ConfigurationException {
		GlobalVariables.publishBulletinsAran = publishBulletinsAran;
		setConfigProperty("publishBulletinsAran", publishBulletinsAran);
	}

	public static boolean isPublishBlogsTyrol() {
		return publishBlogsTyrol;
	}

	public static void setPublishBlogsTyrol(boolean publishBlogsTyrol) throws ConfigurationException {
		GlobalVariables.publishBlogsTyrol = publishBlogsTyrol;
		setConfigProperty("publishBlogsTyrol", publishBlogsTyrol);
	}

	public static boolean isPublishBlogsSouthTyrol() {
		return publishBlogsSouthTyrol;
	}

	public static void setPublishBlogsSouthTyrol(boolean publishBlogsSouthTyrol) throws ConfigurationException {
		GlobalVariables.publishBlogsSouthTyrol = publishBlogsSouthTyrol;
		setConfigProperty("publishBlogsSouthTyrol", publishBlogsSouthTyrol);
	}

	public static boolean isPublishBlogsTrentino() {
		return publishBlogsTrentino;
	}

	public static void setPublishBlogsTrentino(boolean publishBlogsTrentino) throws ConfigurationException {
		GlobalVariables.publishBlogsTrentino = publishBlogsTrentino;
		setConfigProperty("publishBlogsTrentino", publishBlogsTrentino);
	}

	// REGION
	public static boolean isPublishBlogs(String region) {
		switch (region) {
		case codeTyrol:
			return publishBlogsTyrol;
		case codeSouthTyrol:
			return publishBlogsSouthTyrol;
		case codeTrentino:
			return publishBlogsTrentino;
		case codeAran:
			return false;
		default:
			return false;
		}
	}

	public static String getServerMainUrl() {
		return serverMainUrl;
	}

	public static String getScriptsPath() {
		return scriptsPath;
	}

	public static void setScriptsPath(String scriptsPath) throws ConfigurationException {
		GlobalVariables.scriptsPath = scriptsPath;
		setConfigProperty("scriptsPath", scriptsPath);
	}

	public static String getPdfDirectory() {
		return pdfDirectory;
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

	public static String getServerImagesUrlLocalhost() {
		return serverImagesUrlLocalhost;
	}

	public static void setServerImagesUrlLocalhost(String serverImagesUrlLocalhost) throws ConfigurationException {
		GlobalVariables.serverImagesUrlLocalhost = serverImagesUrlLocalhost;
		setConfigProperty("serverImagesUrlLocalhost", serverImagesUrlLocalhost);
	}

	public static String getMapsPath() {
		return mapsPath;
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

	public static String getMapsUrl(LanguageCode lang) {
		return lang.getBundleString("avalanche-report.url") + "/" + getMapsPath().substring(directoryOffset);
	}

	public static String getBulletinUrl(LanguageCode lang, ZonedDateTime date) {
		return lang.getBundleString("avalanche-report.url") + avalancheReportBulletinUrl
				+ date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

	public static String getEuregioLogoPath(boolean grayscale) {
		if (grayscale)
			return "logo/grey/euregio.png";
		else
			return "logo/color/euregio.png";
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
			if (config.containsKey("localImagesPath"))
				localImagesPath = config.getString("localImagesPath");
			if (config.containsKey("localFontsPath"))
				localFontsPath = config.getString("localFontsPath");
			if (config.containsKey("pdfDirectory"))
				pdfDirectory = config.getString("pdfDirectory");
			if (config.containsKey("htmlDirectory"))
				htmlDirectory = config.getString("htmlDirectory");
			if (config.containsKey("serverImagesUrl"))
				serverImagesUrl = config.getString("serverImagesUrl");
			if (config.containsKey("serverImagesUrlLocalhost"))
				serverImagesUrlLocalhost = config.getString("serverImagesUrlLocalhost");
			if (config.containsKey("mapsPath"))
				mapsPath = config.getString("mapsPath");
			if (config.containsKey("mapProductionUrl"))
				mapProductionUrl = config.getString("mapProductionUrl");
			if (config.containsKey("scriptsPath"))
				scriptsPath = config.getString("scriptsPath");
			if (config.containsKey("createMaps"))
				createMaps = config.getBoolean("createMaps");
			if (config.containsKey("createPdf"))
				createPdf = config.getBoolean("createPdf");
			if (config.containsKey("createSimpleHtml"))
				createSimpleHtml = config.getBoolean("createSimpleHtml");
			if (config.containsKey("createStaticWidget"))
				createStaticWidget = config.getBoolean("createStaticWidget");
			if (config.containsKey("sendEmails"))
				sendEmails = config.getBoolean("sendEmails");
			if (config.containsKey("publishToTelegramChannel"))
				publishToTelegramChannel = config.getBoolean("publishToTelegramChannel");
			if (config.containsKey("publishAt5PM"))
				publishAt5PM = config.getBoolean("publishAt5PM");
			if (config.containsKey("publishAt8AM"))
				publishAt8AM = config.getBoolean("publishAt8AM");
			if (config.containsKey("publishBulletinsTyrol"))
				publishBulletinsTyrol = config.getBoolean("publishBulletinsTyrol");
			if (config.containsKey("publishBulletinsSouthTyrol"))
				publishBulletinsSouthTyrol = config.getBoolean("publishBulletinsSouthTyrol");
			if (config.containsKey("publishBulletinsTrentino"))
				publishBulletinsTrentino = config.getBoolean("publishBulletinsTrentino");
			if (config.containsKey("publishBulletinsAran"))
				publishBulletinsAran = config.getBoolean("publishBulletinsAran");
			if (config.containsKey("publishBlogsTyrol"))
				publishBlogsTyrol = config.getBoolean("publishBlogsTyrol");
			if (config.containsKey("publishBlogsSouthTyrol"))
				publishBlogsSouthTyrol = config.getBoolean("publishBlogsSouthTyrol");
			if (config.containsKey("publishBlogsTrentino"))
				publishBlogsTrentino = config.getBoolean("publishBlogsTrentino");
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

		if (localImagesPath != null)
			json.put("localImagesPath", localImagesPath);
		if (localFontsPath != null)
			json.put("localFontsPath", localFontsPath);
		if (pdfDirectory != null)
			json.put("pdfDirectory", pdfDirectory);
		if (htmlDirectory != null)
			json.put("htmlDirectory", htmlDirectory);
		if (serverImagesUrl != null)
			json.put("serverImagesUrl", serverImagesUrl);
		if (serverImagesUrlLocalhost != null)
			json.put("serverImagesUrlLocalhost", serverImagesUrlLocalhost);
		if (mapsPath != null)
			json.put("mapsPath", mapsPath);
		if (mapProductionUrl != null)
			json.put("mapProductionUrl", mapProductionUrl);
		if (scriptsPath != null)
			json.put("scriptsPath", scriptsPath);
		json.put("createMaps", createMaps);
		json.put("createPdf", createPdf);
		json.put("createSimpleHtml", createSimpleHtml);
		json.put("createStaticWidget", createStaticWidget);
		json.put("sendEmails", sendEmails);
		json.put("publishToTelegramChannel", publishToTelegramChannel);
		json.put("publishAt5PM", publishAt5PM);
		json.put("publishAt8AM", publishAt8AM);
		json.put("publishBulletinsTyrol", publishBulletinsTyrol);
		json.put("publishBulletinsSouthTyrol", publishBulletinsSouthTyrol);
		json.put("publishBulletinsTrentino", publishBulletinsTrentino);
		json.put("publishBulletinsAran", publishBulletinsAran);
		json.put("publishBlogsTyrol", publishBlogsTyrol);
		json.put("publishBlogsSouthTyrol", publishBlogsSouthTyrol);
		json.put("publishBlogsTrentino", publishBlogsTrentino);

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
			logger.info("[Configuration saved] " + key + ": " + value);
		} catch (ConfigurationException e) {
			logger.error("Configuration could not be saved!", e);
			throw e;
		}
	}

	public static void setConfigurationParameters(JSONObject configuration) throws ConfigurationException {
		if (configuration.has("localImagesPath"))
			setLocalImagesPath(configuration.getString("localImagesPath"));
		if (configuration.has("localFontsPath"))
			setLocalFontsPath(configuration.getString("localFontsPath"));
		if (configuration.has("pdfDirectory"))
			setPdfDirectory(configuration.getString("pdfDirectory"));
		if (configuration.has("htmlDirectory"))
			setHtmlDirectory(configuration.getString("htmlDirectory"));
		if (configuration.has("serverImagesUrl"))
			setServerImagesUrl(configuration.getString("serverImagesUrl"));
		if (configuration.has("serverImagesUrlLocalhost"))
			setServerImagesUrlLocalhost(configuration.getString("serverImagesUrlLocalhost"));
		if (configuration.has("mapsPath"))
			setMapsPath(configuration.getString("mapsPath"));
		if (configuration.has("mapProductionUrl"))
			setMapProductionUrl(configuration.getString("mapProductionUrl"));
		if (configuration.has("createMaps"))
			setCreateMaps(configuration.getBoolean("createMaps"));
		if (configuration.has("createPdf"))
			setCreatePdf(configuration.getBoolean("createPdf"));
		if (configuration.has("createSimpleHtml"))
			setCreateSimpleHtml(configuration.getBoolean("createSimpleHtml"));
		if (configuration.has("createStaticWidget"))
			setCreateStaticWidget(configuration.getBoolean("createStaticWidget"));
		if (configuration.has("sendEmails"))
			setSendEmails(configuration.getBoolean("sendEmails"));
		if (configuration.has("publishToTelegramChannel"))
			setPublishToTelegramChannel(configuration.getBoolean("publishToTelegramChannel"));
		if (configuration.has("publishAt5PM"))
			setPublishAt5PM(configuration.getBoolean("publishAt5PM"));
		if (configuration.has("publishAt8AM"))
			setPublishAt8AM(configuration.getBoolean("publishAt8AM"));
		if (configuration.has("publishBulletinsTyrol"))
			setPublishBulletinsTyrol(configuration.getBoolean("publishBulletinsTyrol"));
		if (configuration.has("publishBulletinsSouthTyrol"))
			setPublishBulletinsSouthTyrol(configuration.getBoolean("publishBulletinsSouthTyrol"));
		if (configuration.has("publishBulletinsTrentino"))
			setPublishBulletinsTrentino(configuration.getBoolean("publishBulletinsTrentino"));
		if (configuration.has("publishBulletinsAran"))
			setPublishBulletinsAran(configuration.getBoolean("publishBulletinsAran"));
		if (configuration.has("scriptsPath"))
			setScriptsPath(configuration.getString("scriptsPath"));
		if (configuration.has("publishBlogsTyrol"))
			setPublishBlogsTyrol(configuration.getBoolean("publishBlogsTyrol"));
		if (configuration.has("publishBlogsSouthTyrol"))
			setPublishBlogsSouthTyrol(configuration.getBoolean("publishBlogsSouthTyrol"));
		if (configuration.has("publishBlogsTrentino"))
			setPublishBlogsTrentino(configuration.getBoolean("publishBlogsTrentino"));
	}

	public static String getTendencySymbolPath(Tendency tendency, boolean grayscale) {
		if (grayscale) {
			switch (tendency) {
			case increasing:
				return "tendency/tendency_increasing_black.png";
			case steady:
				return "tendency/tendency_steady_black.png";
			case decreasing:
				return "tendency/tendency_decreasing_black.png";
			default:
				return null;
			}
		} else {
			switch (tendency) {
			case increasing:
				return "tendency/tendency_increasing_blue.png";
			case steady:
				return "tendency/tendency_steady_blue.png";
			case decreasing:
				return "tendency/tendency_decreasing_blue.png";
			default:
				return null;
			}
		}
	}

	public static String getAvalancheSituationSymbolPath(AvalancheSituation avalancheSituation, boolean grayscale) {
		if (grayscale)
			return "avalanche_situations/grey/" + avalancheSituation.getAvalancheSituation().toStringId() + ".png";
		else
			return "avalanche_situations/color/" + avalancheSituation.getAvalancheSituation().toStringId() + ".png";
	}

	public static String getAspectSymbolPath(int result, boolean grayscale) {
		if (result > -1) {
			if (grayscale)
				return "aspects/grey/" + Integer.valueOf(result).toString() + ".png";
			else
				return "aspects/color/" + Integer.valueOf(result).toString() + ".png";
		} else {
			if (grayscale)
				return "aspects/grey/empty.png";
			else
				return "aspects/color/empty.png";
		}
	}

	public static DangerRating getHighestDangerRating(List<AvalancheBulletin> bulletins) {
		DangerRating result = DangerRating.missing;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			DangerRating highestDangerRating = avalancheBulletin.getHighestDangerRating();
			if (highestDangerRating.compareTo(result) > 0)
				result = highestDangerRating;
		}
		return result;
	}

	// REGION
	public static String getPdfLink(String date, LanguageCode lang, String region) {
		StringBuilder sb = new StringBuilder();
		sb.append(lang.getBundleString("avalanche-report.url"));
		sb.append(avalancheReportFilesUrl);
		sb.append(date);
		sb.append("/");
		sb.append(date);
		sb.append("_");
		sb.append(region);
		sb.append("_");
		sb.append(lang.toString());
		sb.append(".pdf");
		return sb.toString();
	}

	public static String getDangerPatternLink(LanguageCode lang, DangerPattern dangerPattern) {
		StringBuilder sb = new StringBuilder();
		sb.append(lang.getBundleString("avalanche-report.url"));
		sb.append("/education/danger-patterns#");
		sb.append(DangerPattern.getCAAMLv6String(dangerPattern));
		return sb.toString();
	}

	public static String getAvalancheSituationLink(LanguageCode lang,
			eu.albina.model.enumerations.AvalancheSituation avalancheSituation) {
		StringBuilder sb = new StringBuilder();
		sb.append(lang.getBundleString("avalanche-report.url"));
		sb.append("/education/avalanche-problems#");
		sb.append(avalancheSituation.toCaamlv6String());
		return sb.toString();
	}

	public static String getImprintLink(LanguageCode lang) {
		return lang.getBundleString("avalanche-report.url") + "/imprint";
	}

	public static String getExtFileMapDescription(LanguageCode lang, String type, String region) {
		String regionName = AlbinaUtil.getRegionName(lang, region);
		String timeString = AlbinaUtil.getDaytimeString(lang, type);
		return MessageFormat.format(lang.getBundleString("ext-file.map.description"), regionName, timeString);
	}

	public static String getExtFileOverlayDescription(LanguageCode lang, String type) {
		String timeString = AlbinaUtil.getDaytimeString(lang, type);
		return MessageFormat.format(lang.getBundleString("ext-file.overlay.description"), timeString);
	}

	public static String getExtFileRegionsDescription(LanguageCode lang, String type) {
		String timeString = AlbinaUtil.getDaytimeString(lang, type);
		return MessageFormat.format(lang.getBundleString("ext-file.regions.description"), timeString);
	}

	public static String getExtFilePdfDescription(LanguageCode lang, String region) {
		String regionName = AlbinaUtil.getRegionName(lang, region);
		return "PDF " + regionName;
	}
}
