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

import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;

public class GlobalVariables {

	private static final Logger logger = LoggerFactory.getLogger(GlobalVariables.class);

	private static boolean createMaps = false;
	private static boolean createPdf = true;
	private static boolean sendEmails = false;
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

	// TODO for testing
	// private static String pdfDirectory = "D:\\";
	private static String pdfDirectory = "pdfs/";
	private static String serverImagesUrl = "https://natlefs.snowobserver.com/images/";
	// private static String mapsPath =
	// "D:\\norbert\\workspaces\\albina-euregio\\albina-server\\src\\main\\resources\\images\\";
	private static String mapsPath = "images/";

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
	public static DateTimeFormatter dateTimeEn = DateTimeFormat.forPattern(" dd.MM.yyyy");
	public static DateTimeFormatter dateTimeDe = DateTimeFormat.forPattern(" dd.MM.yyyy");
	public static DateTimeFormatter dateTimeIt = DateTimeFormat.forPattern(" dd.MM.yyyy");
	// LANG
	public static DateTimeFormatter publicationDateTimeEn = DateTimeFormat.forPattern("dd.MM.yyyy, hh:mm aa");
	public static DateTimeFormatter publicationDateTimeDe = DateTimeFormat.forPattern("dd.MM.yyyy, HH:mm");
	public static DateTimeFormatter publicationDateTimeIt = DateTimeFormat.forPattern("dd.MM.yyyy, HH:mm");

	// REGION
	public static String codeTrentino = "IT-32-TN";
	public static String codeSouthTyrol = "IT-32-BZ";
	public static String codeTyrol = "AT-07";
	public static String codeStyria = "AT-06";

	public static String propertiesFilePath = "/META-INF/config.properties";

	// REGION
	public static List<String> regions = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("AT-07");
			add("IT-32-BZ");
			add("IT-32-TN");
			add("AT-06");
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

	public static String getMapsPath() {
		return mapsPath;
	}

