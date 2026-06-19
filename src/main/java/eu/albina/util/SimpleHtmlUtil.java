// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Period;
import java.util.List;

import eu.albina.model.AvalancheReport;
import eu.albina.map.MapImageFormat;
import eu.albina.map.MapUtil;
import eu.albina.model.enumerations.DaytimeDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.LanguageCode;

public interface SimpleHtmlUtil {

	Logger logger = LoggerFactory.getLogger(SimpleHtmlUtil.class);

	static void createRegionSimpleHtml(AvalancheReport avalancheReport) {
		if (avalancheReport.getBulletins().isEmpty()) {
			return;
		}
		for (LanguageCode lang : avalancheReport.getRegion().getEnabledLanguages()) {
			createSimpleHtml(avalancheReport, lang);
		}
	}

	static void createSimpleHtml(AvalancheReport avalancheReport, LanguageCode lang) {
		try {
			String simpleHtmlString = createSimpleHtmlString(avalancheReport, lang);
			String filename = avalancheReport.getRegion().getId() + "_" + lang.toString() + ".html";
			Path dirPath = avalancheReport.getHtmlDirectory();
			Files.createDirectories(dirPath);
			Path newHtmlFile = dirPath.resolve(filename);
			Files.writeString(newHtmlFile, simpleHtmlString, StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("Simple html could not be created", e);
		}
	}

	static String createSimpleHtmlString(AvalancheReport avalancheReport, LanguageCode lang) {
		Region region = avalancheReport.getRegion();
		String publicationDate = avalancheReport.getPublicationDate(lang);
		String prefix = avalancheReport.getSimpleHtmlUrl() + "/"
			+ avalancheReport.getValidityDateString() + "/" + region.getId();
		String website = region.getWebsiteUrlWithDate(lang, avalancheReport);
		String previousDayLink = String.format("%s/%s/%s_%s.html", avalancheReport.getSimpleHtmlUrl(), avalancheReport.getValidityDateString(Period.ofDays(-1)), region.getId(), lang);
		String nextDayLink = String.format("%s/%s/%s_%s.html", avalancheReport.getSimpleHtmlUrl(), avalancheReport.getValidityDateString(Period.ofDays(1)), region.getId(), lang);

		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>\n");
		sb.append("<html lang=\"en\">\n");
		sb.append("<head>\n");
		sb.append("<title>").append(region.getWebsiteName(lang)).append(" ").append(avalancheReport.getDate(lang)).append("\n");
		sb.append("</title>\n");
		sb.append("<meta charset=\"utf-8\">\n");
		sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
		sb.append("<style>*{box-sizing: border-box;}body{font-family: Open Sans, Arial, Helvetica, sans-serif;}header{background-color: #FFF; padding: 10px; text-align: center; font-size: 35px; color: #19abff;}a{color: #19abff; margin: 0px; padding: 0px;}h3{padding: 0px; margin: 0px;}h4{padding-top: 20px; padding: 0px; margin: 0px;}h5{padding: 0px; margin: 0px; color: #000000; font-size: large; font-weight: normal;}article{padding: 20px; width: 100%; background-color: #f2f7fa; margin: 10px 0px;}rating1{background-color: #ccff66; color: #000000; font-weight: bold;}rating2{background-color: #ffff00; color: #000000; font-weight: bold;}rating3{color: #ff9900; font-weight: bold;}rating4{color: #ff0000; font-weight: bold;}rating5{color: #ff0000; font-weight: bold;}.day-link{font-size: large; font-weight: normal; margin: 0; padding: 10px 0;}.top-link{font-size: large; font-weight: normal; margin: 0; padding: 10px 0;}.headline-small{padding-top: 20px;}.headline-small-a{text-decoration: none; text-transform: uppercase; font-size: x-large;}.previous-day{float: left;}.next-day{float: right;}section:after{content: \"\"; display: table; clear: both;}\n");
		sb.append("</style>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");
		sb.append("<header>\n");
		sb.append("<p class=\"top-link\">\n");
		sb.append("<a class=\"previous-day\" href=\"").append(website).append("\">").append(lang.getBundleString("standard.link.text")).append("\n");
		sb.append("</a>\n");
		sb.append("<a class=\"next-day\" href=\"").append(prefix).append("_de.html\">DE&nbsp;\n");
		sb.append("</a>\n");
		sb.append("<a class=\"next-day\" href=\"").append(prefix).append("_it.html\">IT&nbsp;\n");
		sb.append("</a>\n");
		sb.append("<a class=\"next-day\" href=\"").append(prefix).append("_en.html\">EN&nbsp;\n");
		sb.append("</a>\n");
		sb.append("<a class=\"next-day\" href=\"").append(prefix).append("_es.html\">ES&nbsp;\n");
		sb.append("</a>\n");
		sb.append("<a class=\"next-day\" href=\"").append(prefix).append("_ca.html\">CA&nbsp;\n");
		sb.append("</a>\n");
		sb.append("<a class=\"next-day\" href=\"").append(prefix).append("_ar.html\">AR&nbsp;\n");
		sb.append("</a>\n");
		sb.append("</p>\n");
		sb.append("<h4>\n");
		sb.append("<a href=\"").append(website).append("\">").append(region.getWebsiteName(lang)).append("\n");
		sb.append("</a>\n");
		sb.append("</h4>\n");
		sb.append("<h3>").append(avalancheReport.getDate(lang)).append("\n");
		sb.append("</h3>\n");
		sb.append("<h5>").append(publicationDate.isEmpty() ? "" : lang.getBundleString("published")).append("<b>").append(publicationDate).append("</b></h5>\n");
		sb.append("<p class=\"day-link\">\n");
		sb.append("<a class=\"previous-day\" href=\"").append(previousDayLink).append("\"> &#8592; ").append(avalancheReport.getPreviousValidityDateString(lang)).append("\n");
		sb.append("</a>\n");
		sb.append("<a class=\"next-day\" href=\"").append(nextDayLink).append("\">").append(avalancheReport.getNextValidityDateString(lang)).append(" &#8594;\n");
		sb.append("</a>\n");
		sb.append("</p>\n");
		sb.append("</header>\n");
		sb.append("<section>\n");

		for (AvalancheBulletin bulletin : avalancheReport.getBulletins()) {
			if (bulletin.getPublishedRegions() == null || bulletin.getPublishedRegions().isEmpty()) {
				continue;
			}
			appendBulletin(sb, bulletin, lang, region, avalancheReport);
		}

		sb.append("</section>\n</body>\n</html>\n");
		return sb.toString();
	}

	private static void appendBulletin(StringBuilder sb, AvalancheBulletin bulletin, LanguageCode lang, Region region,
			AvalancheReport avalancheReport) {
		String mapsUrl = avalancheReport.getMapsUrl();
		String mapAMjpg, mapAMwebp, mapPMjpg, mapPMwebp, heightPMSmall, fontSize, am, pm;
		DaytimeView forenoon = buildDaytime(bulletin.getForenoon(), lang, region);
		DaytimeView afternoon;
		if (bulletin.isHasDaytimeDependency()) {
			mapAMjpg = mapsUrl + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.am, false, MapImageFormat.jpg);
			mapAMwebp = mapsUrl + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.am, false, MapImageFormat.webp);
			mapPMjpg = mapsUrl + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.pm, false, MapImageFormat.jpg);
			mapPMwebp = mapsUrl + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.pm, false, MapImageFormat.webp);
			heightPMSmall = "height=\"50\"";
			fontSize = "";
			am = "<b>" + lang.getBundleString("valid-time-period.earlier").toUpperCase() + "</b><br>";
			pm = "<b>" + lang.getBundleString("valid-time-period.later").toUpperCase() + "</b><br>";
			afternoon = buildDaytime(bulletin.getAfternoon(), lang, region);
		} else {
			mapAMjpg = mapsUrl + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.fd, false, MapImageFormat.jpg);
			mapAMwebp = mapsUrl + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.fd, false, MapImageFormat.webp);
			mapPMjpg = region.getServerImagesUrl() + "empty.png";
			mapPMwebp = region.getServerImagesUrl() + "empty.webp";
			heightPMSmall = "style=\"height: 0; margin: 0\"";
			fontSize = "style=\"font-size: 0\"";
			am = "";
			pm = "";
			afternoon = emptyDaytime();
		}

		String regions = String.join(", ", bulletin.getPublishedRegions().stream().map(lang::getRegionName).toList());

		sb.append("<article>\n");
		sb.append("<p>\n");
		appendDaytime(sb, forenoon, regions, "", am, mapAMwebp, mapAMjpg, "<table>", "height=\"50\" ");
		appendDaytime(sb, afternoon, regions, " " + heightPMSmall, pm, mapPMwebp, mapPMjpg, "<table " + fontSize + ">", heightPMSmall);
		sb.append("</p>\n");
		sb.append("<h3 style=\"color: red; padding: 15px 0; font-weight: normal;\">").append(bulletin.getHighlightsIn(lang).orElse("")).append("\n</h3>\n");
		sb.append("<h3>").append(bulletin.getAvActivityHighlightsIn(lang).orElse("")).append("\n</h3>\n");
		sb.append("<p>").append(bulletin.getAvActivityCommentIn(lang).orElse("")).append("\n</p>\n");
		sb.append("<h3>").append(lang.getBundleString("headline.snowpack")).append("\n</h3>\n");
		String dangerPattern1 = bulletin.getDangerPattern1() != null ? bulletin.getDangerPattern1().toString(lang.getLocale()) + "<br>" : "";
		String dangerPattern2 = bulletin.getDangerPattern2() != null ? bulletin.getDangerPattern2().toString(lang.getLocale()) + "<br>" : "";
		sb.append("<p>").append(dangerPattern1).append(dangerPattern2).append("\n</p>\n");
		sb.append("<p>").append(bulletin.getSnowpackStructureCommentIn(lang).orElse("")).append("\n</p>\n");
		sb.append("<h3>").append(lang.getBundleString("headline.tendency")).append("\n</h3>\n");
		sb.append("<p>").append(bulletin.getTendencyCommentIn(lang).orElse("")).append("\n</p>\n");
		sb.append("</article>\n");
	}

	private static void appendDaytime(StringBuilder sb, DaytimeView daytime, String regions, String pAttr, String content,
			String mapWebp, String mapJpg, String tableTag, String imgAttr) {
		sb.append("<p").append(pAttr).append(">").append(content).append("\n");
		sb.append("<picture>\n");
		sb.append("<source type=\"image/webp\" srcset=\"").append(mapWebp).append("\">\n");
		sb.append("<img style=\"margin-right: 10px;\" src=\"").append(mapJpg).append("\" alt=\"").append(regions).append("\">\n");
		sb.append("</picture>\n");
		sb.append("<br>").append(daytime.dangerLevelText()).append("\n");
		sb.append(tableTag).append("\n");
		sb.append("<tr>\n");
		sb.append("<td>\n");
		sb.append("<img height=\"50\" style=\"margin-right: 10px;\" src=\"").append(daytime.warningPicto()).append("\"/>\n");
		sb.append("</td>\n");
		sb.append("<td>").append(daytime.elevation()).append("\n");
		sb.append("</td>\n");
		sb.append("</tr>\n");
		sb.append("</table>").append(daytime.avalancheProblemText()).append("\n");
		sb.append(tableTag).append("\n");
		for (ProblemView problem : daytime.problems()) {
			if (problem.exist()) {
				appendProblem(sb, problem, imgAttr);
			}
		}
		sb.append("</table>\n");
		sb.append("</p>\n");
	}

	private static void appendProblem(StringBuilder sb, ProblemView problem, String imgAttr) {
		sb.append("<tr>\n");
		sb.append("<td style=\"margin-right: 10px;\" >\n");
		sb.append("<table>\n");
		sb.append("<tr>\n");
		sb.append("<td style=\"text-align: center;\">\n");
		sb.append("<img ").append(imgAttr).append("src=\"").append(problem.icon()).append("\"/>\n");
		sb.append("</td>\n");
		sb.append("</tr>\n");
		sb.append("<tr>\n");
		sb.append("<td style=\"text-align: center; font-size: small; max-width: 80px;\">").append(problem.text()).append("\n");
		sb.append("</td>\n");
		sb.append("</tr>\n");
		sb.append("</table>\n");
		sb.append("</td>\n");
		sb.append("<td>\n");
		sb.append("<img ").append(imgAttr).append("style=\"margin-right: 10px;\" src=\"").append(problem.elevationIcon()).append("\"/>\n");
		sb.append("</td>\n");
		sb.append("<td>\n");
		sb.append("<table>\n");
		sb.append("<tr>\n");
		sb.append("<td>").append(problem.elevationHigh()).append("\n");
		sb.append("</td>\n");
		sb.append("</tr>\n");
		sb.append("<tr>\n");
		sb.append("<td>").append(problem.elevationLow()).append("\n");
		sb.append("</td>\n");
		sb.append("</tr>\n");
		sb.append("</table>\n");
		sb.append("</td>\n");
		sb.append("<td>\n");
		sb.append("<img ").append(imgAttr).append("style=\"margin-right: 10px;\" src=\"").append(problem.aspectsIcon()).append("\"/>\n");
		sb.append("</td>\n");
		sb.append("</tr>\n");
	}

	record DaytimeView(String dangerLevelText, String avalancheProblemText, String warningPicto, String elevation,
			List<ProblemView> problems) {
	}

	record ProblemView(boolean exist, String icon, String text, String elevationIcon, String elevationLow,
			String elevationHigh, String aspectsIcon) {
	}

	private static DaytimeView emptyDaytime() {
		ProblemView empty = new ProblemView(false, "", "", "", "", "", "");
		return new DaytimeView("", "", "", "", List.of(empty, empty, empty, empty, empty));
	}

	private static DaytimeView buildDaytime(AvalancheBulletinDaytimeDescription daytimeDescription, LanguageCode lang, Region region) {
		List<ProblemView> problems = List.of(
			buildProblem(daytimeDescription.getAvalancheProblem1(), lang, region),
			buildProblem(daytimeDescription.getAvalancheProblem2(), lang, region),
			buildProblem(daytimeDescription.getAvalancheProblem3(), lang, region),
			buildProblem(daytimeDescription.getAvalancheProblem4(), lang, region),
			buildProblem(daytimeDescription.getAvalancheProblem5(), lang, region));

		String dangerLevelText = "<b>" + lang.getBundleString("headline.danger-rating") + "</b><br>";
		String avalancheProblemText = problems.stream().anyMatch(ProblemView::exist)
			? "<b>" + lang.getBundleString("headline.avalanche-problem") + "</b><br>"
			: "";
		String warningPicto = region.getServerImagesUrl() + "warning_pictos/color/level_"
			+ daytimeDescription.getWarningLevelId() + ".png";
		String elevation = getElevationString(daytimeDescription.getElevation(), daytimeDescription.getTreeline(), lang);

		return new DaytimeView(dangerLevelText, avalancheProblemText, warningPicto, elevation, problems);
	}

	private static ProblemView buildProblem(AvalancheProblem avalancheProblem, LanguageCode lang, Region region) {
		if (avalancheProblem == null || avalancheProblem.getAvalancheProblem() == null) {
			return new ProblemView(false, "", "", "", "", "", "");
		}
		return new ProblemView(true,
			region.getServerImagesUrl() + avalancheProblem.getAvalancheProblem().getSymbolPath(false),
			avalancheProblem.getAvalancheProblem().toString(lang.getLocale()),
			region.getServerImagesUrl() + getElevationIcon(avalancheProblem),
			getElevationLowText(avalancheProblem, lang),
			getElevationHighText(avalancheProblem, lang),
			region.getServerImagesUrl() + Aspect.getSymbolPath(avalancheProblem.getAspects(), false));
	}

	private static String getElevationIcon(AvalancheProblem avalancheProblem) {
		if (avalancheProblem.getTreelineHigh() || avalancheProblem.getElevationHigh() > 0) {
			if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				// elevation high and low set
				return "elevation/color/levels_middle_two.png";
			} else {
				// elevation high set
				return "elevation/color/levels_below.png";
			}
		} else if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
			// elevation low set
			return "elevation/color/levels_above.png";
		} else {
			// no elevation set
			return "elevation/color/levels_all.png";
		}
	}

	private static String getElevationLowText(AvalancheProblem avalancheProblem, LanguageCode lang) {
		if (avalancheProblem.getTreelineHigh() || avalancheProblem.getElevationHigh() > 0) {
			if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				// elevation high and low set
				if (avalancheProblem.getTreelineLow()) {
					return lang.getBundleString("elevation.treeline.capitalized");
				} else {
					return avalancheProblem.getElevationLow() + "m";
				}
			} else {
				// elevation high set
				return "";
			}
		} else if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
			// elevation low set
			if (avalancheProblem.getTreelineLow()) {
				return lang.getBundleString("elevation.treeline.capitalized");
			} else {
				return avalancheProblem.getElevationLow() + "m";
			}
		} else {
			return "";
		}
	}

	private static String getElevationHighText(AvalancheProblem avalancheProblem, LanguageCode lang) {
		if (avalancheProblem.getTreelineHigh() || avalancheProblem.getElevationHigh() > 0) {
			if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				// elevation high and low set
				if (avalancheProblem.getTreelineHigh()) {
					return lang.getBundleString("elevation.treeline.capitalized");
				} else {
					return avalancheProblem.getElevationHigh() + "m";
				}
			} else {
				// elevation high set
				if (avalancheProblem.getTreelineHigh()) {
					return lang.getBundleString("elevation.treeline.capitalized");
				} else {
					return avalancheProblem.getElevationHigh() + "m";
				}
			}
		} else if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
			// elevation low set
			return "";
		} else {
			return "";
		}
	}

	private static String getElevationString(int elevation, boolean treeline, LanguageCode lang) {
		StringBuilder sb = new StringBuilder();
		if (treeline) {
			sb.append(lang.getBundleString("elevation.treeline"));
		} else if (elevation > 0) {
			sb.append(elevation);
			sb.append("m");
		}
		sb.append("<br>");
		return sb.toString();
	}
}
