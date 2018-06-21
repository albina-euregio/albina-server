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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
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
			image.put("ci", "http://212.47.231.185:8080/images/Colorbar/Colorbar.svg");
			image.put("logo", "http://212.47.231.185:8080/images/Logos/Logo Avalanche.report.png");
			// image.put("ci", "cid:ci);
			// image.put("logo", "cid:logo);
			Map<String, Object> socialMediaImages = new HashMap<>();
			socialMediaImages.put("facebook", "http://212.47.231.185:8080/images/facebook.png");
			socialMediaImages.put("twitter", "http://212.47.231.185:8080/images/twitter.png");
			socialMediaImages.put("instagram", "http://212.47.231.185:8080/images/instagram.png");
			socialMediaImages.put("youtube", "http://212.47.231.185:8080/images/youtube.png");
			socialMediaImages.put("whatsapp", "http://212.47.231.185:8080/images/whatsapp.png");
			// socialMediaImages.put("facebook", "cid:facebook");
			// socialMediaImages.put("twitter", "cid:twitter");
			// socialMediaImages.put("instagram", "cid:instagram");
			// socialMediaImages.put("youtube", "cid:youtube");
			// socialMediaImages.put("whatsapp", "cid:whatsapp");
			image.put("socialmedia", socialMediaImages);
			Map<String, Object> mapImage = new HashMap<>();
			mapImage.put("overview", "http://212.47.231.185:8080/images/map_overview.png");
			// mapImage.put("overview", "cid:map_overview");
			image.put("map", mapImage);
			root.put("image", image);

			Map<String, Object> text = new HashMap<>();
			text.put("title", "Avalanche.report");
			text.put("publicationDate", "03.01.2018, 05:20 PM");

			switch (lang) {
			case en:
				text.put("headline", "Avalanche Forecast");
				// TODO
				text.put("follow", "Follow us");
				text.put("unsubscribe", "Unsubscribe");
				text.put("date", "Thursday 04.01.2018");
				text.put("tendencyDate", "on Friday 05.01.2018");
				text.put("publishedAt", "Published ");
				text.put("tendency", "Tendency");
				text.put("snowpack", "Snowpack");
				text.put("dangerPatterns", "Danger patterns");
				text.put("warningLevelFor", "Warning Level for ");
				break;
			case de:
				text.put("headline", "Lawinen Report");
				// TODO
				text.put("follow", "Folge uns");
				text.put("unsubscribe", "Abmelden");
				text.put("date", "Freitag 04.01.2018");
				text.put("tendencyDate", "am Freitag 05.01.2018");
				text.put("publishedAt", "Publiziert ");
				text.put("publicationDate", "03.01.2018, 05:20 PM");
				text.put("tendency", "Tendenz");
				text.put("snowpack", "Schneedecke");
				text.put("dangerPatterns", "Gefahrenmuster");
				text.put("warningLevelFor", "Warnstufe für ");
				break;
			case it:
				text.put("headline", "Valanghe Report");
				// TODO
				text.put("follow", "TODO");
				text.put("unsubscribe", "TODO");
				text.put("date", "Giovedì 04.01.2018");
				text.put("tendencyDate", "su venerdì 05.01.2018");
				text.put("publishedAt", "TODO ");
				text.put("publicationDate", "03.01.2018, 05:20 PM");
				text.put("tendency", "Tendenza");
				text.put("snowpack", "Descrizione struttura manto nevoso");
				text.put("dangerPatterns", "Situazioni tipo");
				text.put("warningLevelFor", "TODO");
				break;

			default:
				break;
			}
			root.put("text", text);

			ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
			for (AvalancheBulletin avalancheBulletin : bulletins) {
				Map<String, Object> bulletin = new HashMap<>();

				bulletin.put("warningLevelText", getDangerRatingText(avalancheBulletin, lang));

				if (avalancheBulletin.getAvActivityHighlightsIn(lang) != null)
					bulletin.put("avAvalancheHighlights", avalancheBulletin.getAvActivityHighlightsIn(lang));
				else
					bulletin.put("snowpackStructureHighlights", "");

				if (avalancheBulletin.getAvActivityCommentIn(lang) != null)
					bulletin.put("avAvalancheComment", avalancheBulletin.getAvActivityCommentIn(lang));
				else
					bulletin.put("snowpackStructureHighlights", "");

				if (avalancheBulletin.getSnowpackStructureHighlightsIn(lang) != null)
					bulletin.put("snowpackStructureHighlights",
							avalancheBulletin.getSnowpackStructureHighlightsIn(lang));
				else
					bulletin.put("snowpackStructureHighlights", "");

				if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null)
					bulletin.put("snowpackStructureComment", avalancheBulletin.getSnowpackStructureCommentIn(lang));
				else
					bulletin.put("snowpackStructureComment", "");

				// TODO check if this works
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

				if (avalancheBulletin.getTendencyCommentIn(lang) != null)
					bulletin.put("tendencyComment", avalancheBulletin.getTendencyCommentIn(lang));
				else
					bulletin.put("tendencyComment", "");

				if (avalancheBulletin.getTendency() == Tendency.decreasing) {
					// bulletin.put("tendency", "cid:decreasing");
					bulletin.put("tendencySymbol", "http://212.47.231.185:8080/images/tendency_decreasing_black.png");
					switch (lang) {
					case de:
						bulletin.put("tendencyText", "Lawinengefahr sinkt");
						break;
					case en:
						bulletin.put("tendencyText", "Avalanche danger decreasing");
						break;
					case it:
						bulletin.put("tendencyText", "Pericolo valanghe in diminuazione");
						break;
					default:
						break;
					}
				} else if (avalancheBulletin.getTendency() == Tendency.steady) {
					// bulletin.put("tendency", "cid:steady");
					bulletin.put("tendency", "http://212.47.231.185:8080/images/tendency_steady_black.png");
					switch (lang) {
					case de:
						bulletin.put("tendencyText", "Lawinengefahr bleibt gleich");
						break;
					case en:
						bulletin.put("tendencyText", "Avalanche danger stays the same");
						break;
					case it:
						bulletin.put("tendencyText", "Pericolo valanghe stabile");
						break;
					default:
						break;
					}
				} else if (avalancheBulletin.getTendency() == Tendency.increasing) {
					// bulletin.put("tendency", "cid:increasing");
					bulletin.put("tendency", "http://212.47.231.185:8080/images/tendency_increasing_black.png");
					switch (lang) {
					case de:
						bulletin.put("tendencyText", "Lawinengefahr steigt");
						break;
					case en:
						bulletin.put("tendencyText", "Avalanche danger increasing");
						break;
					case it:
						bulletin.put("tendencyText", "Pericolo valanghe in aumento");
						break;
					default:
						break;
					}
				} else {
					bulletin.put("tendency", "");
				}

				bulletin.put("dangerratingcolorstyle", EmailUtil.getInstance().getDangerRatingColorStyle(
						AlbinaUtil.getDangerRatingColor(avalancheBulletin.getHighestDangerRating())));
				bulletin.put("headlinestyle", EmailUtil.getInstance()
						.getHeadlineStyle(AlbinaUtil.getDangerRatingColor(avalancheBulletin.getHighestDangerRating())));

				// TODO use correct map and symbols
				Map<String, Object> dangerRating = new HashMap<>();
				dangerRating.put("symbol", "http://212.47.231.185:8080/images/Warning Pictos/level_3_2.svg");
				dangerRating.put("elevation", "1900m");
				bulletin.put("dangerRating", dangerRating);
				bulletin.put("map", "http://212.47.231.185:8080/images/map_detail_3.png");
				Map<String, Object> avalancheSituation1 = new HashMap<>();
				avalancheSituation1.put("symbol", "http://212.47.231.185:8080/images/Drifting_snow_c.png");
				avalancheSituation1.put("text", "New Snow");
				avalancheSituation1.put("aspects", "http://212.47.231.185:8080/images/Expositions/exposition_bg.svg");
				Map<String, Object> elevation1 = new HashMap<>();
				elevation1.put("symbol", "http://212.47.231.185:8080/images/Warning Pictos/levels_above.svg");
				elevation1.put("limitAbove", "1800m");
				elevation1.put("limitBelow", "");
				avalancheSituation1.put("elevation", elevation1);
				bulletin.put("avalancheSituation1", avalancheSituation1);
				Map<String, Object> avalancheSituation2 = new HashMap<>();
				avalancheSituation2.put("symbol", "http://212.47.231.185:8080/images/Wet_snow_c.png");
				avalancheSituation2.put("text", "Wet Snow");
				avalancheSituation2.put("aspects", "http://212.47.231.185:8080/images/Expositions/exposition_bg.svg");
				Map<String, Object> elevation2 = new HashMap<>();
				elevation2.put("symbol", "http://212.47.231.185:8080/images/Warning Pictos/levels_below.svg");
				elevation2.put("limitAbove", "");
				elevation2.put("limitBelow", "2000m");
				avalancheSituation2.put("elevation", elevation2);
				bulletin.put("avalancheSituation2", avalancheSituation2);
				arrayList.add(bulletin);
			}
			root.put("bulletins", arrayList);

			Map<String, Object> link = new HashMap<>();
			link.put("website", "https://avalanche.report");
			link.put("unsubscribe", "https://avalanche.report/unsubscribe");
			Map<String, Object> socialMediaLinks = new HashMap<>();
			socialMediaLinks.put("facebook", "https://avalanche.report/facebook");
			socialMediaLinks.put("twitter", "https://avalanche.report/twitter");
			socialMediaLinks.put("instagram", "https://avalanche.report/instagram");
			socialMediaLinks.put("youtube", "https://avalanche.report/youtube");
			socialMediaLinks.put("whatsapp", "https://avalanche.report/whatsapp");
			link.put("socialmedia", socialMediaLinks);
			root.put("link", link);

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

	private String getDangerRatingText(AvalancheBulletin bulletin, LanguageCode lang) {
		switch (bulletin.getHighestDangerRating()) {
		case low:
			switch (lang) {
			case en:
				return "Low, Level 1";
			case de:
				return "Gering, Stufe 1";
			case it:
				return "Debole, Grado 1";
			default:
				return "Low, Level 1";
			}
		case moderate:
			switch (lang) {
			case en:
				return "Moderate, Level 2";
			case de:
				return "Mäßig, Stufe 2";
			case it:
				return "Moderato, Grado 2";
			default:
				return "Moderate, Level 2";
			}
		case considerable:
			switch (lang) {
			case en:
				return "Considerable, Level 3";
			case de:
				return "Erheblich, Stufe 3";
			case it:
				return "Marcato, Grado 3";
			default:
				return "Considerable, Level 3";
			}
		case high:
			switch (lang) {
			case en:
				return "High, Level 4";
			case de:
				return "Groß, Stufe 4";
			case it:
				return "Forte, Grado 4";
			default:
				return "High, Level 4";
			}
		case very_high:
			switch (lang) {
			case en:
				return "Very High, Level 5";
			case de:
				return "Sehr Groß, Stufe 5";
			case it:
				return "Molto Forte, Grado 5";
			default:
				return "Very High, Level 5";
			}
		default:
			switch (lang) {
			case en:
				return "Missing";
			case de:
				return "Fehlt";
			case it:
				return "Mancha";
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
		// TODO set subject
		message.setSubject("Avalanche Report");
		// TODO set from
		message.setFrom(new InternetAddress("info@avalanche.report"));
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
		URL imageUrl = ClassLoader.getSystemResource("images/ci.png");
		DataSource fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "ci");
		multipart.addBodyPart(messageBodyPart);

		// add logo image
		messageBodyPart = new MimeBodyPart();
		switch (lang) {
		case en:
			imageUrl = ClassLoader.getSystemResource("images/logo_en.png");
			break;
		case de:
			imageUrl = ClassLoader.getSystemResource("images/logo_de.png");
			break;
		case it:
			imageUrl = ClassLoader.getSystemResource("images/logo_it.png");
			break;
		default:
			break;
		}
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "logo");
		multipart.addBodyPart(messageBodyPart);

		// add facebook image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/facebook.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "facebook");
		multipart.addBodyPart(messageBodyPart);

		// add twitter image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/twitter.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "twitter");
		multipart.addBodyPart(messageBodyPart);

		// add instagram image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/instagram.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "instagram");
		multipart.addBodyPart(messageBodyPart);

		// add youtube image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/youtube.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "youtube");
		multipart.addBodyPart(messageBodyPart);

		// add whatsapp image
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/whatsapp.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "whatsapp");
		multipart.addBodyPart(messageBodyPart);

		// TODO add tendency symbols (decreasing, steady, increasing)
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/tendency_decreasing.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "decreasing");
		multipart.addBodyPart(messageBodyPart);
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/tendency_steady.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "steady");
		multipart.addBodyPart(messageBodyPart);
		messageBodyPart = new MimeBodyPart();
		imageUrl = ClassLoader.getSystemResource("images/tendency_increasing.png");
		fds = new FileDataSource(imageUrl.toString());
		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "increasing");
		multipart.addBodyPart(messageBodyPart);

		// TODO add maps

		// TODO add PDF (shell we add it?)

		message.setContent(multipart);

		transport.connect();
		transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
		transport.close();
	}
}