	public static void setMapsPath(String mapsPath) throws ConfigurationException {
		GlobalVariables.mapsPath = mapsPath;
		setConfigProperty("mapsPath", mapsPath);
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
			return "Publiziert ";
		case it:
			return "Pubblicato ";
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
					return "Tendency: Avalanche danger increasing";
				default:
					return "Tendency: Avalanche danger increasing";
				}
			case steady:
				switch (lang) {
				case de:
					return "Tendenz: Lawinengefahr bleibt gleich";
				case it:
					return "Tendenza: Pericolo valanghe stabile";
				case en:
					return "Tendency: Avalanche danger stays the same";
				default:
					return "Tendency: Avalanche danger stays the same";
				}
			case decreasing:
				switch (lang) {
				case de:
					return "Tendenz: Lawinengefahr nimmt ab";
				case it:
					return "Tendenza: Pericolo valanghe in diminuazione";
				case en:
					return "Tendency: Avalanche danger decreasing";
				default:
					return "Tendency: Avalanche danger decreasing";
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
			return "Situazioni tipo";
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
	public static String getDangerRatingSymbolHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Symbol";
		case it:
			return "Emblema";
		case en:
			return "Icon";
		default:
			return "Icon";
		}
	}

	// LANG
	public static String getDangerRatingSnowpackHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Schneedeckenstabilität";
		case it:
			return "Stabilità del manto nevoso";
		case en:
			return "Snowpack stability";
		default:
			return "Snowpack stability";
		}
	}

	// LANG
	public static String getDangerRatingAvalancheHeadline(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinen-Auslösewahrscheinlichkeit";
		case it:
			return "Probabilità di distacco di valanghe";
		case en:
			return "Avalanche triggering probability";
		default:
			return "Avalanche triggering probability";
		}
	}

	// LANG
	public static String getDangerRatingVeryHighSnowpackText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Die Schneedecke ist allgemein schwach verfestigt und weitgehend instabil.";
		case it:
			return "";
		case en:
			return "The snowpack is poorly bonded and largely unstable in general.";
		default:
			return "The snowpack is poorly bonded and largely unstable in general.";
		}
	}

	// LANG
	public static String getDangerRatingHighSnowpackText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Die Schneedecke ist an den meisten Steilhängen schwach verfestigt.";
		case it:
			return "";
		case en:
			return "The snowpack is poorly bonded on most steep slopes.";
		default:
			return "The snowpack is poorly bonded on most steep slopes.";
		}
	}

	// LANG
	public static String getDangerRatingConsiderableSnowpackText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Die Schneedecke ist an vielen Steilhängen* nur mäßig bis schwach verfestigt.";
		case it:
			return "";
		case en:
			return "The snowpack is moderately to poorly bonded on many steep slopes.";
		default:
			return "The snowpack is moderately to poorly bonded on many steep slopes.";
		}
	}

	// LANG
	public static String getDangerRatingModerateSnowpackText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Die Schneedecke ist an einigen Steilhängen* nur mäßig verfestigt, ansonsten allgemein gut verfestigt.";
		case it:
			return "";
		case en:
			return "The snowpack is only moderately well bonded on some steep slopes*, otherwise well bonded in general.";
		default:
			return "The snowpack is only moderately well bonded on some steep slopes*, otherwise well bonded in general.";
		}
	}

	// LANG
	public static String getDangerRatingLowSnowpackText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Die Schneedecke ist allgemein gut verfestigt und stabil.";
		case it:
			return "";
		case en:
			return "The snowpack is well bonded and stable in general.";
		default:
			return "The snowpack is well bonded and stable in general.";
		}
	}

	// LANG
	public static String getDangerRatingVeryHighAvalancheText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Spontan sind viele große, mehrfach auch sehr große Lawinen, auch in mäßig steilem Gelände zu erwarten.";
		case it:
			return "";
		case en:
			return "Numerous large-sized and often very large-sized natural avalanches can be expected, even in moderately steep terrain.";
		default:
			return "Numerous large-sized and often very large-sized natural avalanches can be expected, even in moderately steep terrain.";
		}
	}

	// LANG
	public static String getDangerRatingHighAvalancheText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinenauslösung ist bereits bei geringer Zusatzbelastung** an zahlreichen Steilhängen wahrscheinlich. Fallweise sind spontan viele mittlere, mehrfach auch große Lawinen zu erwarten.";
		case it:
			return "";
		case en:
			return "Triggering is likely even from low additional loads** on many steep slopes. In some cases, numerous medium-sized and often large-sized natural avalanches can be expected.";
		default:
			return "Triggering is likely even from low additional loads** on many steep slopes. In some cases, numerous medium-sized and often large-sized natural avalanches can be expected.";
		}
	}

	// LANG
	public static String getDangerRatingConsiderableAvalancheText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinenauslösung ist bereits bei geringer Zusatzbelastung** vor allem an den angegebenen Steilhängen* möglich. Fallweise sind spontan einige mittlere, vereinzelt aber auch große Lawinen möglich.";
		case it:
			return "";
		case en:
			return "Triggering is possible, evenen from low additional loads** particularly on the indicated steep slopes*. In some cases medium-sized, in isolated cases large-sized natural avalanches are possible.";
		default:
			return "Triggering is possible, evenen from low additional loads** particularly on the indicated steep slopes*. In some cases medium-sized, in isolated cases large-sized natural avalanches are possible.";
		}
	}

	// LANG
	public static String getDangerRatingModerateAvalancheText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinenauslösung ist insbesondere bei großer Zusatzbelastung**, vor allem an den angegebenen Steilhängen* möglich. Große spontane Lawinen sind nicht zu erwarten.";
		case it:
			return "";
		case en:
			return "Triggering is possible primarly from high additional loads**, particularly on the indicated steep slopes*. Large-sized natural avalanches are unlikely.";
		default:
			return "Triggering is possible primarly from high additional loads**, particularly on the indicated steep slopes*. Large-sized natural avalanches are unlikely.";
		}
	}

	// LANG
	public static String getDangerRatingLowAvalancheText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Lawinenauslösung ist allgemein nur bei großer Zusatzbelastung** an vereinzelten Stellen im extremen Steilgelände* möglich. Spontan sind nur Rutsche und kleine Lawinen möglich.";
		case it:
			return "";
		case en:
			return "Triggering is generally possible only from high additional loads** in isolated areas of very steep, extreme terrain. Only sluffs and small-sized natural avalanches are possible.";
		default:
			return "Triggering is generally possible only from high additional loads** in isolated areas of very steep, extreme terrain. Only sluffs and small-sized natural avalanches are possible.";
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
	// TODO it
	public static String getSlopeText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "* Das lawinengefährliche Gelände ist im Lawinenlagebericht im Allgemeinen näher beschrieben (Höhenlage, Exposition, Geländeform):";
		case it:
			return "";
		case en:
			return "* The avalanche-prone locations are described in greater detail in the avalanche bulletin (altitude, slope aspect, type of terrain):";
		default:
			return "* The avalanche-prone locations are described in greater detail in the avalanche bulletin (altitude, slope aspect, type of terrain):";
		}
	}

	// LANG
	// TODO it
	public static String getSlopeTextItem1(LanguageCode lang) {
		switch (lang) {
		case de:
			return "mässig steiles Gelände: Hänge flacher als rund 30 Grad";
		case it:
			return "";
		case en:
			return "moderately steep terrain: slopes shallower than 30 degrees";
		default:
			return "moderately steep terrain: slopes shallower than 30 degrees";
		}
	}

	// LANG
	// TODO it
	public static String getSlopeTextItem2(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Steilhänge: Hänge steiler als rund 30 Grad";
		case it:
			return "";
		case en:
			return "steep slope: slopes steeper than 30 degrees";
		default:
			return "steep slope: slopes steeper than 30 degrees";
		}
	}

	// LANG
	// TODO it
	public static String getSlopeTextItem3(LanguageCode lang) {
		switch (lang) {
		case de:
			return "extremes Steilgelände: besonders ungünstige Hänge bezüglich Neigung (steiler als etwa 40 Grad), Geländeform, Kammnähe und Bodenrauigkeit";
		case it:
			return "";
		case en:
			return "very steep, extreme terrain: adverse slope angle (more than 40 degrees), terrain profile, proximity to ridge, smoothness of underlying ground surface";
		default:
			return "very steep, extreme terrain: adverse slope angle (more than 40 degrees), terrain profile, proximity to ridge, smoothness of underlying ground surface";
		}
	}

	// LANG
	// TODO it
	public static String getAdditionalLoadText(LanguageCode lang) {
		switch (lang) {
		case de:
			return "** Zusatzbelastung:";
		case it:
			return "";
		case en:
			return "** additional loads:";
		default:
			return "** additional loads:";
		}
	}

	// LANG
	// TODO it
	public static String getAdditionalLoadTextItem1(LanguageCode lang) {
		switch (lang) {
		case de:
			return "gering: einzelner Skifahrer / Snowboarder, sanft schwingend, nicht stürzend; Schneeschuhgeher; Gruppe mit Entlastungsabständen (>10m)";
		case it:
			return "";
		case en:
			return "low: individual skier / snowboarder, riding softly, not falling; snowshoer; group with good spacing (minimum 10m) keeping distances";
		default:
			return "low: individual skier / snowboarder, riding softly, not falling; snowshoer; group with good spacing (minimum 10m) keeping distances";
		}
	}

	// LANG
	// TODO it
	public static String getAdditionalLoadTextItem2(LanguageCode lang) {
		switch (lang) {
		case de:
			return "gross: zwei oder mehrere Skifahrer / Snowboarder etc. ohne Entlastungsabstände; Pistenfahrzeug; Sprengung; einzelner Fussgänger / Alpinist";
		case it:
			return "";
		case en:
			return "high: two or more skiers / snowboarders etc. without good spacing (or without intervals); snowmachine; explosives; single hiker/climber";
		default:
			return "high: two or more skiers / snowboarders etc. without good spacing (or without intervals); snowmachine; explosives; single hiker/climber";
		}
	}

	// LANG
	public static String getDangerRatingText(DangerRating dangerRating, LanguageCode lang) {
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
			mapsPath = config.getString("mapsPath");
			emailUsername = config.getString("emailUsername");
			emailPassword = config.getString("emailPassword");
			createMaps = config.getBoolean("createMaps");
			createPdf = config.getBoolean("createPdf");
			sendEmails = config.getBoolean("sendEmails");
			publishToSocialMedia = config.getBoolean("publishToSocialMedia");
			publishAt5PM = config.getBoolean("publishAt5PM");
			publishAt8AM = config.getBoolean("publishAt8AM");
			publishBulletinsTyrol = config.getBoolean("publishBulletinsTyrol");
			publishBulletinsSouthTyrol = config.getBoolean("publishBulletinsSouthTyrol");
			publishBulletinsTrentino = config.getBoolean("publishBulletinsTrentino");
			publishBulletinsStyria = config.getBoolean("publishBulletinsStyria");
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
		if (mapsPath != null)
			json.put("mapsPath", mapsPath);
		if (emailUsername != null)
			json.put("emailUsername", emailUsername);
		if (emailPassword != null)
			json.put("emailPassword", emailPassword);
		json.put("createMaps", createMaps);
		json.put("createPdf", createPdf);
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
		Configuration config;
		try {
			config = configs.properties(propertiesFilePath);
			config.setProperty(key, value);
			propertiesBuilder.save();
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
		if (configuration.has("mapsPath"))
			setMapsPath(configuration.getString("mapsPath"));
		if (configuration.has("emailUsername"))
			setEmailUsername(configuration.getString("emailUsername"));
		if (configuration.has("emailPassword"))
			setEmailPassword(configuration.getString("emailPassword"));
		if (configuration.has("createMaps"))
			setCreateMaps(configuration.getBoolean("createMaps"));
		if (configuration.has("createPdf"))
			setCreatePdf(configuration.getBoolean("createPdf"));
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
}
