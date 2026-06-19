// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

		StringWriter out = new StringWriter();
		PrintWriter pw = new PrintWriter(out);
		pw.format("<!DOCTYPE html>\n");
		pw.format("<html lang=\"en\">\n");
		pw.format("<head>\n");
		pw.format("<title>%s %s\n</title>\n", region.getWebsiteName(lang), avalancheReport.getDate(lang));
		pw.format("<meta charset=\"utf-8\">\n");
		pw.format("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
		pw.format("<style>*{box-sizing: border-box;}body{font-family: Open Sans, Arial, Helvetica, sans-serif;}header{background-color: #FFF; padding: 10px; text-align: center; font-size: 35px; color: #19abff;}a{color: #19abff; margin: 0px; padding: 0px;}h3{padding: 0px; margin: 0px;}h4{padding-top: 20px; padding: 0px; margin: 0px;}h5{padding: 0px; margin: 0px; color: #000000; font-size: large; font-weight: normal;}article{padding: 20px; width: 100%%; background-color: #f2f7fa; margin: 10px 0px;}rating1{background-color: #ccff66; color: #000000; font-weight: bold;}rating2{background-color: #ffff00; color: #000000; font-weight: bold;}rating3{color: #ff9900; font-weight: bold;}rating4{color: #ff0000; font-weight: bold;}rating5{color: #ff0000; font-weight: bold;}.day-link{font-size: large; font-weight: normal; margin: 0; padding: 10px 0;}.top-link{font-size: large; font-weight: normal; margin: 0; padding: 10px 0;}.headline-small{padding-top: 20px;}.headline-small-a{text-decoration: none; text-transform: uppercase; font-size: x-large;}.previous-day{float: left;}.next-day{float: right;}section:after{content: \"\"; display: table; clear: both;}\n</style>\n");
		pw.format("</head>\n");
		pw.format("<body>\n");
		pw.format("<header>\n");
		pw.format("<p class=\"top-link\">\n");
		pw.format("<a class=\"previous-day\" href=\"%s\">%s\n</a>\n", website, lang.getBundleString("standard.link.text"));
		pw.format("<a class=\"next-day\" href=\"%s_de.html\">DE&nbsp;\n</a>\n", prefix);
		pw.format("<a class=\"next-day\" href=\"%s_it.html\">IT&nbsp;\n</a>\n", prefix);
		pw.format("<a class=\"next-day\" href=\"%s_en.html\">EN&nbsp;\n</a>\n", prefix);
		pw.format("<a class=\"next-day\" href=\"%s_es.html\">ES&nbsp;\n</a>\n", prefix);
		pw.format("<a class=\"next-day\" href=\"%s_ca.html\">CA&nbsp;\n</a>\n", prefix);
		pw.format("<a class=\"next-day\" href=\"%s_ar.html\">AR&nbsp;\n</a>\n", prefix);
		pw.format("</p>\n");
		pw.format("<h4>\n");
		pw.format("<a href=\"%s\">%s\n</a>\n", website, region.getWebsiteName(lang));
		pw.format("</h4>\n");
		pw.format("<h3>%s\n</h3>\n", avalancheReport.getDate(lang));
		pw.format("<h5>%s<b>%s</b></h5>\n", publicationDate.isEmpty() ? "" : lang.getBundleString("published"), publicationDate);
		pw.format("<p class=\"day-link\">\n");
		pw.format("<a class=\"previous-day\" href=\"%s\"> &#8592; %s\n</a>\n", previousDayLink, avalancheReport.getPreviousValidityDateString(lang));
		pw.format("<a class=\"next-day\" href=\"%s\">%s &#8594;\n</a>\n", nextDayLink, avalancheReport.getNextValidityDateString(lang));
		pw.format("</p>\n");
		pw.format("</header>\n");
		pw.format("<section>\n");

		for (AvalancheBulletin bulletin : avalancheReport.getBulletins()) {
			if (bulletin.getPublishedRegions() == null || bulletin.getPublishedRegions().isEmpty()) {
				continue;
			}
			appendBulletin(pw, bulletin, lang, region, avalancheReport);
		}

		pw.format("</section>\n</body>\n</html>\n");
		pw.flush();
		return out.toString();
	}

	private static void appendBulletin(PrintWriter pw, AvalancheBulletin bulletin, LanguageCode lang, Region region,
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

		pw.format("<article>\n");
		pw.format("<p>\n");
		appendDaytime(pw, forenoon, regions, "", am, mapAMwebp, mapAMjpg, "<table>", "height=\"50\" ");
		appendDaytime(pw, afternoon, regions, " " + heightPMSmall, pm, mapPMwebp, mapPMjpg, "<table " + fontSize + ">", heightPMSmall);
		pw.format("</p>\n");
		pw.format("<h3 style=\"color: red; padding: 15px 0; font-weight: normal;\">%s\n</h3>\n", bulletin.getHighlightsIn(lang).orElse(""));
		pw.format("<h3>%s\n</h3>\n", bulletin.getAvActivityHighlightsIn(lang).orElse(""));
		pw.format("<p>%s\n</p>\n", bulletin.getAvActivityCommentIn(lang).orElse(""));
		pw.format("<h3>%s\n</h3>\n", lang.getBundleString("headline.snowpack"));
		String dangerPattern1 = bulletin.getDangerPattern1() != null ? bulletin.getDangerPattern1().toString(lang.getLocale()) + "<br>" : "";
		String dangerPattern2 = bulletin.getDangerPattern2() != null ? bulletin.getDangerPattern2().toString(lang.getLocale()) + "<br>" : "";
		pw.format("<p>%s%s\n</p>\n", dangerPattern1, dangerPattern2);
		pw.format("<p>%s\n</p>\n", bulletin.getSnowpackStructureCommentIn(lang).orElse(""));
		pw.format("<h3>%s\n</h3>\n", lang.getBundleString("headline.tendency"));
		pw.format("<p>%s\n</p>\n", bulletin.getTendencyCommentIn(lang).orElse(""));
		pw.format("</article>\n");
	}

	private static void appendDaytime(PrintWriter pw, DaytimeView daytime, String regions, String pAttr, String content,
			String mapWebp, String mapJpg, String tableTag, String imgAttr) {
		pw.format("<p%s>%s\n", pAttr, content);
		pw.format("<picture>\n");
		pw.format("<source type=\"image/webp\" srcset=\"%s\">\n", mapWebp);
		pw.format("<img style=\"margin-right: 10px;\" src=\"%s\" alt=\"%s\">\n", mapJpg, regions);
		pw.format("</picture>\n");
		pw.format("<br>%s\n", daytime.dangerLevelText());
		pw.format("%s\n", tableTag);
		pw.format("<tr>\n");
		pw.format("<td>\n");
		pw.format("<img height=\"50\" style=\"margin-right: 10px;\" src=\"%s\"/>\n", daytime.warningPicto());
		pw.format("</td>\n");
		pw.format("<td>%s\n</td>\n", daytime.elevation());
		pw.format("</tr>\n");
		pw.format("</table>%s\n", daytime.avalancheProblemText());
		pw.format("%s\n", tableTag);
		for (ProblemView problem : daytime.problems()) {
			if (problem.exist()) {
				appendProblem(pw, problem, imgAttr);
			}
		}
		pw.format("</table>\n");
		pw.format("</p>\n");
	}

	private static void appendProblem(PrintWriter pw, ProblemView problem, String imgAttr) {
		pw.format("<tr>\n");
		pw.format("<td style=\"margin-right: 10px;\" >\n");
		pw.format("<table>\n");
		pw.format("<tr>\n");
		pw.format("<td style=\"text-align: center;\">\n");
		pw.format("<img %ssrc=\"%s\"/>\n", imgAttr, problem.icon());
		pw.format("</td>\n");
		pw.format("</tr>\n");
		pw.format("<tr>\n");
		pw.format("<td style=\"text-align: center; font-size: small; max-width: 80px;\">%s\n</td>\n", problem.text());
		pw.format("</tr>\n");
		pw.format("</table>\n");
		pw.format("</td>\n");
		pw.format("<td>\n");
		pw.format("<img %sstyle=\"margin-right: 10px;\" src=\"%s\"/>\n", imgAttr, problem.elevationIcon());
		pw.format("</td>\n");
		pw.format("<td>\n");
		pw.format("<table>\n");
		pw.format("<tr>\n");
		pw.format("<td>%s\n</td>\n", problem.elevationHigh());
		pw.format("</tr>\n");
		pw.format("<tr>\n");
		pw.format("<td>%s\n</td>\n", problem.elevationLow());
		pw.format("</tr>\n");
		pw.format("</table>\n");
		pw.format("</td>\n");
		pw.format("<td>\n");
		pw.format("<img %sstyle=\"margin-right: 10px;\" src=\"%s\"/>\n", imgAttr, problem.aspectsIcon());
		pw.format("</td>\n");
		pw.format("</tr>\n");
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
		String result = "";
		if (treeline) {
			result = lang.getBundleString("elevation.treeline");
		} else if (elevation > 0) {
			result = elevation + "m";
		}
		return result + "<br>";
	}
}
