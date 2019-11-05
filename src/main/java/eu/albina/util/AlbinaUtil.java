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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public static final String dangerRatingColorLow = "#CCFF66";
	public static final String dangerRatingColorModerate = "#FFFF00";
	public static final String dangerRatingColorConsiderable = "#FF9900";
	public static final String dangerRatingColorHigh = "#FF0000";
	public static final String dangerRatingColorVeryHigh = "#800000";
	public static final String dangerRatingColorMissing = "#969696";
	public static final String greyDarkColor = "#565F61";

	// LANG
	public static final String dp1De = "gm 1: bodennahe schwachschicht vom frühwinter";
	public static final String dp2De = "gm 2: gleitschnee";
	public static final String dp3De = "gm 3: regen";
	public static final String dp4De = "gm 4: kalt auf warm / warm auf kalt";
	public static final String dp5De = "gm 5: schnee nach langer kälteperiode";
	public static final String dp6De = "gm 6: lockerer schnee und wind";
	public static final String dp7De = "gm 7: schneearm neben schneereich";
	public static final String dp8De = "gm 8: eingeschneiter oberflächenreif";
	public static final String dp9De = "gm 9: eingeschneiter graupel";
	public static final String dp10De = "gm 10: frühjahrssituation";

	// LANG
	public static final String dp1It = "st 1: la seconda nevicata";
	public static final String dp2It = "st 2: valanga per scivolamento di neve";
	public static final String dp3It = "st 3: pioggia";
	public static final String dp4It = "st 4: freddo su caldo / caldo su freddo";
	public static final String dp5It = "st 5: neve dopo un lungo periodo di freddo";
	public static final String dp6It = "st 6: Neve fresca fredda a debole coesione e vento";
	public static final String dp7It = "st 7: zone con poca neve durante inverni ricchi di neve";
	public static final String dp8It = "st 8: brina di superficie sepolta";
	public static final String dp9It = "st 9: neve pallottolare coperta da neve fresca";
	public static final String dp10It = "st 10: situazione primaverile";

	// LANG
	public static final String dp1En = "dp 1: deep persistent weak layer";
	public static final String dp2En = "dp 2: gliding snow";
	public static final String dp3En = "dp 3: rain";
	public static final String dp4En = "dp 4: cold following warm / warm following cold";
	public static final String dp5En = "dp 5: snowfall after a long period of cold";
	public static final String dp6En = "dp 6: cold, loose snow and wind";
	public static final String dp7En = "dp 7: snow-poor zones in snow-rich surrounding";
	public static final String dp8En = "dp 8: surface hoar blanketed with snow";
	public static final String dp9En = "dp 9: graupel blanketed with snow";
	public static final String dp10En = "dp 10: springtime scenario";

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

	public static String getDangerPatternText(DangerPattern dp, LanguageCode lang) {
		switch (dp) {
		case dp1:
			switch (lang) {
			case en:
				return dp1En;
			case de:
				return dp1De;
			case it:
				return dp1It;
			default:
				return dp1En;
			}
		case dp2:
			switch (lang) {
			case en:
				return dp2En;
			case de:
				return dp2De;
			case it:
				return dp2It;
			default:
				return dp2En;
			}
		case dp3:
			switch (lang) {
			case en:
				return dp3En;
			case de:
				return dp3De;
			case it:
				return dp3It;
			default:
				return dp3En;
			}
		case dp4:
			switch (lang) {
			case en:
				return dp4En;
			case de:
				return dp4De;
			case it:
				return dp4It;
			default:
				return dp4En;
			}
		case dp5:
			switch (lang) {
			case en:
				return dp5En;
			case de:
				return dp5De;
			case it:
				return dp5It;
			default:
				return dp5En;
			}
		case dp6:
			switch (lang) {
			case en:
				return dp6En;
			case de:
				return dp6De;
			case it:
				return dp6It;
			default:
				return dp6En;
			}
		case dp7:
			switch (lang) {
			case en:
				return dp7En;
			case de:
				return dp7De;
			case it:
				return dp7It;
			default:
				return dp7En;
			}
		case dp8:
			switch (lang) {
			case en:
				return dp8En;
			case de:
				return dp8De;
			case it:
				return dp8It;
			default:
				return dp8En;
			}
		case dp9:
			switch (lang) {
			case en:
				return dp9En;
			case de:
				return dp9De;
			case it:
				return dp9It;
			default:
				return dp9En;
			}
		case dp10:
			switch (lang) {
			case en:
				return dp10En;
			case de:
				return dp10De;
			case it:
				return dp10It;
			default:
				return dp10En;
			}
		default:
			return null;
		}
	}

	public static String getDangerRatingColor(DangerRating dangerRating) {
		switch (dangerRating) {
		case low:
			return dangerRatingColorLow;
		case moderate:
			return dangerRatingColorModerate;
		case considerable:
			return dangerRatingColorConsiderable;
		case high:
			return dangerRatingColorHigh;
		case very_high:
			return dangerRatingColorVeryHigh;
		default:
			return dangerRatingColorMissing;
		}
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

	public static String getTendencyDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
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
			result.append(GlobalVariables.getTendencyBindingWord(lang));
			result.append(GlobalVariables.getDayName(date.getDayOfWeek(), lang));
			result.append(date.toString(GlobalVariables.getTendencyDateTimeFormatter(lang)));
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
			encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
			fileInputStreamReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return encodedfile;
	}

	public static String getDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		StringBuilder result = new StringBuilder();
		DateTime date = getDate(bulletins);
		if (date != null) {
			result.append(GlobalVariables.getDayName(date.getDayOfWeek(), lang));
			result.append(date.toString(GlobalVariables.getDateTimeFormatter(lang)));
		} else {
			// TODO what if no date is given (should not happen)
			result.append("-");
		}

		return result.toString();
	}

	public static String getShortDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		StringBuilder result = new StringBuilder();
		DateTime date = getDate(bulletins);
		if (date != null) {
			result.append(GlobalVariables.getDayName(date.getDayOfWeek(), lang));
			result.append(date.toString(GlobalVariables.getShortDateTimeFormatter(lang)));
		} else {
			// TODO what if no date is given (should not happen)
			result.append("-");
		}

		return result.toString();
	}

	public static int getYear(List<AvalancheBulletin> bulletins, LanguageCode lang) {
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

	public static String getPreviousValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = getValidityDate(bulletins);
		date = date.minusDays(1);
		return date.toString(GlobalVariables.getPublicationDateTimeFormatter(lang));
	}

	public static String getNextValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = getValidityDate(bulletins);
		date = date.plusDays(1);
		return date.toString(GlobalVariables.getPublicationDateTimeFormatter(lang));
	}

	private static DateTime getValidityDate(List<AvalancheBulletin> bulletins) {
		DateTime date = getDate(bulletins);
		if (date.getHourOfDay() > 12)
			date = date.plusDays(1);
		return date;
	}

	public static String getPreviousDayLink(List<AvalancheBulletin> bulletins, LanguageCode lang, String region) {
		if (region != null && !region.isEmpty())
			return GlobalVariables.simpleBulletinBaseUrl + AlbinaUtil.getPreviousValidityDateString(bulletins) + "/"
					+ region + "_" + lang.toString() + ".html";
		else
			return GlobalVariables.simpleBulletinBaseUrl + AlbinaUtil.getPreviousValidityDateString(bulletins) + "/"
					+ lang.toString() + ".html";
	}

	public static String getNextDayLink(List<AvalancheBulletin> bulletins, LanguageCode lang, String region) {
		if (region != null && !region.isEmpty())
			return GlobalVariables.simpleBulletinBaseUrl + AlbinaUtil.getNextValidityDateString(bulletins) + "/"
					+ region + "_" + lang.toString() + ".html";
		else
			return GlobalVariables.simpleBulletinBaseUrl + AlbinaUtil.getNextValidityDateString(bulletins) + "/"
					+ lang.toString() + ".html";
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

	public static String getPublicationDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = getPublicationDate(bulletins);
		if (date != null)
			return date.toString(GlobalVariables.getPublicationDateTimeFormatter(lang));
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

	public static void runCopyMapsScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyMaps.sh",
					GlobalVariables.getMapsPath(), date, publicationTime);
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps copied to directory for " + date + ".");
		} catch (Exception e) {
			logger.error("Maps could not be copied to directory for " + date + "!");
			e.printStackTrace();
		}
	}

	public static void runDeleteFilesScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "deleteFiles.sh",
					GlobalVariables.getPdfDirectory(), date);
			Process p = pb.start();
			p.waitFor();
			logger.info("Files deleted for " + date + ".");
		} catch (Exception e) {
			logger.error("Files could not be deleted for " + date + "!");
			e.printStackTrace();
		}
	}

	public static void runCopyPdfsScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyPdfs.sh",
					GlobalVariables.getPdfDirectory(), date, publicationTime);
			Process p = pb.start();
			p.waitFor();
			logger.info("PDFs copied to date directory for " + date + ".");
		} catch (Exception e) {
			logger.error("PDFs could not be copied to date directory for " + date + "!");
			e.printStackTrace();
		}
	}

	public static void runCopyLatestPdfsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyLatestPdfs.sh",
					GlobalVariables.getPdfDirectory(), date);
			Process p = pb.start();
			p.waitFor();
			logger.info("PDFs for " + date + " copied to latest.");
		} catch (Exception e) {
			logger.error("PDFs for " + date + " could not be copied to latest!");
			e.printStackTrace();
		}
	}

	public static void runCopyLatestHtmlsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyLatestHtmls.sh",
					GlobalVariables.getPdfDirectory(), date);
			Process p = pb.start();
			p.waitFor();
			logger.info("HTMLs for " + date + " copied to latest.");
		} catch (Exception e) {
			logger.error("HTMLs for " + date + " could not be copied to latest!");
			e.printStackTrace();
		}
	}

	public static void runCopyXmlsScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyXmls.sh",
					GlobalVariables.getPdfDirectory(), date, publicationTime);
			Process p = pb.start();
			p.waitFor();
			logger.info("XMLs copied to date directory for " + date + ".");
		} catch (Exception e) {
			logger.error("XMLs could not be copied to date directory for " + date + "!");
			e.printStackTrace();
		}
	}

	public static void runCopyLatestXmlsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyLatestXmls.sh",
					GlobalVariables.getPdfDirectory(), date);
			Process p = pb.start();
			p.waitFor();
			logger.info("XMLs for " + date + " copied to latest.");
		} catch (Exception e) {
			logger.error("XMLs for " + date + " could not be copied to latest!");
			e.printStackTrace();
		}
	}

	public static void runCopyPngsScript(String date, String publicationTime) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyPngs.sh",
					GlobalVariables.getPdfDirectory(), date, publicationTime);
			Process p = pb.start();
			p.waitFor();
			logger.info("PNGs copied to date directory for " + date + ".");
		} catch (Exception e) {
			logger.error("PNGs could not be copied to date directory for " + date + "!");
			e.printStackTrace();
		}
	}

	public static void runCopyLatestPngsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyLatestPngs.sh",
					GlobalVariables.getPdfDirectory(), date);
			Process p = pb.start();
			p.waitFor();
			logger.info("PNGs for " + date + " copied to latest.");
		} catch (Exception e) {
			logger.error("PNGs for " + date + " could not be copied to latest!");
			e.printStackTrace();
		}
	}

	public static void runCopyLatestMapsScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "copyLatestMaps.sh",
					GlobalVariables.getMapsPath(), GlobalVariables.getMapsPath(), date);
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps for " + date + " copied to latest.");
		} catch (Exception e) {
			logger.error("Maps for " + date + " could not be copied to latest!");
			e.printStackTrace();
		}
	}

	public static void runDeleteLatestFilesScript(String date) {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "deleteLatestFiles.sh",
					GlobalVariables.getPdfDirectory());
			Process p = pb.start();
			p.waitFor();
			logger.info("Latest files deleted.");
		} catch (Exception e) {
			logger.error("Latest files could not be deleted!");
			e.printStackTrace();
		}
	}

	public static void runDeleteLatestHtmlsScript() {
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", GlobalVariables.scriptsPath + "deleteLatestHtmls.sh",
					GlobalVariables.getHtmlDirectory());
			Process p = pb.start();
			p.waitFor();
			logger.info("Latest htmls deleted.");
		} catch (Exception e) {
			logger.error("Latest htmls could not be deleted!");
			e.printStackTrace();
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
