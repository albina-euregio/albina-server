// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.io.Resources;
import eu.albina.caaml.Caaml6;
import eu.albina.map.MapImageFormat;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheReport;
import eu.albina.model.EawsMatrixInformation;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;
import org.caaml.v6.AvalancheBulletin;
import org.caaml.v6.AvalancheBulletins;
import org.caaml.v6.AvalancheBulletinsCustomData;
import org.caaml.v6.AvalancheProblem;
import org.caaml.v6.AvalancheProblemType;
import org.caaml.v6.DangerRatingValue;
import org.caaml.v6.TendencyType;
import org.caaml.v6.Texts;
import org.caaml.v6.ValidTimePeriod;

public record EmailUtil(AvalancheReport avalancheReport, AvalancheBulletins bulletins, LanguageCode lang) {

	/** Opens a layout table. Outlook needs the presentational attributes on every one of them. */
	private static final String TABLE = "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"";

	/** Separates one avalanche problem from the next. Outlook supports padding on cells, not on tables. */
	private static final String PROBLEM_CELL = "padding: 10px;";

	/** Separates the daytime row from the avalanche problems below it. */
	private static final String ROW_BORDER = "border-bottom: 1px solid #e6eef2; padding-bottom: 5px;";

	/** A cell of the danger scale legend. Outlook renders a coloured cell, but not an empty table. */
	private static final String SWATCH = "<td width=\"75\" height=\"10\" style=\"font-size: 0; line-height: 0;\"";

	/** Keeps Outlook from scaling the whole mail up on displays with more than 96 dpi. */
	private static final String MSO_PIXELS_PER_INCH = "<!--[if mso]><xml><o:OfficeDocumentSettings>"
		+ "<o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml><![endif]-->";

	public EmailUtil(AvalancheReport avalancheReport, LanguageCode lang) {
		this(avalancheReport, new Caaml6(avalancheReport, List.of(), lang).toCAAML(), lang);
	}

	public static String createBulletinEmailHtml(AvalancheReport avalancheReport, LanguageCode lang) {
		return new EmailUtil(avalancheReport, lang).createBulletinEmailHtml();
	}

	static String getDangerPatternLink(LanguageCode lang, Region region, DangerPattern dangerPattern) {
		return String.format("%s/education/danger-patterns#%s",
			region.getWebsiteUrl(lang), DangerPattern.getCAAMLv6String(dangerPattern));
	}

	static String getAvalancheProblemLink(LanguageCode lang, Region region, AvalancheProblemType problemType) {
		return String.format("%s/education/avalanche-problems#%s", region.getWebsiteUrl(lang), problemType.name());
	}

