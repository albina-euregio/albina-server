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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.albina.model.AvalancheReport;
import eu.albina.map.MapImageFormat;
import eu.albina.map.MapUtil;
import eu.albina.model.enumerations.DaytimeDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.EawsMatrixInformation;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public record SimpleHtmlUtil(AvalancheReport avalancheReport, LanguageCode lang) {

	private static final Logger logger = LoggerFactory.getLogger(SimpleHtmlUtil.class);

	/** Language codes linked in the header, in display order. */
	private static final List<String> LINKED_LANGUAGES = List.of("de", "it", "en", "es", "ca", "ar");

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
		pw.format("<html lang=\"%s\">\n", lang);
		pw.format("<head>\n");
		pw.format("<meta charset=\"utf-8\">\n");
		pw.format("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
		pw.format("<title>%s %s</title>\n", region.getWebsiteName(lang), avalancheReport.getDate(lang));
		pw.format("<style>%s</style>\n", EmailUtil.css("templates/SimpleHtmlUtil.css"));
		pw.format("</head>\n");
		pw.format("<body>\n");

		pw.format("<header>\n");
		pw.format("<nav class=\"language-nav\">\n");
		pw.format("<a href=\"%s\">%s</a>\n", website, lang.getBundleString("standard.link.text"));
		pw.format("<span>%s</span>\n", LINKED_LANGUAGES.stream()
			.map(code -> String.format("<a href=\"%s_%s.html\">%s</a>", prefix, code, code.toUpperCase()))
			.collect(Collectors.joining("\n")));
		pw.format("</nav>\n");
		pw.format("<h1><a href=\"%s\">%s</a></h1>\n", website, region.getWebsiteName(lang));
		pw.format("<p class=\"date\">%s</p>\n", avalancheReport.getDate(lang));
		if (!publicationDate.isEmpty()) {
			pw.format("<p class=\"published\">%s<b>%s</b></p>\n", lang.getBundleString("published"), publicationDate);
		}
		pw.format("<nav class=\"day-nav\">\n");
		pw.format("<a rel=\"prev\" href=\"%s\">← %s</a>\n", previousDayLink, avalancheReport.getPreviousValidityDateString(lang));
		pw.format("<a rel=\"next\" href=\"%s\">%s →</a>\n", nextDayLink, avalancheReport.getNextValidityDateString(lang));
		pw.format("</nav>\n");
		pw.format("</header>\n");

		pw.format("<main>\n");
		for (AvalancheBulletin bulletin : avalancheReport.getBulletins()) {
			if (bulletin.getPublishedRegions() == null || bulletin.getPublishedRegions().isEmpty()) {
				continue;
			}
			appendBulletin(pw, bulletin);
		}
		pw.format("</main>\n");

		pw.format("</body>\n</html>\n");
		pw.flush();
		return out.toString();
	}

	private void appendBulletin(PrintWriter pw, AvalancheBulletin bulletin) {
		DangerRating dangerRating = bulletin.getHighestDangerRating();
		pw.format("<article id=\"%s\"%s>\n", bulletin.getId(), borderColor(dangerRating));
		appendDangerLevel(pw, dangerRating);
		if (bulletin.isHasDaytimeDependency()) {
			appendDaytime(pw, bulletin, bulletin.getForenoon(), DaytimeDependency.am, lang.getCaamlBundleString("validTimePeriod.earlier"));
			appendDaytime(pw, bulletin, bulletin.getAfternoon(), DaytimeDependency.pm, lang.getCaamlBundleString("validTimePeriod.later"));
		} else {
			appendDaytime(pw, bulletin, bulletin.getForenoon(), DaytimeDependency.fd, null);
		}

		String highlights = bulletin.getHighlightsIn(lang).orElse("");
		if (!highlights.isBlank()) {
			pw.format("<section class=\"highlights\">\n");
			pw.format("<h3>%s</h3>\n", highlights);
			pw.format("</section>\n");
		}
		appendTextBlock(pw, "avalanche-activity", bulletin.getAvActivityHighlightsIn(lang).orElse(""),
			bulletin.getAvActivityCommentIn(lang).orElse(""));
		String dangerPatterns = Stream.of(bulletin.getDangerPattern1(), bulletin.getDangerPattern2())
			.filter(Objects::nonNull)
			.map(dangerPattern -> dangerPattern.toString(lang.getLocale()))
			.collect(Collectors.joining("<br>"));
		appendTextBlock(pw, "snowpack", lang.getCaamlBundleString("snowpack.label"),
			dangerPatterns, bulletin.getSnowpackStructureHighlightsIn(lang).orElse(""),
			bulletin.getSnowpackStructureCommentIn(lang).orElse(""));
		appendTextBlock(pw, "tendency", lang.getCaamlBundleString("tendency.label"),
			bulletin.getTendencyCommentIn(lang).orElse(""));
		pw.format("</article>\n");
	}

	/** The headline of a bulletin, colored like the danger level headlines on the website. */
	private void appendDangerLevel(PrintWriter pw, DangerRating dangerRating) {
		if (dangerRating == null || dangerRating == DangerRating.missing) {
			return;
		}
		// the light colors of the low danger levels are legible as a background only
		String style = DangerRating.getInt(dangerRating) >= 3
			? String.format("color: %s", dangerRating.getColor())
			: String.format("background-color: %s", dangerRating.getColor());
		pw.format("<h2 class=\"danger-level\" style=\"%s\">%s</h2>\n", style,
			dangerRating.toString(lang.getLocale(), true));
	}

	/** Writes a section with the heading and the non-empty paragraphs, or nothing at all if there is no text. */
	private void appendTextBlock(PrintWriter pw, String cssClass, String heading, String... paragraphs) {
		List<String> texts = Stream.of(paragraphs).filter(text -> !text.isBlank()).toList();
		if (texts.isEmpty()) {
			return;
		}
		pw.format("<section class=\"%s\">\n", cssClass);
		if (!heading.isBlank()) {
			pw.format("<h3>%s</h3>\n", heading);
		}
		texts.forEach(text -> pw.format("<p>%s</p>\n", text));
		pw.format("</section>\n");
	}

	private void appendDaytime(PrintWriter pw, AvalancheBulletin bulletin,
			AvalancheBulletinDaytimeDescription daytimeDescription, DaytimeDependency daytimeDependency, String heading) {
		String regions = bulletin.getPublishedRegions().stream().map(lang::getRegionName).collect(Collectors.joining(", "));
		String map = avalancheReport.getMapsUrl() + "/"
			+ MapUtil.filename(avalancheReport.getRegion(), bulletin, daytimeDependency, false, MapImageFormat.webp);
		String warningPicto = DataURL.ofResource("images/warning_pictos/color/level_" + daytimeDescription.getWarningLevelId() + ".webp");
		String dangerRatingLabel = lang.getCaamlBundleString("dangerRating.label");
		String elevation = getElevationString(daytimeDescription.getElevation(), daytimeDescription.getTreeline());
		List<AvalancheProblem> problems = daytimeDescription.getAvalancheProblems().stream()
			.filter(problem -> problem != null && problem.getAvalancheProblem() != null)
			.toList();

		pw.format("<section class=\"daytime\">\n");
		if (heading != null) {
			pw.format("<h3>%s</h3>\n", heading);
		}
		pw.format("<img class=\"map\" src=\"%s\" alt=\"%s\" loading=\"lazy\">\n", map, regions);
		pw.format("<h4>%s</h4>\n", dangerRatingLabel);
		pw.format("<div class=\"danger-rating\">\n");
		pw.format("<img class=\"icon\" src=\"%s\" alt=\"%s\">\n", warningPicto, dangerRatingLabel);
		if (!elevation.isEmpty()) {
			pw.format("<span>%s</span>\n", elevation);
		}
		pw.format("</div>\n");
		if (!problems.isEmpty()) {
			pw.format("<h4>%s</h4>\n", lang.getCaamlBundleString("avalancheProblem.label"));
			pw.format("<ul class=\"problems\">\n");
			problems.forEach(problem -> appendProblem(pw, problem));
			pw.format("</ul>\n");
		}
		pw.format("</section>\n");
	}

	private void appendProblem(PrintWriter pw, AvalancheProblem avalancheProblem) {
		eu.albina.model.enumerations.AvalancheProblem problem = avalancheProblem.getAvalancheProblem();
		String aspects = avalancheProblem.getAspects().stream()
			.map(aspect -> aspect.toString(lang.getLocale()))
			.collect(Collectors.joining(", "));
		String elevation = Stream.of(avalancheProblem.getElevationHighText(lang), avalancheProblem.getElevationLowText(lang))
			.filter(text -> !text.isEmpty())
			.collect(Collectors.joining("<br>"));

		pw.format("<li class=\"problem\"%s>\n", dangerRatingAttributes(avalancheProblem));
		pw.format("<figure>\n");
		pw.format("<img class=\"icon\" src=\"%s\" alt=\"\">\n", problem.getDataURL());
		pw.format("<figcaption>%s</figcaption>\n", problem.toString(lang.getLocale()));
		pw.format("</figure>\n");
		pw.format("<img class=\"icon\" src=\"%s\" alt=\"%s\">\n", Aspect.getDataURL(avalancheProblem.getAspects(), false), aspects);
		pw.format("<div class=\"problem-elevation\">\n");
		pw.format("<img class=\"icon\" src=\"%s\" alt=\"\">\n", avalancheProblem.getElevationDataURL());
		if (!elevation.isEmpty()) {
			pw.format("<span>%s</span>\n", elevation);
		}
		pw.format("</div>\n");
		appendMatrix(pw, avalancheProblem);
		pw.format("</li>\n");
	}

	/** Colors the bar in front of an avalanche problem according to its danger rating. */
	private String dangerRatingAttributes(AvalancheProblem avalancheProblem) {
		EawsMatrixInformation matrix = avalancheProblem.getEawsMatrixInformation();
		DangerRating dangerRating = matrix != null ? matrix.getDangerRating() : null;
		if (dangerRating == null) {
			return "";
		}
		return borderColor(dangerRating) + String.format(" title=\"%s: %s\"",
			lang.getCaamlBundleString("dangerRating.label"), dangerRating.toString(lang.getLocale(), false));
	}

	/** Colors the bar in front of a bulletin or an avalanche problem according to the given danger rating. */
	private String borderColor(DangerRating dangerRating) {
		if (dangerRating == null || dangerRating == DangerRating.missing) {
			return "";
		}
		return String.format(" style=\"border-color: %s\"", dangerRating.getColor());
	}

	private void appendMatrix(PrintWriter pw, AvalancheProblem avalancheProblem) {
		Map<String, String> parameters = avalancheProblem.getMatrixParameters(lang);
		if (parameters.isEmpty()) {
			return;
		}
		pw.format("<span class=\"arrow\" aria-hidden=\"true\">\u2192</span>\n");
		pw.format("<div class=\"problem-matrix\">\n");
		pw.format("<p class=\"avalanche-type\">%s</p>\n", avalancheProblem.getAvalancheType().toString(lang.getLocale()));
		pw.format("<dl class=\"matrix\">\n");
		parameters.forEach((label, value) -> pw.format("<dt>%s:</dt>\n<dd>%s</dd>\n", label, value));
		pw.format("</dl>\n");
		pw.format("</div>\n");
	}

	private String getElevationString(int elevation, boolean treeline) {
		if (treeline) {
			return lang.getCaamlBundleString("elevation.treeline");
		} else if (elevation > 0) {
			return elevation + "m";
		} else {
			return "";
		}
	}
}
