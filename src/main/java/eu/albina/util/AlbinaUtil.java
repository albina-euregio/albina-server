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
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.ServerInstanceController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.User;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public interface AlbinaUtil {

	Logger logger = LoggerFactory.getLogger(AlbinaUtil.class);
	String greyDarkColor = "#565F61";
	DateTimeFormatter formatterPublicationTime = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.of("UTC"));

	static ZoneId localZone() {
		return ZoneId.of("Europe/Vienna");
	}

	static String getWarningLevelId(AvalancheBulletinDaytimeDescription avalancheBulletinDaytimeDescription) {
		if (avalancheBulletinDaytimeDescription.isHasElevationDependency())
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingBelow()) + "_"
					+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
		else
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove()) + "_"
					+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
	}

	static String getTendencyDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		ZonedDateTime date = bulletins.stream()
			.map(AvalancheBulletin::getValidUntil)
			.filter(Objects::nonNull)
			.max(Comparator.naturalOrder())
			.orElse(null);
		if (date == null) {
			return "";
		}
		date = date.withZoneSameInstant(localZone());
		return lang.getTendencyDate(date);
	}

	static Instant getInstantNowNoNanos() {
		return ZonedDateTime.now().withNano(0).toInstant();
	}

	static ZonedDateTime getZonedDateTimeNowNoNanos() {
		return AlbinaUtil.getInstantNowNoNanos().atZone(ZoneId.of("UTC"));
	}

	static ZonedDateTime getZonedDateTimeUtc(Instant instant) {
		return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
	}

	static String encodeFileToBase64Binary(File file) {
		String encodedfile = null;
		try {
			FileInputStream fileInputStreamReader = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			fileInputStreamReader.read(bytes);
			encodedfile = Base64.getEncoder().encodeToString(bytes);
			fileInputStreamReader.close();
		} catch (IOException e) {
			logger.error("Failed to encode to base64", e);
		}

		return encodedfile;
	}

	static String getDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		ZonedDateTime date = getDate(bulletins);
		if (date == null) {
			// TODO what if no date is given (should not happen)
			return "-";
		}
		return getDate(date, lang);
	}

	static String getDate(ZonedDateTime date, LanguageCode lang) {
		date = date.withZoneSameInstant(localZone());
		return String.format("%s %s",
			lang.getBundleString("day." + date.getDayOfWeek()),
			date.format(lang.getFormatter()));
	}

	static int getYear(List<AvalancheBulletin> bulletins) {
		return getDate(bulletins).getYear();
	}

	static String getValidityDateString(List<AvalancheBulletin> bulletins) {
		return getValidityDateString(bulletins, Period.ZERO);
	}

	static String getValidityDateString(List<AvalancheBulletin> bulletins, Period offset) {
		ZonedDateTime date = getDate(bulletins);
		date = date.withZoneSameInstant(localZone());
		date = date.plus(offset);
		return date.toLocalDate().toString();
	}

	static String getPreviousValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		ZonedDateTime date = getDate(bulletins);
		date = date.withZoneSameInstant(localZone());
		date = date.minusDays(1);
		return date.format(lang.getFormatter());
	}

	static String getNextValidityDateString(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		ZonedDateTime date = getDate(bulletins);
		date = date.withZoneSameInstant(localZone());
		date = date.plusDays(1);
		return date.format(lang.getFormatter());
	}

	static String getBulletinLink(List<AvalancheBulletin> bulletins, LanguageCode lang, Region region, Period offset, ServerInstance serverInstance) {
		if (region != null && !region.getId().isEmpty())
			return LinkUtil.getSimpleHtmlUrl(lang, region, serverInstance) + "/"
					+ AlbinaUtil.getValidityDateString(bulletins, offset) + "/" + region.getId() + "_" + lang.toString()
					+ ".html";
		else
			return LinkUtil.getSimpleHtmlUrl(lang, region, serverInstance) + "/"
					+ AlbinaUtil.getValidityDateString(bulletins, offset) + "/" + lang.toString() + ".html";
	}

	static ZonedDateTime getDate(List<AvalancheBulletin> bulletins) {
		return bulletins.stream()
			.map(AvalancheBulletin::getValidFrom)
			.filter(Objects::nonNull)
			.max(Comparator.naturalOrder())
			.orElse(null);
	}

	static boolean isUpdate(List<AvalancheBulletin> bulletins) {
		ZonedDateTime publicationDate = getPublicationDate(bulletins);
		LocalDateTime localDateTime = publicationDate.withZoneSameInstant(localZone()).toLocalDateTime();
		return !LocalTime.of(17, 0).equals(localDateTime.toLocalTime());
	}

	static ZonedDateTime getPublicationDate(List<AvalancheBulletin> bulletins) {
		return bulletins.stream()
			.map(AvalancheBulletin::getPublicationDate)
			.filter(Objects::nonNull)
			.max(Comparator.naturalOrder())
			.orElse(null);

	}

	static String getPublicationDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		ZonedDateTime date = getPublicationDate(bulletins);
		if (date != null) {
			date = date.withZoneSameInstant(localZone());
			return date.format(DateTimeFormatter.ofPattern(lang.getBundleString("date-time-format.publication")));
		} else
			return "";
	}

	static String getPublicationTime(List<AvalancheBulletin> bulletins) {
		ZonedDateTime utcTime = getPublicationDate(bulletins);
		if (utcTime != null)
			return utcTime.format(formatterPublicationTime);
		else
			return "";
	}

	static String getPublicationTime(Instant publicationTime) {
		return publicationTime.atZone(ZoneId.of("UTC")).format(formatterPublicationTime);
	}

	static void setFilePermissions(String fileName) throws IOException {
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

	static boolean isLatest(ZonedDateTime date) {
		return isLatest(date, Clock.system(localZone()));
	}

	static boolean isLatest(ZonedDateTime date, Clock clock) {
		date = date.withZoneSameInstant(localZone());
		ZonedDateTime now = ZonedDateTime.now(clock);

		if (now.getHour() >= 17) {
			return date.toLocalDate().equals(now.toLocalDate().plusDays(1));
		} else {
			return date.toLocalDate().equals(now.toLocalDate());
		}
	}

	static void runUpdateMapsScript(String date, String publicationTime) {
		try {
			String path = getScriptPath("scripts/updateMaps.sh");
			ProcessBuilder pb;
			if (SystemUtils.IS_OS_WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/C",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getMapsPath(),
						date, publicationTime).inheritIO();
			} else {
				pb = new ProcessBuilder("/bin/sh",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getMapsPath(),
						date, publicationTime).inheritIO();
			}
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps for {} copied using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Maps could not be copied to directory for " + date + "!", e);
		}
	}

	static void runUpdateFilesScript(String date, String publicationTime) {
		try {
			String path = getScriptPath("scripts/updateFiles.sh");
			ProcessBuilder pb;
			if (SystemUtils.IS_OS_WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/C",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date, publicationTime, ServerInstanceController.getInstance().getLocalServerInstance().getHtmlDirectory()).inheritIO();
			} else {
				pb = new ProcessBuilder("/bin/sh",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date, publicationTime, ServerInstanceController.getInstance().getLocalServerInstance().getHtmlDirectory()).inheritIO();
			}
			Process p = pb.start();
			p.waitFor();
			logger.info("Files updated for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Files could not be deleted for " + date + "!", e);
		}
	}

	static void runUpdatePdfsScript(String date, String publicationTime) {
		try {
			String path = getScriptPath("scripts/updatePdfs.sh");
			ProcessBuilder pb;
			if (SystemUtils.IS_OS_WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/C",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date, publicationTime).inheritIO();
			} else {
				pb = new ProcessBuilder("/bin/sh",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date, publicationTime).inheritIO();
			}
			Process p = pb.start();
			p.waitFor();
			logger.info("PDFs updated in date directory for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("PDFs could not be updated in date directory for " + date + "!", e);
		}
	}

	static void runUpdateLatestPdfsScript(String date) {
		try {
			String path = getScriptPath("scripts/updateLatestPdfs.sh");
			ProcessBuilder pb;
			if (SystemUtils.IS_OS_WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/C",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date).inheritIO();
			} else {
				pb = new ProcessBuilder("/bin/sh",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date).inheritIO();
			}
			Process p = pb.start();
			p.waitFor();
			logger.info("PDFs for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("PDFs for " + date + " could not be updated in latest directory!", e);
		}
	}

	static void runUpdateCaamlsScript(String date, String publicationTime) {
		try {
			String path = getScriptPath("scripts/updateCaamls.sh");
			ProcessBuilder pb;
			if (SystemUtils.IS_OS_WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/C",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date, publicationTime).inheritIO();
			} else {
				pb = new ProcessBuilder("/bin/sh",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date, publicationTime).inheritIO();
			}
			Process p = pb.start();
			p.waitFor();
			logger.info("CAAMLs updated in date directory for {} using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("CAAMLs could not be updated in date directory for " + date + "!", e);
		}
	}

	static void runUpdateLatestCaamlsScript(String date) {
		try {
			String path = getScriptPath("scripts/updateLatestCaamls.sh");
			ProcessBuilder pb;
			if (SystemUtils.IS_OS_WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/C",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date).inheritIO();
			} else {
				pb = new ProcessBuilder("/bin/sh",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date).inheritIO();
			}
			Process p = pb.start();
			p.waitFor();
			logger.info("CAAMLs for region {} for {} update in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("CAAMLs for " + date + " could not be updated in latest directory!", e);
		}
	}

	static void runUpdateLatestMapsScript(String date) {
		try {
			String path = getScriptPath("scripts/updateLatestMaps.sh");
			ProcessBuilder pb;
			if (SystemUtils.IS_OS_WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/C",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getMapsPath(),
					date).inheritIO();
			} else {
				pb = new ProcessBuilder("/bin/sh",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getMapsPath(),
					date).inheritIO();
			}
			Process p = pb.start();
			p.waitFor();
			logger.info("Maps for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("Maps for " + date + " could not be updated in latest directory!", e);
		}
	}

	static void runUpdateLatestFilesScript(String date) {
		try {
			String path = getScriptPath("scripts/updateLatestFiles.sh");
			ProcessBuilder pb;
			if (SystemUtils.IS_OS_WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/C",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date, ServerInstanceController.getInstance().getLocalServerInstance().getHtmlDirectory()).inheritIO();
			} else {
				pb = new ProcessBuilder("/bin/sh",
					path, ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory(),
					date, ServerInstanceController.getInstance().getLocalServerInstance().getHtmlDirectory()).inheritIO();
			}
			Process p = pb.start();
			p.waitFor();
			logger.info("Latest files updated using {}", pb.command());
		} catch (Exception e) {
			logger.error("Latest files could not be updated!", e);
		}
	}

	static void runUpdateLatestHtmlsScript(String date) {
		try {
			String path = getScriptPath("scripts/updateLatestHtmls.sh");
			ProcessBuilder pb;
			if (SystemUtils.IS_OS_WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/C",
					path,
					ServerInstanceController.getInstance().getLocalServerInstance().getHtmlDirectory(), date).inheritIO();
			} else {
				pb = new ProcessBuilder("/bin/sh",
					path,
					ServerInstanceController.getInstance().getLocalServerInstance().getHtmlDirectory(), date).inheritIO();
			}
			Process p = pb.start();
			p.waitFor();
			logger.info("HTMLs for {} updated in latest directory using {}", date, pb.command());
		} catch (Exception e) {
			logger.error("HTMLs for " + date + " could not be udpated in latest directory!", e);
		}
	}

	static String getScriptPath(String name) {
		URL resource = AlbinaUtil.class.getClassLoader().getResource(name);
		File file = new File(resource.getFile());
        return URLDecoder.decode(file.getPath(), StandardCharsets.UTF_8);
	}

	static String getDangerRatingText(AvalancheBulletinDaytimeDescription daytimeBulletin, LanguageCode lang) {
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

	static String getElevationString(AvalancheProblem avalancheProblem, LanguageCode lang) {
		if (avalancheProblem.getTreelineHigh() || avalancheProblem.getElevationHigh() > 0) {
			if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				// elevation high and low set
				String low = "";
				String high = "";
				if (avalancheProblem.getTreelineLow()) {
					// elevation low treeline
					low = lang.getBundleString("elevation.treeline");
				} else if (avalancheProblem.getElevationLow() > 0) {
					// elevation low number
					low = avalancheProblem.getElevationLow() + lang.getBundleString("unit.meter");
				}
				if (avalancheProblem.getTreelineHigh()) {
					// elevation high treeline
					high = lang.getBundleString("elevation.treeline");
				} else if (avalancheProblem.getElevationHigh() > 0) {
					// elevation high number
					high = avalancheProblem.getElevationHigh() + lang.getBundleString("unit.meter");
				}
				return MessageFormat.format(lang.getBundleString("elevation.band"), low, high);
			} else {
				// elevation high set
				String high = "";
				if (avalancheProblem.getTreelineHigh()) {
					// elevation high treeline
					high = lang.getBundleString("elevation.treeline");
				} else if (avalancheProblem.getElevationHigh() > 0) {
					// elevation high number
					high = avalancheProblem.getElevationHigh() + lang.getBundleString("unit.meter");
				}
				return MessageFormat.format(lang.getBundleString("elevation.below"), high);
			}
		} else if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
			// elevation low set
			String low = "";
			if (avalancheProblem.getTreelineLow()) {
				// elevation low treeline
				low = lang.getBundleString("elevation.treeline");
			} else if (avalancheProblem.getElevationLow() > 0) {
				// elevation low number
				low = avalancheProblem.getElevationLow() + lang.getBundleString("unit.meter");
			}
			return MessageFormat.format(lang.getBundleString("elevation.above"), low);
		} else {
			// no elevation set
			return lang.getBundleString("elevation.all");
		}
	}

    static String getAspectString(Set<Aspect> aspects, Locale locale) {
		StringJoiner aspectString = new StringJoiner(", ");
		for (Aspect aspect : aspects) {
			aspectString.add(aspect.toString(locale));
		}
		return aspectString.toString();
    }

    static String getMediaFileName(String date, User user, LanguageCode language, String fileExtension) {
		String stringDate = OffsetDateTime.parse(date).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return stringDate + "_" + language.getBundleString("media-file.name") + "_" + user.getName().toLowerCase().replace(" ", "-") + fileExtension;
    }

}
