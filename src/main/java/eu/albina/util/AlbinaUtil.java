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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public class AlbinaUtil {

	private static final Logger logger = LoggerFactory.getLogger(AlbinaUtil.class);

	// REGION
	public static final int regionCountTyrol = 29;
	public static final int regionCountSouthTyrol = 20;
	public static final int regionCountTrentino = 21;

	// REGION
	public static final Map<String, String> regionsMapDe = Stream.of(new String[][] { { "AT-07-01", "Allgäuer Alpen" },
			{ "AT-07-02", "Östliche Lechtaler Alpen - Ammergauer Alpen" }, { "AT-07-03", "Mieminger Gebirge" },
			{ "AT-07-04", "Karwendel" }, { "AT-07-05", "Brandenberger Alpen" },
			{ "AT-07-06", "Wilder Kaiser - Waidringer Alpen" }, { "AT-07-07", "Westliche Lechtaler Alpen" },
			{ "AT-07-08", "Zentrale Lechtaler Alpen" }, { "AT-07-09", "Grieskogelgruppe" },
			{ "AT-07-10", "Westliche Verwallgruppe" }, { "AT-07-11", "Östliche Verwallgruppe" },
			{ "AT-07-12", "Silvretta" }, { "AT-07-13", "Samnaungruppe" },
			{ "AT-07-14", "Nördliche Ötztaler- und Stubaier Alpen" }, { "AT-07-15", "Westliche Tuxer Alpen" },
			{ "AT-07-16", "Östliche Tuxer Alpen" }, { "AT-07-17", "Westliche Kitzbüheler Alpen" },
			{ "AT-07-18", "Östliche Kitzbüheler Alpen" }, { "AT-07-19", "Glockturmgruppe" },
			{ "AT-07-20", "Weißkugelgruppe" }, { "AT-07-21", "Gurgler Gruppe" },
			{ "AT-07-22", "Zentrale Stubaier Alpen" }, { "AT-07-23", "Nördliche Zillertaler Alpen" },
			{ "AT-07-24", "Venedigergruppe" }, { "AT-07-25", "Östliche Rieserfernergruppe" },
			{ "AT-07-26", "Glocknergruppe" }, { "AT-07-27", "Östliche Deferegger Alpen" },
			{ "AT-07-28", "Schobergruppe" }, { "AT-07-29", "Lienzer Dolomiten" },
			{ "IT-32-BZ-01", "Münstertaler Alpen" }, { "IT-32-BZ-02", "Langtaufers" },
			{ "IT-32-BZ-03", "Schnalser Kamm" }, { "IT-32-BZ-04", "Südliche Stubaier Alpen" },
			{ "IT-32-BZ-05", "Südliche Zillertaler Alpen und Hohe Tauern" }, { "IT-32-BZ-06", "Saldurn-Mastaun Kamm" },
			{ "IT-32-BZ-07", "Texelgruppe" }, { "IT-32-BZ-08", "Sarntaler Alpen" },
			{ "IT-32-BZ-09", "Westliche Pfunderer Berge" }, { "IT-32-BZ-10", "Östliche Pfunderer Berge" },
			{ "IT-32-BZ-11", "Durreckgruppe" }, { "IT-32-BZ-12", "Westliche Rieserfernergruppe" },
			{ "IT-32-BZ-13", "Westliche Deferegger Alpen" }, { "IT-32-BZ-14", "Ortlergruppe" },
			{ "IT-32-BZ-15", "Ultental" }, { "IT-32-BZ-16", "Östliche Nonsberger Alpen" },
			{ "IT-32-BZ-17", "Nördliche Fleimstaler Alpen" }, { "IT-32-BZ-18", "Grödner Dolomiten" },
			{ "IT-32-BZ-19", "Pragser Dolomiten" }, { "IT-32-BZ-20", "Sextner Dolomiten" },
			{ "IT-32-TN-01", "Adamello - Presanella" }, { "IT-32-TN-02", "Südlicher Adamello" },
			{ "IT-32-TN-03", "Bondone und Stivo" }, { "IT-32-TN-04", "Nördliche Brenta - Peller" },
			{ "IT-32-TN-05", "Südliche Brenta" }, { "IT-32-TN-06", "Folgaria - Lavarone" },
			{ "IT-32-TN-07", "Nördliche Lagorai" }, { "IT-32-TN-08", "Südliche Lagorai" }, { "IT-32-TN-09", "Latemar" },
			{ "IT-32-TN-10", "Marzola - Valsugana" }, { "IT-32-TN-11", "Paganella" }, { "IT-32-TN-12", "Prealpi" },
			{ "IT-32-TN-13", "Primiero - Pale di S. Martino" }, { "IT-32-TN-14", "Vallarse" },
			{ "IT-32-TN-15", "Cimberntal" }, { "IT-32-TN-16", "Fassatal" },
			{ "IT-32-TN-17", "Westliche Nonsberger Alpen" }, { "IT-32-TN-18", "Ledrotal" },
			{ "IT-32-TN-19", "Sole, Pejo und Rabbi" }, { "IT-32-TN-20", "Maddalene" },
			{ "IT-32-TN-21", "Pine' - Fersental" } }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

	// REGION
	public static final Map<String, String> regionsMapIt = Stream.of(new String[][] {
			{ "AT-07-01", "Alpi dell'Allgäu" }, { "AT-07-02", "Alpi della Lechtal orientali - Alpi dell'Ammergau" },
			{ "AT-07-03", "Monti di Mieming" }, { "AT-07-04", "Monti del Karwendel" },
			{ "AT-07-05", "Alpi di Brandenberg" }, { "AT-07-06", "Monti del Kaiser - Alpi di Waidring" },
			{ "AT-07-07", "Alpi della Lechtal occidentali" }, { "AT-07-08", "Alpi della Lechtal centrali" },
			{ "AT-07-09", "Gruppo del Grieskogel" }, { "AT-07-10", "Gruppo Verwall occidentali" },
			{ "AT-07-11", "Gruppo Verwall orientali" }, { "AT-07-12", "Silvretta" }, { "AT-07-13", "Gruppo Samnaun" },
			{ "AT-07-14", "Alpi della Valle Oetz e dello Stubai settentrionali" },
			{ "AT-07-15", "Alpi del Tux occidentali" }, { "AT-07-16", "Alpi del Tux orientali" },
			{ "AT-07-17", "Alpi di Kitzbühel occidentali" }, { "AT-07-18", "Alpi di Kitzbühel orientali" },
			{ "AT-07-19", "Gruppo della Punta della Gallina" }, { "AT-07-20", "Gruppo della Palla Bianca" },
			{ "AT-07-21", "Alpi Passirie" }, { "AT-07-22", "Alpi dello Stubai centrali" },
			{ "AT-07-23", "Alpi della Zillertal settentrionali" }, { "AT-07-24", "Gruppo del Venediger" },
			{ "AT-07-25", "Catena delle Vedrette di Ries orientali" }, { "AT-07-26", "Gruppo del Glockner" },
			{ "AT-07-27", "Alpi del Defereggen orientali" }, { "AT-07-28", "Gruppo dello Schober" },
			{ "AT-07-29", "Dolomiti di Lienz" }, { "IT-32-BZ-01", "Alpi della Val Müstair" },
			{ "IT-32-BZ-02", "Vallelunga" }, { "IT-32-BZ-03", "Cresta di Senales" },
			{ "IT-32-BZ-04", "Alpi dello Stubai meridionali" },
			{ "IT-32-BZ-05", "Alpi della Zillertal meridionali e Alti Tauri" },
			{ "IT-32-BZ-06", "Gruppo Saldura-Mastaun" }, { "IT-32-BZ-07", "Gruppo Tessa" },
			{ "IT-32-BZ-08", "Alpi Sarentine" }, { "IT-32-BZ-09", "Monti di Fundres occidentali" },
			{ "IT-32-BZ-10", "Monti di Fundres orientali" }, { "IT-32-BZ-11", "Gruppo della Cima Dura" },
			{ "IT-32-BZ-12", "Catena delle Vedrette di Ries occidentali" },
			{ "IT-32-BZ-13", "Alpi del Defereggen occidentali" }, { "IT-32-BZ-14", "Gruppo dell'Ortles" },
			{ "IT-32-BZ-15", "Val d'Ultimo" }, { "IT-32-BZ-16", "Alpi della Val di Non orientali" },
			{ "IT-32-BZ-17", "Dolomiti di Fiemme settentrionali" }, { "IT-32-BZ-18", "Dolomiti di Gardena" },
			{ "IT-32-BZ-19", "Dolomiti di Braies" }, { "IT-32-BZ-20", "Dolomiti di Sesto" },
			{ "IT-32-TN-01", "Adamello - Presanella" }, { "IT-32-TN-02", "Adamello meridionale" },
			{ "IT-32-TN-03", "Bondone e Stivo" }, { "IT-32-TN-04", "Brenta settentrionale - Peller" },
			{ "IT-32-TN-05", "Brenta meridionale" }, { "IT-32-TN-06", "Folgaria - Lavarone" },
			{ "IT-32-TN-07", "Lagorai settentrionale" }, { "IT-32-TN-08", "Lagorai meridionale" },
			{ "IT-32-TN-09", "Latemar" }, { "IT-32-TN-10", "Marzola - Valsugana" }, { "IT-32-TN-11", "Paganella" },
			{ "IT-32-TN-12", "Prealpi" }, { "IT-32-TN-13", "Primiero - Pale di S. Martino" },
			{ "IT-32-TN-14", "Vallarsa" }, { "IT-32-TN-15", "Valle di Cembra" }, { "IT-32-TN-16", "Valle di Fassa" },
			{ "IT-32-TN-17", "Alpi della Val di Non occidentali" }, { "IT-32-TN-18", "Valle di Ledro" },
			{ "IT-32-TN-19", "Sole, Pejo e Rabbi" }, { "IT-32-TN-20", "Maddalene" },
			{ "IT-32-TN-21", "Pine' - Valle dei Mocheni" } })
			.collect(Collectors.toMap(data -> data[0], data -> data[1]));

	// REGION
	public static final Map<String, String> regionsMapEn = Stream.of(new String[][] { { "AT-07-01", "Allgäu Alps" },
			{ "AT-07-02", "Eastern Lechtal Alps - Ammergau Alps" }, { "AT-07-03", "Mieming Mountains" },
			{ "AT-07-04", "Karwendel Mountains" }, { "AT-07-05", "Brandenberg Alps" },
			{ "AT-07-06", "Wilder Kaiser Mountains - Waidring Alps" }, { "AT-07-07", "Western Lechtal Alps" },
			{ "AT-07-08", "Central Lechtal Alps" }, { "AT-07-09", "Grieskogel Mountains" },
			{ "AT-07-10", "Western Verwall Mountains" }, { "AT-07-11", "Eastern Verwall Mountains" },
			{ "AT-07-12", "Silvretta" }, { "AT-07-13", "Samnaun Mountains" },
			{ "AT-07-14", "Northern Oetz and Stubai Alps" }, { "AT-07-15", "Western Tuxer Alps" },
			{ "AT-07-16", "Eastern Tuxer Alps" }, { "AT-07-17", "Western Kitzbühel Alps" },
			{ "AT-07-18", "Eastern Kitzbühel Alps" }, { "AT-07-19", "Glockturm Range" },
			{ "AT-07-20", "Weißkugel Range" }, { "AT-07-21", "Gurgler Range" }, { "AT-07-22", "Central Stubai Alps" },
			{ "AT-07-23", "Northern Zillertal Alps" }, { "AT-07-24", "Venediger Range" },
			{ "AT-07-25", "Eastern Rieserferner Mountains" }, { "AT-07-26", "Glockner Range" },
			{ "AT-07-27", "Eastern Deferegger Alps" }, { "AT-07-28", "Schober Mountains" },
			{ "AT-07-29", "Lienzer Dolomites" }, { "IT-32-BZ-01", "Val Müstair Alps" },
			{ "IT-32-BZ-02", "Langtaufers" }, { "IT-32-BZ-03", "Schnals Ridge" },
			{ "IT-32-BZ-04", "Southern Stubai Alps" }, { "IT-32-BZ-05", "Southern Zillertal Alps and High Tauern" },
			{ "IT-32-BZ-06", "Saldurn-Mastaun Ridge" }, { "IT-32-BZ-07", "Texel Mountains" },
			{ "IT-32-BZ-08", "Sarntal Alps" }, { "IT-32-BZ-09", "Western Pfunderer Mountains" },
			{ "IT-32-BZ-10", "Eastern Pfunderer Mountains" }, { "IT-32-BZ-11", "Durreck Range" },
			{ "IT-32-BZ-12", "Western Rieserferner Mountains" }, { "IT-32-BZ-13", "Western Deferegger Alps" },
			{ "IT-32-BZ-14", "Ortler Range" }, { "IT-32-BZ-15", "Ulten Valley" },
			{ "IT-32-BZ-16", "Eastern Nonsberger Alps" }, { "IT-32-BZ-17", "Northern Dolomites of Fiemme" },
			{ "IT-32-BZ-18", "Gröden Dolomites" }, { "IT-32-BZ-19", "Prags Dolomites" },
			{ "IT-32-BZ-20", "Sexten Dolomites" }, { "IT-32-TN-01", "Adamello - Presanella" },
			{ "IT-32-TN-02", "Southern Adamello" }, { "IT-32-TN-03", "Bondone and Stivo" },
			{ "IT-32-TN-04", "Northern Brenta - Peller" }, { "IT-32-TN-05", "Southern Brenta" },
			{ "IT-32-TN-06", "Folgaria - Laverone" }, { "IT-32-TN-07", "Northern Lagorai" },
			{ "IT-32-TN-08", "Southern Lagorai" }, { "IT-32-TN-09", "Latemar" },
			{ "IT-32-TN-10", "Marzola - Valsugana" }, { "IT-32-TN-11", "Paganella" }, { "IT-32-TN-12", "Prealps" },
			{ "IT-32-TN-13", "Primiero - Pale di S. Martino" }, { "IT-32-TN-14", "Vallarsa" },
			{ "IT-32-TN-15", "Cembra Valley" }, { "IT-32-TN-16", "Fassa Valley" },
			{ "IT-32-TN-17", "Western Nonsberg Alps" }, { "IT-32-TN-18", "Ledro Valley" },
			{ "IT-32-TN-19", "Sole, Pejo and Rabbi" }, { "IT-32-TN-20", "Maddalene" },
			{ "IT-32-TN-21", "Pine' - Mocheni Valley" } }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

	public static final String greyDarkColor = "#565F61";

	// REGION
	public static int getRegionCount(String region) {
		switch (region) {
		case "AT-07":
			return regionCountTyrol;
		case "IT-32-BZ":
			return regionCountSouthTyrol;
		case "IT-32-TN":
			return regionCountTrentino;

		default:
			return -1;
		}
	}

	// TODO LANG
	// REGION
	public static String getMicroRegionName(LanguageCode lang, String microRegionId) {
		switch (lang) {
		case de:
			return AlbinaUtil.regionsMapDe.get(microRegionId);
		case it:
			return AlbinaUtil.regionsMapIt.get(microRegionId);
		case en:
			return AlbinaUtil.regionsMapEn.get(microRegionId);
		default:
			return AlbinaUtil.regionsMapEn.get(microRegionId);
		}
	}

	// TODO LANG
	// REGION
	public static String getRegionName(LanguageCode lang, String region) {
		switch (lang) {
		case de:
			switch (region) {
			case "AT-07":
				return "Tirol ";
			case "IT-32-BZ":
				return "Südtirol ";
			case "IT-32-TN":
				return "Trentino ";
			default:
				return "";
			}
		case it:
			switch (region) {
			case "AT-07":
				return "Tirolo ";
			case "IT-32-BZ":
				return "Alto Adige ";
			case "IT-32-TN":
				return "Trentino ";
			default:
				return "";
			}
		case en:
			switch (region) {
			case "AT-07":
				return "Tirol ";
			case "IT-32-BZ":
				return "Southtyrol ";
			case "IT-32-TN":
				return "Trentino ";
			default:
				return "";
			}
		default:
			switch (region) {
			case "AT-07":
				return "Tirol ";
			case "IT-32-BZ":
				return "Southtyrol ";
			case "IT-32-TN":
				return "Trentino ";
			default:
				return "";
			}
		}
	}

	public static String getDaytimeString(ResourceBundle messages, String type) {
		return messages.getString("daytime." + type);
	}

	public static String getDangerPatternText(DangerPattern dp, LanguageCode lang) {
		return dp.toString(lang.getLocale());
	}

	public static String getWarningLevelId(AvalancheBulletinDaytimeDescription avalancheBulletinDaytimeDescription,
			boolean elevationDependency) {
		if (elevationDependency)
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingBelow()) + "_"
					+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
		else
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove()) + "_"
					+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
	}

	public static String getTendencyDate(List<AvalancheBulletin> bulletins, ResourceBundle messages) {
		DateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			DateTime bulletinDate = avalancheBulletin.getValidUntil();
			if (date == null)
				date = bulletinDate;
			else if (bulletinDate.isAfter(date))
				date = bulletinDate;
		}

		if (date != null) {
			StringBuilder result = new StringBuilder();
			result.append(messages.getString("tendency.binding-word"));
			result.append(messages.getString("day." + date.getDayOfWeek()));
			result.append(date.toString(messages.getString("date-time-format.tendency")));
			return result.toString();
		} else {
			return "";
		}
	}

	public static boolean hasBulletinChanged(DateTime startDate, String region) throws AlbinaException {
		boolean result = false;
		Map<DateTime, BulletinStatus> status = AvalancheReportController.getInstance().getInternalStatus(startDate,
				startDate, region);
		if (status.size() == 1 && status.get(startDate) != BulletinStatus.published
				&& status.get(startDate) != BulletinStatus.republished)
			result = true;
		return result;
	}

	public static String encodeFileToBase64Binary(File file) {
		String encodedfile = null;
		try {
			FileInputStream fileInputStreamReader = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			fileInputStreamReader.read(bytes);
			encodedfile = new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
			fileInputStreamReader.close();
		} catch (IOException e) {
			logger.error("Failed to encode to base64", e);
		}

		return encodedfile;
	}

	public static String getDate(List<AvalancheBulletin> bulletins, ResourceBundle messages) {
		StringBuilder result = new StringBuilder();
		DateTime date = getDate(bulletins);
		if (date != null) {
			result.append(messages.getString("day." + date.getDayOfWeek()));
			result.append(date.toString(messages.getString("date-time-format")));
		} else {
			// TODO what if no date is given (should not happen)
			result.append("-");
		}

		return result.toString();
	}

	public static int getYear(List<AvalancheBulletin> bulletins) {
		return getDate(bulletins).getYear();
	}

	public static String getFilenameDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = getDate(bulletins);
		if (date != null)
			return date.toString(DateTimeFormat.forPattern("yyyy-MM-dd")) + "_" + lang.toString();
		else
			return "_" + lang.toString();
	}

	public static String getValidityDateString(List<AvalancheBulletin> bulletins) {
		DateTime date = getValidityDate(bulletins);
		return date.toString(DateTimeFormat.forPattern("yyyy-MM-dd"));
	}

	public static String getPreviousValidityDateString(List<AvalancheBulletin> bulletins) {
		DateTime date = getValidityDate(bulletins);
		date = date.minusDays(1);
		return date.toString(DateTimeFormat.forPattern("yyyy-MM-dd"));
	}

	public static String getNextValidityDateString(List<AvalancheBulletin> bulletins) {
		DateTime date = getValidityDate(bulletins);
		date = date.plusDays(1);
		return date.toString(DateTimeFormat.forPattern("yyyy-MM-dd"));
	}

	public static String getPreviousValidityDateString(List<AvalancheBulletin> bulletins, ResourceBundle messages) {
		DateTime date = getValidityDate(bulletins);
		date = date.minusDays(1);
		return date.toString(messages.getString("date-time-format")).trim();
	}

	public static String getNextValidityDateString(List<AvalancheBulletin> bulletins, ResourceBundle messages) {
		DateTime date = getValidityDate(bulletins);
		date = date.plusDays(1);
		return date.toString(messages.getString("date-time-format")).trim();
	}

	private static DateTime getValidityDate(List<AvalancheBulletin> bulletins) {
		DateTime date = getDate(bulletins);
		if (date.getHourOfDay() > 12)
			date = date.plusDays(1);
		return date;
	}

	public static String getPreviousDayLink(List<AvalancheBulletin> bulletins, LanguageCode lang, String region,
			ResourceBundle messages) {
		if (region != null && !region.isEmpty())
			return messages.getString("avalanche-report.url") + "/"
					+ GlobalVariables.getHtmlDirectory().substring(GlobalVariables.directoryOffset) + "/"
					+ AlbinaUtil.getPreviousValidityDateString(bulletins) + "/" + region + "_" + lang.toString()
					+ ".html";
		else
			return messages.getString("avalanche-report.url") + "/"
					+ GlobalVariables.getHtmlDirectory().substring(GlobalVariables.directoryOffset) + "/"
					+ AlbinaUtil.getPreviousValidityDateString(bulletins) + "/" + lang.toString() + ".html";
	}

	public static String getNextDayLink(List<AvalancheBulletin> bulletins, LanguageCode lang, String region,
			ResourceBundle messages) {
		if (region != null && !region.isEmpty())
			return messages.getString("avalanche-report.url") + "/"
					+ GlobalVariables.getHtmlDirectory().substring(GlobalVariables.directoryOffset) + "/"
					+ AlbinaUtil.getNextValidityDateString(bulletins) + "/" + region + "_" + lang.toString() + ".html";
		else
			return messages.getString("avalanche-report.url") + "/"
					+ GlobalVariables.getHtmlDirectory().substring(GlobalVariables.directoryOffset) + "/"
					+ AlbinaUtil.getNextValidityDateString(bulletins) + "/" + lang.toString() + ".html";
	}

	public static String getRegionOverviewMapFilename(String region, boolean isAfternoon) {
		StringBuilder sb = new StringBuilder();
		if (isAfternoon)
			sb.append("pm_");
		else
			sb.append("am_");
		sb.append(getMapFilename(region));
		return sb.toString();
	}

	public static String getRegionOverviewMapFilename(String region) {
		StringBuilder sb = new StringBuilder();
		sb.append("fd_");
		sb.append(getMapFilename(region));
		return sb.toString();
	}

	// REGION
	private static String getMapFilename(String region) {
		StringBuilder sb = new StringBuilder();
		switch (region) {
		case "AT-07":
			sb.append("tyrol");
			break;
		case "IT-32-BZ":
			sb.append("southtyrol");
			break;
		case "IT-32-TN":
			sb.append("trentino");
			break;
		default:
			sb.append("albina");
			break;
		}
		sb.append("_map.jpg");
		return sb.toString();
	}

	public static DateTime getDate(List<AvalancheBulletin> bulletins) {
		DateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			DateTime bulletinDate = avalancheBulletin.getValidFrom();
			if (date == null)
				date = bulletinDate;
			else if (bulletinDate.isAfter(date))
				date = bulletinDate;
		}
		return date;
	}

	private static DateTime getPublicationDate(List<AvalancheBulletin> bulletins) {
		DateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			DateTime bulletinDate = avalancheBulletin.getPublicationDate();
			if (bulletinDate != null) {
				if (date == null)
					date = bulletinDate;
				else if (bulletinDate.isAfter(date))
					date = bulletinDate;
			}
		}
		return date;
	}

	public static String getPublicationDate(List<AvalancheBulletin> bulletins, ResourceBundle messages) {
		DateTime date = getPublicationDate(bulletins);
		if (date != null)
			return date.toString(messages.getString("date-time-format.publication"));
		else
			return "";
	}

	public static String getPublicationTime(List<AvalancheBulletin> bulletins) {
		DateTime date = getPublicationDate(bulletins);
		DateTime utcTime = new DateTime(date, DateTimeZone.UTC);
		if (date != null)
			return utcTime.toString(GlobalVariables.publicationTime);
		else
			return "";
	}

	public static boolean hasDaytimeDependency(List<AvalancheBulletin> bulletins) {
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			if (avalancheBulletin.isHasDaytimeDependency())
				return true;
		}
		return false;
	}

	public static void setFilePermissions(String fileName) throws IOException {
		// using PosixFilePermission to set file permissions 755
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		// add owners permission
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		// add group permissions
		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_WRITE);
		// add others permissions
		perms.add(PosixFilePermission.OTHERS_READ);

		try {
			Files.setPosixFilePermissions(Paths.get(fileName), perms);
		} catch (UnsupportedOperationException | IOException e) {
			logger.debug("File permission could not be set!");
		}
	}

	public static boolean isLatest(DateTime date) {
		DateTime now = new DateTime();

		if (now.getHourOfDay() >= 17) {
			if ((new LocalDate()).plusDays(1).equals(date.toLocalDate())) {
				return true;
			}
		} else {
			if (date.toLocalDate().equals(new LocalDate())) {
				return true;
			}
		}
		return false;
	}

	public static void runUpdateMapsScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateMaps.sh",
					GlobalVariables.getMapsPath(), date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps for {} copied using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Maps could not be copied to directory for " + date + "!", e);
		}
	}

	public static void runCopyMapsUnivieScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyMapsUnivie.sh",
					GlobalVariables.getMapsPath(), date, publicationTime, GlobalVariables.getUnivieMapsPath())
							.inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps for {} copied from Univie using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Maps could not be copied from Univie to directory for " + date + "!", e);
		}
	}

	public static void runUpdateFilesScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateFiles.sh",
					GlobalVariables.getPdfDirectory(), date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("Files updated for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Files could not be deleted for " + date + "!", e);
		}
	}

	public static void runUpdatePdfsScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updatePdfs.sh",
					GlobalVariables.getPdfDirectory(), date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("PDFs updated in date directory for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("PDFs could not be updated in date directory for " + date + "!", e);
		}
	}

	public static void runUpdateLatestPdfsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateLatestPdfs.sh",
					GlobalVariables.getPdfDirectory(), date).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("PDFs for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("PDFs for " + date + " could not be updated in latest directory!", e);
		}
	}

	public static void runUpdateJsonScript(String validityDateString, String publicationTimeString) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateJson.sh",
					GlobalVariables.getPdfDirectory(), validityDateString, publicationTimeString).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("JSON updated in date directory for {} using {}", validityDateString, pb.command());
		} catch (Exception e) {
			logger.error("JSON could not be updated in date directory for " + validityDateString + "!", e);
		}
	}

	public static void runUpdateXmlsScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateXmls.sh",
					GlobalVariables.getPdfDirectory(), date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("XMLs updated in date directory for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("XMLs could not be updated in date directory for " + date + "!", e);
		}
	}

	public static void runUpdateLatestJsonScript(String validityDateString) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateLatestJson.sh",
					GlobalVariables.getPdfDirectory(), validityDateString).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("JSON for {} updated in latest directory using {}", validityDateString, pb.command());
		} catch (Exception e) {
			logger.error("JSON for " + validityDateString + " could not be updated in latest directory!", e);
		}
	}

	public static void runUpdateLatestXmlsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateLatestXmls.sh",
					GlobalVariables.getPdfDirectory(), date).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("XMLs for {} update in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("XMLs for " + date + " could not be updated in latest directory!", e);
		}
	}

	public static void runUpdateStaticWidgetsScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyPngs.sh",
					GlobalVariables.getPdfDirectory(), date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("PNGs updated in date directory for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("PNGs could not be updated in date directory for " + date + "!", e);
		}
	}

	public static void runUpdateLatestStaticWidgetsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					GlobalVariables.scriptsPath + "updateLatestStaticWidgets.sh", GlobalVariables.getPdfDirectory(),
					date).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("PNGs for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("PNGs for " + date + " could not be updated in latest directory!", e);
		}
	}

	public static void runUpdateLatestMapsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateLatestMaps.sh",
					GlobalVariables.getMapsPath(), date).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Maps for " + date + " could not be updated in latest directory!", e);
		}
	}

	public static void runUpdateLatestFilesScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateLatestFiles.sh",
					GlobalVariables.getPdfDirectory(), date, GlobalVariables.getHtmlDirectory()).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("Latest files updated using {}", pb.command());
		} catch (Exception e) {
			logger.error("Latest files could not be updated!", e);
		}
	}

	public static void runUpdateLatestHtmlsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "updateLatestHtmls.sh",
					GlobalVariables.getHtmlDirectory(), date).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("HTMLs for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("HTMLs for " + date + " could not be udpated in latest directory!", e);
		}
	}

	public static double getDangerRatingDouble(DangerRating dangerRating) {
		switch (dangerRating) {
		case missing:
			return .0;
		case no_rating:
			return .0;
		case no_snow:
			return .0;
		case low:
			return 1.0 / 1364;
		case moderate:
			return 1.0 / 1364 * 4.0;
		case considerable:
			return 1.0 / 1364 * 4.0 * 4.0;
		case high:
			return 1.0 / 1364 * 4.0 * 4.0 * 4.0;
		case very_high:
			return 1.0 / 1364 * 4.0 * 4.0 * 4.0 * 4.0;
		default:
			return .0;
		}
	}
}
