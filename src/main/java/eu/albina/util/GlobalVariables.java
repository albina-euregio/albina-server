package eu.albina.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;

public class GlobalVariables {

	private static final Logger logger = LoggerFactory.getLogger(GlobalVariables.class);

	private static boolean createMaps = false;
	private static boolean createPdf = true;
	private static boolean createStaticWidget = true;
	private static boolean sendEmails = true;
	private static boolean publishToSocialMedia = false;
	private static boolean publishAt5PM = true;
	private static boolean publishAt8AM = true;

	private static String localImagesPath = "images/";
	private static String localFontsPath = "fonts/open-sans";

	// REGION
	private static boolean publishBulletinsTyrol = true;
	private static boolean publishBulletinsSouthTyrol = true;
	private static boolean publishBulletinsTrentino = true;
	private static boolean publishBulletinsStyria = true;

	// TODO set correct directory for all files (path should include date,
	// suggestion from vienna)
	// private static String pdfDirectory = "D:\\";
	private static String pdfDirectory = "/mnt/daten1/pdfs/";
	// private static String mapsPath =
	// "D:\\norbert\\workspaces\\albina-euregio\\albina-server\\src\\main\\resources\\images\\";
	private static String mapsPath = "/mnt/daten1/images/";
	private static String serverImagesUrl = "https://admin.avalanche.report/images/";
	private static String serverImagesUrlLocalhost = "http://localhost:8080/images/";

	private static String socketIoOrigin = "https://admin.avalanche.report";
	private static int socketIoPort = 9092;

	private static boolean smtpAuth = true;
	private static boolean smtpTls = true;
	private static String smtpHost = "smtp.gmail.com";
	private static String smtpPort = "587";
	private static String emailUsername = "norbert.lanzanasto@gmail.com";
	private static String emailPassword = "Go6Zaithee";

	// LANG
	public static String[] daysDe = { "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag" };
	public static String[] daysIt = { "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica" };
	public static String[] daysEn = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

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

	// public static String univieMapProductionUrl =
	// "http://data1.geo.univie.ac.at/projects/albina/tools/create_trentino_maps/create_trentino_maps.php";
	public static String univieMapProductionUrl = "http://data1.geo.univie.ac.at/projects/albina2/tools/awm/create_albina_maps/create_albina_maps2.php";

	private static String emailEncoding = "UTF-8";

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

	public static String getSocketIoOrigin() {
		return socketIoOrigin;
	}

	public static void setSocketIoOrigin(String socketIoOrigin) throws ConfigurationException {
		GlobalVariables.socketIoOrigin = socketIoOrigin;
		setConfigProperty("socketIoOrigin", socketIoOrigin);
	}

	public static int getSocketIoPort() {
		return socketIoPort;
	}

