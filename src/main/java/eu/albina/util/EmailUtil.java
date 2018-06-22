package eu.albina.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class EmailUtil {

	private static EmailUtil instance = null;

	private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

	private static Configuration cfg;

	protected EmailUtil() {
	}

	public static EmailUtil getInstance() {
		if (instance == null) {
			instance = new EmailUtil();
		}
		return instance;
	}

	public void createFreemarkerConfigurationInstance() throws IOException, URISyntaxException {
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

	public String createEmailHtml(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		try {
			// Create data model
			Map<String, Object> root = new HashMap<>();

			Map<String, Object> image = new HashMap<>();
			// image.put("logo", "cid:logo");
			switch (lang) {
			case de:
				image.put("logo", GlobalVariables.serverImagesUrl + "logo/Logo Lawinen.report.png");
				break;
			case it:
				image.put("logo", GlobalVariables.serverImagesUrl + "logo/Logo Valanghe.report.png");
				break;
			case en:
				image.put("logo", GlobalVariables.serverImagesUrl + "logo/Logo Avalanche.report.png");
				break;
			default:
				image.put("logo", GlobalVariables.serverImagesUrl + "logo/Logo Avalanche.report.png");
				break;
			}
			image.put("ci", GlobalVariables.serverImagesUrl + "Colorbar.gif");
			// image.put("ci", "cid:ci);
			// image.put("logo", "cid:logo);
			Map<String, Object> socialMediaImages = new HashMap<>();
			socialMediaImages.put("facebook", GlobalVariables.serverImagesUrl + "facebook.png");
			socialMediaImages.put("twitter", GlobalVariables.serverImagesUrl + "twitter.png");
			socialMediaImages.put("instagram", GlobalVariables.serverImagesUrl + "instagram.png");
			socialMediaImages.put("youtube", GlobalVariables.serverImagesUrl + "youtube.png");
			socialMediaImages.put("whatsapp", GlobalVariables.serverImagesUrl + "whatsapp.png");
			// socialMediaImages.put("facebook", "cid:facebook");
			// socialMediaImages.put("twitter", "cid:twitter");
			// socialMediaImages.put("instagram", "cid:instagram");
			// socialMediaImages.put("youtube", "cid:youtube");
			// socialMediaImages.put("whatsapp", "cid:whatsapp");
			image.put("socialmedia", socialMediaImages);
			Map<String, Object> mapImage = new HashMap<>();

			// TODO add map URL to email
			mapImage.put("overview", GlobalVariables.serverImagesUrl + "map_overview.png");
			// mapImage.put("overview", "cid:map_overview");
			if (hasDaytimeDependency(bulletins))
				mapImage.put("overviewPM", GlobalVariables.serverImagesUrl + "map_overview.png");
			else
				mapImage.put("overviewPM", "");
			// mapImage.put("overview", "cid:map_overview_pm");

			image.put("map", mapImage);
			root.put("image", image);

			Map<String, Object> text = new HashMap<>();
			String publicationDate = getPublicationDate(bulletins, lang);
			text.put("publicationDate", publicationDate);
			if (publicationDate.isEmpty())
				text.put("publishedAt", "");
			else
				text.put("publishedAt", GlobalVariables.getPublishedText(lang));
			text.put("date", getDate(bulletins, lang));
			text.put("title", GlobalVariables.getTitle(lang));
			text.put("headline", GlobalVariables.getHeadline(lang));
			text.put("follow", GlobalVariables.getFollowUs(lang));
			text.put("unsubscribe", GlobalVariables.getUnsubscribe(lang));
			if (hasDaytimeDependency(bulletins)) {
				text.put("am", "AM");
				text.put("pm", "PM");
			} else {
				text.put("am", "");
				text.put("pm", "");
			}

			Map<String, Object> dangerRatings = new HashMap<>();
			dangerRatings.put("low", GlobalVariables.getDangerRatingText(DangerRating.low, lang));
			dangerRatings.put("moderate", GlobalVariables.getDangerRatingText(DangerRating.moderate, lang));
			dangerRatings.put("considerable", GlobalVariables.getDangerRatingText(DangerRating.considerable, lang));
			dangerRatings.put("high", GlobalVariables.getDangerRatingText(DangerRating.high, lang));
			dangerRatings.put("veryHigh", GlobalVariables.getDangerRatingText(DangerRating.very_high, lang));
			text.put("dangerRating", dangerRatings);

			root.put("text", text);

			ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
			for (AvalancheBulletin avalancheBulletin : bulletins) {
				Map<String, Object> bulletin = new HashMap<>();

				bulletin.put("stylepm", getPMStyle(avalancheBulletin.isHasDaytimeDependency()));
				if (avalancheBulletin.isHasDaytimeDependency()) {
					bulletin.put("textam", "AM:");
					bulletin.put("textpm", "PM:");
				} else {
					bulletin.put("textam", "");
					bulletin.put("textpm", "");
				}

				bulletin.put("warningLevelText", getDangerRatingText(avalancheBulletin, lang));

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
					bulletin.put("snowpackstyle", EmailUtil.getInstance().getSnowpackStyle(true));
					if (avalancheBulletin.getDangerPattern1() != null || avalancheBulletin.getDangerPattern2() != null
							|| avalancheBulletin.getSnowpackStructureCommentIn(lang) != null
							|| avalancheBulletin.getSnowpackStructureHighlightsIn(lang) != null) {
						bulletin.put("snowpackStructureHeadline", GlobalVariables.getSnowpackHeadline(lang));

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
							bulletin.put("dangerPatternsHeadline", GlobalVariables.getDangerPatternsHeadline(lang));
							bulletin.put("dangerpatternstyle", EmailUtil.getInstance().getDangerPatternStyle(true));
							if (avalancheBulletin.getDangerPattern1() != null)
								bulletin.put("dangerPattern1",
										AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern1(), lang));
							else
								bulletin.put("dangerPattern1", "");
							if (avalancheBulletin.getDangerPattern2() != null)
								bulletin.put("dangerPattern2",
										AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern2(), lang));
							else
								bulletin.put("dangerPattern2", "");
						} else {
							bulletin.put("dangerPatternsHeadline", "");
							bulletin.put("dangerpatternstyle", EmailUtil.getInstance().getDangerPatternStyle(false));
						}
					} else {
						bulletin.put("snowpackStructureHeadline", "");
						bulletin.put("snowpackStructureHighlights", "");
						bulletin.put("snowpackStructureComment", "");
						bulletin.put("dangerPatternsHeadline", "");
						bulletin.put("dangerPattern1", "");
						bulletin.put("dangerPattern2", "");
						bulletin.put("dangerpatternstyle", EmailUtil.getInstance().getDangerPatternStyle(false));
					}

					// tendency
					if (avalancheBulletin.getTendencyCommentIn(lang) != null) {
						bulletin.put("tendencyHeadline", GlobalVariables.getTendencyHeadline(lang));
						bulletin.put("tendencyComment", avalancheBulletin.getTendencyCommentIn(lang));
					} else {
						bulletin.put("tendencyHeadline", "");
						bulletin.put("tendencyComment", "");
					}
				} else {
					bulletin.put("snowpackstyle", EmailUtil.getInstance().getSnowpackStyle(false));
					bulletin.put("snowpackStructureHeadline", "");
					bulletin.put("snowpackStructureHighlights", "");
					bulletin.put("snowpackStructureComment", "");
					bulletin.put("dangerPatternsHeadline", "");
					bulletin.put("dangerPattern1", "");
					bulletin.put("dangerPattern2", "");
					bulletin.put("dangerpatternstyle", EmailUtil.getInstance().getDangerPatternStyle(false));
					bulletin.put("tendencyHeadline", "");
					bulletin.put("tendencyComment", "");
				}

				Map<String, Object> tendency = new HashMap<>();
				tendency.put("text", GlobalVariables.getTendencyText(avalancheBulletin.getTendency(), lang));
				if (avalancheBulletin.getTendency() == Tendency.decreasing) {
					// tendency.put("symbol", "cid:tendency/decreasing");
					tendency.put("symbol", GlobalVariables.serverImagesUrl + "tendency_decreasing_blue.png");
					tendency.put("date", getTendencyDate(bulletins, lang));
				} else if (avalancheBulletin.getTendency() == Tendency.steady) {
					// tendency.put("symbol", "cid:tendency/steady");
					tendency.put("symbol", GlobalVariables.serverImagesUrl + "tendency_steady_blue.png");
					tendency.put("date", getTendencyDate(bulletins, lang));
				} else if (avalancheBulletin.getTendency() == Tendency.increasing) {
					// tendency.put("symbol", "cid:tendency/increasing");
					tendency.put("symbol", GlobalVariables.serverImagesUrl + "tendency_increasing_blue.png");
					tendency.put("date", getTendencyDate(bulletins, lang));
				} else {
					tendency.put("symbol", "");
					tendency.put("date", "");
				}
				bulletin.put("tendency", tendency);

				bulletin.put("dangerratingcolorstyle", EmailUtil.getInstance().getDangerRatingColorStyle(
						AlbinaUtil.getDangerRatingColor(avalancheBulletin.getHighestDangerRating())));
				bulletin.put("headlinestyle", EmailUtil.getInstance()
						.getHeadlineStyle(AlbinaUtil.getDangerRatingColor(avalancheBulletin.getHighestDangerRating())));

				addDaytimeInfo(lang, avalancheBulletin, bulletin, false);
				Map<String, Object> pm = new HashMap<>();
				if (avalancheBulletin.isHasDaytimeDependency())
					addDaytimeInfo(lang, avalancheBulletin, pm, true);
				else
					addDaytimeInfo(lang, avalancheBulletin, pm, false);
				bulletin.put("pm", pm);

				arrayList.add(bulletin);
			}
			root.put("bulletins", arrayList);

			Map<String, Object> links = new HashMap<>();
			links.put("website", "https://avalanche.report");
			links.put("unsubscribe", "https://avalanche.report/unsubscribe");
			Map<String, Object> socialMediaLinks = new HashMap<>();
			socialMediaLinks.put("facebook", "https://avalanche.report/facebook");
			socialMediaLinks.put("twitter", "https://avalanche.report/twitter");
			socialMediaLinks.put("instagram", "https://avalanche.report/instagram");
			socialMediaLinks.put("youtube", "https://avalanche.report/youtube");
			socialMediaLinks.put("whatsapp", "https://avalanche.report/whatsapp");
			links.put("socialmedia", socialMediaLinks);
			root.put("link", links);

			// Get template
			// TODO get template w/o daytime dependency
			Template temp = cfg.getTemplate("albina-email.html");

			// Merge template and model
			Writer out = new OutputStreamWriter(System.out);
			temp.process(root, out);

			return out.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private void addDaytimeInfo(LanguageCode lang, AvalancheBulletin avalancheBulletin, Map<String, Object> bulletin,
			boolean isAfternoon) {
		AvalancheBulletinDaytimeDescription daytimeBulletin;
		if (isAfternoon)
			daytimeBulletin = avalancheBulletin.getAfternoon();
		else
			daytimeBulletin = avalancheBulletin.getForenoon();

		// danger rating
		Map<String, Object> dangerRating = new HashMap<>();
		dangerRating.put("symbol", GlobalVariables.serverImagesUrl + "warning_pictos/level_"
				+ getWarningLevelId(avalancheBulletin.getForenoon(), avalancheBulletin.isHasElevationDependency())
				+ ".svg");
		// dangerRating.put("symbol", "cid:warning_picto/" +
		// getWarningLevelId(avalancheBulletin.getForenoon(),
		// avalancheBulletin.isHasElevationDependency()));
		if (avalancheBulletin.isHasElevationDependency()) {
			if (avalancheBulletin.getTreeline())
				dangerRating.put("elevation", GlobalVariables.getTreelineString(lang));
			else if (avalancheBulletin.getElevation() > 0)
				dangerRating.put("elevation", avalancheBulletin.getElevation() + "m");
			else
				dangerRating.put("elevation", "");
		} else
			dangerRating.put("elevation", "");
		bulletin.put("dangerRating", dangerRating);

		// TODO add correct map
		bulletin.put("map", GlobalVariables.serverImagesUrl + "map_detail_3.png");

		// avalanche situation 1
		Map<String, Object> avalancheSituation1 = new HashMap<>();
		if (daytimeBulletin.getAvalancheSituation1() != null) {
			if (daytimeBulletin.getAvalancheSituation1().getAvalancheSituation() != null) {
				avalancheSituation1.put("symbol", GlobalVariables.serverImagesUrl + "avalanche_situations/color/"
						+ daytimeBulletin.getAvalancheSituation1().getAvalancheSituation().toStringId() + ".svg");
				// avalancheSituation1.put("symbol", "cid:avalanche-situation/" +
				// daytimeBulletin
				// .getAvalancheSituation1().getAvalancheSituation().toStringId());
				avalancheSituation1.put("text",
						daytimeBulletin.getAvalancheSituation1().getAvalancheSituation().toString(lang));
			} else {
				avalancheSituation1.put("symbol", "");
				avalancheSituation1.put("text", "");
			}
			avalancheSituation1.put("aspectBg", GlobalVariables.serverImagesUrl + "aspects/exposition_bg.svg");
			// avalancheSituation1.put("aspectBg", "cid:aspect/bg");
			if (daytimeBulletin.getAvalancheSituation1().getAspects() != null) {
				Set<Aspect> aspects = daytimeBulletin.getAvalancheSituation1().getAspects();
				for (Aspect aspect : Aspect.values()) {
					if (aspects.contains(aspect)) {
						avalancheSituation1.put("aspect" + aspect.toUpperCaseString(),
								GlobalVariables.serverImagesUrl + "aspects/exposition_" + aspect.toString() + ".svg");
						// avalancheSituation1.put("aspect" + aspect.toUpperCaseString(), "cid:aspect/"
						// + aspect.toString());
					} else {
						avalancheSituation1.put("aspect" + aspect.toUpperCaseString(),
								GlobalVariables.serverImagesUrl + "aspects/exposition_empty.svg");
						// avalancheSituation1.put("aspect" + aspect.toUpperCaseString(),
						// "cid:aspect/empty");
					}
				}
			} else
				for (Aspect aspect : Aspect.values()) {
					avalancheSituation1.put("aspect" + aspect.toUpperCaseString(),
							GlobalVariables.serverImagesUrl + "aspects/exposition_empty.svg");
					// avalancheSituation1.put("aspect" + aspect.toUpperCaseString(),
					// "cid:aspect/empty");
				}
			Map<String, Object> elevation = new HashMap<>();
			if (daytimeBulletin.getAvalancheSituation1().getTreelineHigh()
					|| daytimeBulletin.getAvalancheSituation1().getElevationHigh() > 0) {
				if (daytimeBulletin.getAvalancheSituation1().getTreelineLow()
						|| daytimeBulletin.getAvalancheSituation1().getElevationLow() > 0) {
					// elevation high and low set
					elevation.put("symbol", GlobalVariables.serverImagesUrl + "elevation/levels_middle.svg");
					// elevation.put("symbol", "cid:elevation/middle");
					if (daytimeBulletin.getAvalancheSituation1().getTreelineLow())
						elevation.put("limitAbove", GlobalVariables.getTreelineString(lang));
					else if (daytimeBulletin.getAvalancheSituation1().getElevationLow() > 0)
						elevation.put("limitAbove", daytimeBulletin.getAvalancheSituation1().getElevationLow() + "m");
					if (daytimeBulletin.getAvalancheSituation1().getTreelineHigh())
						elevation.put("limitBelow", GlobalVariables.getTreelineString(lang));
					else if (daytimeBulletin.getAvalancheSituation1().getElevationHigh() > 0)
						elevation.put("limitBelow", daytimeBulletin.getAvalancheSituation1().getElevationHigh() + "m");
				} else {
					// elevation high set
					elevation.put("symbol", GlobalVariables.serverImagesUrl + "elevation/levels_below.svg");
					// elevation.put("symbol", "cid:elevation/below");
					elevation.put("limitAbove", "");
					if (daytimeBulletin.getAvalancheSituation1().getTreelineHigh())
						elevation.put("limitBelow", GlobalVariables.getTreelineString(lang));
					else if (daytimeBulletin.getAvalancheSituation1().getElevationHigh() > 0)
						elevation.put("limitBelow", daytimeBulletin.getAvalancheSituation1().getElevationHigh() + "m");
				}
			} else if (daytimeBulletin.getAvalancheSituation1().getTreelineLow()
					|| daytimeBulletin.getAvalancheSituation1().getElevationLow() > 0) {
				// elevation low set
				elevation.put("symbol", GlobalVariables.serverImagesUrl + "elevation/levels_above.svg");
				// elevation.put("symbol", "cid:elevation/above");
				if (daytimeBulletin.getAvalancheSituation1().getTreelineLow())
					elevation.put("limitAbove", GlobalVariables.getTreelineString(lang));
				else if (daytimeBulletin.getAvalancheSituation1().getElevationLow() > 0)
					elevation.put("limitAbove", daytimeBulletin.getAvalancheSituation1().getElevationLow() + "m");
				elevation.put("limitBelow", "");
			} else {
				// no elevation set
				elevation.put("symbol", GlobalVariables.serverImagesUrl + "elevation/levels_all.svg");
				// elevation.put("symbol", "cid:elevation/all");
				elevation.put("limitAbove", "");
				elevation.put("limitBelow", "");
			}
			avalancheSituation1.put("elevation", elevation);
		} else {
			avalancheSituation1.put("symbol", "");
			avalancheSituation1.put("text", "");
			avalancheSituation1.put("aspectBg", GlobalVariables.serverImagesUrl + "aspects/exposition_bg.svg");
			// avalancheSituation1.put("aspectBg", "cid:aspect/bg");
			for (Aspect aspect : Aspect.values()) {
				avalancheSituation1.put("aspect" + aspect.toUpperCaseString(),
						GlobalVariables.serverImagesUrl + "aspects/exposition_empty.svg");
				// avalancheSituation1.put("aspect" + aspect.toUpperCaseString(),
				// "cid:aspect/empty");
			}
			Map<String, Object> elevation = new HashMap<>();
			elevation.put("symbol", "");
			elevation.put("limitAbove", "");
			elevation.put("limitBelow", "");
			avalancheSituation1.put("elevation", elevation);
		}
		bulletin.put("avalancheSituation1", avalancheSituation1);

		// avalanche situation 2
		Map<String, Object> avalancheSituation2 = new HashMap<>();
		if (daytimeBulletin.getAvalancheSituation2() != null) {
			if (daytimeBulletin.getAvalancheSituation2().getAvalancheSituation() != null) {
				avalancheSituation2.put("symbol", GlobalVariables.serverImagesUrl + "avalanche_situations/color/"
						+ daytimeBulletin.getAvalancheSituation2().getAvalancheSituation().toStringId() + ".svg");
				// avalancheSituation2.put("symbol", "cid:avalanche-situation/" +
				// daytimeBulletin
				// .getAvalancheSituation2().getAvalancheSituation().toStringId());
				avalancheSituation2.put("text",
						daytimeBulletin.getAvalancheSituation2().getAvalancheSituation().toString(lang));
			} else {
				avalancheSituation2.put("symbol", "");
				avalancheSituation2.put("text", "");
			}
			avalancheSituation2.put("aspectBg", GlobalVariables.serverImagesUrl + "aspects/exposition_bg.svg");
			// avalancheSituation2.put("aspectBg", "cid:aspect/bg");
			if (daytimeBulletin.getAvalancheSituation2().getAspects() != null) {
				Set<Aspect> aspects = daytimeBulletin.getAvalancheSituation2().getAspects();
				for (Aspect aspect : Aspect.values()) {
					if (aspects.contains(aspect)) {
						avalancheSituation2.put("aspect" + aspect.toUpperCaseString(),
								GlobalVariables.serverImagesUrl + "aspects/exposition_" + aspect.toString() + ".svg");
						// avalancheSituation2.put("aspect" + aspect.toUpperCaseString(), "cid:aspect/"
						// + aspect.toString());
					} else {
						avalancheSituation2.put("aspect" + aspect.toUpperCaseString(),
								GlobalVariables.serverImagesUrl + "aspects/exposition_empty.svg");
						// avalancheSituation2.put("aspect" + aspect.toUpperCaseString(),
						// "cid:aspect/empty");
					}
				}
			} else
				for (Aspect aspect : Aspect.values()) {
					avalancheSituation2.put("aspect" + aspect.toUpperCaseString(),
							GlobalVariables.serverImagesUrl + "aspects/exposition_empty.svg");
					// avalancheSituation2.put("aspect" + aspect.toUpperCaseString(),
					// "cid:aspect/empty");
				}
			Map<String, Object> elevation = new HashMap<>();
			if (daytimeBulletin.getAvalancheSituation2().getTreelineHigh()
					|| daytimeBulletin.getAvalancheSituation2().getElevationHigh() > 0) {
				if (daytimeBulletin.getAvalancheSituation2().getTreelineLow()
						|| daytimeBulletin.getAvalancheSituation2().getElevationLow() > 0) {
					// elevation high and low set
					elevation.put("symbol", GlobalVariables.serverImagesUrl + "elevation/levels_middle.svg");
					// elevation.put("symbol", "cid:elevation/middle");
					if (daytimeBulletin.getAvalancheSituation2().getTreelineLow())
						elevation.put("limitAbove", GlobalVariables.getTreelineString(lang));
					else if (daytimeBulletin.getAvalancheSituation2().getElevationLow() > 0)
						elevation.put("limitAbove", daytimeBulletin.getAvalancheSituation2().getElevationLow() + "m");
					if (daytimeBulletin.getAvalancheSituation2().getTreelineHigh())
						elevation.put("limitBelow", GlobalVariables.getTreelineString(lang));
					else if (daytimeBulletin.getAvalancheSituation2().getElevationHigh() > 0)
						elevation.put("limitBelow", daytimeBulletin.getAvalancheSituation2().getElevationHigh() + "m");
				} else {
					// elevation high set
					elevation.put("symbol", GlobalVariables.serverImagesUrl + "elevation/levels_below.svg");
					// elevation.put("symbol", "cid:elevation/below");
					elevation.put("limitAbove", "");
					if (daytimeBulletin.getAvalancheSituation2().getTreelineHigh())
						elevation.put("limitBelow", GlobalVariables.getTreelineString(lang));
					else if (daytimeBulletin.getAvalancheSituation2().getElevationHigh() > 0)
						elevation.put("limitBelow", daytimeBulletin.getAvalancheSituation2().getElevationHigh() + "m");
				}
			} else if (daytimeBulletin.getAvalancheSituation2().getTreelineLow()
					|| daytimeBulletin.getAvalancheSituation2().getElevationLow() > 0) {
				// elevation low set
				elevation.put("symbol", GlobalVariables.serverImagesUrl + "elevation/levels_above.svg");
				// elevation.put("symbol", "cid:elevation/above");
				if (daytimeBulletin.getAvalancheSituation2().getTreelineLow())
					elevation.put("limitAbove", GlobalVariables.getTreelineString(lang));
				else if (daytimeBulletin.getAvalancheSituation2().getElevationLow() > 0)
					elevation.put("limitAbove", daytimeBulletin.getAvalancheSituation2().getElevationLow() + "m");
				elevation.put("limitBelow", "");
			} else {
				// no elevation set
				elevation.put("symbol", GlobalVariables.serverImagesUrl + "elevation/levels_all.svg");
				// elevation.put("symbol", "cid:elevation/all");
				elevation.put("limitAbove", "");
				elevation.put("limitBelow", "");
			}
			avalancheSituation2.put("elevation", elevation);
		} else {
			avalancheSituation2.put("symbol", "");
			avalancheSituation2.put("text", "");
			avalancheSituation2.put("aspectBg", GlobalVariables.serverImagesUrl + "aspects/exposition_bg.svg");
			// avalancheSituation2.put("aspectBg", "cid:aspect/bg");
			for (Aspect aspect : Aspect.values()) {
				avalancheSituation2.put("aspect" + aspect.toUpperCaseString(),
						GlobalVariables.serverImagesUrl + "aspects/exposition_empty.svg");
				// avalancheSituation2.put("aspect" + aspect.toUpperCaseString(),
				// "cid:aspect/empty");
			}
			Map<String, Object> elevation = new HashMap<>();
			elevation.put("symbol", "");
			elevation.put("limitAbove", "");
			elevation.put("limitBelow", "");
			avalancheSituation2.put("elevation", elevation);
		}
		bulletin.put("avalancheSituation2", avalancheSituation2);
	}

	private boolean hasDaytimeDependency(List<AvalancheBulletin> bulletins) {
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			if (avalancheBulletin.hasDaytimeDependency())
				return true;
		}
		return false;
	}

	private String getDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			DateTime bulletinDate = avalancheBulletin.getValidUntil();
			if (date == null)
				date = bulletinDate;
			else if (bulletinDate.isAfter(date))
				date = bulletinDate;
		}

		StringBuilder result = new StringBuilder();
		if (date != null) {
			result.append(GlobalVariables.getDayName(date.getDayOfWeek(), lang));

			switch (lang) {
			case en:
				result.append(date.toString(GlobalVariables.dateTimeEn));
				break;
			case de:
				result.append(date.toString(GlobalVariables.dateTimeDe));
				break;
			case it:
				result.append(date.toString(GlobalVariables.dateTimeIt));
				break;
			default:
				result.append(date.toString(GlobalVariables.dateTimeEn));
				break;
			}
		} else {
			// TODO what if no date is given (should not happen)
			result.append("-");
		}

		return result.toString();
	}

	private String getTendencyDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			DateTime bulletinDate = avalancheBulletin.getValidUntil();
			if (date == null)
				date = bulletinDate;
			else if (bulletinDate.isAfter(date))
				date = bulletinDate;
		}

		if (date != null) {
			date = date.plusDays(1);
			StringBuilder result = new StringBuilder();

			switch (lang) {
			case en:
				result.append("on ");
				result.append(GlobalVariables.getDayName(date.getDayOfWeek(), lang));
				result.append(date.toString(GlobalVariables.dateTimeEn));
				break;
			case de:
				result.append("am ");
				result.append(GlobalVariables.getDayName(date.getDayOfWeek(), lang));
				result.append(date.toString(GlobalVariables.dateTimeDe));
				break;
			case it:
				result.append("su ");
				result.append(GlobalVariables.getDayName(date.getDayOfWeek(), lang));
				result.append(date.toString(GlobalVariables.dateTimeIt));
				break;
			default:
				result.append("on ");
				result.append(GlobalVariables.getDayName(date.getDayOfWeek(), lang));
				result.append(date.toString(GlobalVariables.dateTimeEn));
				break;
			}

			return result.toString();
		} else {
			return "";
		}
	}

	private String getPublicationDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			DateTime bulletinDate = avalancheBulletin.getPublicationDate();
			if (date == null)
				date = bulletinDate;
			else if (bulletinDate.isAfter(date))
				date = bulletinDate;
		}
		if (date != null) {
			switch (lang) {
			case en:
				return date.toString(GlobalVariables.publicationDateTimeEn);
			case de:
				return date.toString(GlobalVariables.publicationDateTimeDe);
			case it:
				return date.toString(GlobalVariables.publicationDateTimeIt);
			default:
				return date.toString(GlobalVariables.publicationDateTimeEn);
			}
		} else
			return "";
	}

	private String getDangerRatingText(AvalancheBulletin bulletin, LanguageCode lang) {
		switch (bulletin.getHighestDangerRating()) {
		case low:
			switch (lang) {
			case de:
				return "Gefahrenstufe 1 - Gering";
			case it:
				return "Grado Pericolo 1 - Debole";
			case en:
				return "Danger Level 1 - Low";
			default:
				return "Danger Level 1 - Low";
			}
		case moderate:
			switch (lang) {
			case de:
				return "Gefahrenstufe 2 - Mäßig";
			case it:
				return "Grado Pericolo 2 - Moderato";
			case en:
				return "Danger Level 2 - Moderate";
			default:
				return "Danger Level 2 - Moderate";
			}
		case considerable:
			switch (lang) {
			case de:
				return "Gefahrenstufe 3 - Erheblich";
			case it:
				return "Grado Pericolo 3 - Marcato";
			case en:
				return "Danger Level 3 - Considerable";
			default:
				return "Danger Level 3 - Considerable";
			}
		case high:
			switch (lang) {
			case de:
				return "Gefahrenstufe 4 - Groß";
			case it:
				return "Grado Pericolo 4 - Forte";
			case en:
				return "Danger Level 4 - High";
			default:
				return "Danger Level 4 - High";
			}
		case very_high:
			switch (lang) {
			case de:
				return "Gefahrenstufe 5 - Sehr Groß";
			case it:
				return "Grado Pericolo 5 - Molto Forte";
			case en:
				return "Danger Level 5 - Very High";
			default:
				return "Danger Level 5 - Very High";
			}
		case no_rating:
			switch (lang) {
			case de:
				return "Keine Beurteilung";
			case it:
				return "Senza Valutazione";
			case en:
				return "No Rating";
			default:
				return "No Rating";
			}
		default:
			switch (lang) {
			case de:
				return "Fehlt";
			case it:
				return "Mancha";
			case en:
				return "Missing";
			default:
				return "Missing";
			}
		}
	}

	private String getDangerRatingColorStyle(String dangerRatingColor) {
		return "style=\"margin: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; color: #565f61; width: 100%; padding: 15px; border-left: 5px solid "
				+ dangerRatingColor + ";\"";
	}

	private String getHeadlineStyle(String dangerRatingColor) {
		return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif; line-height: 1.1; margin-bottom: 0px; font-weight: bold; font-size: 24px; color: "
				+ dangerRatingColor + ";\"";
	}

	private String getDangerPatternStyle(boolean b) {
		if (b)
			return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; margin-bottom: 10px; font-weight: normal; line-height: 1.6; font-size: 12px; color: #565f61; border: 1px solid #565f61; border-radius: 15px; padding-left: 10px; padding-right: 10px; padding-top: 2px; padding-bottom: 2px; margin-right: 5px; display: inline-block; background-color: #FFFFFF;\"";
		else
			return "";
	}

	private String getSnowpackStyle(boolean b) {
		if (!b)
			return "style=\"display: none;\"";
		else
			return "style=\"margin: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; color: #565f61; border-left: 5px solid #1aabff; padding-left: 10px; width: 100%; padding: 15px; background-color: #f6fafc;\"";
	}

	private String getPMStyle(boolean daytimeDependency) {
		if (!daytimeDependency)
			return "style=\"display: none; overflow: hidden; height: 0px;\"";
		else
			return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; color: #565f61; width: 100%; margin-top: 10px; border-top: 1px solid #e6eef2; padding-top: 10px;\"";
	}

	public void sendEmail(List<AvalancheBulletin> bulletins, LanguageCode lang, String region)
			throws MessagingException {
		logger.debug("Sending mail...");
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		// TODO create mail server and add infos
		props.setProperty("mail.host", "smtp.mymailserver.com");
		props.setProperty("mail.user", "myuser");
		props.setProperty("mail.password", "mypwd");
		Session mailSession = Session.getDefaultInstance(props, null);
		mailSession.setDebug(true);
		Transport transport = mailSession.getTransport();

		MimeMessage message = new MimeMessage(mailSession);
		switch (lang) {
		case de:
			message.setSubject("Lawinenvorhersage, " + getDate(bulletins, lang));
			break;
		case it:
			message.setSubject("Avalanche Forecast, " + getDate(bulletins, lang));
			break;
		case en:
			message.setSubject("Previsione Valanghe, " + getDate(bulletins, lang));
			break;

		default:
			break;
		}
		message.setFrom(new InternetAddress(GlobalVariables.avalancheReportUsername));

		// TODO set recipients based on region
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("n.lanzanasto@gmail.com"));
		MimeMultipart multipart = new MimeMultipart("related");

		// add html
		BodyPart messageBodyPart = new MimeBodyPart();
		String htmlText = EmailUtil.getInstance().createEmailHtml(bulletins, lang);
		messageBodyPart.setContent(htmlText, "text/html");
		multipart.addBodyPart(messageBodyPart);

		// add CI image
		messageBodyPart = new MimeBodyPart();
		URL imageUrl = ClassLoader.getSystemResource("images/Colorbar.gif");
		DataSource fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "cid:ci");
		multipart.addBodyPart(messageBodyPart);

		// add logo image
		messageBodyPart = new MimeBodyPart();
		switch (lang) {
		case en:
			imageUrl = ClassLoader.getSystemResource("images/Logo Avalanche.report.png");
			break;
		case de:
			imageUrl = ClassLoader.getSystemResource("images/Logo Lawinen.report.png");
			break;
		case it:
			imageUrl = ClassLoader.getSystemResource("images/Logo Valanghe.report.png");
			break;
		default:
			break;
		}
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "cid:logo");
		multipart.addBodyPart(messageBodyPart);

		// add facebook image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/facebook.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "cid:facebook");
		multipart.addBodyPart(messageBodyPart);

		// add twitter image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/twitter.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "cid:twitter");
		multipart.addBodyPart(messageBodyPart);

		// add instagram image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/instagram.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "cid:instagram");
		multipart.addBodyPart(messageBodyPart);

		// add youtube image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/youtube.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "cid:youtube");
		multipart.addBodyPart(messageBodyPart);

		// add whatsapp image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/whatsapp.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "cid:whatsapp");
		multipart.addBodyPart(messageBodyPart);

		// add icons for each bulletin
		addIcons(bulletins, multipart);

		// TODO add maps

		message.setContent(multipart);

		transport.connect();
		transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
		transport.close();
	}

	private void addIcons(List<AvalancheBulletin> bulletins, MimeMultipart multipart) throws MessagingException {
		List<String> warningPictos = new ArrayList<String>();
		List<Tendency> tendencies = new ArrayList<Tendency>();
		List<eu.albina.model.enumerations.AvalancheSituation> avalancheSituations = new ArrayList<eu.albina.model.enumerations.AvalancheSituation>();
		List<String> elevations = new ArrayList<String>();

		MimeBodyPart messageBodyPart;
		URL imageUrl;
		FileDataSource fds;

		// add aspects icons
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/aspects/exposition_bg.svg");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "cid:aspect/bg");
		multipart.addBodyPart(messageBodyPart);

		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/aspects/exposition_empty.svg");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "cid:aspect/empty");
		multipart.addBodyPart(messageBodyPart);

		for (Aspect aspect : Aspect.values()) {
			messageBodyPart = new MimeBodyPart();
			imageUrl = ClassLoader.getSystemResource("images/aspects/exposition_" + aspect.toString() + ".svg");
			fds = new FileDataSource(imageUrl.toString());
			messageBodyPart.setDataHandler(new DataHandler(fds));
			messageBodyPart.setHeader("Content-ID", "cid:aspect/" + aspect.toString());
			multipart.addBodyPart(messageBodyPart);
		}

		for (AvalancheBulletin avalancheBulletin : bulletins) {
			// add danger level
			String id = getWarningLevelId(avalancheBulletin.getForenoon(),
					avalancheBulletin.isHasElevationDependency());
			if (!warningPictos.contains(id)) {
				messageBodyPart = new MimeBodyPart();
				imageUrl = ClassLoader.getSystemResource("images/warning_pictos/level_" + id);
				fds = new FileDataSource(imageUrl.toString());
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "warning_picto/" + id);
				multipart.addBodyPart(messageBodyPart);
				warningPictos.add(id);
			}

			// add tendency symbol
			switch (avalancheBulletin.getTendency()) {
			case increasing:
				if (!tendencies.contains(Tendency.increasing)) {
					messageBodyPart = new MimeBodyPart();
					imageUrl = ClassLoader.getSystemResource("images/tendency_increasing_blue.png");
					fds = new FileDataSource(imageUrl.toString());
					messageBodyPart.setDataHandler(new DataHandler(fds));
					messageBodyPart.setHeader("Content-ID", "tendency/increasing");
					multipart.addBodyPart(messageBodyPart);
					tendencies.add(Tendency.increasing);
				}
				break;
			case steady:
				if (!tendencies.contains(Tendency.steady)) {
					messageBodyPart = new MimeBodyPart();
					imageUrl = ClassLoader.getSystemResource("images/tendency_steady_blue.png");
					fds = new FileDataSource(imageUrl.toString());
					messageBodyPart.setDataHandler(new DataHandler(fds));
					messageBodyPart.setHeader("Content-ID", "tendency/steady");
					multipart.addBodyPart(messageBodyPart);
					tendencies.add(Tendency.steady);
				}
				break;
			case decreasing:
				if (!tendencies.contains(Tendency.decreasing)) {
					messageBodyPart = new MimeBodyPart();
					imageUrl = ClassLoader.getSystemResource("images/tendency_decreasing_blue.png");
					fds = new FileDataSource(imageUrl.toString());
					messageBodyPart.setDataHandler(new DataHandler(fds));
					messageBodyPart.setHeader("Content-ID", "tendency/decreasing");
					multipart.addBodyPart(messageBodyPart);
					tendencies.add(Tendency.decreasing);
				}
				break;

			default:
				break;
			}

			// add avalanche situation icons
			if (avalancheBulletin.getForenoon().getAvalancheSituation1() != null) {
				if (avalancheBulletin.getForenoon().getAvalancheSituation1().getAvalancheSituation() != null)
					addAvalancheSituation(avalancheBulletin.getForenoon().getAvalancheSituation1(), multipart,
							avalancheSituations);
				addAvalancheSituationElevation(avalancheBulletin.getForenoon().getAvalancheSituation1(), multipart,
						elevations);
			}
			if (avalancheBulletin.getForenoon().getAvalancheSituation2() != null) {
				if (avalancheBulletin.getForenoon().getAvalancheSituation2().getAvalancheSituation() != null)
					addAvalancheSituation(avalancheBulletin.getForenoon().getAvalancheSituation2(), multipart,
							avalancheSituations);
				addAvalancheSituationElevation(avalancheBulletin.getForenoon().getAvalancheSituation2(), multipart,
						elevations);
			}

			if (avalancheBulletin.isHasDaytimeDependency()) {
				id = getWarningLevelId(avalancheBulletin.getAfternoon(), avalancheBulletin.isHasElevationDependency());
				if (!warningPictos.contains(id)) {
					messageBodyPart = new MimeBodyPart();
					imageUrl = ClassLoader.getSystemResource("images/warning_pictos/level_" + id);
					fds = new FileDataSource(imageUrl.toString());
					messageBodyPart.setDataHandler(new DataHandler(fds));
					messageBodyPart.setHeader("Content-ID", "cid:warning_picto/" + id);
					multipart.addBodyPart(messageBodyPart);
					warningPictos.add(id);
				}

				// add avalanche situation icons
				if (avalancheBulletin.getAfternoon().getAvalancheSituation1() != null) {
					if (avalancheBulletin.getAfternoon().getAvalancheSituation1().getAvalancheSituation() != null)
						addAvalancheSituation(avalancheBulletin.getAfternoon().getAvalancheSituation1(), multipart,
								avalancheSituations);
					addAvalancheSituationElevation(avalancheBulletin.getAfternoon().getAvalancheSituation1(), multipart,
							elevations);
				}
				if (avalancheBulletin.getAfternoon().getAvalancheSituation2() != null) {
					if (avalancheBulletin.getAfternoon().getAvalancheSituation2().getAvalancheSituation() != null)
						addAvalancheSituation(avalancheBulletin.getAfternoon().getAvalancheSituation2(), multipart,
								avalancheSituations);
					addAvalancheSituationElevation(avalancheBulletin.getAfternoon().getAvalancheSituation2(), multipart,
							elevations);
				}
			}
		}

	}

	private void addAvalancheSituationElevation(AvalancheSituation avalancheSituation, MimeMultipart multipart,
			List<String> elevations) throws MessagingException {
		MimeBodyPart messageBodyPart;
		URL imageUrl;
		FileDataSource fds;

		if (avalancheSituation.getTreelineHigh() || avalancheSituation.getElevationHigh() > 0) {
			if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
				// elevation high and low set
				messageBodyPart = new MimeBodyPart();
				imageUrl = ClassLoader.getSystemResource("images/elevation/levels_middle.svg");
				fds = new FileDataSource(imageUrl.toString());
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "cid:elevation/middle");
				multipart.addBodyPart(messageBodyPart);
				elevations.add("levels_middle");
			} else {
				// elevation high set
				messageBodyPart = new MimeBodyPart();
				imageUrl = ClassLoader.getSystemResource("images/elevation/levels_below.svg");
				fds = new FileDataSource(imageUrl.toString());
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "cid:elevation/below");
				multipart.addBodyPart(messageBodyPart);
				elevations.add("levels_below");
			}
		} else if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
			// elevation low set
			messageBodyPart = new MimeBodyPart();
			imageUrl = ClassLoader.getSystemResource("images/elevation/levels_above.svg");
			fds = new FileDataSource(imageUrl.toString());
			messageBodyPart.setDataHandler(new DataHandler(fds));
			messageBodyPart.setHeader("Content-ID", "cid:elevation/above");
			multipart.addBodyPart(messageBodyPart);
			elevations.add("levels_above");
		} else {
			// no elevation set
			messageBodyPart = new MimeBodyPart();
			imageUrl = ClassLoader.getSystemResource("images/elevation/levels_all.svg");
			fds = new FileDataSource(imageUrl.toString());
			messageBodyPart.setDataHandler(new DataHandler(fds));
			messageBodyPart.setHeader("Content-ID", "cid:elevation/all");
			multipart.addBodyPart(messageBodyPart);
			elevations.add("levels_all");
		}
	}

	private void addAvalancheSituation(AvalancheSituation avalancheSituation, MimeMultipart multipart,
			List<eu.albina.model.enumerations.AvalancheSituation> avalancheSituations) throws MessagingException {
		MimeBodyPart messageBodyPart;
		URL imageUrl;
		FileDataSource fds;

		switch (avalancheSituation.getAvalancheSituation()) {
		case new_snow:
			if (!avalancheSituations.contains(eu.albina.model.enumerations.AvalancheSituation.new_snow)) {
				messageBodyPart = new MimeBodyPart();
				imageUrl = ClassLoader.getSystemResource("images/avalanche_situations/color/new_snow.svg");
				fds = new FileDataSource(imageUrl.toString());
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "cid:avalanche_situation/new_snow");
				multipart.addBodyPart(messageBodyPart);
				avalancheSituations.add(eu.albina.model.enumerations.AvalancheSituation.new_snow);
			}
			break;
		case wind_drifted_snow:
			if (!avalancheSituations.contains(eu.albina.model.enumerations.AvalancheSituation.wind_drifted_snow)) {
				messageBodyPart = new MimeBodyPart();
				imageUrl = ClassLoader.getSystemResource("images/avalanche_situations/color/wind_drifted_snow.svg");
				fds = new FileDataSource(imageUrl.toString());
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "cid:avalanche_situation/wind_drifted_snow");
				multipart.addBodyPart(messageBodyPart);
				avalancheSituations.add(eu.albina.model.enumerations.AvalancheSituation.wind_drifted_snow);
			}
			break;
		case weak_persistent_layer:
			if (!avalancheSituations.contains(eu.albina.model.enumerations.AvalancheSituation.weak_persistent_layer)) {
				messageBodyPart = new MimeBodyPart();
				imageUrl = ClassLoader.getSystemResource("images/avalanche_situations/color/weak_persistent_layer.svg");
				fds = new FileDataSource(imageUrl.toString());
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "cid:avalanche_situation/weak_persistent_layer");
				multipart.addBodyPart(messageBodyPart);
				avalancheSituations.add(eu.albina.model.enumerations.AvalancheSituation.weak_persistent_layer);
			}
			break;
		case wet_snow:
			if (!avalancheSituations.contains(eu.albina.model.enumerations.AvalancheSituation.wet_snow)) {
				messageBodyPart = new MimeBodyPart();
				imageUrl = ClassLoader.getSystemResource("images/avalanche_situations/color/wet_snow.svg");
				fds = new FileDataSource(imageUrl.toString());
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "cid:avalanche_situation/wet_snow");
				multipart.addBodyPart(messageBodyPart);
				avalancheSituations.add(eu.albina.model.enumerations.AvalancheSituation.wet_snow);
			}
			break;
		case gliding_snow:
			if (!avalancheSituations.contains(eu.albina.model.enumerations.AvalancheSituation.gliding_snow)) {
				messageBodyPart = new MimeBodyPart();
				imageUrl = ClassLoader.getSystemResource("images/avalanche_situations/color/gliding_snow.svg");
				fds = new FileDataSource(imageUrl.toString());
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "cid:avalanche_situation/gliding_snow");
				multipart.addBodyPart(messageBodyPart);
				avalancheSituations.add(eu.albina.model.enumerations.AvalancheSituation.gliding_snow);
			}
			break;
		case favourable_situation:
			if (!avalancheSituations.contains(eu.albina.model.enumerations.AvalancheSituation.favourable_situation)) {
				messageBodyPart = new MimeBodyPart();
				imageUrl = ClassLoader.getSystemResource("images/avalanche_situations/color/favourable_situation.svg");
				fds = new FileDataSource(imageUrl.toString());
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "cid:avalanche_situation/favourable_situation");
				multipart.addBodyPart(messageBodyPart);
				avalancheSituations.add(eu.albina.model.enumerations.AvalancheSituation.favourable_situation);
			}
			break;

		default:
			break;
		}
	}

	private String getWarningLevelId(AvalancheBulletinDaytimeDescription avalancheBulletinDaytimeDescription,
			boolean elevationDependency) {
		if (elevationDependency)
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingBelow()) + "_"
					+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
		else
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove()) + "_"
					+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
	}
}
