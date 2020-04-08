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

import java.util.ArrayList;
import java.util.List;

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

	// REGION
	private static boolean publishBlogsTyrol = false;
	private static boolean publishBlogsSouthTyrol = false;
	private static boolean publishBlogsTrentino = false;

	// TODO: find better solution how to get the URL for simple html and images
	public static int directoryOffset = 5;
	public static String avalancheReportBaseUrlEn = "https://avalanche.report/";
	public static String avalancheReportBaseUrlDe = "https://lawinen.report/";
	public static String avalancheReportBaseUrlIt = "https://valanghe.report/";
	public static String avalancheReportBlogUrl = "blog/";
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

	// LANG
	public static String[] daysDe = { "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag" };
	public static String[] daysIt = { "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica" };
	public static String[] daysEn = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

	// LANG
	public static String incompleteTranslationTextDe = "Warte auf laufende Übersetzung ....";
	public static String incompleteTranslationTextIt = "Attendere traduzione in corso....";
	public static String incompleteTranslationTextEn = "Wait for translation in progress ....";

	public static DateTimeFormatter formatterDateTime = ISODateTimeFormat.dateTimeNoMillis();
	public static DateTimeFormatter formatterDate = ISODateTimeFormat.date();
	public static DateTimeFormatter parserDateTime = ISODateTimeFormat.dateTimeParser();
	public static DateTimeFormatter publicationTime = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

	// LANG
	public static DateTimeFormatter dateTimeEn = DateTimeFormat.forPattern(" dd MM yyyy");
	public static DateTimeFormatter dateTimeDe = DateTimeFormat.forPattern(" dd.MM.yyyy");
	public static DateTimeFormatter dateTimeIt = DateTimeFormat.forPattern(" dd.MM.yyyy");
	// LANG
	public static DateTimeFormatter publicationDateTimeEn = DateTimeFormat.forPattern("dd MM yyyy, HH:mm");
	public static DateTimeFormatter publicationDateTimeDe = DateTimeFormat.forPattern("dd.MM.yyyy 'um' HH:mm");
	public static DateTimeFormatter publicationDateTimeIt = DateTimeFormat.forPattern("dd.MM.yyyy 'alle ore' HH:mm");
	// LANG
	public static DateTimeFormatter tendencyDateTimeEn = DateTimeFormat.forPattern(" dd MM yyyy");
	public static DateTimeFormatter tendencyDateTimeDe = DateTimeFormat.forPattern(", 'den' dd.MM.yyyy");
	public static DateTimeFormatter tendencyDateTimeIt = DateTimeFormat.forPattern(" 'il' dd.MM.yyyy");

	// REGION
	public static String codeTrentino = "IT-32-TN";
	public static String codeSouthTyrol = "IT-32-BZ";
	public static String codeTyrol = "AT-07";

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
	public static String bulletinCaamlSchemaFileString = "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS/CAAMLv5_BulletinEAWS.xsd";

	private static String emailEncoding = "UTF-8";

	public static String notAvailableString = "N/A";

	// LANG
	public static String getAvalancheReportBaseUrl(LanguageCode lang) {
		switch (lang) {
		case en:
			return avalancheReportBaseUrlEn;
		case de:
			return avalancheReportBaseUrlDe;
		case it:
			return avalancheReportBaseUrlIt;
		default:
			return avalancheReportBaseUrlEn;
		}
	}

	public static String getAvalancheReportSimpleBaseUrl(LanguageCode lang) {
		return getAvalancheReportBaseUrl(lang) + "simple/";
	}

	public static String getAvalancheReportFullBlogUrl(LanguageCode lang) {
		return getAvalancheReportBaseUrl(lang) + avalancheReportBlogUrl;
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

	public static String getMapsUrl(LanguageCode lang) {
		return GlobalVariables.getAvalancheReportBaseUrl(lang)
				+ GlobalVariables.getMapsPath().substring(GlobalVariables.directoryOffset);
	}

	// LANG
	public static String getDayName(int day, LanguageCode lang) {
		if (day < 8) {
			switch (lang) {
			case de:
				return daysDe[day - 1];
			case it:
				return daysIt[day - 1];
			case en:
				return daysEn[day - 1];
			default:
				return daysEn[day - 1];
			}
		} else
			return "";
	}

	// LANG
	public static String getTreelineString(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Waldgrenze";
		case it:
			return "Linea del bosco";
		case en:
			return "Treeline";
		default:
			return "Treeline";
		}
	}

	// LANG
	public static String getTreelineStringLowercase(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Waldgrenze";
		case it:
			return "linea del bosco";
		case en:
			return "treeline";
		default:
			return "treeline";
		}
	}

	// LANG
	public static String getTreelinePreString(boolean above, LanguageCode lang) {
		if (above) {
			switch (lang) {
			case de:
				return " über der ";
			case it:
				return " sopra la ";
			case en:
				return " above the ";
			default:
				return " above the ";
			}
		} else {
			switch (lang) {
			case de:
				return " unter der ";
			case it:
				return " sotto la ";
			case en:
				return " below the ";
			default:
				return " below the ";
			}
		}
	}

	// LANG
	public static String getElevationPreString(boolean above, LanguageCode lang) {
		if (above) {
			switch (lang) {
			case de:
				return " über ";
			case it:
				return " sopra i ";
			case en:
				return " above ";
			default:
				return " above ";
			}
		} else {
			switch (lang) {
			case de:
				return " unter ";
			case it:
				return " sotto i ";
			case en:
				return " above ";
			default:
				return " below ";
			}
		}
	}

	// LANG
	public static String getPublishedText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Veröffentlicht am ";
		case it:
			return "Pubblicato il ";
		case en:
			return "Published ";
		default:
			return "Published ";
		}
	}

	// LANG
	public static String getTendencyHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Tendenz";
		case it:
			return "Tendenza";
		case en:
			return "Tendency";
		default:
			return "Tendency";
		}
	}

	// LANG
	public static String getRegionsHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Regionen";
		case it:
			return "Regioni";
		case en:
			return "Regions";
		default:
			return "Regions";
		}
	}

	// LANG
	public static String getAvalancheProblemsHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinenproblem";
		case it:
			return "Problema Valanghe";
		case en:
			return "Avalanche Problem";
		default:
			return "Avalanche Problem";
		}
	}

	// LANG
	public static String getTendencyText(Tendency tendency, LanguageCode lang) {
		if (tendency != null) {
			switch (tendency) {
			case increasing:
				switch (lang) {
				case de:
					return "Tendenz: Lawinengefahr steigt";
				case it:
					return "Tendenza: Pericolo valanghe in aumento";
				case en:
					return "Tendency: Increasing avalanche danger";
				default:
					return "Tendency: Increasing avalanche danger";
				}
			case steady:
				switch (lang) {
				case de:
					return "Tendenz: Lawinengefahr bleibt gleich";
				case it:
					return "Tendenza: Pericolo valanghe stabile";
				case en:
					return "Tendency: Constant avalanche danger";
				default:
					return "Tendency: Constant avalanche danger";
				}
			case decreasing:
				switch (lang) {
				case de:
					return "Tendenz: Lawinengefahr nimmt ab";
				case it:
					return "Tendenza: Pericolo valanghe in diminuzione";
				case en:
					return "Tendency: Decreasing avalanche danger";
				default:
					return "Tendency: Decreasing avalanche danger";
				}
			default:
				return "";
			}
		} else
			return "";
	}

	// LANG
	public static String getDangerPatternsHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Gefahrenmuster";
		case it:
			return "Situazione tipo";
		case en:
			return "Danger patterns";
		default:
			return "Danger patterns";
		}
	}

	// LANG
	public static String getSnowpackHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Schneedecke";
		case it:
			return "Manto nevoso";
		case en:
			return "Snowpack";
		default:
			return "Snowpack";
		}
	}

	// LANG
	public static String getTitle(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinen.report";
		case it:
			return "Valanghe.report";
		case en:
			return "Avalanche.report";
		default:
			return "Avalanche.report";
		}
	}

	// LANG
	public static String getDangerRatingHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Gefahrenstufe";
		case it:
			return "Grado";
		case en:
			return "Danger level";
		default:
			return "Danger level";
		}
	}

	// LANG
	public static String getCharacteristicsHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Merkmale";
		case it:
			return "Caratteristiche";
		case en:
			return "Characteristics";
		default:
			return "Characteristics";
		}
	}

	// LANG
	public static String getRecommendationsHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Empfehlungen für Personen außerhalb gesicherter Gebiete";
		case it:
			return "Raccomandazioni per le persone che praticano attività fuoripista";
		case en:
			return "Recommendations for backcountry recreationists";
		default:
			return "Recommendations for backcountry recreationists";
		}
	}

	// LANG
	public static String getDangerRatingVeryHighCharacteristicsTextBold(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Katastrophensituation";
		case it:
			return "Situazione catastrofica";
		case en:
			return "Disaster situation";
		default:
			return "Disaster situation";
		}
	}

	// LANG
	public static String getDangerRatingVeryHighCharacteristicsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Viele große und sehr große spontane Lawinen sind zu erwarten. Diese können Straßen und Siedlungen in Tallagen erreichen.";
		case it:
			return "Si prevedono numerose valanghe spontanee di dimensioni grandi e molto grandi che possono raggiungere le strade e i centri abitati situati a fondovalle.";
		case en:
			return "Numerous large and very large natural avalanches can be expected. These can reach roads and settlements in the valley.";
		default:
			return "Numerous large and very large natural avalanches can be expected. These can reach roads and settlements in the valley.";
		}
	}

	// LANG
	public static String getDangerRatingHighCharacteristicsTextBold(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Sehr kritische Lawinensituation";
		case it:
			return "Situazione valanghiva molto critica";
		case en:
			return "Very critical avalanche situation";
		default:
			return "Very critical avalanche situation";
		}
	}

	// LANG
	public static String getDangerRatingHighCharacteristicsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Spontane und oft auch grosse Lawinen sind wahrscheinlich. An vielen Steilhängen können Lawinen leicht ausgelöst werden. Fernauslösungen sind typisch. Wummgeräusche und Risse sind häufig.";
		case it:
			return "Probabili valanghe spontanee, spesso anche di grandi dimensioni. Su molti pendii ripidi è facile provocare il distacco di valanghe. I distacchi a distanza sono tipici di questo grado di pericolo. I rumori di “whum” e le fessure sono frequenti.";
		case en:
			return "Natural and often large avalanches are likely. Avalanches can easily be triggered on many steep slopes. Remote triggering is typical. Whumpf sounds and shooting cracks occur frequently.";
		default:
			return "Natural and often large avalanches are likely. Avalanches can easily be triggered on many steep slopes. Remote triggering is typical. Whumpf sounds and shooting cracks occur frequently.";
		}
	}

	// LANG
	public static String getDangerRatingConsiderableCharacteristicsTextBold(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Kritische Lawinensituation";
		case it:
			return "Situazione valanghiva critica";
		case en:
			return "Critical avalanche situation";
		default:
			return "Critical avalanche situation";
		}
	}

	// LANG
	public static String getDangerRatingConsiderableCharacteristicsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Wummgeräusche und Risse sind typisch. Lawinen können vor allem an Steilhängen der in der Lawinenvorhersage angegebenen Expositionen und Höhenlagen leicht ausgelöst werden. Spontane Lawinen und Fernauslösungen sind möglich.";
		case it:
			return "I rumori di “whum” e le fessure sono tipici. Le valanghe possono facilmente essere staccate, soprattutto sui pendii ripidi alle esposizioni e alle quote indicate nel bollettino delle valanghe. Possibili valanghe spontanee e distacchi a distanza.";
		case en:
			return "Whumpf sounds and shooting cracks are typical. Avalanches can easily be triggered, particularly on steep slopes with the aspect and elevation indicated in the avalanche bulletin. Natural avalanches and remote triggering can occur.";
		default:
			return "Whumpf sounds and shooting cracks are typical. Avalanches can easily be triggered, particularly on steep slopes with the aspect and elevation indicated in the avalanche bulletin. Natural avalanches and remote triggering can occur.";
		}
	}

	// LANG
	public static String getDangerRatingModerateCharacteristicsTextBold(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Mehrheitlich günstige Lawinensituation";
		case it:
			return "Situazione valanghiva per lo più favorevole";
		case en:
			return "Mostly favourable avalanche situation";
		default:
			return "Mostly favourable avalanche situation";
		}
	}

	// LANG
	public static String getDangerRatingModerateCharacteristicsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Alarmzeichen können vereinzelt auftreten. Lawinen können vor allem an sehr steilen Hängen der in der Lawinenvorhersage angegebenen Expositionen und Höhenlagen ausgelöst werden. Größere spontane Lawinen sind nicht zu erwarten.";
		case it:
			return "Possibile la presenza di singoli segnali di allarme. Le valanghe possono essere staccate specialmente sui pendii molto ripidi alle esposizioni e alle quote indicate nel bollettino delle valanghe. Non sono previste valanghe spontanee di grandi dimensioni.";
		case en:
			return "Warning signs can occur in isolated cases. Avalanches can be triggered in particular on very steep slopes with the aspect and elevation indicated in the avalanche bulletin. Large natural avalanches are unlikely.";
		default:
			return "Warning signs can occur in isolated cases. Avalanches can be triggered in particular on very steep slopes with the aspect and elevation indicated in the avalanche bulletin. Large natural avalanches are unlikely.";
		}
	}

	// LANG
	public static String getDangerRatingLowCharacteristicsTextBold(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Allgemein günstige Lawinensituation";
		case it:
			return "Situazione valanghiva generalmente favorevole";
		case en:
			return "Generally favourable avalanche situation";
		default:
			return "Generally favourable avalanche situation";
		}
	}

	// LANG
	public static String getDangerRatingLowCharacteristicsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Es sind keine Alarmzeichen feststellbar. Lawinen können nur vereinzelt, vor allem an extrem steilen Hängen ausgelöst werden.";
		case it:
			return "Non si manifestano segnali di allarme. Possibile solo il distacco di valanghe isolate, soprattutto sui pendii estremamente ripidi.";
		case en:
			return "No warning signs present. Avalanches can only be triggered in isolated cases, in particular on extremely steep slopes.";
		default:
			return "No warning signs present. Avalanches can only be triggered in isolated cases, in particular on extremely steep slopes.";
		}
	}

	// LANG
	public static String getSocialMediaText(LanguageCode lang, DateTime date, boolean update) {
		String dateString = GlobalVariables.getDayName(date.getDayOfWeek(), lang)
				+ date.toString(GlobalVariables.getShortDateTimeFormatter(lang));
		if (update) {
			switch (lang) {
			case de:
				return "UPDATE zum Lawinen.report für " + dateString + ": " + getBulletinUrl(lang, date);
			case it:
				return "AGGIORNAMENTO sulla Valanghe.report per " + dateString + ": " + getBulletinUrl(lang, date);
			case en:
				return "UDPATE on Avalanche.report for " + dateString + ": " + getBulletinUrl(lang, date);
			default:
				return "UPDATE on Avalanche.report for " + dateString + ": " + getBulletinUrl(lang, date);
			}
		} else {
			switch (lang) {
			case de:
				return "Lawinen.report für " + dateString + ": " + getBulletinUrl(lang, date);
			case it:
				return "Valanghe.report per " + dateString + ": " + getBulletinUrl(lang, date);
			case en:
				return "Avalanche.report for " + dateString + ": " + getBulletinUrl(lang, date);
			default:
				return "Avalanche.report for " + dateString + ": " + getBulletinUrl(lang, date);
			}
		}
	}

	// LANG
	public static String getBulletinUrl(LanguageCode lang, DateTime date) {
		return GlobalVariables.getAvalancheReportBaseUrl(lang) + "bulletin/"
				+ date.toString(DateTimeFormat.forPattern("yyyy-MM-dd"));
	}

	// LANG
	public static String getHeadline(LanguageCode lang, boolean update) {
		if (!update) {
			switch (lang) {
			case de:
				return "Lawinenvorhersage";
			case it:
				return "Previsione Valanghe";
			case en:
				return "Avalanche Forecast";
			default:
				return "Avalanche Forecast";
			}
		} else {
			switch (lang) {
			case de:
				return "UPDATE zur Lawinenvorhersage";
			case it:
				return "AGGIORNAMENTO sulla Previsione Valanghe";
			case en:
				return "UPDATE on Avalanche Forecast";
			default:
				return "UPDATE on Avalanche Forecast";
			}
		}
	}

	// LANG
	public static String getFollowUs(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Folge uns";
		case it:
			return "Seguici";
		case en:
			return "Follow us";
		default:
			return "Follow us";
		}
	}

	// LANG
	public static String getUnsubscribe(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Abmelden";
		case it:
			return "Annullare l'iscrizione";
		case en:
			return "Unsubscribe";
		default:
			return "Unsubscribe";
		}
	}

	// LANG
	public static String getLogoPath(LanguageCode lang, boolean grayscale) {
		if (grayscale) {
			switch (lang) {
			case de:
				return "logo/grey/lawinen_report.png";
			case it:
				return "logo/grey/valanghe_report.png";
			case en:
				return "logo/grey/avalanche_report.png";
			default:
				return "logo/grey/avalanche_report.png";
			}
		} else {
			switch (lang) {
			case de:
				return "logo/color/lawinen_report.png";
			case it:
				return "logo/color/valanghe_report.png";
			case en:
				return "logo/color/avalanche_report.png";
			default:
				return "logo/color/avalanche_report.png";
			}
		}
	}

	public static String getEuregioLogoPath(boolean grayscale) {
		if (grayscale)
			return "logo/grey/euregio.png";
		else
			return "logo/color/euregio.png";
	}

	// LANG
	public static String getCopyrightText(LanguageCode lang) {
		switch (lang) {
		case de:
			// return AlbinaUtil.getYear(bulletins, lang) + " Bla bla bla";
			return "";
		case it:
			// return AlbinaUtil.getYear(bulletins, lang) + " Bla bla bla";
			return "";
		case en:
			// return AlbinaUtil.getYear(bulletins, lang) + " Bla bla bla";
			return "";
		default:
			// return AlbinaUtil.getYear(bulletins, lang) + " Bla bla bla";
			return "";
		}
	}

	// LANG
	public static String getHeadlineText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinen.report";
		case it:
			return "Valanghe.report";
		case en:
			return "Avalanche.report";
		default:
			return "Avalanche.report";
		}
	}

	// LANG
	public static String getEmailSubject(LanguageCode lang, boolean update) {
		if (update) {
			switch (lang) {
			case de:
				return "UPDATE zum Lawinen.report, ";
			case it:
				return "AGGIORNAMENTO sulla Valanghe.report, ";
			case en:
				return "UPDATE on Avalanche.report, ";
			default:
				return "UPDATE on Avalanche.report, ";
			}
		} else {
			switch (lang) {
			case de:
				return "Lawinen.report, ";
			case it:
				return "Valanghe.report, ";
			case en:
				return "Avalanche.report, ";
			default:
				return "Avalanche.report, ";
			}
		}
	}

	// LANG
	public static String getSimpleHtmlTitle(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinen.report ";
		case it:
			return "Valanghe.report ";
		case en:
			return "Avalanche.report ";
		default:
			return "Avalanche.report ";
		}
	}

	// LANG
	public static String getStandardViewText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Standardansicht laden";
		case it:
			return "Vista standard del carico";
		case en:
			return "Load standard view";
		default:
			return "Load standard view";
		}
	}

	// LANG
	public static String getEmailFromPersonal(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinen.report";
		case it:
			return "Valanghe.report";
		case en:
			return "Avalanche.report";
		default:
			return "Avalanche.report";
		}
	}

	// LANG
	public static String getConfirmationEmailSubject(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Anmeldung Lawinen.report";
		case it:
			return "Registrazione Valanghe.report";
		case en:
			return "Subscription Avalanche.report";
		default:
			return "Subscription Avalanche.report";
		}
	}

	public static String getEmailEncoding() {
		return emailEncoding;
	}

	// LANG
	public static String getPdfFilename(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinenvorhersage";
		case it:
			return "Previsione Valanghe";
		case en:
			return "Avalanche Forecast";
		default:
			return "Avalanche Forecast";
		}
	}

	// LANG
	public static DateTimeFormatter getDateTimeFormatter(LanguageCode lang) {
		switch (lang) {
		case de:
			return dateTimeDe;
		case it:
			return dateTimeIt;
		case en:
			return dateTimeEn;
		default:
			return dateTimeEn;
		}
	}

	// LANG
	public static DateTimeFormatter getTendencyDateTimeFormatter(LanguageCode lang) {
		switch (lang) {
		case de:
			return tendencyDateTimeDe;
		case it:
			return tendencyDateTimeIt;
		case en:
			return tendencyDateTimeEn;
		default:
			return tendencyDateTimeEn;
		}
	}

	// LANG
	public static DateTimeFormatter getShortDateTimeFormatter(LanguageCode lang) {
		switch (lang) {
		case de:
			return dateTimeDe;
		case it:
			return dateTimeIt;
		case en:
			return dateTimeEn;
		default:
			return dateTimeEn;
		}
	}

	// LANG
	public static String getCapitalUrl(LanguageCode lang) {
		switch (lang) {
		case de:
			return "WWW.LAWINEN.REPORT";
		case it:
			return "WWW.VALANGHE.REPORT";
		case en:
			return "WWW.AVALANCHE.REPORT";
		default:
			return "WWW.AVALANCHE.REPORT";
		}
	}

	// LANG
	public static DateTimeFormatter getPublicationDateTimeFormatter(LanguageCode lang) {
		switch (lang) {
		case en:
			return publicationDateTimeEn;
		case de:
			return publicationDateTimeDe;
		case it:
			return publicationDateTimeIt;
		default:
			return publicationDateTimeEn;
		}
	}

	// LANG
	public static String getAMText(LanguageCode lang) {
		switch (lang) {
		case en:
			return "AM";
		case de:
			return "Vormittag";
		case it:
			return "Mattina";
		default:
			return "AM";
		}
	}

	// LANG
	public static String getPMText(LanguageCode lang) {
		switch (lang) {
		case en:
			return "PM";
		case de:
			return "Nachmittag";
		case it:
			return "Pomeriggio";
		default:
			return "PM";
		}
	}

	// LANG
	public static String getDangerRatingTextLong(DangerRating dangerRating, LanguageCode lang) {
		StringBuilder sb = new StringBuilder();
		switch (lang) {
		case de:
			sb.append("Gefahrenstufe ");
			break;
		case it:
			sb.append("Grado Pericolo ");
			break;
		case en:
			sb.append("Danger Level ");
			break;
		default:
			sb.append("Danger Level ");
			break;
		}
		sb.append(getDangerRatingTextMiddle(dangerRating, lang));
		return sb.toString();
	}

	// LANG
	public static String getDangerRatingTextMiddle(DangerRating dangerRating, LanguageCode lang) {
		switch (dangerRating) {
		case low:
			switch (lang) {
			case de:
				return "Gefahrenstufe 1 - Gering";
			case it:
				return "Grado Pericolo 1 - Debole";
			case en:
				return "Danger Level 1 - Low";
			default:
				return "Danger Level 1 - Low";
			}
		case moderate:
			switch (lang) {
			case de:
				return "Gefahrenstufe 2 - Mäßig";
			case it:
				return "Grado Pericolo 2 - Moderato";
			case en:
				return "Danger Level 2 - Moderate";
			default:
				return "Danger Level 2 - Moderate";
			}
		case considerable:
			switch (lang) {
			case de:
				return "Gefahrenstufe 3 - Erheblich";
			case it:
				return "Grado Pericolo 3 - Marcato";
			case en:
				return "Danger Level 3 - Considerable";
			default:
				return "Danger Level 3 - Considerable";
			}
		case high:
			switch (lang) {
			case de:
				return "Gefahrenstufe 4 - Groß";
			case it:
				return "Grado Pericolo 4 - Forte";
			case en:
				return "Danger Level 4 - High";
			default:
				return "Danger Level 4 - High";
			}
		case very_high:
			switch (lang) {
			case de:
				return "Gefahrenstufe 5 - Sehr Groß";
			case it:
				return "Grado Pericolo 5 - Molto Forte";
			case en:
				return "Danger Level 5 - Very High";
			default:
				return "Danger Level 5 - Very High";
			}
		case no_rating:
			switch (lang) {
			case de:
				return "Keine Beurteilung";
			case it:
				return "Senza Valutazione";
			case en:
				return "No Rating";
			default:
				return "No Rating";
			}
		default:
			switch (lang) {
			case de:
				return "Keine Beurteilung";
			case it:
				return "Senza Valutazione";
			case en:
				return "No Rating";
			default:
				return "No Rating";
			}
		}
	}

	// LANG
	public static String getDangerRatingTextShort(DangerRating dangerRating, LanguageCode lang) {
		switch (dangerRating) {
		case low:
			switch (lang) {
			case de:
				return "gering";
			case it:
				return "debole";
			case en:
				return "low";
			default:
				return "low";
			}
		case moderate:
			switch (lang) {
			case de:
				return "mäßig";
			case it:
				return "moderato";
			case en:
				return "moderate";
			default:
				return "moderate";
			}
		case considerable:
			switch (lang) {
			case de:
				return "erheblich";
			case it:
				return "marcato";
			case en:
				return "considerable";
			default:
				return "considerable";
			}
		case high:
			switch (lang) {
			case de:
				return "groß";
			case it:
				return "forte";
			case en:
				return "high";
			default:
				return "high";
			}
		case very_high:
			switch (lang) {
			case de:
				return "sehr groß";
			case it:
				return "molto forte";
			case en:
				return "very high";
			default:
				return "very high";
			}
		case missing:
			switch (lang) {
			case de:
				return "fehlt";
			case it:
				return "mancha";
			case en:
				return "missing";
			default:
				return "missing";
			}
		case no_rating:
			switch (lang) {
			case de:
				return "keine Beurteilung";
			case it:
				return "senza valutazione";
			case en:
				return "no rating";
			default:
				return "no rating";
			}
		default:
			switch (lang) {
			case de:
				return "fehlt";
			case it:
				return "mancha";
			case en:
				return "missing";
			default:
				return "missing";
			}
		}
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

	// LANG
	public static Object getTendencyBindingWord(LanguageCode lang) {
		switch (lang) {
		case en:
			return "on ";
		case de:
			return "am ";
		case it:
			return "per ";
		default:
			return "on ";
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

	// LANG
	public static String getFromEmail(LanguageCode lang) {
		switch (lang) {
		case de:
			return "info@lawinen.report";
		case en:
			return "info@avalanche.report";
		case it:
			return "info@valanghe.report";
		default:
			return "info@avalanche.report";
		}
	}

	// LANG
	public static String getFromName(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinen.report";
		case en:
			return "Avalanche.report";
		case it:
			return "Valanghe.report";
		default:
			return "Avalanche.report";
		}
	}

	public static String getUnsubscribeLink(LanguageCode lang, String region) {
		switch (lang) {
		case de:
			switch (region) {
			case "AT-07":
				return unsubscribeLinkTyrolDe;
			case "IT-32-BZ":
				return unsubscribeLinkSouthTyrolDe;
			case "IT-32-TN":
				return unsubscribeLinkTrentinoDe;
			default:
				return "";
			}
		case it:
			switch (region) {
			case "AT-07":
				return unsubscribeLinkTyrolIt;
			case "IT-32-BZ":
				return unsubscribeLinkSouthTyrolIt;
			case "IT-32-TN":
				return unsubscribeLinkTrentinoIt;
			default:
				return "";
			}
		case en:
			switch (region) {
			case "AT-07":
				return unsubscribeLinkTyrolEn;
			case "IT-32-BZ":
				return unsubscribeLinkSouthTyrolEn;
			case "IT-32-TN":
				return unsubscribeLinkTrentinoEn;
			default:
				return "";
			}
		default:
			switch (region) {
			case "AT-07":
				return unsubscribeLinkTyrolEn;
			case "IT-32-BZ":
				return unsubscribeLinkSouthTyrolEn;
			case "IT-32-TN":
				return unsubscribeLinkTrentinoEn;
			default:
				return "";
			}
		}
	}

	// LANG
	// REGION
	public static String getPdfLink(String date, LanguageCode lang, String region) {
		StringBuilder sb = new StringBuilder();
		sb.append("https://avalanche.report/albina_files/");
		sb.append(date);
		sb.append("/");
		sb.append(date);
		sb.append("_");
		switch (region) {
		case "AT-07":
			sb.append(GlobalVariables.codeTyrol);
			break;
		case "IT-32-BZ":
			sb.append(GlobalVariables.codeSouthTyrol);
			break;
		case "IT-32-TN":
			sb.append(GlobalVariables.codeTrentino);
			break;
		default:
			break;
		}
		sb.append("_");
		switch (lang) {
		case de:
			sb.append(LanguageCode.de);
			break;
		case it:
			sb.append(LanguageCode.it);
			break;
		case en:
			sb.append(LanguageCode.en);
			break;
		default:
			sb.append(LanguageCode.en);
			break;
		}
		sb.append(".pdf");
		return sb.toString();
	}

	public static String getImprintLink(LanguageCode lang) {
		StringBuilder sb = new StringBuilder();
		sb.append(GlobalVariables.getAvalancheReportBaseUrl(lang));
		sb.append("imprint");
		return sb.toString();
	}

	// LANG
	public static String getImprint(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Impressum";
		case it:
			return "Impressum";
		case en:
			return "Imprint";
		default:
			return "Imprint";
		}
	}

	// LANG
	public static String getPageNumberText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Seite %d";
		case it:
			return "Pagina %d";
		case en:
			return "Page %d";
		default:
			return "Page %d";
		}
	}
}
