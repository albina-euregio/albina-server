// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.io.Resources;
import eu.albina.map.MapImageFormat;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;

public record EmailUtil(AvalancheReport avalancheReport, LanguageCode lang) {

	public static String createBulletinEmailHtml(AvalancheReport avalancheReport, LanguageCode lang) {
		return new EmailUtil(avalancheReport, lang).createBulletinEmailHtml();
	}

	static String getDangerPatternLink(LanguageCode lang, Region region, DangerPattern dangerPattern) {
		return String.format("%s/education/danger-patterns#%s",
			region.getWebsiteUrl(lang), DangerPattern.getCAAMLv6String(dangerPattern));
	}

	static String getAvalancheProblemLink(LanguageCode lang, Region region,
	                                      eu.albina.model.enumerations.AvalancheProblem avalancheProblem) {
		return String.format("%s/education/avalanche-problems#%s",
			region.getWebsiteUrl(lang), avalancheProblem.toStringId());
	}

	String createBulletinEmailHtml() {
		Region region = avalancheReport.getRegion();
		String serverImagesUrl = region.getServerImagesUrl();
		String color = region.getEmailColor();
		String ci = serverImagesUrl + region.getImageColorbarColorPath();
		String website = region.getWebsiteUrlWithDate(lang, avalancheReport);
		String mapsUrl = avalancheReport.getMapsUrl();
		List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();
		boolean daytime = avalancheReport.hasDaytimeDependency();

		String publicationDate = avalancheReport.getPublicationDate(lang);
		String publishedAt = publicationDate.isEmpty() ? "" : lang.getBundleString("published");
		String headline = avalancheReport.getStatus() == BulletinStatus.republished
			? lang.getBundleString("headline.update")
			: lang.getBundleString("headline");
		String textAm = daytime ? lang.getBundleString("valid-time-period.earlier") : "";
		String textPm = daytime ? lang.getBundleString("valid-time-period.later") : "";
		String overview = daytime
			? mapsUrl + "/" + MapUtil.getOverviewMapFilename(region, DaytimeDependency.am, false)
			: mapsUrl + "/" + MapUtil.getOverviewMapFilename(region, DaytimeDependency.fd, false);
		String overviewPM = daytime
			? mapsUrl + "/" + MapUtil.getOverviewMapFilename(region, DaytimeDependency.pm, false)
			: serverImagesUrl + "/empty.png";
		String widthPM = daytime ? "width=\"600\" " : "";
		String dangerLevel5Style = "background=\"" + serverImagesUrl + "bg_checkered.png"
			+ "\" height=\"10\" width=\"75\" bgcolor=\"#FF0000\"";

		StringWriter out = new StringWriter();
		PrintWriter pw = new PrintWriter(out);

		// head
		pw.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		pw.print("<html>");
		pw.print("<head>");
		pw.print("<meta name=\"viewport\" content=\"width=device-width\"/>");
		pw.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
		pw.format("<title>%s</title>", lang.getBundleString("headline"));
		pw.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/avalanche-report.css\" >");
		pw.format("<style>%s</style>", css().replace("var(--albina-color)", "#" + color));
		pw.print("</head>");
		pw.print("<body bgcolor=\"#FFFFFF\" topmargin=\"0\" leftmargin=\"0\" marginheight=\"0\" marginwidth=\"0\">");

