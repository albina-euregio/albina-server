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
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.albina.map.MapImageFormat;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheReport;
import eu.albina.model.enumerations.BulletinStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.publication.RapidMailController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequest;
import eu.albina.model.publication.rapidmail.mailings.PostMailingsRequestPostFile;
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

	public void sendBulletinEmails(AvalancheReport avalancheReport) {
		for (LanguageCode lang : LanguageCode.ENABLED) {
			sendBulletinEmails(avalancheReport, lang);
		}
	}

	public void sendBulletinEmails(AvalancheReport avalancheReport, LanguageCode lang) {
		Region region = avalancheReport.getRegion();
		if (region.isSendEmails()) {
			String subject;
			if (avalancheReport.getStatus() == BulletinStatus.republished) {
				subject = MessageFormat.format(lang.getBundleString("email.subject.update", region), lang.getBundleString("website.name", region)) + AlbinaUtil.getDate(avalancheReport.getBulletins(), lang);
			} else
				subject = MessageFormat.format(lang.getBundleString("email.subject", region), lang.getBundleString("website.name", region)) + AlbinaUtil.getDate(avalancheReport.getBulletins(), lang);
			List<AvalancheBulletin> regionBulletins = avalancheReport.getBulletins();
			if (regionBulletins != null && !regionBulletins.isEmpty()) {
				String emailHtml = createBulletinEmailHtml(avalancheReport, lang);
				sendBulletinEmailRapidmail(lang, region, emailHtml, subject, avalancheReport.getStatus() == BulletinStatus.test);
			}
		}
	}

	public void sendMediaEmails(String text, String mp3FileName, String txtFileName, Instant date, Region region, String username, boolean test, LanguageCode lang, ServerInstance serverInstance, boolean important) {
		StringBuilder sb = new StringBuilder();
		ZonedDateTime localDate = date.atZone(AlbinaUtil.localZone());
		sb.append(lang.getBundleString("day." + localDate.getDayOfWeek()));
		sb.append(", ");
		sb.append(localDate.format(lang.getFormatter()));

		String mp3FileUrl = LinkUtil.getMediaFileUrl(lang, region, serverInstance) + "/" + mp3FileName;

		String subject = MessageFormat.format(lang.getBundleString("email.media.subject"), lang.getBundleString("website.name"), sb.toString(), username);
		String emailHtml = text.replace("\n", "<br>") + "<br><br>" + LinkUtil.createHtmlLink(lang.getBundleString("email.media.link.mp3"), mp3FileUrl) + "<br><br>" + MessageFormat.format(lang.getBundleString("email.media.text"), username);

		sendMediaEmailRapidmail(lang, region, emailHtml, subject, test, false);
		if (important) {
			subject = MessageFormat.format(lang.getBundleString("email.media.important.subject"), lang.getBundleString("website.name"), sb.toString(), username);
			sendMediaEmailRapidmail(lang, region, emailHtml, subject, test, true);
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
			return Base64.getEncoder().encodeToString(zipData);
		}
	}

	public void sendBulletinEmailRapidmail(LanguageCode lang, Region region, String emailHtml, String subject, boolean test) {
		logger.info("Sending bulletin email in {} for {} ({} bytes)...", lang, region.getId(), emailHtml.getBytes(StandardCharsets.UTF_8).length);
		sendEmail(lang, region, emailHtml, subject, test, false, false);
	}

	public void sendBlogPostEmailRapidmail(LanguageCode lang, Region region, String emailHtml, String subject, boolean test) {
		logger.info("Sending blog post email in {} for {} ({} bytes)...", lang, region.getId(), emailHtml.getBytes(StandardCharsets.UTF_8).length);
		sendEmail(lang, region, emailHtml, subject, test, false, false);
	}

	public void sendMediaEmailRapidmail(LanguageCode lang, Region region, String emailHtml, String subject, boolean test, boolean important) {
		logger.info("Sending media email in {} for {} ({} bytes)...", lang, region.getId(), emailHtml.getBytes(StandardCharsets.UTF_8).length);
		sendEmail(lang, region, emailHtml, subject, test, true, important);
	}

	private void sendEmail(LanguageCode lang, Region region, String emailHtml, String subject, boolean test, boolean media, boolean important) {
		try {
			PostMailingsRequestPostFile file = new PostMailingsRequestPostFile()
				.description("mail-content.zip")
				.type("application/zip")
				.content(createZipFile(emailHtml, null));
			final String fromEmail = lang.getBundleString("email", region);
			final String fromName = lang.getBundleString("website.name", region);
			PostMailingsRequest request = new PostMailingsRequest()
				.fromEmail(fromEmail)
				.fromName(fromName)
				.subject(subject)
				.status("scheduled")
				.file(file);
			RapidMailController.getInstance().sendMessage(region, lang, request, test, media, false);
		} catch (Exception e) {
			logger.error("Emails could not be sent in " + lang + " for " + region.getId(), e);
		}
	}

	public String createBulletinEmailHtml(AvalancheReport avalancheReport, LanguageCode lang) {
		try {
			// Create data model
			Map<String, Object> root = new HashMap<>();
			Map<String, Object> image = new HashMap<>();

			final ServerInstance serverInstance = avalancheReport.getServerInstance();
			final String serverImagesUrl = serverInstance.getServerImagesUrl();
			final Region region = avalancheReport.getRegion();
			final List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();

			image.put("logo", serverImagesUrl + lang.getBundleString("logo.path", region));
			image.put("dangerLevel5Style", "background=\"" + serverImagesUrl + "bg_checkered.png"
				+ "\" height=\"10\" width=\"75\" bgcolor=\"#FF0000\"");
			image.put("ci", serverImagesUrl + region.getImageColorbarColorPath());
			image.put("color", region.getEmailColor());

			Map<String, Object> socialMediaImages = new HashMap<>();
			socialMediaImages.put("facebook", serverImagesUrl + "social_media/facebook.png");
			socialMediaImages.put("instagram", serverImagesUrl + "social_media/instagram.png");
			socialMediaImages.put("youtube", serverImagesUrl + "social_media/youtube.png");
			image.put("socialmedia", socialMediaImages);
			Map<String, Object> mapImage = new HashMap<>();

			// overview maps
			if (avalancheReport.hasDaytimeDependency()) {
				mapImage.put("overview",
						LinkUtil.getMapsUrl(lang, region, serverInstance) + "/" + AlbinaUtil.getValidityDateString(bulletins) + "/"
								+ AlbinaUtil.getPublicationTime(bulletins) + "/"
								+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.am, false));
				mapImage.put("overviewPM",
						LinkUtil.getMapsUrl(lang, region, serverInstance) + "/" + AlbinaUtil.getValidityDateString(bulletins) + "/"
								+ AlbinaUtil.getPublicationTime(bulletins) + "/"
								+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.pm, false));
				mapImage.put("widthPM", "width=\"600\"");
			} else {
				mapImage.put("overview",
						LinkUtil.getMapsUrl(lang, region, serverInstance) + "/" + AlbinaUtil.getValidityDateString(bulletins) + "/"
								+ AlbinaUtil.getPublicationTime(bulletins) + "/"
								+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.fd, false));
				mapImage.put("overviewPM", serverImagesUrl + "/empty.png");
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
			if (avalancheReport.getStatus() == BulletinStatus.republished)
				text.put("headline", lang.getBundleString("headline.update"));
			else
				text.put("headline", lang.getBundleString("headline"));
			text.put("follow", lang.getBundleString("email.follow-us"));
			text.put("unsubscribe", lang.getBundleString("email.unsubscribe"));
			text.put("imprint", lang.getBundleString("email.imprint"));
			if (avalancheReport.hasDaytimeDependency()) {
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
								bulletin.put("dangerPatternLink1", LinkUtil.getDangerPatternLink(lang, region,
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
								bulletin.put("dangerPatternLink2", LinkUtil.getDangerPatternLink(lang, region,
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
							serverImagesUrl + "tendency/tendency_decreasing_blue.png");
					tendency.put("date", AlbinaUtil.getTendencyDate(bulletins, lang));
				} else if (avalancheBulletin.getTendency() == Tendency.steady) {
					tendency.put("symbol", serverImagesUrl + "tendency/tendency_steady_blue.png");
					tendency.put("date", AlbinaUtil.getTendencyDate(bulletins, lang));
				} else if (avalancheBulletin.getTendency() == Tendency.increasing) {
					tendency.put("symbol",
							serverImagesUrl + "tendency/tendency_increasing_blue.png");
					tendency.put("date", AlbinaUtil.getTendencyDate(bulletins, lang));
				} else {
					tendency.put("symbol", serverImagesUrl + "tendency/empty.png");
					tendency.put("date", "");
				}
				bulletin.put("tendency", tendency);

				bulletin.put("dangerratingcolorstyle",
						getDangerRatingColorStyle(avalancheBulletin.getHighestDangerRating(), serverInstance));
				bulletin.put("headlinestyle", getHeadlineStyle(avalancheBulletin.getHighestDangerRating()));

				addDaytimeInfo(lang, region, avalancheBulletin, bulletin, false, AlbinaUtil.getPublicationTime(bulletins), serverInstance);
				Map<String, Object> pm = new HashMap<>();
				if (avalancheBulletin.isHasDaytimeDependency())
					addDaytimeInfo(lang, region, avalancheBulletin, pm, true, AlbinaUtil.getPublicationTime(bulletins), serverInstance);
				else
					addDaytimeInfo(lang, region, avalancheBulletin, pm, false, AlbinaUtil.getPublicationTime(bulletins), serverInstance);
				bulletin.put("pm", pm);

				arrayList.add(bulletin);
			}
			root.put("bulletins", arrayList);

			Map<String, Object> links = new HashMap<>();
			links.put("website", LinkUtil.getBulletinUrl(avalancheReport, lang));
			links.put("unsubscribe", "{%link_unsubscribe}");
			links.put("pdf", LinkUtil.getPdfLink(avalancheReport, lang));
			links.put("imprint", LinkUtil.getImprintLink(lang, region));
			Map<String, Object> socialMediaLinks = new HashMap<>();
			socialMediaLinks.put("facebook", lang.getBundleString("website.url") + "/#followDialog");
			socialMediaLinks.put("instagram", lang.getBundleString("website.url") + "/#followDialog");
			socialMediaLinks.put("youtube", lang.getBundleString("website.url") + "/#followDialog");
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

	private void addDaytimeInfo(LanguageCode lang, Region region, AvalancheBulletin avalancheBulletin, Map<String, Object> bulletin,
			boolean isAfternoon, String publicationTime, ServerInstance serverInstance) {
		AvalancheBulletinDaytimeDescription daytimeBulletin;
		if (isAfternoon)
			daytimeBulletin = avalancheBulletin.getAfternoon();
		else
			daytimeBulletin = avalancheBulletin.getForenoon();

		final String serverImagesUrl =  serverInstance.getServerImagesUrl();

		// danger rating
		Map<String, Object> dangerRating = new HashMap<>();
		if ((daytimeBulletin.getDangerRatingBelow() == null
				|| daytimeBulletin.getDangerRatingBelow() == DangerRating.missing
				|| daytimeBulletin.getDangerRatingBelow() == DangerRating.no_rating)
				&& (daytimeBulletin.getDangerRatingAbove() == null
						|| daytimeBulletin.getDangerRatingAbove() == DangerRating.missing
						|| daytimeBulletin.getDangerRatingAbove() == DangerRating.no_rating)) {
			dangerRating.put("symbol", serverImagesUrl + "warning_pictos/color/level_0_0.png");
		} else {
			dangerRating.put("symbol", serverImagesUrl + "warning_pictos/color/level_"
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
			bulletin.put("map", LinkUtil.getMapsUrl(lang, region, serverInstance) + "/" + avalancheBulletin.getValidityDateString() + "/"
					+ publicationTime + "/" + MapUtil.filename(region, avalancheBulletin, DaytimeDependency.pm, false, MapImageFormat.jpg));
		else
			bulletin.put("map", LinkUtil.getMapsUrl(lang, region, serverInstance) + "/" + avalancheBulletin.getValidityDateString() + "/"
					+ publicationTime + "/" + MapUtil.filename(region, avalancheBulletin, DaytimeDependency.am, false, MapImageFormat.jpg));

		// avalanche problems
		bulletin.put("avalancheProblem1", createAvalancheProblem(daytimeBulletin.getAvalancheProblem1(), lang, region, serverInstance));
		bulletin.put("avalancheProblem2", createAvalancheProblem(daytimeBulletin.getAvalancheProblem2(), lang, region, serverInstance));
		bulletin.put("avalancheProblem3", createAvalancheProblem(daytimeBulletin.getAvalancheProblem3(), lang, region, serverInstance));
		bulletin.put("avalancheProblem4", createAvalancheProblem(daytimeBulletin.getAvalancheProblem4(), lang, region, serverInstance));
		bulletin.put("avalancheProblem5", createAvalancheProblem(daytimeBulletin.getAvalancheProblem5(), lang, region, serverInstance));
	}

	private Map<String, Object> createAvalancheProblem(eu.albina.model.AvalancheProblem avalancheProblem,
			LanguageCode lang, Region region, ServerInstance serverInstance) {
		Map<String, Object> avalancheProblemMap = new HashMap<>();
		final String serverImagesUrl =  serverInstance.getServerImagesUrl();

		if (avalancheProblem != null && avalancheProblem.getAvalancheProblem() != null) {
			avalancheProblemMap.put("empty", false);
			if (avalancheProblem.getAvalancheProblem() != null) {
				avalancheProblemMap.put("symbol", serverImagesUrl + "avalanche_problems/color/"
						+ avalancheProblem.getAvalancheProblem().toStringId() + ".png");
				avalancheProblemMap.put("text", avalancheProblem.getAvalancheProblem().toString(lang.getLocale()));
				avalancheProblemMap.put("link",
						LinkUtil.getAvalancheProblemLink(lang, region, avalancheProblem.getAvalancheProblem()));
			} else {
				avalancheProblemMap.put("symbol",
						serverImagesUrl + "avalanche_problems/color/empty.png");
				avalancheProblemMap.put("text", "");
				avalancheProblemMap.put("link", "");
			}

			String path = Aspect.getSymbolPath(avalancheProblem.getAspects(), false);
			avalancheProblemMap.put("aspects", serverImagesUrl + path);

			Map<String, Object> elevation = new HashMap<>();
			if (avalancheProblem.getTreelineHigh() || avalancheProblem.getElevationHigh() > 0) {
				if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
					// elevation high and low set
					elevation.put("symbol",
							serverImagesUrl + "elevation/color/levels_middle_two.png");
					// elevation.put("symbol", "cid:elevation/middle");
					if (avalancheProblem.getTreelineLow())
						elevation.put("limitAbove", lang.getBundleString("elevation.treeline.capitalized"));
					else if (avalancheProblem.getElevationLow() > 0)
						elevation.put("limitAbove", avalancheProblem.getElevationLow() + "m");
					if (avalancheProblem.getTreelineHigh())
						elevation.put("limitBelow", lang.getBundleString("elevation.treeline.capitalized"));
					else if (avalancheProblem.getElevationHigh() > 0)
						elevation.put("limitBelow", avalancheProblem.getElevationHigh() + "m");
				} else {
					// elevation high set
					elevation.put("symbol", serverImagesUrl + "elevation/color/levels_below.png");
					// elevation.put("symbol", "cid:elevation/below");
					elevation.put("limitAbove", "");
					if (avalancheProblem.getTreelineHigh())
						elevation.put("limitBelow", lang.getBundleString("elevation.treeline.capitalized"));
					else if (avalancheProblem.getElevationHigh() > 0)
						elevation.put("limitBelow", avalancheProblem.getElevationHigh() + "m");
				}
			} else if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				// elevation low set
				elevation.put("symbol", serverImagesUrl + "elevation/color/levels_above.png");
				// elevation.put("symbol", "cid:elevation/above");
				if (avalancheProblem.getTreelineLow())
					elevation.put("limitAbove", lang.getBundleString("elevation.treeline.capitalized"));
				else if (avalancheProblem.getElevationLow() > 0)
					elevation.put("limitAbove", avalancheProblem.getElevationLow() + "m");
				elevation.put("limitBelow", "");
			} else {
				// no elevation set
				elevation.put("symbol", serverImagesUrl + "elevation/color/levels_all.png");
				// elevation.put("symbol", "cid:elevation/all");
				elevation.put("limitAbove", "");
				elevation.put("limitBelow", "");
			}
			avalancheProblemMap.put("elevation", elevation);
		} else {
			avalancheProblemMap.put("empty", true);
			avalancheProblemMap.put("symbol",
					serverImagesUrl + "avalanche_problems/color/empty.png");
			avalancheProblemMap.put("text", "");
			avalancheProblemMap.put("link", "");

			String path = Aspect.getSymbolPath(null, false);
			avalancheProblemMap.put("aspects", serverImagesUrl + path);

			Map<String, Object> elevation = new HashMap<>();
			elevation.put("symbol", serverImagesUrl + "elevation/color/empty.png");
			elevation.put("limitAbove", "");
			elevation.put("limitBelow", "");
			avalancheProblemMap.put("elevation", elevation);
		}
		return avalancheProblemMap;
	}

	private String getDangerRatingColorStyle(DangerRating dangerRating, ServerInstance serverInstance) {
		if (dangerRating.equals(DangerRating.very_high)) {
			return "background=\"" + serverInstance.getServerImagesUrl() + "bg_checkered.png"
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
