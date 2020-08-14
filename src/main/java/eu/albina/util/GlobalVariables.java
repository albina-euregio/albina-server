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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONObject;

import eu.albina.caaml.CaamlVersion;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;

public class GlobalVariables {

	private static final Logger logger = LoggerFactory.getLogger(GlobalVariables.class);

	private static boolean createMaps = false;
	private static boolean createPdf = false;
	private static boolean createStaticWidget = false;
	private static boolean createSimpleHtml = false;
	private static boolean sendEmails = false;
	private static boolean publishToMessengerpeople = false;
	private static boolean publishToTelegramChannel = false;
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

	// TODO: find better solution how to get the URL for simple html and images
	public static int directoryOffset = 5;

	public static String avalancheReportBlogUrl = "/blog/";
	public static String avalancheReportFilesUrl = "/albina_files/";
	public static String avalancheReportBulletinUrl = "/bulletin/";
	public static String avalancheReportSimpleUrl = "/simple/";

	private static String serverImagesUrl = "https://admin.avalanche.report/images/";
	private static String serverImagesUrlLocalhost = "https://admin.avalanche.report/images/";
	private static String pdfDirectory = "/mnt/albina_files_local";
	private static String htmlDirectory = "/mnt/simple_local";
	private static String univieMapsPath = "";
	private static String mapsPath = "/mnt/albina_files_local";
	public static String mapProductionUrl = "";
	public static String scriptsPath = "/opt/local/";

	public static String blogApiUrl = "https://www.googleapis.com/blogger/v3/blogs/";
	public static String googleApiKey = "AIzaSyBN0GUcRlTfoVnarVFVpA1hUEnDcDjuXQY";
	public static String blogIdTyrolDe = "7062477220886068374";
	public static String blogIdTyrolIt = "6964779683196470218";
	public static String blogIdTyrolEn = "2617650030094227041";
	public static String blogIdSouthTyrolDe = "1263754381945501754";
	public static String blogIdSouthTyrolIt = "8922564068473612459";
	public static String blogIdTrentinoIt = "232334535081247038";
	public static String blogIdTest = "7136072879615998197";
	public static String blogUrlTyrolDe = "lawinenwarndienst.blogspot.com";
	public static String blogUrlTyrolIt = "servizio-valanghe-tirolo.blogspot.com";
	public static String blogUrlTyrolEn = "avalanche-warning-service-tirol.blogspot.com";
	public static String blogUrlSouthTyrolDe = "lawinensuedtirol.blogspot.com";
	public static String blogUrlSouthTyrolIt = "valanghealtoadige.blogspot.com";
	public static String blogUrlTrentinoIt = "trentinovalanghe.blogspot.com";

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

	// LANG
	// REGION
	private static String unsubscribeLinkTyrolDe = "https://t271d3041.emailsys1a.net/38/2221/61ad34fba0/unsubscribe/form.html";
	private static String unsubscribeLinkTyrolIt = "https://t271d3041.emailsys1a.net/38/2219/5b3f174f66/unsubscribe/form.html";
	private static String unsubscribeLinkTyrolEn = "https://t271d3041.emailsys1a.net/38/2223/bc01e461f6/unsubscribe/form.html";
	private static String unsubscribeLinkSouthTyrolDe = "https://t271d3041.emailsys1a.net/38/2215/da5e036304/unsubscribe/form.html";
	private static String unsubscribeLinkSouthTyrolIt = "https://t271d3041.emailsys1a.net/38/2213/e8b5e15ee4/unsubscribe/form.html";
	private static String unsubscribeLinkSouthTyrolEn = "https://t271d3041.emailsys1a.net/38/2217/2fce2c512f/unsubscribe/form.html";
	private static String unsubscribeLinkTrentinoDe = "https://t271d3041.emailsys1a.net/38/2209/3a7fe947b4/unsubscribe/form.html";
	private static String unsubscribeLinkTrentinoIt = "https://t271d3041.emailsys1a.net/38/2207/bc2b53964c/unsubscribe/form.html";
	private static String unsubscribeLinkTrentinoEn = "https://t271d3041.emailsys1a.net/38/2211/361ad0a282/unsubscribe/form.html";