		// header
		pw.print("<table class=\"head-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<img height=\"4\" style=\"width: 100%;\" src=\"" + ci + "\"/>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td class=\"header container\" style=\"padding: 15px;\">");
		pw.print("<div class=\"content\">");
		pw.print("<table bgcolor=\"\" >");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<p class=\"lead\">%s</p>", headline);
		pw.format("<h2 style=\"margin-bottom: 5px\">%s</h2>", avalancheReport.getDate(lang));
		pw.format("<p style=\"margin-bottom: 0px; font-size: 12px\">%s<b>%s</b>", publishedAt, publicationDate);
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td align=\"right\">");
		pw.format("<a class=\"btn\" href=\"%s\">", website);
		pw.format("<img width=\"110\" src=\"%s\"/>", serverImagesUrl + region.getLogoPath());
		pw.print("</a>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// overview maps
		pw.print("<table align=\"center\" class=\"body-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td class=\"container\" bgcolor=\"#FFFFFF\">");
		pw.print("<div class=\"content\">");
		pw.print("<table style=\"padding: 15px 0;\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<h2 class=\"map-daytime-text\">%s</h2>", textAm);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<p style=\"margin-bottom: 0px; text-align: center;\">");
		pw.format("<a href=\"%s\">", website);
		pw.format("<img width=\"600\" style=\"max-width: 600px;\" src=\"%s\"/>", overview);
		pw.print("</a>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<h2 class=\"map-daytime-text\" style=\"margin-top: 15px;\">%s</h2>", textPm);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<p style=\"margin-bottom: 0px; text-align: center;\">");
		pw.format("<a href=\"%s\">", website);
		pw.format("<img %sstyle=\"max-width: 600px;\" src=\"%s\"/>", widthPM, overviewPM);
		pw.print("</a>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// danger scale
		pw.print("<table align=\"center\" style=\"width: auto; margin-left: auto; margin-right: auto; text-align: center; border-spacing: 0px;\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<table height=\"10\" width=\"75\" bgcolor=\"#CCFF66\">");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<table height=\"10\" width=\"75\" bgcolor=\"#FFFF00\">");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<table height=\"10\" width=\"75\" bgcolor=\"#FF9900\">");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<table height=\"10\" width=\"75\" bgcolor=\"#FF0000\">");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<table %s>", dangerLevel5Style);
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>1</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>2</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>3</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>4</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>5</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.low.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.moderate.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.considerable.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.high.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.very_high.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// general headline
		avalancheReport.getGeneralHeadline(lang).ifPresent(generalHeadline -> {
			pw.print("<table bgcolor=\"\" >");
			pw.print("<tr>");
			pw.print("<td>");
			pw.format("<h2 style=\"margin: 24px 0\">%s</h2>", generalHeadline);
			pw.print("</td>");
			pw.print("</tr>");
			pw.print("</table>");
		});

		for (AvalancheBulletin bulletin : bulletins) {
			appendBulletin(pw, bulletin, color);
		}

		// footer
		pw.print("<table class=\"footer-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td class=\"container\">");
		pw.print("<div class=\"content\">");
		pw.print("<table>");
		pw.print("<tr>");
		pw.print("<td align=\"center\">");
		pw.print("<p>");
		pw.format("<a href=\"%s\">%s</a>", region.getImprintLink(lang), lang.getBundleString("email.imprint"));
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td class=\"container\">");
		pw.print("<div class=\"content\">");
		pw.print("<table>");
		pw.print("<tr>");
		pw.print("<td align=\"center\">");
		pw.print("<p>");
		pw.print("<a href=\"{%link_unsubscribe}\">" + lang.getBundleString("email.unsubscribe") + "</a>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("<img height=\"4px\" style=\"width: 100%;\" src=\"" + ci + "\"/>");
		pw.print("</body>");
		pw.print("</html>");

		pw.flush();
		return out.toString();
	}

	private static String css() {
		try {
			URL resource = Resources.getResource("templates/EmailUtil.css");
			return Resources.readLines(resource, StandardCharsets.UTF_8).stream()
				.map(String::trim)
				.filter(l -> !l.isEmpty())
				.filter(l -> !(l.startsWith("/*") && l.endsWith("*/")))
				.collect(Collectors.joining(""));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void appendBulletin(PrintWriter pw, AvalancheBulletin bulletin, String color) {
		Region region = avalancheReport.getRegion();
		DangerRating highestDangerRating = bulletin.getHighestDangerRating();

		pw.print("<table class=\"body-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td class=\"container\" align=\"\" bgcolor=\"#FFFFFF\">");
		pw.print("<div class=\"content bulletin-content\">");
		pw.print("<table style=\"border-spacing: 0px;\">");
		pw.print("<tr>");
		pw.format("<td %s>", getDangerRatingColorStyle(highestDangerRating, region));
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<table style=\"border-spacing: 0px;\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<h2 %s>%s</h2>", getHeadlineStyle(highestDangerRating), highestDangerRating.toString(lang.getLocale(), true));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// forenoon / all-day
		daytime(pw, false, bulletin);

		// afternoon
		daytime(pw, true, bulletin);

		pw.print("<table style=\"padding-left: 15px;\">");
		pw.print("<tr>");
		pw.print("<td style=\"vertical-align: top; padding-top: 10px;\">");
		pw.format("<h4>%s</h4>", bulletin.getAvActivityHighlightsIn(lang).orElse(""));
		pw.format("<p class=\"\">%s</p>", bulletin.getAvActivityCommentIn(lang).orElse(""));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// snowpack structure / danger patterns / synopsis / tendency
		DangerPattern dangerPattern1 = bulletin.getDangerPattern1();
		DangerPattern dangerPattern2 = bulletin.getDangerPattern2();
		Optional<String> snowpackStructureComment = bulletin.getSnowpackStructureCommentIn(lang);
		Optional<String> snowpackStructureHighlights = bulletin.getSnowpackStructureHighlightsIn(lang);
		Optional<String> tendencyComment = bulletin.getTendencyCommentIn(lang);
		Optional<String> synopsisComment = bulletin.getSynopsisCommentIn(lang);
		boolean hasSnowpackSection = dangerPattern1 != null || dangerPattern2 != null
			|| snowpackStructureComment.isPresent() || snowpackStructureHighlights.isPresent()
			|| tendencyComment.isPresent();
		boolean hasStructure = dangerPattern1 != null || dangerPattern2 != null
			|| snowpackStructureComment.isPresent() || snowpackStructureHighlights.isPresent();
		boolean hasDangerPatterns = dangerPattern1 != null || dangerPattern2 != null;

		String snowpackStructureHeadline = hasStructure ? lang.getBundleString("headline.snowpack") : "";
		String snowpackStructureCommentText = hasStructure ? snowpackStructureComment.orElse("") : "";
		String dangerPatternsHeadline = hasStructure && hasDangerPatterns ? lang.getBundleString("headline.danger-patterns") : "";
		String dangerPattern1Text = hasStructure && hasDangerPatterns && dangerPattern1 != null ? dangerPattern1.toString(lang.getLocale()) : "";
		String dangerPatternLink1 = hasStructure && hasDangerPatterns && dangerPattern1 != null ? getDangerPatternLink(lang, region, dangerPattern1) : "";
		String dangerPatternStyle1 = getDangerPatternStyle(hasStructure && hasDangerPatterns && dangerPattern1 != null);
		String dangerPattern2Text = hasStructure && hasDangerPatterns && dangerPattern2 != null ? dangerPattern2.toString(lang.getLocale()) : "";
		String dangerPatternLink2 = hasStructure && hasDangerPatterns && dangerPattern2 != null ? getDangerPatternLink(lang, region, dangerPattern2) : "";
		String dangerPatternStyle2 = getDangerPatternStyle(hasStructure && hasDangerPatterns && dangerPattern2 != null);
		String tendencyHeadline = tendencyComment.isPresent() ? lang.getBundleString("headline.tendency") : "";
		String tendencyCommentText = tendencyComment.orElse("");

		pw.format("<table %s>", getSnowpackStyle(hasSnowpackSection));
		pw.print("<tr>");
		pw.print("<td style=\"background-color: #" + color + "; width: 10px; min-width: 10px; height: 100%;\"></td>");
		pw.print("<td style=\"vertical-align: top; padding: 15px;\">");
		pw.format("<h4 style=\"padding-top: 5px;\">%s</h4>", snowpackStructureHeadline);
		pw.format("<h5 style=\"margin-right: 5px; display: inline-block\">%s</h5>", dangerPatternsHeadline);
		pw.format("<a href=\"%s\" target=\"_blank\">", dangerPatternLink1);
		pw.format("<p %s>%s</p>", dangerPatternStyle1, dangerPattern1Text);
		pw.print("</a>");
		pw.format("<a href=\"%s\" target=\"_blank\">", dangerPatternLink2);
		pw.format("<p %s>%s</p>", dangerPatternStyle2, dangerPattern2Text);
		pw.print("</a>");
		pw.format("<p class=\"\">%s</p>", snowpackStructureCommentText);
		if (hasSnowpackSection && synopsisComment.isPresent()) {
			pw.format("<h4 style=\"padding-top: 15px;\">%s</h4>", lang.getBundleString("headline.synopsis"));
			pw.format("<p class=\"\">%s</p>", synopsisComment.get());
		}
		pw.format("<h4 style=\"padding-top: 15px;\">%s</h4>", tendencyHeadline);
		pw.format("<p class=\"\">%s</p>", tendencyCommentText);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
	}

	private void daytime(PrintWriter pw, boolean pm, AvalancheBulletin bulletin) {
		Region region = avalancheReport.getRegion();
		String serverImagesUrl = region.getServerImagesUrl();
		String mapsUrl = avalancheReport.getMapsUrl();
		boolean bulletinDaytime = bulletin.isHasDaytimeDependency();

		String text = bulletinDaytime
			? lang.getBundleString(pm ? "valid-time-period.later" : "valid-time-period.earlier")
			: "";
		// when there is no daytime dependency, the afternoon block reuses the forenoon description and its map
		AvalancheBulletinDaytimeDescription description = pm && bulletinDaytime ? bulletin.getAfternoon() : bulletin.getForenoon();
		DaytimeDependency daytimeDependency = pm && bulletinDaytime ? DaytimeDependency.pm : DaytimeDependency.am;
		String map = mapsUrl + "/" + MapUtil.filename(region, bulletin, daytimeDependency, false, MapImageFormat.jpg);

		// tendency
		String tendencyText = bulletin.getTendency() == null ? "" : bulletin.getTendency().toString(lang.getLocale());
		String tendencySymbol;
		String tendencyDate;
		if (bulletin.getTendency() == Tendency.decreasing) {
			tendencySymbol = serverImagesUrl + "tendency/tendency_decreasing_blue.png";
			tendencyDate = avalancheReport.getTendencyDate(lang);
		} else if (bulletin.getTendency() == Tendency.steady) {
			tendencySymbol = serverImagesUrl + "tendency/tendency_steady_blue.png";
			tendencyDate = avalancheReport.getTendencyDate(lang);
		} else if (bulletin.getTendency() == Tendency.increasing) {
			tendencySymbol = serverImagesUrl + "tendency/tendency_increasing_blue.png";
			tendencyDate = avalancheReport.getTendencyDate(lang);
		} else {
			tendencySymbol = serverImagesUrl + "tendency/empty.png";
			tendencyDate = "";
		}

		String stylepmtable = getPMStyleTable(bulletinDaytime);
		String styleAttr = pm ? " " + stylepmtable : "";
		if (pm) {
			pw.format("<div %s>", getPMStyle(bulletinDaytime));
		}
		pw.format("<table style=\"%s\"%s>", pm ? "padding-left: 15px;" : "margin-top: 10px; padding-left: 15px;", styleAttr);
		pw.print("<tr>");
		pw.print("<td class=\"daytime-text-div\">");
		pw.format("<h2 class=\"daytime-text\">%s</h2>", text);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.format("<td style=\"%s\">", pm ? "width: 150px;" : "width: 150px; padding-right: 10px;");
		pw.format("<img width=\"150\" class=\"detail-map\" src=\"%s\"/>", map);
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<table style=\"border-bottom: 1px solid #e6eef2; padding-bottom: 5px;\"%s>", styleAttr);
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<table style=\"width: 0;\"%s>", styleAttr);
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<div style=\"height: 48px;\">");
		pw.print("<div style=\"height: 48px; width: 60px; margin-right: 10px;\">");
		pw.format("<img height=\"48\" width=\"60\" style=\"display: inline-block; margin-bottom: 10px;\" src=\"%s\"/>", dangerRatingSymbol(description));
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td class=\"mountain\">");
		pw.print("<div style=\"height: 48px;\">");
		pw.print("<div style=\"height: 48px; margin-right: 10px;\">");
		pw.print("<p style=\"height: 48px; display: inline-block; font-size: 12px; padding-top: 18px;\">");
		pw.format("<b>%s</b>", dangerRatingElevation(description));
		pw.print("</p>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td class=\"tendency\">");
		pw.format("<table%s>", styleAttr);
		pw.print("<tr>");
		pw.print("<td>");
		if (pm) {
			pw.format("<h5 style=\"text-align: left; margin-bottom: 10px;\">%s</h5>", tendencyText);
			pw.format("<h5 style=\"text-align: left; font-weight: 100;\">%s</h5>", tendencyDate);
		} else {
			pw.format("<p style=\"text-align: left; font-weight: 900; margin-bottom: 10px;\">%s</p>", tendencyText);
			pw.format("<p style=\"text-align: left; margin-bottom: 0;\">%s</p>", tendencyDate);
		}
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<img class=\"tendency-symbol\" src=\"%s\"/>", tendencySymbol);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		appendAvalancheProblem(pw, description.getAvalancheProblem1(), true, styleAttr);
		appendAvalancheProblem(pw, description.getAvalancheProblem2(), false, styleAttr);
		appendAvalancheProblem(pw, description.getAvalancheProblem3(), false, styleAttr);
		appendAvalancheProblem(pw, description.getAvalancheProblem4(), false, styleAttr);
		appendAvalancheProblem(pw, description.getAvalancheProblem5(), false, styleAttr);

		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		if (pm) {
			pw.print("</div>");
		}
	}

	private void appendAvalancheProblem(PrintWriter pw, AvalancheProblem problem, boolean first, String tableExtraAttr) {
		if (problem == null || problem.getAvalancheProblem() == null) {
			return;
		}
		Region region = avalancheReport.getRegion();
		String serverImagesUrl = region.getServerImagesUrl();

		String symbol = serverImagesUrl + "avalanche_problems/color/" + problem.getAvalancheProblem().toStringId() + ".png";
		String text = problem.getAvalancheProblem().toString(lang.getLocale());
		String link = getAvalancheProblemLink(lang, region, problem.getAvalancheProblem());
		String aspects = serverImagesUrl + Aspect.getSymbolPath(problem.getAspects(), false);

		String elevationSymbol;
		String limitAbove;
		String limitBelow;
		if (problem.getTreelineHigh() || problem.getElevationHigh() > 0) {
			if (problem.getTreelineLow() || problem.getElevationLow() > 0) {
				// elevation high and low set
				elevationSymbol = serverImagesUrl + "elevation/color/levels_middle_two.png";
				limitAbove = problem.getTreelineLow()
					? lang.getBundleString("elevation.treeline.capitalized")
					: problem.getElevationLow() + "m";
				limitBelow = problem.getTreelineHigh()
					? lang.getBundleString("elevation.treeline.capitalized")
					: problem.getElevationHigh() + "m";
			} else {
				// elevation high set
				elevationSymbol = serverImagesUrl + "elevation/color/levels_below.png";
				limitAbove = "";
				limitBelow = problem.getTreelineHigh()
					? lang.getBundleString("elevation.treeline.capitalized")
					: problem.getElevationHigh() + "m";
			}
		} else if (problem.getTreelineLow() || problem.getElevationLow() > 0) {
			// elevation low set
			elevationSymbol = serverImagesUrl + "elevation/color/levels_above.png";
			limitAbove = problem.getTreelineLow()
				? lang.getBundleString("elevation.treeline.capitalized")
				: problem.getElevationLow() + "m";
			limitBelow = "";
		} else {
			// no elevation set
			elevationSymbol = serverImagesUrl + "elevation/color/levels_all.png";
			limitAbove = "";
			limitBelow = "";
		}

		String margin = first ? "margin: 5px 5px 0 5px" : "margin-left: 5px; margin-top: 0px";
		pw.format("<table style=\"%s; width: 0;\"%s>", margin, tableExtraAttr);
		pw.print("<tr>");
		pw.print("<td style=\"margin: 0 5px; width: 70px; text-align: center;\">");
		pw.format("<a href=\"%s\" target=\"_blank\">", link);
		pw.format("<img width=\"50\" class=\"avalanche-problem\" src=\"%s\"/>", symbol);
		pw.print("</a>");
		pw.format("<p style=\"margin-bottom: 0px; font-size: 12px; line-height: 1.0;\">%s</p>", text);
		pw.print("</td>");
		pw.print("<td style=\"margin: 0 5px; width: 70px;\">");
		pw.print("<div class=\"avalanche-problem-aspects-outer-div\">");
		pw.print("<div class=\"avalanche-problem-aspects-div\">");
		pw.print("<div class=\"avalanche-problem-aspects\">");
		pw.format("<img width=\"60\" class=\"avalanche-problem-aspects-img\" src=\"%s\"/>", aspects);
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td style=\"width: 100px; margin: 0 5px;\">");
		pw.print("<div style=\"width: 100px; height: 48px;\">");
		pw.print("<div style=\"max-height: 0; max-width: 0; overflow: visible;\">");
		pw.print("<div style=\"width: 100px; height: 48px;\">");
		pw.format("<img height=\"48\" class=\"avalanche-problem-elevation-img\" src=\"%s\"/>", elevationSymbol);
		pw.print("</div>");
		pw.print("</div>");
		pw.print("<div style=\"max-height: 0; max-width: 0; overflow: visible;\">");
		pw.print("<div style=\"width: 100px; height: 48px;\">");
		pw.print("<p style=\"width: 100px; height: 48px; display: inline-block; font-size: 12px; margin-top: 24px; margin-left: 68px;\">");
		pw.format("<b>%s</b>", limitAbove);
		pw.print("</p>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("<div style=\"max-height: 0; max-width: 0; overflow: visible;\">");
		pw.print("<div style=\"width: 100px; height: 48px;\">");
		pw.print("<p style=\"width: 100px; height: 48px; display: inline-block; font-size: 12px; margin-top: 7px; margin-left: 68px;\">");
		pw.format("<b>%s</b>", limitBelow);
		pw.print("</p>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
	}

	private String dangerRatingSymbol(AvalancheBulletinDaytimeDescription daytimeBulletin) {
		String serverImagesUrl = avalancheReport.getRegion().getServerImagesUrl();
		if ((daytimeBulletin.dangerRating(false) == null
			|| daytimeBulletin.dangerRating(false) == DangerRating.missing
			|| daytimeBulletin.dangerRating(false) == DangerRating.no_rating)
			&& (daytimeBulletin.dangerRating(true) == null
			|| daytimeBulletin.dangerRating(true) == DangerRating.missing
			|| daytimeBulletin.dangerRating(true) == DangerRating.no_rating)) {
			return serverImagesUrl + "warning_pictos/color/level_0_0.png";
		} else {
			return serverImagesUrl + "warning_pictos/color/level_" + daytimeBulletin.getWarningLevelId() + ".png";
		}
	}

	private String dangerRatingElevation(AvalancheBulletinDaytimeDescription daytimeBulletin) {
		if (daytimeBulletin.isHasElevationDependency()
			&& (daytimeBulletin.dangerRating(true) != daytimeBulletin.dangerRating(false))) {
			if (daytimeBulletin.getTreeline())
				return lang.getBundleString("elevation.treeline.capitalized");
			else if (daytimeBulletin.getElevation() > 0)
				return daytimeBulletin.getElevation() + "m";
			else
				return "";
		} else
			return "";
	}

	private static String getDangerRatingColorStyle(DangerRating dangerRating, Region region) {
		if (dangerRating.equals(DangerRating.very_high)) {
			return "background=\"" + region.getServerImagesUrl() + "bg_checkered.png"
				+ "\" height=\"100%\" width=\"10px\" bgcolor=\"#FF0000\"";
		} else
			return "style=\"background-color: " + dangerRating.getColor()
				+ "; height: 100%; width: 10px; min-width: 10px; padding: 0px; margin: 0px;\"";
	}

	private static String getHeadlineStyle(DangerRating dangerRating) {
		if (dangerRating.equals(DangerRating.low) || dangerRating.equals(DangerRating.moderate)) {
			return "style=\"margin: 0; padding: 0; padding-left: 15px; text-decoration: none; font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif; line-height: 1.6; margin-bottom: 0px; font-weight: bold; font-size: 24px; color: "
				+ "#565F61" + "; background-color: " + dangerRating.getColor() + ";\"";
		} else {
			return "style=\"margin: 0; padding: 0; padding-left: 15px; text-decoration: none; font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif; line-height: 1.6; margin-bottom: 0px; font-weight: bold; font-size: 24px; color: "
				+ dangerRating.getColor() + ";\"";
		}
	}

	private static String getDangerPatternStyle(boolean b) {
		if (b)
			return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; margin-bottom: 10px; font-weight: normal; line-height: 1.6; font-size: 12px; color: #565f61; border: 1px solid #565f61; border-radius: 15px; padding-left: 10px; padding-right: 10px; padding-top: 2px; padding-bottom: 2px; margin-right: 5px; display: inline-block; background-color: #FFFFFF;\"";
		else
			return "";
	}

	private static String getSnowpackStyle(boolean b) {
		if (!b)
			return "style=\"overflow: hidden; float: left; display: none !important; line-height: 0px; height: 0px; border-spacing: 0px;\"";
		else
			return "style=\"padding: 0px; border-spacing: 0px; width: 100%; background-color: #f6fafc;\"";
	}

	private static String getPMStyle(boolean daytimeDependency) {
		if (!daytimeDependency)
			return "style=\"display:none;width:0px;max-height:0px;overflow:hidden;mso-hide:all;height:0;font-size:0;max-height:0;line-height:0;margin:0 auto;\"";
		else
			return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; color: #565f61; width: 100%; margin-top: 10px; border-top: 1px solid #e6eef2; padding-top: 10px;\"";
	}

	private static String getPMStyleTable(boolean daytimeDependency) {
		if (!daytimeDependency)
			return "style=\"mso-hide: all;\"";
		else
			return "";
	}
}
