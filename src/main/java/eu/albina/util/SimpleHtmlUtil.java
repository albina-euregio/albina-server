// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Period;

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

public record SimpleHtmlUtil(AvalancheReport avalancheReport, LanguageCode lang) {

	private static final Logger logger = LoggerFactory.getLogger(SimpleHtmlUtil.class);

	public static void createRegionSimpleHtml(AvalancheReport avalancheReport) {
		if (avalancheReport.getBulletins().isEmpty()) {
			return;
		}
		for (LanguageCode lang : avalancheReport.getRegion().getEnabledLanguages()) {
			new SimpleHtmlUtil(avalancheReport, lang).createSimpleHtml();
		}
	}

	void createSimpleHtml() {
		try {
			String simpleHtmlString = createSimpleHtmlString();
			String filename = avalancheReport.getRegion().getId() + "_" + lang.toString() + ".html";
			Path dirPath = avalancheReport.getHtmlDirectory();
			Files.createDirectories(dirPath);
			Path newHtmlFile = dirPath.resolve(filename);
			Files.writeString(newHtmlFile, simpleHtmlString, StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("Simple html could not be created", e);
		}
	}

	String createSimpleHtmlString() {
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
			appendBulletin(pw, bulletin);
		}

		pw.format("</section>\n</body>\n</html>\n");
		pw.flush();
		return out.toString();
	}

	private void appendBulletin(PrintWriter pw, AvalancheBulletin bulletin) {

		pw.format("<article>\n");
		pw.format("<p>\n");
		if (bulletin.isHasDaytimeDependency()) {
			appendDaytime(pw, bulletin, bulletin.getForenoon(), DaytimeDependency.am, "<b>" + lang.getBundleString("valid-time-period.earlier").toUpperCase() + "</b><br>");
			appendDaytime(pw, bulletin, bulletin.getAfternoon(), DaytimeDependency.pm, "<b>" + lang.getBundleString("valid-time-period.later").toUpperCase() + "</b><br>");
		} else {
			appendDaytime(pw, bulletin, bulletin.getForenoon(), DaytimeDependency.fd, "");
		}
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

	private void appendDaytime(PrintWriter pw, AvalancheBulletin bulletin,
			AvalancheBulletinDaytimeDescription daytimeDescription, DaytimeDependency daytimeDependency, String content) {
		String regions = String.join(", ", bulletin.getPublishedRegions().stream().map(lang::getRegionName).toList());
		String mapsUrl = avalancheReport.getMapsUrl();
		Region region = avalancheReport.getRegion();

		String dangerLevelText = "<b>" + lang.getBundleString("headline.danger-rating") + "</b><br>";
		String avalancheProblemText = daytimeDescription.getAvalancheProblems().stream()
			.noneMatch(p -> p != null && p.getAvalancheProblem() != null) ? "" : "<b>" + lang.getBundleString("headline.avalanche-problem") + "</b><br>";
		String warningPicto = DataURL.ofResource("images/warning_pictos/color/level_" + daytimeDescription.getWarningLevelId() + ".webp");
		String elevation = getElevationString(daytimeDescription.getElevation(), daytimeDescription.getTreeline());

		String mapWebp = mapsUrl + "/" + MapUtil.filename(region, bulletin, daytimeDependency, false, MapImageFormat.webp);
		pw.format("<p>%s\n", content);
		pw.format("<img style=\"margin-right: 10px;\" src=\"%s\" alt=\"%s\" loading=\"lazy\">\n", mapWebp, regions);
		pw.format("<br>%s\n", dangerLevelText);
		pw.format("<table>\n");
		pw.format("<tr>\n");
		pw.format("<td>\n");
		pw.format("<img height=\"50\" style=\"margin-right: 10px;\" src=\"%s\"/>\n", warningPicto);
		pw.format("</td>\n");
		pw.format("<td>%s\n</td>\n", elevation);
		pw.format("</tr>\n");
		pw.format("</table>%s\n", avalancheProblemText);
		pw.format("<table>\n");
		daytimeDescription.getAvalancheProblems().stream()
			.filter(p -> p != null && p.getAvalancheProblem() != null)
			.forEach(p -> appendProblem(pw, p));
		pw.format("</table>\n");
		pw.format("</p>\n");
	}

	private void appendProblem(PrintWriter pw, AvalancheProblem avalancheProblem) {
		Region region = avalancheReport.getRegion();
		pw.format("<tr>\n");
		pw.format("<td style=\"margin-right: 10px;\" >\n");
		pw.format("<table>\n");
		pw.format("<tr>\n");
		pw.format("<td style=\"text-align: center;\">\n");
		pw.format("<img height=\"50\" src=\"%s\"/>\n", avalancheProblem.getAvalancheProblem().getDataURL());
		pw.format("</td>\n");
		pw.format("</tr>\n");
		pw.format("<tr>\n");
		pw.format("<td style=\"text-align: center; font-size: small; max-width: 80px;\">%s\n</td>\n", avalancheProblem.getAvalancheProblem().toString(lang.getLocale()));
		pw.format("</tr>\n");
		pw.format("</table>\n");
		pw.format("</td>\n");
		pw.format("<td>\n");
		pw.format("<img height=\"50\" style=\"margin-right: 10px;\" src=\"%s\"/>\n", avalancheProblem.getElevationDataURL());
		pw.format("</td>\n");
		pw.format("<td>\n");
		pw.format("<table>\n");
		pw.format("<tr>\n");
		pw.format("<td>%s\n</td>\n", getElevationHighText(avalancheProblem));
		pw.format("</tr>\n");
		pw.format("<tr>\n");
		pw.format("<td>%s\n</td>\n", getElevationLowText(avalancheProblem));
		pw.format("</tr>\n");
		pw.format("</table>\n");
		pw.format("</td>\n");
		pw.format("<td>\n");
		pw.format("<img height=\"50\" style=\"margin-right: 10px;\" src=\"%s\"/>\n", Aspect.getDataURL(avalancheProblem.getAspects(), false));
		pw.format("</td>\n");
		pw.format("</tr>\n");
	}

	private String getElevationLowText(AvalancheProblem avalancheProblem) {
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

	private String getElevationHighText(AvalancheProblem avalancheProblem) {
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

	private String getElevationString(int elevation, boolean treeline) {
		String result = "";
		if (treeline) {
			result = lang.getBundleString("elevation.treeline");
		} else if (elevation > 0) {
			result = elevation + "m";
		}
		return result + "<br>";
	}
}