	public static DateTimeFormatter formatterDateTime = ISODateTimeFormat.dateTimeNoMillis();
	public static DateTimeFormatter formatterDate = ISODateTimeFormat.date();
	public static DateTimeFormatter parserDateTime = ISODateTimeFormat.dateTimeParser();
	public static DateTimeFormatter publicationTime = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

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
			add(LanguageCode.es);
			add(LanguageCode.ca);
			add(LanguageCode.oc);
		}
	};

	public static String avalancheReportUsername = "info@avalanche.report";

	// TODO create secret
	public static String tokenEncodingSecret = "secret";
	public static String tokenEncodingIssuer = "albina";
	public static long accessTokenExpirationDuration = 1000 * 60 * 60 * 24;
	public static long refreshTokenExpirationDuration = 1000 * 60 * 60 * 24 * 7;
	public static long confirmationTokenExpirationDuration = 1000 * 60 * 60 * 24 * 3;

	public static String referenceSystemUrn = "urn:ogc:def:crs:OGC:1.3:CRS84";
	// public static String referenceSystemUrn = "EPSG:32632";
	public static String bulletinCaamlSchemaFileString = CaamlVersion.V5.schemaLocation();

	private static String emailEncoding = "UTF-8";

	public static String notAvailableString = "N/A";

	public static String getAvalancheReportSimpleBaseUrl(ResourceBundle messages) {
		return messages.getString("avalanche-report.url") + avalancheReportSimpleUrl;
	}

	public static String getAvalancheReportFullBlogUrl(ResourceBundle messages) {
		return messages.getString("avalanche-report.url") + avalancheReportBlogUrl;
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

	public static boolean isPublishToMessengerpeople() {
		return publishToMessengerpeople;
	}

	public static void setPublishToMessengerpeople(boolean publishToMessengerpeople) throws ConfigurationException {
		GlobalVariables.publishToMessengerpeople = publishToMessengerpeople;
		setConfigProperty("publishToMessengerpeople", publishToMessengerpeople);
	}

	public static boolean isPublishToTelegramChannel() {
		return publishToTelegramChannel;
	}

	public static void setPublishToTelegramChannel(boolean publishToTelegramChannel) throws ConfigurationException {
		GlobalVariables.publishToTelegramChannel = publishToTelegramChannel;
		setConfigProperty("publishToTelegramChannel", publishToTelegramChannel);
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

	public static String getUnivieMapsPath() {
		return univieMapsPath;
	}

	public static void setUnivieMapsPath(String univieMapsPath) throws ConfigurationException {
		GlobalVariables.univieMapsPath = univieMapsPath;
		setConfigProperty("univieMapsPath", univieMapsPath);
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

	public static boolean isMapProductionUrlUnivie() {
		return getMapProductionUrl().startsWith("http://data1.geo.univie.ac.at/");
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

	public static String getMapsUrl(ResourceBundle messages) {
		return messages.getString("avalanche-report.url") + "/" + getMapsPath().substring(directoryOffset);
	}

	public static String getSocialMediaText(DateTime date, boolean update, ResourceBundle messages) {
		String dateString = messages.getString("day." + date.getDayOfWeek())
				+ date.toString(messages.getString("date-time-format"));
		if (update) {
			return MessageFormat.format(messages.getString("social-media.message.update"),
					messages.getString("avalanche-report.name"), dateString, getBulletinUrl(messages, date));
		} else {
			return MessageFormat.format(messages.getString("social-media.message"),
					messages.getString("avalanche-report.name"), dateString, getBulletinUrl(messages, date));
		}
	}

	public static String getBulletinUrl(ResourceBundle messages, DateTime date) {
		return messages.getString("avalanche-report.url") + avalancheReportBulletinUrl
				+ date.toString(DateTimeFormat.forPattern("yyyy-MM-dd"));
	}

	public static String getEuregioLogoPath(boolean grayscale) {
		if (grayscale)
			return "logo/grey/euregio.png";
		else
			return "logo/color/euregio.png";
	}

	public static String getCopyrightText(ResourceBundle messages) {
		// return AlbinaUtil.getYear(bulletins) + messages.getString("copyright");
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
			if (config.containsKey("univieMapsPath"))
				univieMapsPath = config.getString("univieMapsPath");
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
			if (config.containsKey("publishToMessengerpeople"))
				publishToMessengerpeople = config.getBoolean("publishToMessengerpeople");
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
		if (univieMapsPath != null)
			json.put("univieMapsPath", univieMapsPath);
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
		json.put("publishToMessengerpeople", publishToMessengerpeople);
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
		if (configuration.has("publishToMessengerpeople"))
			setPublishToMessengerpeople(configuration.getBoolean("publishToMessengerpeople"));
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
				return "aspects/grey/" + new Integer(result).toString() + ".png";
			else
				return "aspects/color/" + new Integer(result).toString() + ".png";
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

	// LANG: rapid mail support
	// REGION: rapid mail support
	public static String getUnsubscribeLink(LanguageCode lang, String region) {
		switch (lang) {
		case de:
			switch (region) {
			case GlobalVariables.codeTyrol:
				return unsubscribeLinkTyrolDe;
			case GlobalVariables.codeSouthTyrol:
				return unsubscribeLinkSouthTyrolDe;
			case GlobalVariables.codeTrentino:
				return unsubscribeLinkTrentinoDe;
			default:
				return "";
			}
		case it:
			switch (region) {
			case GlobalVariables.codeTyrol:
				return unsubscribeLinkTyrolIt;
			case GlobalVariables.codeSouthTyrol:
				return unsubscribeLinkSouthTyrolIt;
			case GlobalVariables.codeTrentino:
				return unsubscribeLinkTrentinoIt;
			default:
				return "";
			}
		case en:
			switch (region) {
			case GlobalVariables.codeTyrol:
				return unsubscribeLinkTyrolEn;
			case GlobalVariables.codeSouthTyrol:
				return unsubscribeLinkSouthTyrolEn;
			case GlobalVariables.codeTrentino:
				return unsubscribeLinkTrentinoEn;
			default:
				return "";
			}
		default:
			switch (region) {
			case GlobalVariables.codeTyrol:
				return unsubscribeLinkTyrolEn;
			case GlobalVariables.codeSouthTyrol:
				return unsubscribeLinkSouthTyrolEn;
			case GlobalVariables.codeTrentino:
				return unsubscribeLinkTrentinoEn;
			default:
				return "";
			}
		}
	}

	// REGION
	public static String getPdfLink(String date, LanguageCode lang, String region, ResourceBundle messages) {
		StringBuilder sb = new StringBuilder();
		sb.append(messages.getString("avalanche-report.url"));
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

	public static String getImprintLink(ResourceBundle messages) {
		StringBuilder sb = new StringBuilder();
		sb.append(messages.getString("avalanche-report.url"));
		sb.append("/");
		sb.append("imprint");
		return sb.toString();
	}

	public static String getExtFileMapDescription(LanguageCode lang, String type, String region,
			ResourceBundle messages) {
		String regionName = AlbinaUtil.getRegionName(lang, region);
		String timeString = AlbinaUtil.getDaytimeString(messages, type);
		return MessageFormat.format(messages.getString("ext-file.map.description"), regionName, timeString);
	}

	public static String getExtFileOverlayDescription(String type, ResourceBundle messages) {
		String timeString = AlbinaUtil.getDaytimeString(messages, type);
		return MessageFormat.format(messages.getString("ext-file.overlay.description"), timeString);
	}

	public static String getExtFileRegionsDescription(ResourceBundle messages, String type) {
		String timeString = AlbinaUtil.getDaytimeString(messages, type);
		return MessageFormat.format(messages.getString("ext-file.regions.description"), timeString);
	}

	public static String getExtFilePdfDescription(LanguageCode lang, String region) {
		String regionName = AlbinaUtil.getRegionName(lang, region);
		return "PDF " + regionName;
	}
}
