package eu.albina.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

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

	/**
	 * Create static widgets containing overview information for the EUREGIO for
	 * print media.
	 * 
	 * @param bulletins
	 *            The bulletins to create the PDF of.
	 */
	public void createSimpleHtml(List<AvalancheBulletin> bulletins) {
		for (LanguageCode lang : GlobalVariables.languages)
			createSimpleHtml(bulletins, lang);
	}

	// LANG
	public String createSimpleHtml(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		try {
			if (bulletins != null && !bulletins.isEmpty()) {
				// Create data model
				Map<String, Object> root = new HashMap<>();

				Map<String, Object> text = new HashMap<>();
				text.put("tabtitle",
						GlobalVariables.getSimpleHtmlTitle(lang) + AlbinaUtil.getShortDate(bulletins, lang));
				text.put("title", GlobalVariables.getTitle(lang));
				text.put("subtitle", AlbinaUtil.getDate(bulletins, lang));
				String publicationDate = AlbinaUtil.getPublicationDate(bulletins, lang);
				text.put("publicationDate", publicationDate);
				if (publicationDate.isEmpty())
					text.put("publishedAt", "");
				else
					text.put("publishedAt", GlobalVariables.getPublishedText(lang));
				text.put("regions", GlobalVariables.getRegionsHeadline(lang));
				text.put("snowpack", GlobalVariables.getSnowpackHeadline(lang));
				text.put("tendency", GlobalVariables.getTendencyHeadline(lang));
				root.put("text", text);

				Map<String, Object> link = new HashMap<>();
				link.put("website", GlobalVariables.avalancheReportBaseUrl + "bulletin/"
						+ AlbinaUtil.getValidityDate(bulletins) + "?lang=" + lang.toString());
				root.put("link", link);

				ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
				for (AvalancheBulletin avalancheBulletin : bulletins) {
					Map<String, Object> bulletin = new HashMap<>();

					if (avalancheBulletin.getPublishedRegions() != null
							&& !avalancheBulletin.getPublishedRegions().isEmpty()) {
						StringBuilder sb = new StringBuilder();
						for (String region : avalancheBulletin.getPublishedRegions()) {
							sb.append(region);
							sb.append(", ");
						}
						sb.delete(sb.length() - 2, sb.length());
						bulletin.put("regions", sb.toString());

						bulletin.put("forenoon", getDaytime(avalancheBulletin, false, lang));
						if (avalancheBulletin.isHasDaytimeDependency()) {
							bulletin.put("afternoon", getDaytime(avalancheBulletin, true, lang));
							bulletin.put("am", GlobalVariables.getAMText(lang));
							bulletin.put("pm", GlobalVariables.getPMText(lang));
						} else {
							bulletin.put("afternoon", getEmptyDaytime());
							bulletin.put("am", "");
							bulletin.put("pm", "");
						}

						bulletin.put("avAvalancheHighlights", avalancheBulletin.getAvActivityHighlightsIn(lang));
						bulletin.put("avAvalancheComment", avalancheBulletin.getAvActivityCommentIn(lang));
						if (avalancheBulletin.getDangerPattern1() != null)
							bulletin.put("dangerPattern1",
									AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern1(), lang)
											+ "<br>");
						else
							bulletin.put("dangerPattern1", "");
						if (avalancheBulletin.getDangerPattern2() != null)
							bulletin.put("dangerPattern2",
									AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern2(), lang)
											+ "<br>");
						else
							bulletin.put("dangerPattern2", "");
						if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null)
							bulletin.put("snowpackStructureComment",
									avalancheBulletin.getSnowpackStructureCommentIn(lang));
						else
							bulletin.put("snowpackStructureComment", "");
						bulletin.put("tendencyComment", avalancheBulletin.getTendencyCommentIn(lang));
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
			} else
				return null;
		} catch (IOException e) {
			logger.error("Confirmation email could not be created: " + e.getMessage());
			e.printStackTrace();
		} catch (TemplateException e) {
			logger.error("Confirmation email could not be created: " + e.getMessage());
			e.printStackTrace();
		}

		return null;
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

	private Map<String, Object> getDaytime(AvalancheBulletin avalancheBulletin, boolean afternoon, LanguageCode lang) {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> dangerLevel = new HashMap<>();
		Map<String, Object> text = new HashMap<>();

		text.put("dangerLevel", "<b>" + GlobalVariables.getDangerRatingHeadline(lang) + "</b><br>");

		AvalancheBulletinDaytimeDescription daytimeDescription;
		if (afternoon)
			daytimeDescription = avalancheBulletin.getAfternoon();
		else
			daytimeDescription = avalancheBulletin.getForenoon();

		if (daytimeDescription.getAvalancheSituation1() != null || daytimeDescription.getAvalancheSituation2() != null)
			text.put("avalancheProblem", "<b>" + GlobalVariables.getAvalancheProblemsHeadline(lang) + "</b><br>");
		else
			text.put("avalancheProblem", "");
		result.put("text", text);

		dangerLevel.put("above", getDangerRatingLineString(daytimeDescription.getDangerRatingAbove(),
				avalancheBulletin.getElevation(), avalancheBulletin.getTreeline(), lang, true));
		if (avalancheBulletin.isHasElevationDependency())
			dangerLevel.put("below", getDangerRatingLineString(daytimeDescription.getDangerRatingBelow(),
					avalancheBulletin.getElevation(), avalancheBulletin.getTreeline(), lang, false));
		else
			dangerLevel.put("below", "");

		result.put("dangerLevel", dangerLevel);

		if (daytimeDescription.getAvalancheSituation1() != null
				&& daytimeDescription.getAvalancheSituation1().getAvalancheSituation() != null) {
			result.put("avalancheProblem1",
					getAvalancheSituationString(daytimeDescription.getAvalancheSituation1(), lang));
		} else
			result.put("avalancheProblem1", "");
		if (daytimeDescription.getAvalancheSituation2() != null
				&& daytimeDescription.getAvalancheSituation2().getAvalancheSituation() != null) {
			result.put("avalancheProblem2",
					getAvalancheSituationString(daytimeDescription.getAvalancheSituation2(), lang));
		} else
			result.put("avalancheProblem2", "");

		return result;
	}

	private String getAvalancheSituationString(AvalancheSituation avalancheSituation, LanguageCode lang) {
		StringBuilder sb = new StringBuilder();
		sb.append(avalancheSituation.getAvalancheSituation().toString(lang));
		if (avalancheSituation.getTreelineHigh() || avalancheSituation.getElevationHigh() > 0) {
			if (avalancheSituation.getTreelineHigh()) {
				sb.append(GlobalVariables.getTreelinePreString(true, lang));
				sb.append(GlobalVariables.getTreelineStringLowercase(lang));
			} else if (avalancheSituation.getElevationHigh() > -1) {
				sb.append(GlobalVariables.getElevationPreString(true, lang));
				sb.append(avalancheSituation.getElevationHigh());
				sb.append("m");
			}
		}
		if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
			if (avalancheSituation.getTreelineLow()) {
				sb.append(GlobalVariables.getTreelinePreString(true, lang));
				sb.append(GlobalVariables.getTreelineStringLowercase(lang));
			} else if (avalancheSituation.getElevationLow() > -1) {
				sb.append(GlobalVariables.getElevationPreString(true, lang));
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
			LanguageCode lang, boolean above) {
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
		sb.append(GlobalVariables.getDangerRatingTextMiddle(dangerRating, lang));
		sb.append("</");
		sb.append(tag);
		sb.append(">");

		if (treeline) {
			sb.append(GlobalVariables.getTreelinePreString(above, lang));
			sb.append(GlobalVariables.getTreelineStringLowercase(lang));
		} else if (elevation > 0) {
			sb.append(GlobalVariables.getElevationPreString(above, lang));
			sb.append(elevation);
			sb.append("m");
		}

		sb.append("<br>");

		return sb.toString();
	}
}
