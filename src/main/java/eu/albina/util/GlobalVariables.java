package eu.albina.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;

public class GlobalVariables {

	private static final Logger logger = LoggerFactory.getLogger(GlobalVariables.class);

	private static boolean createCaaml = false;
	private static boolean createMaps = false;
	private static boolean createPdf = false;
	private static boolean createStaticWidget = false;
	private static boolean createSimpleHtml = false;
	private static boolean sendEmails = false;
	private static boolean publishToSocialMedia = false;
	private static boolean publishAt5PM = false;
	private static boolean publishAt8AM = false;

	private static String localImagesPath = "images/";
	private static String localFontsPath = "fonts/open-sans";

	// REGION
	private static boolean publishBulletinsTyrol = true;
	private static boolean publishBulletinsSouthTyrol = true;
	private static boolean publishBulletinsTrentino = true;
	private static boolean publishBulletinsStyria = false;

	public static String simpleBulletinBaseUrl = "https://avalanche.report/simple/";
	public static String avalancheReportBaseUrl = "https://avalanche.report/albina-web/";
	private static String serverImagesUrl = "https://admin.avalanche.report/images/";
	private static String serverImagesUrlLocalhost = "https://admin.avalanche.report/images/";
	private static String pdfDirectory = "/mnt/albina_files_local/";
	private static String htmlDirectory = "/mnt/simple_local/";
	private static String mapsPath = "http://data1.geo.univie.ac.at/exchange/albina2/awm_dev/";
	public static String univieMapProductionUrl = "http://data1.geo.univie.ac.at/projects/albina2/tools/awm/create_albina_maps/create_albina_maps2_dev.php";
	public static String scriptsPath = "/opt/local/";

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
	public static String codeStyria = "AT-06";

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
			add(codeStyria);
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

	public static boolean isCreateCaaml() {
		return createCaaml;
	}

	public static void setCreateCaaml(boolean createCaaml) throws ConfigurationException {
		GlobalVariables.createCaaml = createCaaml;
		setConfigProperty("createCaaml", createCaaml);
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

	public static boolean isPublishToSocialMedia() {
		return publishToSocialMedia;
	}

	public static void setPublishToSocialMedia(boolean publishToSocialMedia) throws ConfigurationException {
		GlobalVariables.publishToSocialMedia = publishToSocialMedia;
		setConfigProperty("publishToSocialMedia", publishToSocialMedia);
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

	public static boolean isPublishBulletinsStyria() {
		return publishBulletinsStyria;
	}

	public static void setPublishBulletinsStyria(boolean publishBulletinsStyria) throws ConfigurationException {
		GlobalVariables.publishBulletinsStyria = publishBulletinsStyria;
		setConfigProperty("publishBulletinsStyria", publishBulletinsStyria);
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

	public static String getUnivieMapProductionUrl() {
		return univieMapProductionUrl;
	}

	public static void setUnivieMapProductionUrl(String univieMapProductionUrl) throws ConfigurationException {
		GlobalVariables.univieMapProductionUrl = univieMapProductionUrl;
		setConfigProperty("univieMapProductionUrl", univieMapProductionUrl);
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

	public static String getJsonSchemaFileString(String fileName) throws FileNotFoundException {
		StringBuilder result = new StringBuilder("");

		URL resource = GlobalVariables.class.getResource("/" + fileName + ".json");
		File file = new File(resource.getFile());

		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			result.append(line).append("\n");
		}
		scanner.close();

		return result.toString();
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
				return " above the ";
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
	public static String getMessengerPeopleText(LanguageCode lang, DateTime date, boolean update)
			throws UnsupportedEncodingException {
		String dateString = GlobalVariables.getDayName(date.getDayOfWeek(), lang)
				+ date.toString(GlobalVariables.getShortDateTimeFormatter(lang));
		if (update) {
			switch (lang) {
			case de:
				return URLEncoder.encode(
						"UPDATE zum Lawinen.report für " + dateString + ": " + getBulletinUrl(lang, date), "UTF-8");
			case it:
				return URLEncoder.encode(
						"AGGIORNAMENTO sulla Valanghe.report per " + dateString + ": " + getBulletinUrl(lang, date),
						"UTF-8");
			case en:
				return URLEncoder.encode(
						"UDPATE on Avalanche.report for " + dateString + ": " + getBulletinUrl(lang, date), "UTF-8");
			default:
				return URLEncoder.encode(
						"UPDATE on Avalanche.report for " + dateString + ": " + getBulletinUrl(lang, date), "UTF-8");
			}
		} else {
			switch (lang) {
			case de:
				return URLEncoder.encode("Lawinen.report für " + dateString + ": " + getBulletinUrl(lang, date),
						"UTF-8");
			case it:
				return URLEncoder.encode("Valanghe.report per " + dateString + ": " + getBulletinUrl(lang, date),
						"UTF-8");
			case en:
				return URLEncoder.encode("Avalanche.report for " + dateString + ": " + getBulletinUrl(lang, date),
						"UTF-8");
			default:
				return URLEncoder.encode("Avalanche.report for " + dateString + ": " + getBulletinUrl(lang, date),
						"UTF-8");
			}
		}
	}

	// LANG
	public static String getBulletinUrl(LanguageCode lang, DateTime date) {
		return "https://avalanche.report/albina-web/bulletin/" + date.toString(DateTimeFormat.forPattern("yyyy-MM-dd"))
				+ "?lang=" + lang.toString();
	}

	// LANG
	public static String getDangerRatingVeryHighRecommendationsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Verzicht auf Schneesport abseits geöffneter Abfahrten und Routen empfohlen.";
		case it:
			return "Si consiglia di rinunciare alle attività sportive al di fuori delle discese e degli itinerari aperti.";
		case en:
			return "You are advised not to engage in winter sports beyond open ski runs and trails.";
		default:
			return "You are advised not to engage in winter sports beyond open ski runs and trails.";
		}
	}

	// LANG
	public static String getDangerRatingHighRecommendationsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Sich auf mässig steiles Gelände beschränken. Auslaufbereiche grosser Lawinen beachten. Unerfahrene bleiben auf den geöffneten Abfahrten und Routen.\r\n"
					+ "Rund 10 % aller Todesopfer.";
		case it:
			return "Limitarsi ai pendii poco ripidi. Attenzione alla zona di deposito di valanghe di grandi dimensioni. Le persone inesperte rimangono sulle discese e sugli itinerari aperti.\r\n"
					+ "Circa il 10 % delle vittime.";
		case en:
			return "Stay on moderately steep terrain. Heed runout zones of large avalanches. Unexperienced persons should remain on open ski runs and trails.\r\n"
					+ "Around 10 % of avalanche fatalities.";
		default:
			return "Stay on moderately steep terrain. Heed runout zones of large avalanches. Unexperienced persons should remain on open ski runs and trails.\r\n"
					+ "Around 10 % of avalanche fatalities.";
		}
	}

