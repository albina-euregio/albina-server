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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.enumerations.Aspect;
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

	public static ZoneId localZone() {
		return ZoneId.of("Europe/Vienna");
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
		ZonedDateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			ZonedDateTime bulletinDate = avalancheBulletin.getValidUntil();
			if (date == null)
				date = bulletinDate;
			else if (bulletinDate.isAfter(date))
				date = bulletinDate;
		}

		if (date != null) {
			date = date.withZoneSameInstant(localZone());
			StringBuilder result = new StringBuilder();
			result.append(lang.getBundleString("tendency.binding-word"));
			result.append(lang.getBundleString("day." + date.getDayOfWeek()));
			result.append(date.format(DateTimeFormatter.ofPattern(lang.getBundleString("date-time-format.tendency"))));
			return result.toString();
		} else {
			return "";
		}
	}

	public static boolean hasBulletinChanged(Instant startDate, String region) throws AlbinaException {
		boolean result = false;
		Map<Instant, BulletinStatus> status = AvalancheReportController.getInstance().getInternalStatus(startDate,
				startDate, region);
		if (status.size() == 1 && status.get(startDate) != BulletinStatus.published
				&& status.get(startDate) != BulletinStatus.republished)
			result = true;
		return result;
	}

	public static Instant getInstantStartOfDay() {
		return LocalDate.now(ZoneId.of("Europe/Vienna")).atStartOfDay(ZoneId.of("Europe/Vienna")).toInstant();
	}

	public static Instant getInstantNowNoNanos() {
		return ZonedDateTime.now().withNano(0).toInstant();
	}

	public static ZonedDateTime getZonedDateTimeNowNoNanos() {
		return AlbinaUtil.getInstantNowNoNanos().atZone(ZoneId.of("UTC"));
	}

	public static ZonedDateTime getZonedDateTimeUtc(Instant instant) {
		return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
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
		ZonedDateTime date = getDate(bulletins);
		if (date != null) {
			date = date.withZoneSameInstant(localZone());
			result.append(lang.getBundleString("day." + date.getDayOfWeek()));
			result.append(" ");
			result.append(date.format(DateTimeFormatter.ofPattern(lang.getBundleString("date-time-format"))));
		} else {
			// TODO what if no date is given (should not happen)
			result.append("-");
		}

		return result.toString();
	}

	public static int getYear(List<AvalancheBulletin> bulletins) {
		return getDate(bulletins).getYear();
	}

	public static String getStaticWidgetFilename(String validityDateString, LanguageCode lang) {
		if (validityDateString != null)
			return validityDateString + "_" + lang.toString();
		else
			return "_" + lang.toString();
	}

	public static String getValidityDateString(List<AvalancheBulletin> bulletins) {
		return getValidityDateString(bulletins, Period.ZERO);
	}

	public static String getValidityDateString(List<AvalancheBulletin> bulletins, Period offset) {
		ZonedDateTime date = getDate(bulletins);
		date = date.withZoneSameInstant(localZone());
		date = date.plus(offset);
		return date.toLocalDate().toString();
	}

	public static String getPreviousValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		ZonedDateTime date = getDate(bulletins);
		date = date.withZoneSameInstant(localZone());
		date = date.minusDays(1);
		return date.format(DateTimeFormatter.ofPattern(lang.getBundleString("date-time-format"))).trim();
	}

	public static String getNextValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		ZonedDateTime date = getDate(bulletins);
		date = date.withZoneSameInstant(localZone());
		date = date.plusDays(1);
		return date.format(DateTimeFormatter.ofPattern(lang.getBundleString("date-time-format"))).trim();
	}

	public static String getBulletinLink(List<AvalancheBulletin> bulletins, LanguageCode lang, String region, Period offset) {
		if (region != null && !region.isEmpty())
			return LinkUtil.getSimpleHtmlUrl(lang) + "/"
					+ AlbinaUtil.getValidityDateString(bulletins, offset) + "/" + region + "_" + lang.toString()
					+ ".html";
		else
			return LinkUtil.getSimpleHtmlUrl(lang) + "/"
					+ AlbinaUtil.getValidityDateString(bulletins, offset) + "/" + lang.toString() + ".html";
	}

	public static ZonedDateTime getDate(List<AvalancheBulletin> bulletins) {
		ZonedDateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			ZonedDateTime bulletinDate = avalancheBulletin.getValidFrom();
			if (date == null)
				date = bulletinDate;
			else if (bulletinDate.isAfter(date))
				date = bulletinDate;
		}
		return date;
	}

	public static boolean isUpdate(List<AvalancheBulletin> bulletins) {
		ZonedDateTime publicationDate = getPublicationDate(bulletins);
		LocalDateTime localDateTime = publicationDate.withZoneSameInstant(localZone()).toLocalDateTime();
		int secondOfDay = localDateTime.toLocalTime().toSecondOfDay();
		return (secondOfDay == 61200) ? false : true;
	}

	private static ZonedDateTime getPublicationDate(List<AvalancheBulletin> bulletins) {
		ZonedDateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			ZonedDateTime bulletinDate = avalancheBulletin.getPublicationDate();
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
		ZonedDateTime date = getPublicationDate(bulletins);
		if (date != null) {
			date = date.withZoneSameInstant(localZone());
			return date.format(DateTimeFormatter.ofPattern(lang.getBundleString("date-time-format.publication")));
		} else
			return "";
	}

	public static String getPublicationTime(List<AvalancheBulletin> bulletins) {
		ZonedDateTime utcTime = getPublicationDate(bulletins);
		if (utcTime != null)
			return utcTime.format(GlobalVariables.formatterPublicationTime);
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

	public static boolean isLatest(ZonedDateTime date) {
		return isLatest(date, Clock.system(localZone()));
	}

	public static boolean isLatest(ZonedDateTime date, Clock clock) {
		date = date.withZoneSameInstant(localZone());
		ZonedDateTime now = ZonedDateTime.now(clock);

		if (now.getHour() >= 17) {
			return date.toLocalDate().equals(now.toLocalDate().plusDays(1));
		} else {
			return date.toLocalDate().equals(now.toLocalDate());
		}
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

	public static String getDangerRatingText(AvalancheBulletinDaytimeDescription daytimeBulletin, LanguageCode lang) {
		String dangerRatingBelow;
		String dangerRatingAbove;
		if (daytimeBulletin.getDangerRatingBelow() == null || daytimeBulletin.getDangerRatingBelow().equals(DangerRating.missing) || daytimeBulletin.getDangerRatingBelow().equals(DangerRating.no_rating) || daytimeBulletin.getDangerRatingBelow().equals(DangerRating.no_snow)) {
			dangerRatingBelow = DangerRating.no_rating.toString(lang.getLocale(), true);
		} else {
			dangerRatingBelow = daytimeBulletin.getDangerRatingBelow().toString(lang.getLocale(), true);
		}
		if (daytimeBulletin.getDangerRatingAbove() == null || daytimeBulletin.getDangerRatingAbove().equals(DangerRating.missing) || daytimeBulletin.getDangerRatingAbove().equals(DangerRating.no_rating) || daytimeBulletin.getDangerRatingAbove().equals(DangerRating.no_snow)) {
			dangerRatingAbove = DangerRating.no_rating.toString(lang.getLocale(), true);
		} else {
			dangerRatingAbove = daytimeBulletin.getDangerRatingAbove().toString(lang.getLocale(), true);
		}

		if (daytimeBulletin.getTreeline()) {
			return MessageFormat.format(lang.getBundleString("danger-rating.elevation"), dangerRatingBelow, lang.getBundleString("elevation.treeline"), dangerRatingAbove, lang.getBundleString("elevation.treeline"));
		} else if (daytimeBulletin.getElevation() > 0) {
			String elevation = daytimeBulletin.getElevation() + lang.getBundleString("unit.meter");
			return MessageFormat.format(lang.getBundleString("danger-rating.elevation"), dangerRatingBelow, elevation, dangerRatingAbove, elevation);
		} else {
			return dangerRatingAbove;
		}
	}

	public static String getElevationString(AvalancheSituation avalancheSituation, LanguageCode lang) {
		if (avalancheSituation.getTreelineHigh() || avalancheSituation.getElevationHigh() > 0) {
			if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
				// elevation high and low set
				String low = "";
				String high = "";
				if (avalancheSituation.getTreelineLow()) {
					// elevation low treeline
					low = lang.getBundleString("elevation.treeline");
				} else if (avalancheSituation.getElevationLow() > 0) {
					// elevation low number
					low = avalancheSituation.getElevationLow() + lang.getBundleString("unit.meter");
				}
				if (avalancheSituation.getTreelineHigh()) {
					// elevation high treeline
					high = lang.getBundleString("elevation.treeline");
				} else if (avalancheSituation.getElevationHigh() > 0) {
					// elevation high number
					high = avalancheSituation.getElevationHigh() + lang.getBundleString("unit.meter");
				}
				return MessageFormat.format(lang.getBundleString("elevation.band"), low, high);
			} else {
				// elevation high set
				String high = "";
				if (avalancheSituation.getTreelineHigh()) {
					// elevation high treeline
					high = lang.getBundleString("elevation.treeline");
				} else if (avalancheSituation.getElevationHigh() > 0) {
					// elevation high number
					high = avalancheSituation.getElevationHigh() + lang.getBundleString("unit.meter");
				}
				return MessageFormat.format(lang.getBundleString("elevation.below"), high);
			}
		} else if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
			// elevation low set
			String low = "";
			if (avalancheSituation.getTreelineLow()) {
				// elevation low treeline
				low = lang.getBundleString("elevation.treeline");
			} else if (avalancheSituation.getElevationLow() > 0) {
				// elevation low number
				low = avalancheSituation.getElevationLow() + lang.getBundleString("unit.meter");
			}
			return MessageFormat.format(lang.getBundleString("elevation.above"), low);
		} else {
			// no elevation set
			return lang.getBundleString("elevation.all");
		}
	}

    public static String getAspectString(Set<Aspect> aspects, Locale locale) {
		StringJoiner aspectString = new StringJoiner(", ");
		for (Aspect aspect : aspects) {
			aspectString.add(aspect.toString(locale));
		}
		return aspectString.toString();
    }

}
