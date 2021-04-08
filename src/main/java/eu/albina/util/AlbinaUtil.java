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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import org.joda.time.Period;
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
	private static final ClassLoader classLoader = AlbinaUtil.class.getClassLoader();

	// REGION
	public static final int regionCountTyrol = 29;
	public static final int regionCountSouthTyrol = 20;
	public static final int regionCountTrentino = 21;
	public static final int regionCountAran = 3;

	public static final String greyDarkColor = "#565F61";

	// REGION
	public static int getRegionCount(String region) {
		switch (region) {
		case GlobalVariables.codeTyrol:
			return regionCountTyrol;
		case GlobalVariables.codeSouthTyrol:
			return regionCountSouthTyrol;
		case GlobalVariables.codeTrentino:
			return regionCountTrentino;
		case GlobalVariables.codeAran:
			return regionCountAran;

		default:
			return -1;
		}
	}

	public static String getRegionName(LanguageCode lang, String regionId) {
		if ("".equals(regionId)) {
			return "";
		}
		return lang.getBundle("i18n.Regions").getString(regionId);
	}

	public static String getDaytimeString(LanguageCode lang, String type) {
		return lang.getBundleString("daytime." + type);
	}

	public static String getDangerPatternText(DangerPattern dp, LanguageCode lang) {
		return dp.toString(lang.getLocale());
	}

	public static String getWarningLevelId(AvalancheBulletinDaytimeDescription avalancheBulletinDaytimeDescription) {
		if (avalancheBulletinDaytimeDescription.isHasElevationDependency())
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
			result.append(lang.getBundleString("tendency.binding-word"));
			result.append(lang.getBundleString("day." + date.getDayOfWeek()));
			result.append(date.toString(lang.getBundleString("date-time-format.tendency")));
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

	public static String getDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		StringBuilder result = new StringBuilder();
		DateTime date = getDate(bulletins);
		if (date != null) {
			result.append(lang.getBundleString("day." + date.getDayOfWeek()));
			result.append(" ");
			result.append(date.toString(lang.getBundleString("date-time-format")));
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
			return date.toLocalDate() + "_" + lang.toString();
		else
			return "_" + lang.toString();
	}

	public static String getValidityDateString(List<AvalancheBulletin> bulletins) {
		return getValidityDateString(bulletins, Period.ZERO);
	}

	public static String getValidityDateString(List<AvalancheBulletin> bulletins, Period offset) {
		DateTime date = getValidityDate(bulletins);
		date = date.plus(offset);
		return date.toLocalDate().toString();
	}

	public static String getPreviousValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = getValidityDate(bulletins);
		date = date.minusDays(1);
		return date.toString(lang.getBundleString("date-time-format")).trim();
	}

	public static String getNextValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = getValidityDate(bulletins);
		date = date.plusDays(1);
		return date.toString(lang.getBundleString("date-time-format")).trim();
	}

	private static DateTime getValidityDate(List<AvalancheBulletin> bulletins) {
		DateTime date = getDate(bulletins);
		if (date.getHourOfDay() > 12)
			date = date.plusDays(1);
		return date;
	}

	public static String getBulletinLink(List<AvalancheBulletin> bulletins, LanguageCode lang, String region, Period offset) {
		if (region != null && !region.isEmpty())
			return lang.getBundleString("avalanche-report.url") + "/"
					+ GlobalVariables.getHtmlDirectory().substring(GlobalVariables.directoryOffset) + "/"
					+ AlbinaUtil.getValidityDateString(bulletins, offset) + "/" + region + "_" + lang.toString()
					+ ".html";
		else
			return lang.getBundleString("avalanche-report.url") + "/"
					+ GlobalVariables.getHtmlDirectory().substring(GlobalVariables.directoryOffset) + "/"
					+ AlbinaUtil.getValidityDateString(bulletins, offset) + "/" + lang.toString() + ".html";
	}

	public static String getRegionOverviewMapFilename(String region, boolean isAfternoon, String fileExtension) {
		StringBuilder sb = new StringBuilder();
		if (isAfternoon)
			sb.append("pm_");
		else
			sb.append("am_");
		sb.append(getMapFilename(region, fileExtension));
		return sb.toString();
	}

	public static String getRegionOverviewMapFilename(String region, String fileExtension) {
		StringBuilder sb = new StringBuilder();
		sb.append("fd_");
		sb.append(getMapFilename(region, fileExtension));
		return sb.toString();
	}

	// REGION: only regions supported by map production
	private static String getMapFilename(String region, String fileExtension) {
		StringBuilder sb = new StringBuilder();
		switch (region) {
		case GlobalVariables.codeTyrol:
			sb.append("tyrol");
			break;
		case GlobalVariables.codeSouthTyrol:
			sb.append("southtyrol");
			break;
		case GlobalVariables.codeAran:
			sb.append("trentino");
			break;
		default:
			sb.append("albina");
			break;
		}
		sb.append("_map.");
		sb.append(fileExtension);
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

	public static boolean isUpdate(List<AvalancheBulletin> bulletins) {
		DateTime publicationDate = getPublicationDate(bulletins);
		int time = publicationDate.getSecondOfDay();
		return (time == 61200) ? false : true;
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
			return date.toString(lang.getBundleString("date-time-format.publication"));
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
		return bulletins.stream().anyMatch(AvalancheBulletin::isHasDaytimeDependency);
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
			final File file = new File(classLoader.getResource("scripts/updateMaps.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getMapsPath(),
					date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps for {} copied using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Maps could not be copied to directory for " + date + "!", e);
		}
	}

	public static void runUpdateFilesScript(String date, String publicationTime) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateFiles.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
					date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("Files updated for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Files could not be deleted for " + date + "!", e);
		}
	}

	public static void runUpdatePdfsScript(String date, String publicationTime) {
		try {
			final File file = new File(classLoader.getResource("scripts/updatePdfs.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
					date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("PDFs updated in date directory for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("PDFs could not be updated in date directory for " + date + "!", e);
		}
	}

	public static void runUpdateLatestPdfsScript(String date) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateLatestPdfs.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
					date).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("PDFs for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("PDFs for " + date + " could not be updated in latest directory!", e);
		}
	}

	public static void runUpdateJsonScript(String validityDateString, String publicationTimeString) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateJson.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
					validityDateString, publicationTimeString).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("JSON updated in date directory for {} using {}", validityDateString, pb.command());
		} catch (Exception e) {
			logger.error("JSON could not be updated in date directory for " + validityDateString + "!", e);
		}
	}

	public static void runUpdateXmlsScript(String date, String publicationTime) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateXmls.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
					date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("XMLs updated in date directory for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("XMLs could not be updated in date directory for " + date + "!", e);
		}
	}

	public static void runUpdateLatestJsonScript(String validityDateString) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateLatestJson.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
					validityDateString).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("JSON for {} updated in latest directory using {}", validityDateString, pb.command());
		} catch (Exception e) {
			logger.error("JSON for " + validityDateString + " could not be updated in latest directory!", e);
		}
	}

	public static void runUpdateLatestXmlsScript(String date) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateLatestXmls.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
					date).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("XMLs for {} update in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("XMLs for " + date + " could not be updated in latest directory!", e);
		}
	}

	public static void runUpdateStaticWidgetsScript(String date, String publicationTime) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateStaticWidgets.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
					date, publicationTime).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("PNGs updated in date directory for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("PNGs could not be updated in date directory for " + date + "!", e);
		}
	}

	public static void runUpdateLatestStaticWidgetsScript(String date) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateLatestStaticWidgets.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
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
			final File file = new File(classLoader.getResource("scripts/updateLatestMaps.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getMapsPath(),
					date).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Maps for " + date + " could not be updated in latest directory!", e);
		}
	}

	public static void runUpdateLatestFilesScript(String date) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateLatestFiles.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()), GlobalVariables.getPdfDirectory(),
					date, GlobalVariables.getHtmlDirectory()).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("Latest files updated using {}", pb.command());
		} catch (Exception e) {
			logger.error("Latest files could not be updated!", e);
		}
	}

	public static void runUpdateLatestHtmlsScript(String date) {
		try {
			final File file = new File(classLoader.getResource("scripts/updateLatestHtmls.sh").getFile());
			ProcessBuilder pb = new ProcessBuilder("/bin/sh",
					URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8.name()),
					GlobalVariables.getHtmlDirectory(), date).inheritIO();
			Process p = pb.start();
			p.waitFor();
			logger.info("HTMLs for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("HTMLs for " + date + " could not be udpated in latest directory!", e);
		}
	}

}
