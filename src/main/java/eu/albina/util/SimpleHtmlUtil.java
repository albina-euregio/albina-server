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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import eu.albina.model.enumerations.DaytimeDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

public class SimpleHtmlUtil {

	private static final Logger logger = LoggerFactory.getLogger(SimpleHtmlUtil.class);

	private static Configuration cfg;

	private static SimpleHtmlUtil instance = null;

	public static final Color blueColor = new Color(0, 172, 251);
	public static final Color greyLightColor = new Color(201, 201, 201);
	public static final Color greyDarkColor = new Color(85, 95, 96);
	public static final Color whiteColor = new Color(255, 255, 255);
	public static final Color greyVeryVeryLightColor = new Color(242, 247, 250);
	public static final Color dangerLevel1Color = new Color(197, 255, 118);
	public static final Color dangerLevel2Color = new Color(255, 255, 70);
	public static final Color dangerLevel3Color = new Color(255, 152, 44);
	public static final Color dangerLevel4Color = new Color(255, 0, 23);
	public static final Color dangerLevel5ColorRed = new Color(255, 0, 23);
	public static final Color dangerLevel5ColorBlack = new Color(0, 0, 0);

	protected SimpleHtmlUtil() throws IOException, URISyntaxException {
		createFreemarkerConfigurationInstance();
	}

	private void createFreemarkerConfigurationInstance() throws IOException, URISyntaxException {
		cfg = new Configuration(Configuration.VERSION_2_3_27);

		// Specify the source where the template files come from. Here I set a
		// plain directory for it, but non-file-system sources are possible too:
		URL resource = this.getClass().getResource("/templates");

		URI uri = resource.toURI();
		File file = new File(uri);
		cfg.setDirectoryForTemplateLoading(file);

		// Set the preferred charset template files are stored in. UTF-8 is
		// a good choice in most applications:
		cfg.setDefaultEncoding("UTF-8");

		// Sets how errors will appear.
		// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is
		// better.
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
		cfg.setLogTemplateExceptions(false);

		// Wrap unchecked exceptions thrown during template processing into
		// TemplateException-s.
		cfg.setWrapUncheckedExceptions(true);
	}

	public Configuration getFreeMarkerConfiguration() {
		return cfg;
	}

	public static SimpleHtmlUtil getInstance() throws IOException, URISyntaxException {
		if (instance == null) {
			instance = new SimpleHtmlUtil();
		}
		return instance;
	}

	public boolean createRegionSimpleHtml(AvalancheReport avalancheReport) {
		boolean result = true;

		if (!avalancheReport.getBulletins().isEmpty())
			for (LanguageCode lang : avalancheReport.getRegion().getEnabledLanguages()) {
				if (!createSimpleHtml(avalancheReport, lang))
					result = false;
			}
		return result;
	}

	public boolean createSimpleHtml(AvalancheReport avalancheReport, LanguageCode lang) {
		try {
			if (avalancheReport.getBulletins() != null && !avalancheReport.getBulletins().isEmpty()) {
				String simpleHtmlString = createSimpleHtmlString(avalancheReport, lang);
				String filename = avalancheReport.getRegion().getId() + "_" + lang.toString() + ".html";
				Path dirPath = avalancheReport.getHtmlDirectory();
				Files.createDirectories(dirPath);
				Path newHtmlFile = dirPath.resolve(filename);
				Files.writeString(newHtmlFile, simpleHtmlString, StandardCharsets.UTF_8);
				return true;
			} else
				return false;
		} catch (IOException | TemplateException e) {
			logger.error("Simple html could not be created", e);
		}

		return false;
	}

