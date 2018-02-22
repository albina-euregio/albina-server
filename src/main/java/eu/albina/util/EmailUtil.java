package eu.albina.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

	private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

	private static Configuration cfg;

	public static void createFreemarkerConfigurationInstance() throws IOException, URISyntaxException {
		cfg = new Configuration(Configuration.VERSION_2_3_27);

		// Specify the source where the template files come from. Here I set a
		// plain directory for it, but non-file-system sources are possible too:
		cfg.setDirectoryForTemplateLoading(new File(ClassLoader.getSystemResource("templates").toURI()));

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

	public static Configuration getFreeMarkerConfiguration() {
		return cfg;
	}

	public static String createEmailHtml(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		try {
			// Create data model
			Map<String, Object> root = new HashMap<>();

			Map<String, Object> image = new HashMap<>();
			image.put("ci", "http://212.47.231.185:8080/images/ci.png");
			image.put("logo", "http://212.47.231.185:8080/images/Avalanche.png");
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
			// TODO lang
			switch (lang) {
			case en:
				text.put("headline", "Avalanche Report");
				text.put("website", "Website");
				text.put("follow", "Follow us");
				text.put("unsubscribe", "Unsubscribe");
				// TODO
				text.put("date", "Friday, 02/16/2018");
				text.put("tendency", "Tendency");
				text.put("snowpack", "Snowpack");
				break;
			case de:
				text.put("headline", "Lawinen Report");
				text.put("website", "Webseite");
				text.put("follow", "Folge uns");
				text.put("unsubscribe", "Abmelden");
				// TODO
				text.put("date", "Freitag, 16.02.2018");
				text.put("tendency", "Tendenz");
				text.put("snowpack", "Schneedecke");
				break;
			case it:
				text.put("headline", "Valanghe Report");
				text.put("website", "Website");
				text.put("follow", "TODO");
				text.put("unsubscribe", "TODO");
				// TODO
				text.put("date", "Venerdi, 16.02.2018");
				text.put("tendency", "TODO");
				text.put("snowpack", "TODO");
				break;

			default:
				break;
			}
			root.put("text", text);

			ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
			for (AvalancheBulletin avalancheBulletin : bulletins) {
				Map<String, Object> bulletin = new HashMap<>();

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

				if (avalancheBulletin.getTendencyCommentIn(lang) != null)
					bulletin.put("tendencyComment", avalancheBulletin.getTendencyCommentIn(lang));
				else
					bulletin.put("tendencyComment", "");

				if (avalancheBulletin.getTendency() == Tendency.decreasing)
					bulletin.put("tendency", "cid:decreasing");
				else if (avalancheBulletin.getTendency() == Tendency.steady)
					bulletin.put("tendency", "cid:steady");
				else if (avalancheBulletin.getTendency() == Tendency.increasing)
					bulletin.put("tendency", "cid:increasing");
				else
					bulletin.put("tendency", "");

				bulletin.put("dangerratingcolorstyle", EmailUtil.getDangerRatingColorStyle(
						AlbinaUtil.getDangerRatingColor(avalancheBulletin.getHighestDangerRating())));

				// TODO use correct map and symbols
				bulletin.put("map", "http://212.47.231.185:8080/images/map_detail.png");
				bulletin.put("symbols", "http://212.47.231.185:8080/images/symbols_detail.png");
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

	private static String getDangerRatingColorStyle(String dangerRatingColor) {
		return "style=\"margin: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; color: #565f61; width: 100%; padding: 15px; border-left: 5px solid "
				+ dangerRatingColor + ";\"";
	}

	public static void sendEmail(List<AvalancheBulletin> bulletins, LanguageCode lang) throws MessagingException {
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
		// TODO set recipients
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("n.lanzanasto@gmail.com"));
		MimeMultipart multipart = new MimeMultipart("related");

		// add html
		BodyPart messageBodyPart = new MimeBodyPart();
		String htmlText = EmailUtil.createEmailHtml(bulletins, lang);
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

		// TODO add PDF

		message.setContent(multipart);

		transport.connect();
		transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
		transport.close();
	}
}