	public static void setSocketIoPort(int socketIoPort) throws ConfigurationException {
		GlobalVariables.socketIoPort = socketIoPort;
		setConfigProperty("socketIoPort", socketIoPort);
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

	public static String getPdfDirectory() {
		return pdfDirectory;
	}

	public static void setPdfDirectory(String pdfDirectory) throws ConfigurationException {
		GlobalVariables.pdfDirectory = pdfDirectory;
		setConfigProperty("pdfDirectory", pdfDirectory);
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

	public static boolean getSmtpAuth() {
		return smtpAuth;
	}

	public static void setSmtpAuth(boolean smtpAuth) throws ConfigurationException {
		GlobalVariables.smtpAuth = smtpAuth;
		setConfigProperty("smtpAuth", smtpAuth);
	}

	public static boolean getSmtpTls() {
		return smtpTls;
	}

	public static void setSmtpTls(boolean smtpTls) throws ConfigurationException {
		GlobalVariables.smtpTls = smtpTls;
		setConfigProperty("smtpTls", smtpTls);
	}

	public static String getSmtpHost() {
		return smtpHost;
	}

	public static void setSmtpHost(String smtpHost) throws ConfigurationException {
		GlobalVariables.smtpHost = smtpHost;
		setConfigProperty("smtpHost", smtpHost);
	}

	public static String getSmtpPort() {
		return smtpPort;
	}

	public static void setSmtpPort(String smtpPort) throws ConfigurationException {
		GlobalVariables.smtpPort = smtpPort;
		setConfigProperty("smtpPort", smtpPort);
	}

	public static String getEmailUsername() {
		return emailUsername;
	}

	public static void setEmailUsername(String emailUsername) throws ConfigurationException {
		GlobalVariables.emailUsername = emailUsername;
		setConfigProperty("emailUsername", emailUsername);
	}

	public static String getEmailPassword() {
		return emailPassword;
	}

	public static void setEmailPassword(String emailPassword) throws ConfigurationException {
		GlobalVariables.emailPassword = emailPassword;
		setConfigProperty("emailPassword", emailPassword);
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
	public static String getPublishedText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Veröffentlicht am ";
		case it:
			return "Pubbliccato il ";
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
					return "Tendenza: Pericolo valanghe in diminuazione";
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
	public static String getHeadline(LanguageCode lang) {
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
	public static String getLogoPath(LanguageCode lang) {
		switch (lang) {
		case de:
			return "logo/lawinen_report.png";
		case it:
			return "logo/valanghe_report.png";
		case en:
			return "logo/avalanche_report.png";
		default:
			return "logo/avalanche_report.png";
		}
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
			return "Provisione Valanghe";
		case en:
			return "Avalanche Forecast";
		default:
			return "Avalanche Forecast";
		}
	}

	// LANG
	public static String getEmailSubject(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinenvorhersage, ";
		case it:
			return "Provisione Valanghe, ";
		case en:
			return "Avalanche Forecast, ";
		default:
			return "Avalanche Forecast, ";
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
			return "Pommeriggio";
		default:
			return "PM";
		}
	}

	// LANG
	public static String getDangerRatingTextLong(DangerRating dangerRating, LanguageCode lang) {
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
				return "Fehlt";
			case it:
				return "Mancha";
			case en:
				return "Missing";
			default:
				return "Missing";
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
			localImagesPath = config.getString("localImagesPath");
			localFontsPath = config.getString("localFontsPath");
			pdfDirectory = config.getString("pdfDirectory");
			serverImagesUrl = config.getString("serverImagesUrl");
			serverImagesUrlLocalhost = config.getString("serverImagesUrlLocalhost");
			mapsPath = config.getString("mapsPath");
			emailUsername = config.getString("emailUsername");
			emailPassword = config.getString("emailPassword");
			createMaps = config.getBoolean("createMaps");
			createPdf = config.getBoolean("createPdf");
			createStaticWidget = config.getBoolean("createStaticWidget");
			socketIoOrigin = config.getString("socketIoOrigin");
			socketIoPort = config.getInt("socketIoPort");
			sendEmails = config.getBoolean("sendEmails");
			publishToSocialMedia = config.getBoolean("publishToSocialMedia");
			publishAt5PM = config.getBoolean("publishAt5PM");
			publishAt8AM = config.getBoolean("publishAt8AM");
			publishBulletinsTyrol = config.getBoolean("publishBulletinsTyrol");
			publishBulletinsSouthTyrol = config.getBoolean("publishBulletinsSouthTyrol");
			publishBulletinsTrentino = config.getBoolean("publishBulletinsTrentino");
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
		if (serverImagesUrl != null)
			json.put("serverImagesUrl", serverImagesUrl);
		if (serverImagesUrlLocalhost != null)
			json.put("serverImagesUrlLocalhost", serverImagesUrlLocalhost);
		if (mapsPath != null)
			json.put("mapsPath", mapsPath);
		json.put("smtpAuth", smtpAuth);
		json.put("smtpTls", smtpTls);
		if (smtpHost != null)
			json.put("smtpHost", smtpHost);
		if (smtpPort != null)
			json.put("smtpPort", smtpPort);
		if (emailUsername != null)
			json.put("emailUsername", emailUsername);
		if (emailPassword != null)
			json.put("emailPassword", emailPassword);
		if (socketIoOrigin != null)
			json.put("socketIoOrigin", socketIoOrigin);
		json.put("socketIoPort", socketIoPort);
		json.put("createMaps", createMaps);
		json.put("createPdf", createPdf);
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
			logger.info("Configuration saved!");
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
		if (configuration.has("serverImagesUrl"))
			setServerImagesUrl(configuration.getString("serverImagesUrl"));
		if (configuration.has("serverImagesUrlLocalhost"))
			setServerImagesUrlLocalhost(configuration.getString("serverImagesUrlLocalhost"));
		if (configuration.has("mapsPath"))
			setMapsPath(configuration.getString("mapsPath"));
		if (configuration.has("smtpAuth"))
			setSmtpAuth(configuration.getBoolean("smtpAuth"));
		if (configuration.has("smtpTls"))
			setSmtpTls(configuration.getBoolean("smtpTls"));
		if (configuration.has("smtpHost"))
			setSmtpHost(configuration.getString("smtpHost"));
		if (configuration.has("smtpPort"))
			setSmtpPort(configuration.getString("smtpPort"));
		if (configuration.has("emailUsername"))
			setEmailUsername(configuration.getString("emailUsername"));
		if (configuration.has("emailPassword"))
			setEmailPassword(configuration.getString("emailPassword"));
		if (configuration.has("createMaps"))
			setCreateMaps(configuration.getBoolean("createMaps"));
		if (configuration.has("createPdf"))
			setCreatePdf(configuration.getBoolean("createPdf"));
		if (configuration.has("socketIoOrigin"))
			setSocketIoOrigin(configuration.getString("socketIoOrigin"));
		if (configuration.has("socketIoPort"))
			setSocketIoPort(configuration.getInt("socketIoPort"));
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
}