	public String createSimpleHtmlString(AvalancheReport avalancheReport, LanguageCode lang)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			TemplateException {
		// Create data model
		List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();
		Region region = avalancheReport.getRegion();
		ServerInstance serverInstance = avalancheReport.getServerInstance();
		Map<String, Object> root = new HashMap<>();

		Map<String, Object> text = new HashMap<>();
		text.put("standardView", lang.getBundleString("standard.link.text"));
		text.put("tabtitle", lang.getBundleString("website.name") + " " + avalancheReport.getDate(lang));
		text.put("title", lang.getBundleString("website.name"));
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
		link.put("website", lang.getBundleString("website.url") + "/bulletin/"
				+ avalancheReport.getValidityDateString());
		link.put("previousDay", LinkUtil.getBulletinLink(avalancheReport, lang, region, Period.ofDays(-1), serverInstance));
		link.put("nextDay", LinkUtil.getBulletinLink(avalancheReport, lang, region, Period.ofDays(1), serverInstance));
		String prefix = LinkUtil.getSimpleHtmlUrl(lang, region, serverInstance) + "/"
			+ avalancheReport.getValidityDateString() + "/" + region.getId();
		link.put("linkDe", prefix + "_de.html");
		link.put("linkIt", prefix + "_it.html");
		link.put("linkEn", prefix + "_en.html");
		link.put("linkEs", prefix + "_es.html");
		link.put("linkCa", prefix + "_ca.html");
		link.put("linkAr", prefix + "_ar.html");

		root.put("link", link);

		ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			Map<String, Object> bulletin = new HashMap<>();

			if (avalancheBulletin.getPublishedRegions() != null && !avalancheBulletin.getPublishedRegions().isEmpty()) {

				// maps
				if (avalancheBulletin.isHasDaytimeDependency()) {
					bulletin.put("mapAMjpg", LinkUtil.getMapsUrl(lang, region, serverInstance) + "/"
							+ avalancheBulletin.getValidityDateString() + "/" + MapUtil.filename(region, avalancheBulletin, DaytimeDependency.am, false, MapImageFormat.jpg));
					bulletin.put("mapAMwebp", LinkUtil.getMapsUrl(lang, region, serverInstance) + "/"
							+ avalancheBulletin.getValidityDateString() + "/" + MapUtil.filename(region, avalancheBulletin, DaytimeDependency.am, false, MapImageFormat.webp));
					bulletin.put("mapPMjpg", LinkUtil.getMapsUrl(lang, region, serverInstance) + "/"
							+ avalancheBulletin.getValidityDateString() + "/" + MapUtil.filename(region, avalancheBulletin, DaytimeDependency.pm, false, MapImageFormat.jpg));
					bulletin.put("mapPMwebp", LinkUtil.getMapsUrl(lang, region, serverInstance) + "/"
							+ avalancheBulletin.getValidityDateString() + "/" + MapUtil.filename(region, avalancheBulletin, DaytimeDependency.pm, false, MapImageFormat.webp));
					bulletin.put("widthPM", "width=\"150\"");
					bulletin.put("heightPMSmall", "height=\"50\"");
					bulletin.put("fontSize", "");
				} else {
					bulletin.put("mapAMjpg", LinkUtil.getMapsUrl(lang, region, serverInstance) + "/"
							+ avalancheBulletin.getValidityDateString() + "/" + MapUtil.filename(region, avalancheBulletin, DaytimeDependency.fd, false, MapImageFormat.jpg));
					bulletin.put("mapAMwebp", LinkUtil.getMapsUrl(lang, region, serverInstance) + "/"
							+ avalancheBulletin.getValidityDateString() + "/" + MapUtil.filename(region, avalancheBulletin, DaytimeDependency.fd, false, MapImageFormat.webp));
					bulletin.put("mapPMjpg", serverInstance.getServerImagesUrl() + "empty.png");
					bulletin.put("mapPMwebp", serverInstance.getServerImagesUrl() + "empty.webp");
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

				bulletin.put("forenoon", getDaytime(avalancheBulletin.getForenoon(), lang, serverInstance));
				if (avalancheBulletin.isHasDaytimeDependency()) {
					bulletin.put("afternoon", getDaytime(avalancheBulletin.getAfternoon(), lang, serverInstance));
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
		Template temp = cfg.getTemplate(region.getSimpleHtmlTemplateName());

		// Merge template and model
		Writer out = new StringWriter();
		// Writer out = new OutputStreamWriter(System.out);
		temp.process(root, out);
		return out.toString();
	}

	private Map<String, Object> getEmptyDaytime() {
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

	private Map<String, Object> getEmptyAvalancheProblem() {
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

	private Map<String, Object> getDaytime(AvalancheBulletinDaytimeDescription daytimeDescription, LanguageCode lang, ServerInstance serverInstance) {
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

		dangerLevel.put("warningPicto", serverInstance.getServerImagesUrl() + "warning_pictos/color/level_"
				+ AlbinaUtil.getWarningLevelId(daytimeDescription) + ".png");
		dangerLevel.put("elevation",
				getElevationString(daytimeDescription.getElevation(), daytimeDescription.getTreeline(), lang));

		result.put("dangerLevel", dangerLevel);

		if (daytimeDescription.getAvalancheProblem1() != null
				&& daytimeDescription.getAvalancheProblem1().getAvalancheProblem() != null) {
			result.put("avalancheProblem1", getAvalancheProblem(daytimeDescription.getAvalancheProblem1(), lang, serverInstance));
		} else
			result.put("avalancheProblem1", getEmptyAvalancheProblem());
		if (daytimeDescription.getAvalancheProblem2() != null
				&& daytimeDescription.getAvalancheProblem2().getAvalancheProblem() != null) {
			result.put("avalancheProblem2", getAvalancheProblem(daytimeDescription.getAvalancheProblem2(), lang, serverInstance));
		} else
			result.put("avalancheProblem2", getEmptyAvalancheProblem());
		if (daytimeDescription.getAvalancheProblem3() != null
				&& daytimeDescription.getAvalancheProblem3().getAvalancheProblem() != null) {
			result.put("avalancheProblem3", getAvalancheProblem(daytimeDescription.getAvalancheProblem3(), lang, serverInstance));
		} else
			result.put("avalancheProblem3", getEmptyAvalancheProblem());
		if (daytimeDescription.getAvalancheProblem4() != null
				&& daytimeDescription.getAvalancheProblem4().getAvalancheProblem() != null) {
			result.put("avalancheProblem4", getAvalancheProblem(daytimeDescription.getAvalancheProblem4(), lang, serverInstance));
		} else
			result.put("avalancheProblem4", getEmptyAvalancheProblem());
		if (daytimeDescription.getAvalancheProblem5() != null
				&& daytimeDescription.getAvalancheProblem5().getAvalancheProblem() != null) {
			result.put("avalancheProblem5", getAvalancheProblem(daytimeDescription.getAvalancheProblem5(), lang, serverInstance));
		} else
			result.put("avalancheProblem5", getEmptyAvalancheProblem());

		return result;
	}

	private Map<String, Object> getAvalancheProblem(AvalancheProblem avalancheProblem, LanguageCode lang, ServerInstance serverInstance) {
		Map<String, Object> result = new HashMap<>();

		result.put("exist", true);
		result.put("avalancheProblemIcon", serverInstance.getServerImagesUrl()
				+ avalancheProblem.getAvalancheProblem().getSymbolPath(false));
		result.put("avalancheProblemText", avalancheProblem.getAvalancheProblem().toString(lang.getLocale()));
		result.put("elevationIcon", serverInstance.getServerImagesUrl() + getElevationIcon(avalancheProblem));
		result.put("elevationLow", getElevationLowText(avalancheProblem, lang));
		result.put("elevationHigh", getElevationHighText(avalancheProblem, lang));
		result.put("aspectsIcon", serverInstance.getServerImagesUrl()
				+ Aspect.getSymbolPath(avalancheProblem.getAspects(), false));

		return result;
	}

	private String getElevationIcon(AvalancheProblem avalancheProblem) {
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

	private String getElevationLowText(AvalancheProblem avalancheProblem, LanguageCode lang) {
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

	private String getElevationHighText(AvalancheProblem avalancheProblem, LanguageCode lang) {
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

	private String getElevationString(int elevation, boolean treeline, LanguageCode lang) {
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
