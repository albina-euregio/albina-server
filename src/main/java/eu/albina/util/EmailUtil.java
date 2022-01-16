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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.albina.map.DaytimeDependency;
import eu.albina.map.MapUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.socialmedia.RapidMailProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;
import eu.albina.model.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.rapidmail.mailings.PostMailingsRequestPostFile;
import eu.albina.model.socialmedia.RapidMailConfig;
import eu.albina.model.socialmedia.RegionConfiguration;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class EmailUtil {

	private static EmailUtil instance = null;

	private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

	private static Configuration cfg;

	protected EmailUtil() throws IOException, URISyntaxException {
		createFreemarkerConfigurationInstance();
	}

	public static EmailUtil getInstance() throws IOException, URISyntaxException {
		if (instance == null) {
			instance = new EmailUtil();
		}
		return instance;
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

	public void sendBulletinEmails(List<AvalancheBulletin> bulletins, List<String> regions, boolean update, boolean test) {
		for (LanguageCode lang : LanguageCode.SOCIAL_MEDIA) {
			sendBulletinEmails(bulletins, regions, update, test, lang);
		}
	}

	public void sendBulletinEmails(List<AvalancheBulletin> bulletins, List<String> regions, boolean update, boolean test,
			LanguageCode lang) {
		logger.info("Sending {} bulletin email", lang);
		boolean daytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);
		String subject;
		if (update)
			subject = lang.getBundleString("email.subject.update") + AlbinaUtil.getDate(bulletins, lang);
		else
			subject = lang.getBundleString("email.subject") + AlbinaUtil.getDate(bulletins, lang);
		for (String region : regions) {
			ArrayList<AvalancheBulletin> regionBulletins = new ArrayList<AvalancheBulletin>();
			for (AvalancheBulletin avalancheBulletin : bulletins) {
				if (avalancheBulletin.affectsRegionOnlyPublished(region))
					regionBulletins.add(avalancheBulletin);
			}
			String emailHtml = createBulletinEmailHtml(regionBulletins, lang, region, update, daytimeDependency);
			sendBulletinEmailRapidmail(lang, region, emailHtml, subject, test);
		}
	}

	private String createZipFile(String htmlContent, String textContent) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream out = new ZipOutputStream(baos)) {
			if (htmlContent != null) {
				ZipEntry e = new ZipEntry("content.html");
				out.putNextEntry(e);
				byte[] data = htmlContent.getBytes(StandardCharsets.UTF_8);
				out.write(data, 0, data.length);
				out.closeEntry();
			}
			if (textContent != null) {
				ZipEntry e = new ZipEntry("content.txt");
				out.putNextEntry(e);
				byte[] data = textContent.getBytes(StandardCharsets.UTF_8);
				out.write(data, 0, data.length);
				out.closeEntry();
			}
			out.close();
			byte[] zipData = baos.toByteArray();
			return Base64.encodeBase64String(zipData);
		}
	}

	public void sendBulletinEmailRapidmail(LanguageCode lang, String region, String emailHtml, String subject, boolean test) {
		logger.info("Sending bulletin email in {} for {} ({} bytes)...", lang, region, emailHtml.getBytes(StandardCharsets.UTF_8).length);
		sendEmail(lang, region, emailHtml, subject, test);
	}

	public void sendBlogPostEmailRapidmail(LanguageCode lang, String region, String emailHtml, String subject, boolean test) {
		logger.info("Sending blog post email in {} for {} ({} bytes)...", lang, region, emailHtml.getBytes(StandardCharsets.UTF_8).length);
		sendEmail(lang, region, emailHtml, subject, test);
	}

	private void sendEmail(LanguageCode lang, String region, String emailHtml, String subject, boolean test) {
		try {
			RapidMailProcessorController rmc = RapidMailProcessorController.getInstance();
			RegionConfigurationController rcc = RegionConfigurationController.getInstance();
			RegionConfiguration regionConfiguration = rcc.getRegionConfiguration(region);
			RapidMailConfig rmConfig = regionConfiguration.getRapidMailConfig();

			PostMailingsRequestPostFile file = new PostMailingsRequestPostFile()
				.description("mail-content.zip")
				.type("application/zip")
				.content(createZipFile(emailHtml, null));
			PostMailingsRequest request = new PostMailingsRequest()
				.fromEmail(lang.getBundleString("avalanche-report.email"))
				.fromName(lang.getBundleString("avalanche-report.name"))
				.subject(subject)
				.status("scheduled")
				.file(file);
			rmc.sendMessage(rmConfig, lang, request, test);
		} catch (Exception e) {
			logger.error("Emails could not be sent in " + lang + " for " + region, e);
		}
	}

	public String createBulletinEmailHtml(List<AvalancheBulletin> bulletins, LanguageCode lang, String region,
			boolean update, boolean daytimeDependency) {
		try {
			// Create data model
			Map<String, Object> root = new HashMap<>();

			Map<String, Object> image = new HashMap<>();
			switch (lang) {
			case de:
				image.put("logo", GlobalVariables.getServerImagesUrl() + "logo/color/lawinen_report.png");
				break;
			case it:
				image.put("logo", GlobalVariables.getServerImagesUrl() + "logo/color/valanghe_report.png");
				break;
			case en:
				image.put("logo", GlobalVariables.getServerImagesUrl() + "logo/color/avalanche_report.png");
				break;
			default:
				image.put("logo", GlobalVariables.getServerImagesUrl() + "logo/color/avalanche_report.png");
				break;
			}
			image.put("dangerLevel5Style", getDangerLevel5Style());
			image.put("ci", GlobalVariables.getServerImagesUrl() + "logo/color/colorbar.gif");
			Map<String, Object> socialMediaImages = new HashMap<>();
			socialMediaImages.put("facebook", GlobalVariables.getServerImagesUrl() + "social_media/facebook.png");
			socialMediaImages.put("instagram", GlobalVariables.getServerImagesUrl() + "social_media/instagram.png");
			socialMediaImages.put("youtube", GlobalVariables.getServerImagesUrl() + "social_media/youtube.png");
			image.put("socialmedia", socialMediaImages);
			Map<String, Object> mapImage = new HashMap<>();

			// overview maps
			if (AlbinaUtil.hasDaytimeDependency(bulletins)) {
				mapImage.put("overview",
						LinkUtil.getMapsUrl(lang) + "/" + AlbinaUtil.getValidityDateString(bulletins) + "/"
								+ AlbinaUtil.getPublicationTime(bulletins) + "/"
								+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.am, false));
				mapImage.put("overviewPM",
						LinkUtil.getMapsUrl(lang) + "/" + AlbinaUtil.getValidityDateString(bulletins) + "/"
								+ AlbinaUtil.getPublicationTime(bulletins) + "/"
								+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.pm, false));
				mapImage.put("widthPM", "width=\"600\"");
			} else {
				if (daytimeDependency)
					mapImage.put("overview",
							LinkUtil.getMapsUrl(lang) + "/" + AlbinaUtil.getValidityDateString(bulletins) + "/"
									+ AlbinaUtil.getPublicationTime(bulletins) + "/"
									+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.am, false));
				else
					mapImage.put("overview",
							LinkUtil.getMapsUrl(lang) + "/" + AlbinaUtil.getValidityDateString(bulletins) + "/"
									+ AlbinaUtil.getPublicationTime(bulletins) + "/"
									+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.fd, false));
				mapImage.put("overviewPM", GlobalVariables.getServerImagesUrl() + "/empty.png");
				mapImage.put("widthPM", "");
			}

			image.put("map", mapImage);
			root.put("image", image);

			Map<String, Object> text = new HashMap<>();
			String publicationDate = AlbinaUtil.getPublicationDate(bulletins, lang);
			text.put("publicationDate", publicationDate);
			if (publicationDate.isEmpty())
				text.put("publishedAt", "");
			else
				text.put("publishedAt", lang.getBundleString("published"));
			text.put("date", AlbinaUtil.getDate(bulletins, lang));
			text.put("title", lang.getBundleString("headline"));
			if (update)
				text.put("headline", lang.getBundleString("headline.update"));
			else
				text.put("headline", lang.getBundleString("headline"));
			text.put("follow", lang.getBundleString("email.follow-us"));
			text.put("unsubscribe", lang.getBundleString("email.unsubscribe"));
			text.put("imprint", lang.getBundleString("email.imprint"));
			if (AlbinaUtil.hasDaytimeDependency(bulletins)) {
				text.put("am", lang.getBundleString("daytime.am.capitalized"));
				text.put("pm", lang.getBundleString("daytime.pm.capitalized"));
			} else {
				text.put("am", "");
				text.put("pm", "");
			}

			Map<String, Object> dangerRatings = new HashMap<>();
			dangerRatings.put("low", DangerRating.low.toString(lang.getLocale(), false));
			dangerRatings.put("moderate", DangerRating.moderate.toString(lang.getLocale(), false));
			dangerRatings.put("considerable", DangerRating.considerable.toString(lang.getLocale(), false));
			dangerRatings.put("high", DangerRating.high.toString(lang.getLocale(), false));
			dangerRatings.put("veryHigh", DangerRating.very_high.toString(lang.getLocale(), false));
			text.put("dangerRating", dangerRatings);

			root.put("text", text);

			ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
			for (AvalancheBulletin avalancheBulletin : bulletins) {
				Map<String, Object> bulletin = new HashMap<>();

				bulletin.put("stylepm", getPMStyle(avalancheBulletin.isHasDaytimeDependency()));
				bulletin.put("stylepmtable", getPMStyleTable(avalancheBulletin.isHasDaytimeDependency()));

				if (avalancheBulletin.isHasDaytimeDependency()) {
					bulletin.put("textam", lang.getBundleString("daytime.am.capitalized"));
					bulletin.put("textpm", lang.getBundleString("daytime.pm.capitalized"));
				} else {
					bulletin.put("textam", "");
					bulletin.put("textpm", "");
				}

				bulletin.put("warningLevelText",
						avalancheBulletin.getHighestDangerRating().toString(lang.getLocale(), true));

				if (avalancheBulletin.getAvActivityHighlightsIn(lang) != null)
					bulletin.put("avAvalancheHighlights", avalancheBulletin.getAvActivityHighlightsIn(lang));
				else
					bulletin.put("avAvalancheHighlights", "");

				if (avalancheBulletin.getAvActivityCommentIn(lang) != null)
					bulletin.put("avAvalancheComment", avalancheBulletin.getAvActivityCommentIn(lang));
				else
					bulletin.put("avAvalancheComment", "");

				if (avalancheBulletin.getDangerPattern1() != null || avalancheBulletin.getDangerPattern2() != null
						|| avalancheBulletin.getSnowpackStructureCommentIn(lang) != null
						|| avalancheBulletin.getSnowpackStructureHighlightsIn(lang) != null
						|| avalancheBulletin.getTendencyCommentIn(lang) != null) {
					bulletin.put("snowpackstyle", getSnowpackStyle(true));
					if (avalancheBulletin.getDangerPattern1() != null || avalancheBulletin.getDangerPattern2() != null
							|| avalancheBulletin.getSnowpackStructureCommentIn(lang) != null
							|| avalancheBulletin.getSnowpackStructureHighlightsIn(lang) != null) {
						bulletin.put("snowpackStructureHeadline", lang.getBundleString("headline.snowpack"));

						if (avalancheBulletin.getSnowpackStructureHighlightsIn(lang) != null)
							bulletin.put("snowpackStructureHighlights",
									avalancheBulletin.getSnowpackStructureHighlightsIn(lang));
						else
							bulletin.put("snowpackStructureHighlights", "");

						if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null)
							bulletin.put("snowpackStructureComment",
									avalancheBulletin.getSnowpackStructureCommentIn(lang));
						else
							bulletin.put("snowpackStructureComment", "");

						if (avalancheBulletin.getDangerPattern1() != null
								|| avalancheBulletin.getDangerPattern2() != null) {
							bulletin.put("dangerPatternsHeadline", lang.getBundleString("headline.danger-patterns"));
							if (avalancheBulletin.getDangerPattern1() != null) {
								bulletin.put("dangerPattern1",
										AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern1(), lang));
								bulletin.put("dangerPatternLink1", LinkUtil.getDangerPatternLink(lang,
										avalancheBulletin.getDangerPattern1()));
								bulletin.put("dangerpatternstyle1", getDangerPatternStyle(true));
							} else {
								bulletin.put("dangerPattern1", "");
								bulletin.put("dangerPatternLink1", "");
								bulletin.put("dangerpatternstyle1", getDangerPatternStyle(false));
							}
							if (avalancheBulletin.getDangerPattern2() != null) {
								bulletin.put("dangerPattern2",
										AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern2(), lang));
								bulletin.put("dangerPatternLink2", LinkUtil.getDangerPatternLink(lang,
										avalancheBulletin.getDangerPattern2()));
								bulletin.put("dangerpatternstyle2", getDangerPatternStyle(true));
							} else {
								bulletin.put("dangerPattern2", "");
								bulletin.put("dangerPatternLink2", "");
								bulletin.put("dangerpatternstyle2", getDangerPatternStyle(false));
							}
						} else {
							bulletin.put("dangerPatternsHeadline", "");
							bulletin.put("dangerpatternstyle1", getDangerPatternStyle(false));
							bulletin.put("dangerpatternstyle2", getDangerPatternStyle(false));
							bulletin.put("dangerPattern1", "");
							bulletin.put("dangerPattern2", "");
							bulletin.put("dangerPatternLink1", "");
							bulletin.put("dangerPatternLink2", "");
						}
					} else {
						bulletin.put("snowpackStructureHeadline", "");
						bulletin.put("snowpackStructureHighlights", "");
						bulletin.put("snowpackStructureComment", "");
						bulletin.put("dangerPatternsHeadline", "");
						bulletin.put("dangerPattern1", "");
						bulletin.put("dangerPattern2", "");
						bulletin.put("dangerPatternLink1", "");
						bulletin.put("dangerPatternLink2", "");
						bulletin.put("dangerpatternstyle1", getDangerPatternStyle(false));
						bulletin.put("dangerpatternstyle2", getDangerPatternStyle(false));
					}

					// tendency
					if (avalancheBulletin.getTendencyCommentIn(lang) != null) {
						bulletin.put("tendencyHeadline", lang.getBundleString("headline.tendency"));
						bulletin.put("tendencyComment", avalancheBulletin.getTendencyCommentIn(lang));
					} else {
						bulletin.put("tendencyHeadline", "");
						bulletin.put("tendencyComment", "");
					}
				} else {
					bulletin.put("snowpackstyle", getSnowpackStyle(false));
					bulletin.put("snowpackStructureHeadline", "");
					bulletin.put("snowpackStructureHighlights", "");
					bulletin.put("snowpackStructureComment", "");
					bulletin.put("dangerPatternsHeadline", "");
					bulletin.put("dangerPattern1", "");
					bulletin.put("dangerPattern2", "");
					bulletin.put("dangerPatternLink1", "");
					bulletin.put("dangerPatternLink2", "");
					bulletin.put("dangerpatternstyle1", getDangerPatternStyle(false));
					bulletin.put("dangerpatternstyle2", getDangerPatternStyle(false));
					bulletin.put("tendencyHeadline", "");
					bulletin.put("tendencyComment", "");
				}

				Map<String, Object> tendency = new HashMap<>();
				if (avalancheBulletin.getTendency() == null) {
					tendency.put("text", "");
				} else {
					tendency.put("text", avalancheBulletin.getTendency().toString(lang.getLocale()));
				}
				if (avalancheBulletin.getTendency() == Tendency.decreasing) {
					tendency.put("symbol",
							GlobalVariables.getServerImagesUrl() + "tendency/tendency_decreasing_blue.png");
					tendency.put("date", AlbinaUtil.getTendencyDate(bulletins, lang));
				} else if (avalancheBulletin.getTendency() == Tendency.steady) {
					tendency.put("symbol", GlobalVariables.getServerImagesUrl() + "tendency/tendency_steady_blue.png");
					tendency.put("date", AlbinaUtil.getTendencyDate(bulletins, lang));
				} else if (avalancheBulletin.getTendency() == Tendency.increasing) {
					tendency.put("symbol",
							GlobalVariables.getServerImagesUrl() + "tendency/tendency_increasing_blue.png");
					tendency.put("date", AlbinaUtil.getTendencyDate(bulletins, lang));
				} else {
					tendency.put("symbol", GlobalVariables.getServerImagesUrl() + "tendency/empty.png");
					tendency.put("date", "");
				}
				bulletin.put("tendency", tendency);

				bulletin.put("dangerratingcolorstyle",
						getDangerRatingColorStyle(avalancheBulletin.getHighestDangerRating()));
				bulletin.put("headlinestyle", getHeadlineStyle(avalancheBulletin.getHighestDangerRating()));

				addDaytimeInfo(lang, avalancheBulletin, bulletin, false, AlbinaUtil.getPublicationTime(bulletins));
				Map<String, Object> pm = new HashMap<>();
				if (avalancheBulletin.isHasDaytimeDependency())
					addDaytimeInfo(lang, avalancheBulletin, pm, true, AlbinaUtil.getPublicationTime(bulletins));
				else
					addDaytimeInfo(lang, avalancheBulletin, pm, false, AlbinaUtil.getPublicationTime(bulletins));
				bulletin.put("pm", pm);

				arrayList.add(bulletin);
			}
			root.put("bulletins", arrayList);

			Map<String, Object> links = new HashMap<>();
			links.put("website", LinkUtil.getBulletinUrl(bulletins, lang));
			links.put("unsubscribe", "{%link_unsubscribe}");
			links.put("pdf", LinkUtil.getPdfLink(bulletins, lang, region));
			links.put("imprint", LinkUtil.getImprintLink(lang));
			Map<String, Object> socialMediaLinks = new HashMap<>();
			socialMediaLinks.put("facebook", lang.getBundleString("avalanche-report.url") + "/#followDialog");
			socialMediaLinks.put("instagram", lang.getBundleString("avalanche-report.url") + "/#followDialog");
			socialMediaLinks.put("youtube", lang.getBundleString("avalanche-report.url") + "/#followDialog");
			links.put("socialmedia", socialMediaLinks);
			root.put("link", links);

			// Get template
			Template temp = cfg.getTemplate("albina-email.html");

			// Merge template and model
			Writer out = new StringWriter();
			// Writer out = new OutputStreamWriter(System.out);
			temp.process(root, out);

			return out.toString();
		} catch (IOException | TemplateException e) {
			logger.error("Bulletin email could not be created", e);
		}

		return null;
	}

	private void addDaytimeInfo(LanguageCode lang, AvalancheBulletin avalancheBulletin, Map<String, Object> bulletin,
			boolean isAfternoon, String publicationTime) {
		AvalancheBulletinDaytimeDescription daytimeBulletin;
		if (isAfternoon)
			daytimeBulletin = avalancheBulletin.getAfternoon();
		else
			daytimeBulletin = avalancheBulletin.getForenoon();

		// danger rating
		Map<String, Object> dangerRating = new HashMap<>();
		if ((daytimeBulletin.getDangerRatingBelow() == null
				|| daytimeBulletin.getDangerRatingBelow() == DangerRating.missing
				|| daytimeBulletin.getDangerRatingBelow() == DangerRating.no_rating)
				&& (daytimeBulletin.getDangerRatingAbove() == null
						|| daytimeBulletin.getDangerRatingAbove() == DangerRating.missing
						|| daytimeBulletin.getDangerRatingAbove() == DangerRating.no_rating)) {
			dangerRating.put("symbol", GlobalVariables.getServerImagesUrl() + "warning_pictos/color/level_0_0.png");
		} else {
			dangerRating.put("symbol", GlobalVariables.getServerImagesUrl() + "warning_pictos/color/level_"
					+ AlbinaUtil.getWarningLevelId(daytimeBulletin) + ".png");
		}
		// dangerRating.put("symbol", "cid:warning_picto/" +
		// getWarningLevelId(daytimeBulletin,
		// avalancheBulletin.isHasElevationDependency()));
		if (daytimeBulletin.isHasElevationDependency()
				&& (daytimeBulletin.getDangerRatingAbove() != daytimeBulletin.getDangerRatingBelow())) {
			if (daytimeBulletin.getTreeline())
				dangerRating.put("elevation", lang.getBundleString("elevation.treeline.capitalized"));
			else if (daytimeBulletin.getElevation() > 0)
				dangerRating.put("elevation", daytimeBulletin.getElevation() + "m");
			else
				dangerRating.put("elevation", "");
		} else
			dangerRating.put("elevation", "");
		bulletin.put("dangerRating", dangerRating);

		// maps
		if (isAfternoon)
			bulletin.put("map", LinkUtil.getMapsUrl(lang) + "/" + avalancheBulletin.getValidityDateString() + "/"
					+ publicationTime + "/" + avalancheBulletin.getId() + "_PM.jpg");
		else
			bulletin.put("map", LinkUtil.getMapsUrl(lang) + "/" + avalancheBulletin.getValidityDateString() + "/"
					+ publicationTime + "/" + avalancheBulletin.getId() + ".jpg");

		// avalanche situations
		bulletin.put("avalancheSituation1", createAvalancheSituation(daytimeBulletin.getAvalancheSituation1(), lang));
		bulletin.put("avalancheSituation2", createAvalancheSituation(daytimeBulletin.getAvalancheSituation2(), lang));
		bulletin.put("avalancheSituation3", createAvalancheSituation(daytimeBulletin.getAvalancheSituation3(), lang));
		bulletin.put("avalancheSituation4", createAvalancheSituation(daytimeBulletin.getAvalancheSituation4(), lang));
		bulletin.put("avalancheSituation5", createAvalancheSituation(daytimeBulletin.getAvalancheSituation5(), lang));
	}

	private Map<String, Object> createAvalancheSituation(eu.albina.model.AvalancheSituation avalancheSituation,
			LanguageCode lang) {
		Map<String, Object> avalancheSituation2 = new HashMap<>();
		if (avalancheSituation != null && avalancheSituation.getAvalancheSituation() != null) {
			avalancheSituation2.put("empty", false);
			if (avalancheSituation.getAvalancheSituation() != null) {
				avalancheSituation2.put("symbol", GlobalVariables.getServerImagesUrl() + "avalanche_situations/color/"
						+ avalancheSituation.getAvalancheSituation().toStringId() + ".png");
				avalancheSituation2.put("text", avalancheSituation.getAvalancheSituation().toString(lang.getLocale()));
				avalancheSituation2.put("link",
						LinkUtil.getAvalancheSituationLink(lang, avalancheSituation.getAvalancheSituation()));
			} else {
				avalancheSituation2.put("symbol",
						GlobalVariables.getServerImagesUrl() + "avalanche_situations/color/empty.png");
				avalancheSituation2.put("text", "");
				avalancheSituation2.put("link", "");
			}

			String path = Aspect.getSymbolPath(avalancheSituation.getAspects(), false);
			avalancheSituation2.put("aspects", GlobalVariables.getServerImagesUrl() + path);

			Map<String, Object> elevation = new HashMap<>();
			if (avalancheSituation.getTreelineHigh() || avalancheSituation.getElevationHigh() > 0) {
				if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
					// elevation high and low set
					elevation.put("symbol",
							GlobalVariables.getServerImagesUrl() + "elevation/color/levels_middle_two.png");
					// elevation.put("symbol", "cid:elevation/middle");
					if (avalancheSituation.getTreelineLow())
						elevation.put("limitAbove", lang.getBundleString("elevation.treeline.capitalized"));
					else if (avalancheSituation.getElevationLow() > 0)
						elevation.put("limitAbove", avalancheSituation.getElevationLow() + "m");
					if (avalancheSituation.getTreelineHigh())
						elevation.put("limitBelow", lang.getBundleString("elevation.treeline.capitalized"));
					else if (avalancheSituation.getElevationHigh() > 0)
						elevation.put("limitBelow", avalancheSituation.getElevationHigh() + "m");
				} else {
					// elevation high set
					elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/levels_below.png");
					// elevation.put("symbol", "cid:elevation/below");
					elevation.put("limitAbove", "");
					if (avalancheSituation.getTreelineHigh())
						elevation.put("limitBelow", lang.getBundleString("elevation.treeline.capitalized"));
					else if (avalancheSituation.getElevationHigh() > 0)
						elevation.put("limitBelow", avalancheSituation.getElevationHigh() + "m");
				}
			} else if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
				// elevation low set
				elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/levels_above.png");
				// elevation.put("symbol", "cid:elevation/above");
				if (avalancheSituation.getTreelineLow())
					elevation.put("limitAbove", lang.getBundleString("elevation.treeline.capitalized"));
				else if (avalancheSituation.getElevationLow() > 0)
					elevation.put("limitAbove", avalancheSituation.getElevationLow() + "m");
				elevation.put("limitBelow", "");
			} else {
				// no elevation set
				elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/levels_all.png");
				// elevation.put("symbol", "cid:elevation/all");
				elevation.put("limitAbove", "");
				elevation.put("limitBelow", "");
			}
			avalancheSituation2.put("elevation", elevation);
		} else {
			avalancheSituation2.put("empty", true);
			avalancheSituation2.put("symbol",
					GlobalVariables.getServerImagesUrl() + "avalanche_situations/color/empty.png");
			avalancheSituation2.put("text", "");
			avalancheSituation2.put("link", "");

			String path = Aspect.getSymbolPath(null, false);
			avalancheSituation2.put("aspects", GlobalVariables.getServerImagesUrl() + path);

			Map<String, Object> elevation = new HashMap<>();
			elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/empty.png");
			elevation.put("limitAbove", "");
			elevation.put("limitBelow", "");
			avalancheSituation2.put("elevation", elevation);
		}
		return avalancheSituation2;
	}

	private String getDangerRatingColorStyle(DangerRating dangerRating) {
		if (dangerRating.equals(DangerRating.very_high)) {
			return "background=\"" + GlobalVariables.getServerImagesUrl() + "bg_checkered.png"
					+ "\" height=\"100%\" width=\"10px\" bgcolor=\"#FF0000\"";
		} else
			return "style=\"background-color: " + dangerRating.getColor()
					+ "; height: 100%; width: 10px; min-width: 10px; padding: 0px; margin: 0px;\"";
	}

	private String getHeadlineStyle(DangerRating dangerRating) {
		if (dangerRating.equals(DangerRating.low) || dangerRating.equals(DangerRating.moderate)) {
			return "style=\"margin: 0; padding: 0; padding-left: 15px; text-decoration: none; font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif; line-height: 1.6; margin-bottom: 0px; font-weight: bold; font-size: 24px; color: "
					+ AlbinaUtil.greyDarkColor + "; background-color: " + dangerRating.getColor() + ";\"";
		} else {
			return "style=\"margin: 0; padding: 0; padding-left: 15px; text-decoration: none; font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif; line-height: 1.6; margin-bottom: 0px; font-weight: bold; font-size: 24px; color: "
					+ dangerRating.getColor() + ";\"";
		}
	}

	private String getDangerLevel5Style() {
		return "background=\"" + GlobalVariables.getServerImagesUrl() + "bg_checkered.png"
				+ "\" height=\"10\" width=\"75\" bgcolor=\"#FF0000\"";
	}

	private String getDangerPatternStyle(boolean b) {
		if (b)
			return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; margin-bottom: 10px; font-weight: normal; line-height: 1.6; font-size: 12px; color: #565f61; border: 1px solid #565f61; border-radius: 15px; padding-left: 10px; padding-right: 10px; padding-top: 2px; padding-bottom: 2px; margin-right: 5px; display: inline-block; background-color: #FFFFFF;\"";
		else
			return "";
	}

	private String getSnowpackStyle(boolean b) {
		if (!b)
			return "style=\"overflow: hidden; float: left; display: none !important; line-height: 0px; height: 0px; border-spacing: 0px;\"";
		else
			return "style=\"padding: 0px; border-spacing: 0px; width: 100%; background-color: #f6fafc;\"";
	}

	private String getPMStyle(boolean daytimeDependency) {
		if (!daytimeDependency)
			return "style=\"display:none;width:0px;max-height:0px;overflow:hidden;mso-hide:all;height:0;font-size:0;max-height:0;line-height:0;margin:0 auto;\"";
		else
			return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; color: #565f61; width: 100%; margin-top: 10px; border-top: 1px solid #e6eef2; padding-top: 10px;\"";
	}

	private String getPMStyleTable(boolean daytimeDependency) {
		if (!daytimeDependency)
			return "style=\"mso-hide: all;\"";
		else
			return "";
	}
}
