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
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerRating;
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

	public boolean createOverviewSimpleHtml(List<AvalancheBulletin> bulletins) {
		boolean result = true;
		for (LanguageCode lang : GlobalVariables.languages) {
			if (!createSimpleHtml(bulletins, lang, ""))
				result = false;
		}
		return result;
	}

	public boolean createRegionSimpleHtml(List<AvalancheBulletin> bulletins, String region) {
		boolean result = true;
		ArrayList<AvalancheBulletin> regionBulletins = new ArrayList<AvalancheBulletin>();
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			if (avalancheBulletin.affectsRegionOnlyPublished(region))
				regionBulletins.add(avalancheBulletin);
		}

		if (!regionBulletins.isEmpty())
			for (LanguageCode lang : GlobalVariables.languages) {
				if (!createSimpleHtml(regionBulletins, lang, region))
					result = false;
				if (!createSimpleHtml(regionBulletins, lang, region))
					result = false;
			}
		return result;
	}

	public boolean createSimpleHtml(List<AvalancheBulletin> bulletins, LanguageCode lang, String region) {
		try {
			if (bulletins != null && !bulletins.isEmpty()) {
				String simpleHtmlString = createSimpleHtmlString(bulletins, lang, region);

				String filename;
				String validityDateString = AlbinaUtil.getValidityDateString(bulletins);

				if (region != null && !region.isEmpty())
					filename = region + "_" + lang.toString() + ".html";
				else
					filename = lang.toString() + ".html";

				String dirPath = GlobalVariables.getHtmlDirectory() + "/" + validityDateString;
				new File(dirPath).mkdirs();

				// using PosixFilePermission to set file permissions 755
				Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
				// add owners permission
				perms.add(PosixFilePermission.OWNER_READ);
				perms.add(PosixFilePermission.OWNER_WRITE);
				perms.add(PosixFilePermission.OWNER_EXECUTE);
				// add group permissions
				perms.add(PosixFilePermission.GROUP_READ);
				perms.add(PosixFilePermission.GROUP_EXECUTE);
				// add others permissions
				perms.add(PosixFilePermission.OTHERS_READ);
				perms.add(PosixFilePermission.OTHERS_EXECUTE);

				try {
					Files.setPosixFilePermissions(Paths.get(dirPath), perms);
				} catch (IOException | UnsupportedOperationException e) {
					logger.warn("File permissions could not be set!");
				}

				File newHtmlFile = new File(dirPath + "/" + filename);
				FileUtils.writeStringToFile(newHtmlFile, simpleHtmlString, StandardCharsets.UTF_8);
				AlbinaUtil.setFilePermissions(dirPath + "/" + filename);

				return true;
			} else
				return false;
		} catch (IOException | TemplateException e) {
			logger.error("Simple html could not be created", e);
		}

		return false;
	}

	public String createSimpleHtmlString(List<AvalancheBulletin> bulletins, LanguageCode lang, String region)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			TemplateException {
		ResourceBundle messages = lang.getBundle("i18n.MessagesBundle");

		// Create data model
		Map<String, Object> root = new HashMap<>();

		Map<String, Object> text = new HashMap<>();
		text.put("standardView", messages.getString("avalanche-report.standard.link.text"));
		text.put("tabtitle",
				messages.getString("avalanche-report.name") + " " + AlbinaUtil.getDate(bulletins, messages));
		text.put("title", messages.getString("title"));
		text.put("subtitle", AlbinaUtil.getDate(bulletins, messages));
		String publicationDate = AlbinaUtil.getPublicationDate(bulletins, messages);
		text.put("publicationDate", publicationDate);

		text.put("previousDay", " &#8592; " + AlbinaUtil.getPreviousValidityDateString(bulletins, messages));
		text.put("nextDay", AlbinaUtil.getNextValidityDateString(bulletins, messages) + " &#8594;");

		if (publicationDate.isEmpty())
			text.put("publishedAt", "");
		else
			text.put("publishedAt", messages.getString("published"));
		text.put("regions", messages.getString("headline.regions"));
		text.put("snowpack", messages.getString("headline.snowpack"));
		text.put("tendency", messages.getString("headline.tendency"));
		root.put("text", text);

		Map<String, Object> link = new HashMap<>();
		link.put("website", messages.getString("avalanche-report.url") + "/bulletin/"
				+ AlbinaUtil.getValidityDateString(bulletins));
		link.put("previousDay", AlbinaUtil.getPreviousDayLink(bulletins, lang, region, messages));
		link.put("nextDay", AlbinaUtil.getNextDayLink(bulletins, lang, region, messages));
		link.put("linkDe", GlobalVariables.getAvalancheReportSimpleBaseUrl(messages)
				+ AlbinaUtil.getValidityDateString(bulletins) + "/de.html");
		link.put("linkIt", GlobalVariables.getAvalancheReportSimpleBaseUrl(messages)
				+ AlbinaUtil.getValidityDateString(bulletins) + "/it.html");
		link.put("linkEn", GlobalVariables.getAvalancheReportSimpleBaseUrl(messages)
				+ AlbinaUtil.getValidityDateString(bulletins) + "/en.html");

		root.put("link", link);

		ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			Map<String, Object> bulletin = new HashMap<>();

			if (avalancheBulletin.getPublishedRegions() != null && !avalancheBulletin.getPublishedRegions().isEmpty()) {

				// maps
				if (avalancheBulletin.isHasDaytimeDependency()) {
					bulletin.put("mapAM", GlobalVariables.getMapsUrl(messages) + "/"
							+ avalancheBulletin.getValidityDateString() + "/" + avalancheBulletin.getId() + ".jpg");
					bulletin.put("mapPM", GlobalVariables.getMapsUrl(messages) + "/"
							+ avalancheBulletin.getValidityDateString() + "/" + avalancheBulletin.getId() + "_PM.jpg");
					bulletin.put("widthPM", "width=\"150\"");
				} else {
					bulletin.put("mapAM", GlobalVariables.getMapsUrl(messages) + "/"
							+ avalancheBulletin.getValidityDateString() + "/" + avalancheBulletin.getId() + ".jpg");
					bulletin.put("mapPM", GlobalVariables.getServerImagesUrl() + "empty.png");
					bulletin.put("widthPM", "");
				}

				StringBuilder sb = new StringBuilder();
				for (String publishedRegion : avalancheBulletin.getPublishedRegions()) {
					switch (lang) {
					case de:
						sb.append(AlbinaUtil.regionsMapDe.get(publishedRegion));
						break;
					case it:
						sb.append(AlbinaUtil.regionsMapIt.get(publishedRegion));
						break;
					case en:
						sb.append(AlbinaUtil.regionsMapEn.get(publishedRegion));
						break;
					default:
						sb.append(AlbinaUtil.regionsMapEn.get(publishedRegion));
						break;
					}
					sb.append(", ");
				}
				sb.delete(sb.length() - 2, sb.length());
				bulletin.put("regions", sb.toString());

				bulletin.put("forenoon", getDaytime(avalancheBulletin, false, lang, messages));
				if (avalancheBulletin.isHasDaytimeDependency()) {
					bulletin.put("afternoon", getDaytime(avalancheBulletin, true, lang, messages));
					bulletin.put("am", "<b>" + messages.getString("daytime.am.capitalized").toUpperCase() + "</b><br>");
					bulletin.put("pm", "<b>" + messages.getString("daytime.pm.capitalized").toUpperCase() + "</b><br>");
				} else {
					bulletin.put("afternoon", getEmptyDaytime());
					bulletin.put("am", "");
					bulletin.put("pm", "");
				}

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
							AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern1(), lang) + "<br>");
				else
					bulletin.put("dangerPattern1", "");
				if (avalancheBulletin.getDangerPattern2() != null)
					bulletin.put("dangerPattern2",
							AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern2(), lang) + "<br>");
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
		Template temp = cfg.getTemplate("simple-bulletin.min.html");

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
		dangerLevel.put("above", "");
		dangerLevel.put("below", "");
		result.put("dangerLevel", dangerLevel);
		result.put("avalancheProblem1", "");
		result.put("avalancheProblem2", "");
		return result;
	}

	private Map<String, Object> getDaytime(AvalancheBulletin avalancheBulletin, boolean afternoon, LanguageCode lang,
			ResourceBundle messages) {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> dangerLevel = new HashMap<>();
		Map<String, Object> text = new HashMap<>();

		text.put("dangerLevel", "<b>" + messages.getString("headline.danger-rating") + "</b><br>");

		AvalancheBulletinDaytimeDescription daytimeDescription;
		if (afternoon)
			daytimeDescription = avalancheBulletin.getAfternoon();
		else
			daytimeDescription = avalancheBulletin.getForenoon();

		if ((daytimeDescription.getAvalancheSituation1() != null
				&& daytimeDescription.getAvalancheSituation1().getAvalancheSituation() != null)
				|| (daytimeDescription.getAvalancheSituation2() != null
						&& daytimeDescription.getAvalancheSituation2().getAvalancheSituation() != null))
			text.put("avalancheProblem", "<b>" + messages.getString("headline.avalanche-problem") + "</b><br>");
		else
			text.put("avalancheProblem", "");
		result.put("text", text);

		dangerLevel.put("above", getDangerRatingLineString(daytimeDescription.getDangerRatingAbove(),
				avalancheBulletin.getElevation(), avalancheBulletin.getTreeline(), lang, messages, true));
		if (avalancheBulletin.isHasElevationDependency())
			dangerLevel.put("below", getDangerRatingLineString(daytimeDescription.getDangerRatingBelow(),
					avalancheBulletin.getElevation(), avalancheBulletin.getTreeline(), lang, messages, false));
		else
			dangerLevel.put("below", "");

		result.put("dangerLevel", dangerLevel);

		if (daytimeDescription.getAvalancheSituation1() != null
				&& daytimeDescription.getAvalancheSituation1().getAvalancheSituation() != null) {
			result.put("avalancheProblem1",
					getAvalancheSituationString(daytimeDescription.getAvalancheSituation1(), lang, messages));
		} else
			result.put("avalancheProblem1", "");
		if (daytimeDescription.getAvalancheSituation2() != null
				&& daytimeDescription.getAvalancheSituation2().getAvalancheSituation() != null) {
			result.put("avalancheProblem2",
					getAvalancheSituationString(daytimeDescription.getAvalancheSituation2(), lang, messages));
		} else
			result.put("avalancheProblem2", "");

		return result;
	}

	private String getAvalancheSituationString(AvalancheSituation avalancheSituation, LanguageCode lang,
			ResourceBundle messages) {
		StringBuilder sb = new StringBuilder();
		sb.append(avalancheSituation.getAvalancheSituation().toString(lang.getLocale()));
		if (avalancheSituation.getTreelineHigh() || avalancheSituation.getElevationHigh() > 0) {
			if (avalancheSituation.getTreelineHigh()) {
				sb.append(messages.getString("elevation.treeline.below.pre-string"));
				sb.append(messages.getString("elevation.treeline"));
			} else if (avalancheSituation.getElevationHigh() > 0) {
				sb.append(messages.getString("elevation.below.pre-string"));
				sb.append(avalancheSituation.getElevationHigh());
				sb.append("m");
			}
		}
		if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
			if (avalancheSituation.getTreelineLow()) {
				sb.append(messages.getString("elevation.treeline.above.pre-string"));
				sb.append(messages.getString("elevation.treeline"));
			} else if (avalancheSituation.getElevationLow() > 0) {
				sb.append(messages.getString("elevation.above.pre-string"));
				sb.append(avalancheSituation.getElevationLow());
				sb.append("m");
			}
		}

		if (avalancheSituation.getAspects() != null && !avalancheSituation.getAspects().isEmpty()) {
			sb.append(", ");
			Set<Aspect> aspects = avalancheSituation.getAspects();
			ArrayList<Aspect> array = new ArrayList<Aspect>(aspects);
			Collections.sort(array);
			sb.append(array.get(0).toUpperCaseString());
			for (int i = 1; i < array.size(); i++) {
				sb.append("-");
				sb.append(array.get(i).toUpperCaseString());
			}
		}
		sb.append("<br>");
		return sb.toString();
	}

	private String getDangerRatingLineString(DangerRating dangerRating, int elevation, boolean treeline,
			LanguageCode lang, ResourceBundle messages, boolean above) {
		StringBuilder sb = new StringBuilder();
		String tag;
		switch (dangerRating) {
		case low:
			tag = "rating1";
			break;
		case moderate:
			tag = "rating2";
			break;
		case considerable:
			tag = "rating3";
			break;
		case high:
			tag = "rating4";
			break;
		case very_high:
			tag = "rating5";
			break;
		default:
			tag = "rating0";
			break;
		}
		sb.append("<");
		sb.append(tag);
		sb.append(">");
		sb.append(dangerRating.toString(lang.getLocale(), true));
		sb.append("</");
		sb.append(tag);
		sb.append(">");

		if (treeline) {
			if (above)
				messages.getString("elevation.treeline.above.pre-string");
			else
				messages.getString("elevation.treeline.below.pre-string");
			sb.append(messages.getString("elevation.treeline"));
		} else if (elevation > 0) {
			if (above)
				sb.append(messages.getString("elevation.above.pre-string"));
			else
				sb.append(messages.getString("elevation.below.pre-string"));
			sb.append(elevation);
			sb.append("m");
		}

		sb.append("<br>");

		return sb.toString();
	}
}