	// LANG
	public static String getDangerRatingConsiderableRecommendationsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Für Wintersportler kritischste Situation! Optimale Routenwahl und Anwendung von risikomindernden Massnahmen sind nötig. Sehr steile Hänge der im Lawinenbulletin angegebenen Expositionen und Höhenlagen meiden. Unerfahrene bleiben besser auf den geöffneten Abfahrten und Routen.\r\n"
					+ "Rund die Hälfte aller Todesopfer.";
		case it:
			return "Questa è la situazione più critica per gli appassionati di sport invernali! Sono necessarie una scelta ottimale dell’itinerario e l’adozione di misure atte a ridurre il rischio. Evitare i pendii molto ripidi alle esposizioni e alle quote indicate nel bollettino delle valanghe. È meglio che le persone inesperte rimangano sulle discese e sugli itinerari aperti.\r\n"
					+ "Circa il 50 % delle vittime.";
		case en:
			return "The most critical situation for backcountry recreationists. Select best possible route and take action to reduce risks. Avoid very steep slopes with the aspect and elevation indicated in the avalanche bulletin. Unexperienced persons are advised to remain on open ski runs and trails.\r\n"
					+ "Around 50 % of avalanche fatalities.";
		default:
			return "The most critical situation for backcountry recreationists. Select best possible route and take action to reduce risks. Avoid very steep slopes with the aspect and elevation indicated in the avalanche bulletin. Unexperienced persons are advised to remain on open ski runs and trails.\r\n"
					+ "Around 50 % of avalanche fatalities.";
		}
	}

	// LANG
	public static String getDangerRatingModerateRecommendationsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Vorsichtige Routenwahl, vor allem an Hängen der im Lawinenbulletin angegebenen Expositionen und Höhenlagen. Sehr steile Hänge einzeln befahren. Besondere Vorsicht bei ungünstigem Schneedeckenaufbau (Altschneeproblem).\r\n"
					+ "Rund 30 % aller Todesopfer.";
		case it:
			return "Prudente scelta dell’itinerario, soprattutto sui pendii alle esposizioni e alle quote indicate nel bollettino delle valanghe. Percorrere i pendii molto ripidi una persona alla volta. Un’attenzione particolare è richiesta quando la struttura del manto nevoso è sfavorevole (situazione tipo neve vecchia).\r\n"
					+ "Circa il 30 % delle vittime.";
		case en:
			return "Routes should be selected carefully, especially on slopes with the aspect and elevation indicated in the avalanche bulletin. Travel very steep slopes one person at a time. Pay attention to unfavourable snowpack structure (persistent weak layers, old snow problem).\r\n"
					+ "Around 30 % of avalanche fatalities.";
		default:
			return "Routes should be selected carefully, especially on slopes with the aspect and elevation indicated in the avalanche bulletin. Travel very steep slopes one person at a time. Pay attention to unfavourable snowpack structure (persistent weak layers, old snow problem).\r\n"
					+ "Around 30 % of avalanche fatalities.";
		}
	}

	// LANG
	public static String getDangerRatingLowRecommendationsText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Extrem steile Hänge einzeln befahren und Absturzgefahr beachten.\r\n"
					+ "Rund 5 % aller Todesopfer.";
		case it:
			return "Percorrere i pendii estremamente ripidi una persona alla volta, prestando attenzione al pericolo di caduta.\r\n"
					+ "Circa il 5 % delle vittime.";
		case en:
			return "Travel extremely steep slopes one person at a time and be alert to the danger of falling.\r\n"
					+ "Around 5 % of avalanche fatalities.";
		default:
			return "Travel extremely steep slopes one person at a time and be alert to the danger of falling.\r\n"
					+ "Around 5 % of avalanche fatalities.";
		}
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

	public static String getInterregLogoPath(boolean grayscale) {
		if (grayscale)
			return "logo/grey/interreg.png";
		else
			return "logo/grey/interreg.png";
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
	public static String getEmailSubject(LanguageCode lang, boolean update) {
		if (update) {
			switch (lang) {
			case de:
				return "UPDATE zur Lawinenvorhersage, ";
			case it:
				return "AGGIORNAMENTO sulla Previsione Valanghe, ";
			case en:
				return "UPDATE on Avalanche Forecast, ";
			default:
				return "UPDATE on Avalanche Forecast, ";
			}
		} else {
			switch (lang) {
			case de:
				return "Lawinenvorhersage, ";
			case it:
				return "Previsione Valanghe, ";
			case en:
				return "Avalanche Forecast, ";
			default:
				return "Avalanche Forecast, ";
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
			if (config.containsKey("mapsPath"))
				mapsPath = config.getString("mapsPath");
			if (config.containsKey("univieMapProductionUrl"))
				univieMapProductionUrl = config.getString("univieMapProductionUrl");
			if (config.containsKey("scriptsPath"))
				scriptsPath = config.getString("scriptsPath");
			if (config.containsKey("createCaaml"))
				createCaaml = config.getBoolean("createCaaml");
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
			if (config.containsKey("publishToSocialMedia"))
				publishToSocialMedia = config.getBoolean("publishToSocialMedia");
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
			if (config.containsKey("publishBulletinsStyria"))
				publishBulletinsStyria = config.getBoolean("publishBulletinsStyria");
			logger.info("Configuration file loaded!");
		} catch (ConfigurationException e) {
			logger.error("Configuration file could not be loaded!");
			e.printStackTrace();
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
		if (univieMapProductionUrl != null)
			json.put("univieMapProductionUrl", univieMapProductionUrl);
		if (scriptsPath != null)
			json.put("scriptsPath", scriptsPath);
		json.put("createCaaml", createCaaml);
		json.put("createMaps", createMaps);
		json.put("createPdf", createPdf);
		json.put("createSimpleHtml", createSimpleHtml);
		json.put("createStaticWidget", createStaticWidget);
		json.put("sendEmails", sendEmails);
		json.put("publishToSocialMedia", publishToSocialMedia);
		json.put("publishAt5PM", publishAt5PM);
		json.put("publishAt8AM", publishAt8AM);
		json.put("publishBulletinsTyrol", publishBulletinsTyrol);
		json.put("publishBulletinsSouthTyrol", publishBulletinsSouthTyrol);
		json.put("publishBulletinsTrentino", publishBulletinsTrentino);
		json.put("publishBulletinsStyria", publishBulletinsStyria);

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
			logger.error("Configuration could not be saved!");
			e.printStackTrace();
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
		if (configuration.has("univieMapProductionUrl"))
			setUnivieMapProductionUrl(configuration.getString("univieMapProductionUrl"));
		if (configuration.has("createCaaml"))
			setCreateCaaml(configuration.getBoolean("createCaaml"));
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
		if (configuration.has("publishToSocialMedia"))
			setPublishToSocialMedia(configuration.getBoolean("publishToSocialMedia"));
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
		if (configuration.has("publishBulletinsStyria"))
			setPublishBulletinsStyria(configuration.getBoolean("publishBulletinsStyria"));
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
		case "AT-06":
			sb.append(GlobalVariables.codeStyria);
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
		sb.append(GlobalVariables.avalancheReportBaseUrl);
		sb.append("imprint?lang=");
		if (lang != null)
			sb.append(lang.toString());
		else
			sb.append("en");
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