	String createBulletinEmailHtml() {
		Region region = avalancheReport.getRegion();
		String serverImagesUrl = region.getServerImagesUrl();
		String color = region.getEmailColor();
		String ci = serverImagesUrl + region.getImageColorbarColorPath();
		String website = region.getWebsiteUrlWithDate(lang, avalancheReport);
		String mapsUrl = avalancheReport.getMapsUrl();
		boolean daytime = bulletins.getBulletins().stream().anyMatch(AvalancheBulletin::hasDaytimeDependency);

		String publicationDate = avalancheReport.getPublicationDate(lang);
		String publishedAt = publicationDate.isEmpty() ? "" : lang.getBundleString("published");
		String headline = avalancheReport.getStatus() == BulletinStatus.republished
			? lang.getBundleString("headline.update")
			: lang.getCaamlBundleString("forecast.label");
		String textAm = daytime ? lang.getCaamlBundleString("validTimePeriod.earlier") : "";
		String textPm = daytime ? lang.getCaamlBundleString("validTimePeriod.later") : "";
		String overview = daytime
			? mapsUrl + "/" + MapUtil.getOverviewMapFilename(region, DaytimeDependency.am, false)
			: mapsUrl + "/" + MapUtil.getOverviewMapFilename(region, DaytimeDependency.fd, false);
		String overviewPM = daytime
			? mapsUrl + "/" + MapUtil.getOverviewMapFilename(region, DaytimeDependency.pm, false)
			: null;

		StringWriter out = new StringWriter();
		PrintWriter pw = new PrintWriter(out);

		// head
		pw.print("<!DOCTYPE html>");
		pw.format("<html lang=\"%s\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">", lang);
		pw.print("<head>");
		pw.print("<meta charset=\"utf-8\"/>");
		pw.print("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>");
		pw.print("<meta name=\"x-apple-disable-message-reformatting\"/>");
		pw.format("<title>%s</title>", lang.getCaamlBundleString("forecast.label"));
		pw.print(MSO_PIXELS_PER_INCH);
		pw.format("<style>%s</style>", css("templates/EmailUtil.css").replace("var(--albina-color)", "#" + color));
		pw.print("</head>");
		pw.print("<body bgcolor=\"#FFFFFF\" style=\"margin: 0; padding: 0;\">");

		// header
		pw.print(TABLE + " class=\"head-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<img height=\"4\" style=\"width: 100%;\" src=\"" + ci + "\" alt=\"\"/>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td class=\"header container\" style=\"padding: 15px;\">");
		pw.print("<div class=\"content\">");
		pw.print(TABLE + ">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<p class=\"lead\">%s</p>", headline);
		pw.format("<h2>%s</h2>", avalancheReport.getDate(lang));
		pw.format("<p class=\"small\">%s<b>%s</b>", publishedAt, publicationDate);
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td align=\"right\">");
		pw.format("<a href=\"%s\">", website);
		pw.format("<img width=\"110\" src=\"%s\" alt=\"%s\"/>", serverImagesUrl + region.getLogoPath(),
			region.getWebsiteName(lang));
		pw.print("</a>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// overview maps
		pw.print(TABLE + " align=\"center\" class=\"body-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td class=\"container\" style=\"padding: 15px 0;\" bgcolor=\"#FFFFFF\">");
		pw.print("<div class=\"content\">");
		pw.print(TABLE + ">");
		if (daytime) {
			pw.print("<tr>");
			pw.print("<td>");
			pw.format("<h2 class=\"map-daytime-text\">%s</h2>", textAm);
			pw.print("</td>");
			pw.print("</tr>");
		}
		appendOverviewMap(pw, website, overview);
		if (daytime) {
			pw.print("<tr>");
			pw.print("<td style=\"padding-top: 15px;\">");
			pw.format("<h2 class=\"map-daytime-text\">%s</h2>", textPm);
			pw.print("</td>");
			pw.print("</tr>");
			appendOverviewMap(pw, website, overviewPM);
		}
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// danger scale
		pw.print(TABLE + " align=\"center\" style=\"width: auto; margin-left: auto; margin-right: auto; text-align: center;\">");
		pw.print("<tr>");
		pw.print(SWATCH + " bgcolor=\"#CCFF66\">&nbsp;</td>");
		pw.print(SWATCH + " bgcolor=\"#FFFF00\">&nbsp;</td>");
		pw.print(SWATCH + " bgcolor=\"#FF9900\">&nbsp;</td>");
		pw.print(SWATCH + " bgcolor=\"#FF0000\">&nbsp;</td>");
		pw.format(SWATCH + " bgcolor=\"#FF0000\" background=\"%sbg_checkered.png\">&nbsp;</td>", serverImagesUrl);
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating\">");
		pw.print("<b>1</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating\">");
		pw.print("<b>2</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating\">");
		pw.print("<b>3</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating\">");
		pw.print("<b>4</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating\">");
		pw.print("<b>5</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating\">%s</p>", lang.getCaamlBundleString("dangerRating.low"));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating\">%s</p>", lang.getCaamlBundleString("dangerRating.moderate"));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating\">%s</p>", lang.getCaamlBundleString("dangerRating.considerable"));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating\">%s</p>", lang.getCaamlBundleString("dangerRating.high"));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating\">%s</p>", lang.getCaamlBundleString("dangerRating.very_high"));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// general headline
		getGeneralHeadline().ifPresent(generalHeadline -> {
			pw.print(TABLE + " class=\"body-wrap\" bgcolor=\"#FFFFFF\">");
			pw.print("<tr>");
			pw.print("<td class=\"container\" style=\"padding: 15px 0;\" bgcolor=\"#FFFFFF\">");
			pw.print("<div class=\"content\">");
			pw.format("<h2>%s</h2>", generalHeadline);
			pw.print("</div>");
			pw.print("</td>");
			pw.print("</tr>");
			pw.print("</table>");
		});

		for (AvalancheBulletin bulletin : bulletins.getBulletins()) {
			appendBulletin(pw, bulletin, color);
		}

		// footer
		pw.print(TABLE + " class=\"footer-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td class=\"container\" style=\"padding: 15px 0;\">");
		pw.print("<div class=\"content\">");
		pw.print(TABLE + ">");
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
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td class=\"container\" style=\"padding: 15px 0;\">");
		pw.print("<div class=\"content\">");
		pw.print(TABLE + ">");
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
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("<img height=\"4\" style=\"width: 100%;\" src=\"" + ci + "\" alt=\"\"/>");
		pw.print("</body>");
		pw.print("</html>");

		pw.flush();
		return out.toString();
	}

	private Optional<String> getGeneralHeadline() {
		return Optional.ofNullable(bulletins.getCustomData())
			.map(AvalancheBulletinsCustomData::ALBINA)
			.map(AvalancheBulletinsCustomData.ALBINA::generalHeadline);
	}

	private void appendOverviewMap(PrintWriter pw, String website, String map) {
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<p class=\"last\" style=\"text-align: center;\">");
		pw.format("<a href=\"%s\">", website);
		pw.format("<img width=\"600\" style=\"max-width: 600px;\" src=\"%s\" alt=\"%s\"/>", map,
			lang.getCaamlBundleString("forecast.label"));
		pw.print("</a>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
	}

	static String css(String resourceName) {
		try {
			URL resource = Resources.getResource(resourceName);
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
		DangerRatingValue highestDangerRating = Objects.requireNonNullElse(bulletin.highestDangerRating(), DangerRatingValue.no_rating);

		pw.print(TABLE + " class=\"body-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td class=\"container\" style=\"padding: 15px 0;\" bgcolor=\"#FFFFFF\">");
		pw.print("<div class=\"content\">");
		pw.print(TABLE + ">");
		pw.print("<tr>");
		pw.print(dangerRatingColorCell(highestDangerRating, region));
		pw.print("</td>");
		pw.print("<td>");
		pw.print(TABLE + ">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<h2 %s>%s</h2>", getHeadlineStyle(highestDangerRating),
			lang.getCaamlBundleString("dangerRating." + highestDangerRating.name() + ".long"));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		if (bulletin.hasDaytimeDependency()) {
			appendDaytime(pw, bulletin, ValidTimePeriod.earlier, DaytimeDependency.am, lang.getCaamlBundleString("validTimePeriod.earlier"));
			appendDaytime(pw, bulletin, ValidTimePeriod.later, DaytimeDependency.pm, lang.getCaamlBundleString("validTimePeriod.later"));
		} else {
			appendDaytime(pw, bulletin, ValidTimePeriod.all_day, DaytimeDependency.fd, null);
		}

		pw.print(TABLE + " style=\"padding-left: 15px;\">");
		pw.print("<tr>");
		pw.print("<td style=\"vertical-align: top; padding-top: 15px;\">");
		String highlights = Optional.ofNullable(bulletin.getHighlights()).orElse("");
		if (!highlights.isBlank()) {
			pw.format("<h4 class=\"highlights\">%s</h4>", highlights);
		}
		pw.format("<h4>%s</h4>", highlights(bulletin.getAvalancheActivity()).orElse(""));
		pw.format("<p>%s</p>", comment(bulletin.getAvalancheActivity()).orElse(""));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// snowpack structure / danger patterns / synopsis / tendency
		List<DangerPattern> dangerPatterns = dangerPatterns(bulletin);
		Optional<String> snowpackStructureComment = comment(bulletin.getSnowpackStructure());
		Optional<String> snowpackStructureHighlights = highlights(bulletin.getSnowpackStructure());
		Optional<String> tendencyComment = tendencyComment(bulletin);
		Optional<String> synopsisComment = comment(bulletin.getWeatherForecast());
		TendencyType tendency = tendency(bulletin);
		boolean hasTendency = tendency != null || tendencyComment.isPresent();
		boolean hasStructure = !dangerPatterns.isEmpty()
			|| snowpackStructureComment.isPresent() || snowpackStructureHighlights.isPresent();
		boolean hasSnowpackSection = hasStructure || hasTendency;

		String snowpackStructureHeadline = hasStructure ? lang.getCaamlBundleString("snowpack.label") : "";
		String snowpackStructureCommentText = hasStructure ? snowpackStructureComment.orElse("") : "";
		String dangerPatternsHeadline = hasStructure && !dangerPatterns.isEmpty() ? lang.getCaamlBundleString("dangerPattern.label") : "";

		if (hasSnowpackSection) {
			pw.print(TABLE + " style=\"width: 100%; background-color: #f6fafc;\">");
			pw.print("<tr>");
			pw.format("<td width=\"10\" bgcolor=\"#%s\" style=\"font-size: 0; line-height: 0;\">&nbsp;</td>", color);
			pw.print("<td style=\"vertical-align: top; padding: 15px;\">");
			pw.format("<h4>%s</h4>", snowpackStructureHeadline);
			pw.format("<h5 class=\"inline\">%s</h5>", dangerPatternsHeadline);
			if (hasStructure) {
				dangerPatterns.forEach(dangerPattern -> appendDangerPattern(pw, dangerPattern));
			}
			snowpackStructureHighlights.ifPresent(text -> pw.format("<h5>%s</h5>", text));
			pw.format("<p>%s</p>", snowpackStructureCommentText);
			if (synopsisComment.isPresent()) {
				pw.format("<h4 style=\"padding-top: 15px;\">%s</h4>", lang.getCaamlBundleString("synopsis.label"));
				pw.format("<p>%s</p>", synopsisComment.get());
			}
			if (hasTendency) {
				pw.format("<h4 style=\"padding-top: 15px;\">%s</h4>", lang.getCaamlBundleString("tendency.label"));
				appendTendency(pw, tendency);
				tendencyComment.ifPresent(comment -> pw.format("<p>%s</p>", comment));
			}
			pw.print("</td>");
			pw.print("</tr>");
			pw.print("</table>");
		}
		pw.print("</div>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
	}

	private void appendDaytime(PrintWriter pw, AvalancheBulletin bulletin, ValidTimePeriod validTimePeriod,
			DaytimeDependency daytimeDependency, String heading) {
		Region region = avalancheReport.getRegion();
		String map = avalancheReport.getMapsUrl() + "/"
			+ MapUtil.filename(region, bulletin.getBulletinID(), daytimeDependency, false, MapImageFormat.jpg);

		pw.print(TABLE + " style=\"padding-left: 15px;\">");
		pw.print("<tr>");
		if (heading != null) {
			pw.print("<td colspan=\"3\" class=\"daytime-text-div\">");
			pw.format("<h2 class=\"daytime-text\">%s</h2>", heading);
			pw.print("</td>");
			pw.print("</tr>");
			pw.print("<tr>");
		}
		pw.format("<td style=\"width: 150px; padding-right: 10px; %s\">", ROW_BORDER);
		pw.format("<img width=\"150\" src=\"%s\" alt=\"%s\"/>", map, regions(bulletin));
		pw.print("</td>");
		pw.format("<td style=\"width: 60px; vertical-align: middle; %s\">", ROW_BORDER);
		pw.format("<img height=\"48\" width=\"60\" src=\"%s\" alt=\"%s\"/>", dangerRatingSymbol(bulletin, validTimePeriod),
			lang.getCaamlBundleString("dangerRating.label"));
		pw.print("</td>");
		pw.format("<td style=\"vertical-align: middle; padding-left: 10px; %s\">", ROW_BORDER);
		pw.format("<p class=\"small\"><b>%s</b></p>", lang.getElevationString(bulletin.dangerRatingElevation(validTimePeriod), true));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		bulletin.avalancheProblems(validTimePeriod).forEach(problem -> appendAvalancheProblem(pw, problem));
	}

	private void appendAvalancheProblem(PrintWriter pw, AvalancheProblem problem) {
		Region region = avalancheReport.getRegion();
		String serverImagesUrl = region.getServerImagesUrl();
		AvalancheProblemType problemType = problem.getProblemType();
		String avalancheType = problem.albinaAvalancheType();

		String symbol = serverImagesUrl + eu.albina.model.enumerations.AvalancheProblem.getSymbolPath(problemType, false);
		String text = lang.getCaamlBundleString("avalancheProblem." + problemType.name());
		String link = getAvalancheProblemLink(lang, region, problemType);
		String aspects = serverImagesUrl + Aspect.getSymbolPath(org.caaml.v6.Aspect.bitmask(problem.aspects()), false);
		String elevationSymbol = serverImagesUrl + eu.albina.model.AvalancheProblem.getElevationSymbolPath(
			problem.upperBound() != null, problem.lowerBound() != null) + ".png";

		pw.print(TABLE + " style=\"padding-left: 15px;\">");
		pw.print("<tr>");
		pw.print(dangerRatingColorCell(problem.getDangerRatingValue(), region));
		pw.format("<td style=\"width: 70px; text-align: center; %s\">", PROBLEM_CELL);
		pw.format("<a href=\"%s\" target=\"_blank\">", link);
		pw.format("<img width=\"50\" src=\"%s\" alt=\"\"/>", symbol);
		pw.print("</a>");
		pw.format("<p class=\"small\">%s</p>", text);
		pw.print("</td>");
		pw.format("<td style=\"width: 70px; text-align: center; %s\">", PROBLEM_CELL);
		pw.format("<img width=\"60\" height=\"60\" src=\"%s\" alt=\"%s\"/>", aspects, aspectsText(problem));
		pw.print("</td>");
		pw.format("<td style=\"width: 80px; text-align: center; %s\">", PROBLEM_CELL);
		pw.format("<img width=\"80\" height=\"48\" src=\"%s\" alt=\"\"/>", elevationSymbol);
		pw.print("</td>");
		pw.format("<td style=\"width: 60px; %s\">", PROBLEM_CELL);
		appendElevationLimit(pw, lang.getElevationString(problem.upperBound(), true));
		appendElevationLimit(pw, lang.getElevationString(problem.lowerBound(), true));
		pw.print("</td>");
		pw.format("<td style=\"width: 30px; text-align: center; %s\">%s</td>", PROBLEM_CELL,
			avalancheType != null ? "\u2192" : "");
		pw.format("<td style=\"%s\">", PROBLEM_CELL);
		if (avalancheType != null) {
			pw.format("<p class=\"small\"><b>%s</b></p>", lang.getCaamlBundleString("avalancheType." + avalancheType));
		}
		EawsMatrixInformation.getMatrixParameters(lang, problem).forEach((label, value) ->
			pw.format("<p class=\"small\">%s: %s</p>", label, value));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
	}

	private static void appendElevationLimit(PrintWriter pw, String elevation) {
		if (!elevation.isEmpty()) {
			pw.format("<p class=\"small\"><b>%s</b></p>", elevation);
		}
	}

	private static String regions(AvalancheBulletin bulletin) {
		return bulletin.getRegions().stream().map(org.caaml.v6.Region::getName).collect(Collectors.joining(", "));
	}

	private String aspectsText(AvalancheProblem problem) {
		return problem.aspects().stream()
			.map(aspect -> lang.getCaamlBundleString("aspect." + aspect.name()))
			.collect(Collectors.joining(", "));
	}

	private static List<DangerPattern> dangerPatterns(AvalancheBulletin bulletin) {
		return bulletin.dangerPatterns().stream()
			.map(DangerPattern::fromString)
			.filter(Objects::nonNull)
			.toList();
	}

	private static TendencyType tendency(AvalancheBulletin bulletin) {
		return bulletin.getTendency().stream()
			.map(org.caaml.v6.Tendency::getTendencyType)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	private static Optional<String> tendencyComment(AvalancheBulletin bulletin) {
		return bulletin.getTendency().stream()
			.map(org.caaml.v6.Tendency::getHighlights)
			.filter(Objects::nonNull)
			.findFirst();
	}

	private static Optional<String> highlights(Texts texts) {
		return Optional.ofNullable(texts).map(Texts::getHighlights);
	}

	private static Optional<String> comment(Texts texts) {
		return Optional.ofNullable(texts).map(Texts::getComment);
	}

	private String dangerRatingSymbol(AvalancheBulletin bulletin, ValidTimePeriod validTimePeriod) {
		return avalancheReport.getRegion().getServerImagesUrl() + "warning_pictos/color/level_"
			+ DangerRatingValue.level(bulletin.dangerRating(validTimePeriod, false)) + "_"
			+ DangerRatingValue.level(bulletin.dangerRating(validTimePeriod, true)) + ".png";
	}

	/**
	 * The coloured bar in front of a bulletin or an avalanche problem. An empty cell collapses in
	 * Outlook, hence the space.
	 */
	private static String dangerRatingColorCell(DangerRatingValue dangerRating, Region region) {
		if (DangerRatingValue.level(dangerRating) == 0) {
			return "<td width=\"10\" style=\"font-size: 0; line-height: 0;\">&nbsp;</td>";
		}
		String background = dangerRating == DangerRatingValue.very_high
			? " background=\"" + region.getServerImagesUrl() + "bg_checkered.png\""
			: "";
		return String.format("<td width=\"10\" bgcolor=\"%s\"%s style=\"font-size: 0; line-height: 0;\">&nbsp;</td>",
			dangerRating.color(), background);
	}

	/** The light colours of the low danger levels are legible as a background only. */
	private static String getHeadlineStyle(DangerRatingValue dangerRating) {
		String color = dangerRating.level() >= 3
			? "color: " + dangerRating.color()
			: "color: #565F61; background-color: " + dangerRating.color();
		return String.format("style=\"padding-left: 15px; %s;\"", color);
	}

	private void appendTendency(PrintWriter pw, TendencyType tendency) {
		if (tendency == null) {
			return;
		}
		pw.format("<p><b>%s</b> %s</p>", lang.getCaamlBundleString("tendency." + tendency.name()),
			avalancheReport.getTendencyDate(lang));
	}

	private void appendDangerPattern(PrintWriter pw, DangerPattern dangerPattern) {
		if (dangerPattern == null) {
			return;
		}
		pw.format("<a href=\"%s\" target=\"_blank\"><span class=\"danger-pattern\">%s</span></a>",
			getDangerPatternLink(lang, avalancheReport.getRegion(), dangerPattern),
			dangerPattern.toString(lang.getLocale()));
	}

}
