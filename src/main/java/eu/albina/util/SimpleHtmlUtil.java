// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.albina.model.AvalancheReport;
import eu.albina.map.MapImageFormat;
import eu.albina.map.MapUtil;
import eu.albina.model.LocalServerInstance;
import eu.albina.model.enumerations.DaytimeDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public interface SimpleHtmlUtil {

	Logger logger = LoggerFactory.getLogger(SimpleHtmlUtil.class);

	Color blueColor = new Color(0, 172, 251);
	Color greyLightColor = new Color(201, 201, 201);
	Color greyDarkColor = new Color(85, 95, 96);
	Color whiteColor = new Color(255, 255, 255);
	Color greyVeryVeryLightColor = new Color(242, 247, 250);
	Color dangerLevel1Color = new Color(197, 255, 118);
	Color dangerLevel2Color = new Color(255, 255, 70);
	Color dangerLevel3Color = new Color(255, 152, 44);
	Color dangerLevel4Color = new Color(255, 0, 23);
	Color dangerLevel5ColorRed = new Color(255, 0, 23);
	Color dangerLevel5ColorBlack = new Color(0, 0, 0);

	private static Configuration createFreemarkerConfigurationInstance()  {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
		cfg.setClassForTemplateLoading(EmailUtil.class, "/templates");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);
		return cfg;
	}

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
		} catch (IOException | TemplateException e) {
			logger.error("Simple html could not be created", e);
		}

	}

	static String createSimpleHtmlString(AvalancheReport avalancheReport, LanguageCode lang)
			throws IOException,
			TemplateException {
		// Create data model
		List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();
		Region region = avalancheReport.getRegion();
		LocalServerInstance serverInstance = avalancheReport.getServerInstance();
		Map<String, Object> root = new HashMap<>();

		Map<String, Object> text = new HashMap<>();
		text.put("standardView", lang.getBundleString("standard.link.text"));
		text.put("tabtitle", region.getWebsiteName(lang) + " " + avalancheReport.getDate(lang));
		text.put("title", region.getWebsiteName(lang));
		text.put("subtitle", avalancheReport.getDate(lang));
		String publicationDate = avalancheReport.getPublicationDate(lang);
		text.put("publicationDate", publicationDate);

		text.put("previousDay", " &#8592; " + avalancheReport.getPreviousValidityDateString(lang));
		text.put("nextDay", avalancheReport.getNextValidityDateString(lang) + " &#8594;");

		if (publicationDate.isEmpty())
			text.put("publishedAt", "");
		else
			text.put("publishedAt", lang.getBundleString("published"));
		text.put("regions", lang.getBundleString("headline.regions"));
		text.put("snowpack", lang.getBundleString("headline.snowpack"));
		text.put("tendency", lang.getBundleString("headline.tendency"));
		root.put("text", text);

		Map<String, Object> link = new HashMap<>();
		link.put("website", region.getWebsiteUrlWithDate(lang, avalancheReport));
		link.put("previousDay", String.format("%s/%s/%s_%s.html", avalancheReport.getSimpleHtmlUrl(), avalancheReport.getValidityDateString(Period.ofDays(-1)), region.getId(), lang));
		link.put("nextDay", String.format("%s/%s/%s_%s.html", avalancheReport.getSimpleHtmlUrl(), avalancheReport.getValidityDateString(Period.ofDays(1)), region.getId(), lang));
		String prefix = avalancheReport.getSimpleHtmlUrl() + "/"
			+ avalancheReport.getValidityDateString() + "/" + region.getId();
		link.put("linkDe", prefix + "_de.html");
		link.put("linkIt", prefix + "_it.html");
		link.put("linkEn", prefix + "_en.html");
		link.put("linkEs", prefix + "_es.html");
		link.put("linkCa", prefix + "_ca.html");
		link.put("linkAr", prefix + "_ar.html");

		root.put("link", link);

		ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			Map<String, Object> bulletin = new HashMap<>();

			if (avalancheBulletin.getPublishedRegions() != null && !avalancheBulletin.getPublishedRegions().isEmpty()) {

				// maps
				if (avalancheBulletin.isHasDaytimeDependency()) {
					bulletin.put("mapAMjpg", avalancheReport.getMapsUrl() + "/"
							+ MapUtil.filename(region, avalancheBulletin, DaytimeDependency.am, false, MapImageFormat.jpg));
					bulletin.put("mapAMwebp", avalancheReport.getMapsUrl() + "/"
							+ MapUtil.filename(region, avalancheBulletin, DaytimeDependency.am, false, MapImageFormat.webp));
					bulletin.put("mapPMjpg", avalancheReport.getMapsUrl() + "/"
							+ MapUtil.filename(region, avalancheBulletin, DaytimeDependency.pm, false, MapImageFormat.jpg));
					bulletin.put("mapPMwebp", avalancheReport.getMapsUrl() + "/"
							+ MapUtil.filename(region, avalancheBulletin, DaytimeDependency.pm, false, MapImageFormat.webp));
					bulletin.put("widthPM", "width=\"150\"");
					bulletin.put("heightPMSmall", "height=\"50\"");
					bulletin.put("fontSize", "");
				} else {
					bulletin.put("mapAMjpg", avalancheReport.getMapsUrl() + "/"
							+ MapUtil.filename(region, avalancheBulletin, DaytimeDependency.fd, false, MapImageFormat.jpg));
					bulletin.put("mapAMwebp", avalancheReport.getMapsUrl() + "/"
							+ MapUtil.filename(region, avalancheBulletin, DaytimeDependency.fd, false, MapImageFormat.webp));
					bulletin.put("mapPMjpg", region.getServerImagesUrl() + "empty.png");
					bulletin.put("mapPMwebp", region.getServerImagesUrl() + "empty.webp");
					bulletin.put("widthPM", "width=\"0\"");
					bulletin.put("heightPMSmall", "style=\"height: 0; margin: 0\"");
					bulletin.put("fontSize", "style=\"font-size: 0\"");
				}

				StringBuilder sb = new StringBuilder();
				for (String publishedRegion : avalancheBulletin.getPublishedRegions()) {
					sb.append(lang.getRegionName(publishedRegion));
					sb.append(", ");
				}
				sb.delete(sb.length() - 2, sb.length());
				bulletin.put("regions", sb.toString());

				bulletin.put("forenoon", getDaytime(avalancheBulletin.getForenoon(), lang, region));
				if (avalancheBulletin.isHasDaytimeDependency()) {
					bulletin.put("afternoon", getDaytime(avalancheBulletin.getAfternoon(), lang, region));
					bulletin.put("am",
							"<b>" + lang.getBundleString("valid-time-period.earlier").toUpperCase() + "</b><br>");
					bulletin.put("pm",
							"<b>" + lang.getBundleString("valid-time-period.later").toUpperCase() + "</b><br>");
				} else {
					bulletin.put("afternoon", getEmptyDaytime());
					bulletin.put("am", "");
					bulletin.put("pm", "");
				}

				if (avalancheBulletin.getHighlightsIn(lang) != null
						&& !avalancheBulletin.getHighlightsIn(lang).isEmpty())
					bulletin.put("highlights", avalancheBulletin.getHighlightsIn(lang));
				else
					bulletin.put("highlights", "");
				if (avalancheBulletin.getAvActivityHighlightsIn(lang) != null
						&& !avalancheBulletin.getAvActivityHighlightsIn(lang).isEmpty())
					bulletin.put("avAvalancheHighlights", avalancheBulletin.getAvActivityHighlightsIn(lang));
				else
					bulletin.put("avAvalancheHighlights", "");
				if (avalancheBulletin.getAvActivityCommentIn(lang) != null
						&& !avalancheBulletin.getAvActivityCommentIn(lang).isEmpty())
					bulletin.put("avAvalancheComment", avalancheBulletin.getAvActivityCommentIn(lang));
				else
					bulletin.put("avAvalancheComment", "");
				if (avalancheBulletin.getDangerPattern1() != null)
					bulletin.put("dangerPattern1",
                        	avalancheBulletin.getDangerPattern1().toString(lang.getLocale()) + "<br>");
				else
					bulletin.put("dangerPattern1", "");
				if (avalancheBulletin.getDangerPattern2() != null)
					bulletin.put("dangerPattern2",
                        	avalancheBulletin.getDangerPattern2().toString(lang.getLocale()) + "<br>");
				else
					bulletin.put("dangerPattern2", "");
				if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null
						&& !avalancheBulletin.getSnowpackStructureCommentIn(lang).isEmpty())
					bulletin.put("snowpackStructureComment", avalancheBulletin.getSnowpackStructureCommentIn(lang));
				else
					bulletin.put("snowpackStructureComment", "");
				if (avalancheBulletin.getTendencyCommentIn(lang) != null
						&& !avalancheBulletin.getTendencyCommentIn(lang).isEmpty())
					bulletin.put("tendencyComment", avalancheBulletin.getTendencyCommentIn(lang));
				else
					bulletin.put("tendencyComment", "");
				arrayList.add(bulletin);
			}
		}
		root.put("bulletins", arrayList);

		// Get template
		Template temp = createFreemarkerConfigurationInstance().getTemplate(region.getSimpleHtmlTemplateName());

		// Merge template and model
		Writer out = new StringWriter();
		// Writer out = new OutputStreamWriter(System.out);
		temp.process(root, out);
		return out.toString();
	}

	private static Map<String, Object> getEmptyDaytime() {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> dangerLevel = new HashMap<>();
		Map<String, Object> text = new HashMap<>();
		text.put("dangerLevel", "");
		text.put("avalancheProblem", "");
		result.put("text", text);
		dangerLevel.put("elevation", "");
		dangerLevel.put("warningPicto", "");
		result.put("dangerLevel", dangerLevel);
		result.put("avalancheProblem1", getEmptyAvalancheProblem());
		result.put("avalancheProblem2", getEmptyAvalancheProblem());
		result.put("avalancheProblem3", getEmptyAvalancheProblem());
		result.put("avalancheProblem4", getEmptyAvalancheProblem());
		result.put("avalancheProblem5", getEmptyAvalancheProblem());
		return result;
	}

	private static Map<String, Object> getEmptyAvalancheProblem() {
		Map<String, Object> result = new HashMap<>();
		result.put("exist", false);
		result.put("avalancheProblemIcon", "");
		result.put("avalancheProblemText", "");
		result.put("elevationIcon", "");
		result.put("elevationLow", "");
		result.put("elevationHigh", "");
		result.put("aspectsIcon", "");
		return result;
	}

	private static Map<String, Object> getDaytime(AvalancheBulletinDaytimeDescription daytimeDescription, LanguageCode lang, Region region) {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> dangerLevel = new HashMap<>();
		Map<String, Object> text = new HashMap<>();

		text.put("dangerLevel", "<b>" + lang.getBundleString("headline.danger-rating") + "</b><br>");

		if ((daytimeDescription.getAvalancheProblem1() != null
				&& daytimeDescription.getAvalancheProblem1().getAvalancheProblem() != null)
				|| (daytimeDescription.getAvalancheProblem2() != null
						&& daytimeDescription.getAvalancheProblem2().getAvalancheProblem() != null)
				|| (daytimeDescription.getAvalancheProblem3() != null
						&& daytimeDescription.getAvalancheProblem3().getAvalancheProblem() != null)
				|| (daytimeDescription.getAvalancheProblem4() != null
						&& daytimeDescription.getAvalancheProblem4().getAvalancheProblem() != null)
				|| (daytimeDescription.getAvalancheProblem5() != null
						&& daytimeDescription.getAvalancheProblem5().getAvalancheProblem() != null))
			text.put("avalancheProblem", "<b>" + lang.getBundleString("headline.avalanche-problem") + "</b><br>");
		else
			text.put("avalancheProblem", "");
		result.put("text", text);

		dangerLevel.put("warningPicto", region.getServerImagesUrl() + "warning_pictos/color/level_"
				+ AlbinaUtil.getWarningLevelId(daytimeDescription) + ".png");
		dangerLevel.put("elevation",
				getElevationString(daytimeDescription.getElevation(), daytimeDescription.getTreeline(), lang));

		result.put("dangerLevel", dangerLevel);

		if (daytimeDescription.getAvalancheProblem1() != null
				&& daytimeDescription.getAvalancheProblem1().getAvalancheProblem() != null) {
			result.put("avalancheProblem1", getAvalancheProblem(daytimeDescription.getAvalancheProblem1(), lang, region));
		} else
			result.put("avalancheProblem1", getEmptyAvalancheProblem());
		if (daytimeDescription.getAvalancheProblem2() != null
				&& daytimeDescription.getAvalancheProblem2().getAvalancheProblem() != null) {
			result.put("avalancheProblem2", getAvalancheProblem(daytimeDescription.getAvalancheProblem2(), lang, region));
		} else
			result.put("avalancheProblem2", getEmptyAvalancheProblem());
		if (daytimeDescription.getAvalancheProblem3() != null
				&& daytimeDescription.getAvalancheProblem3().getAvalancheProblem() != null) {
			result.put("avalancheProblem3", getAvalancheProblem(daytimeDescription.getAvalancheProblem3(), lang, region));
		} else
			result.put("avalancheProblem3", getEmptyAvalancheProblem());
		if (daytimeDescription.getAvalancheProblem4() != null
				&& daytimeDescription.getAvalancheProblem4().getAvalancheProblem() != null) {
			result.put("avalancheProblem4", getAvalancheProblem(daytimeDescription.getAvalancheProblem4(), lang, region));
		} else
			result.put("avalancheProblem4", getEmptyAvalancheProblem());
		if (daytimeDescription.getAvalancheProblem5() != null
				&& daytimeDescription.getAvalancheProblem5().getAvalancheProblem() != null) {
			result.put("avalancheProblem5", getAvalancheProblem(daytimeDescription.getAvalancheProblem5(), lang, region));
		} else
			result.put("avalancheProblem5", getEmptyAvalancheProblem());

		return result;
	}

	private static Map<String, Object> getAvalancheProblem(AvalancheProblem avalancheProblem, LanguageCode lang, Region region) {
		Map<String, Object> result = new HashMap<>();

		result.put("exist", true);
		result.put("avalancheProblemIcon", region.getServerImagesUrl()
				+ avalancheProblem.getAvalancheProblem().getSymbolPath(false));
		result.put("avalancheProblemText", avalancheProblem.getAvalancheProblem().toString(lang.getLocale()));
		result.put("elevationIcon", region.getServerImagesUrl() + getElevationIcon(avalancheProblem));
		result.put("elevationLow", getElevationLowText(avalancheProblem, lang));
		result.put("elevationHigh", getElevationHighText(avalancheProblem, lang));
		result.put("aspectsIcon", region.getServerImagesUrl()
				+ Aspect.getSymbolPath(avalancheProblem.getAspects(), false));

		return result;
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
