package eu.albina.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import eu.albina.controller.socialmedia.RapidMailProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.Subscriber;
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

	public void sendConfirmationEmail(Subscriber subscriber)
			throws IllegalArgumentException, UnsupportedEncodingException, AlbinaException {
		logger.debug("Sending confirmation email to " + subscriber.getEmail());
		String token = issueConfirmationToken(subscriber.getEmail());
		Session session = getEmailSession();

		try {
			MimeMessage message = new MimeMessage(session);
			message.addHeader("Content-type", "text/HTML; charset=UTF-8");
			message.addHeader("format", "flowed");
			message.addHeader("Content-Transfer-Encoding", "8bit");

			message.setSubject(GlobalVariables.getConfirmationEmailSubject(subscriber.getLanguage()),
					GlobalVariables.getEmailEncoding());
			message.setFrom(new InternetAddress(GlobalVariables.avalancheReportUsername,
					GlobalVariables.getEmailFromPersonal(subscriber.getLanguage())));

			message.addRecipient(Message.RecipientType.TO, new InternetAddress(subscriber.getEmail()));

			MimeMultipart multipart = new MimeMultipart("related");

			// add html
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			String htmlText = createConfirmationEmailHtml(token, subscriber.getLanguage());
			messageBodyPart.setContent(htmlText, "text/html; charset=utf-8");
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart, GlobalVariables.getEmailEncoding());
			Transport.send(message);

			logger.debug("Confirmation email sent to " + subscriber.getEmail());
		} catch (MessagingException e) {
			logger.error("Confirmation email could not be sent to " + subscriber.getEmail() + ": " + e.getMessage());
			e.printStackTrace();
			throw new AlbinaException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("Confirmation email could not be sent to " + subscriber.getEmail() + ": " + e.getMessage());
			e.printStackTrace();
			throw new AlbinaException(e.getMessage());
		}
	}

	private String issueConfirmationToken(String email) throws IllegalArgumentException, UnsupportedEncodingException {
		Algorithm algorithm = Algorithm.HMAC256(GlobalVariables.tokenEncodingSecret);
		long time = System.currentTimeMillis() + GlobalVariables.confirmationTokenExpirationDuration;
		Date expirationTime = new Date(time);
		Date issuedAt = new Date();
		String token = JWT.create().withIssuer(GlobalVariables.tokenEncodingIssuer).withSubject(email)
				.withIssuedAt(issuedAt).withExpiresAt(expirationTime).sign(algorithm);
		return token;
	}

	public void sendBulletinEmails(List<AvalancheBulletin> bulletins, List<String> regions) {
		for (LanguageCode lang : GlobalVariables.languages) {
			String subject = GlobalVariables.getEmailSubject(lang) + AlbinaUtil.getDate(bulletins, lang);
			for (String region : regions) {
				String emailHtml = createBulletinEmailHtml(bulletins, lang, region);
				sendBulletinEmailRapidmail(lang, region, emailHtml, subject);
			}
		}
	}

	private Session getEmailSession() {
		Properties props = new Properties();
		props.put("mail.smtp.auth", GlobalVariables.getSmtpAuth());
		props.put("mail.smtp.starttls.enable", GlobalVariables.getSmtpTls());
		props.put("mail.smtp.host", GlobalVariables.getSmtpHost());
		props.put("mail.smtp.port", GlobalVariables.getSmtpPort());

		return Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(GlobalVariables.getEmailUsername(),
						GlobalVariables.getEmailPassword());
			}
		});
	}

	private String createZipFile(String htmlContent, String textContent) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(baos);
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

	public HttpResponse sendBulletinEmailRapidmail(LanguageCode lang, String region, String emailHtml, String subject) {
		logger.debug("Sending bulletin email in " + lang + " for " + region + "...");
		try {
			RapidMailProcessorController rmc = RapidMailProcessorController.getInstance();
			RegionConfigurationController rcc = RegionConfigurationController.getInstance();
			RegionConfiguration regionConfiguration = rcc.getRegionConfiguration(region);
			RapidMailConfig rmConfig = regionConfiguration.getRapidMailConfig();

			return rmc.sendMessage(rmConfig, lang.name().toUpperCase(),
					new PostMailingsRequest().fromEmail(GlobalVariables.getFromEmail(lang))
							.fromName(GlobalVariables.getFromName(lang)).subject(subject)
							.file(new PostMailingsRequestPostFile().description("mail-content.zip")
									.type("application/zip").content(createZipFile(emailHtml, null))));
		} catch (Exception e) {
			logger.error("Emails could not be sent in " + lang + " for " + region + ": " + e.getMessage());
			return null;
		}
	}

	// public void sendBulletinEmail(List<AvalancheBulletin> bulletins, LanguageCode
	// lang, List<String> recipients) {
	// logger.debug("Sending bulletin email in " + lang + "...");
	//
	// Session session = getEmailSession();
	//
	// try {
	// MimeMessage message = new MimeMessage(session);
	// message.addHeader("Content-type", "text/HTML; charset=UTF-8");
	// message.addHeader("format", "flowed");
	// message.addHeader("Content-Transfer-Encoding", "8bit");
	// message.setSubject(GlobalVariables.getEmailSubject(lang) +
	// AlbinaUtil.getDate(bulletins, lang),
	// GlobalVariables.getEmailEncoding());
	// message.setFrom(new InternetAddress(GlobalVariables.avalancheReportUsername,
	// GlobalVariables.getEmailFromPersonal(lang)));
	//
	// if (recipients != null && !recipients.isEmpty()) {
	// for (String recipient : recipients)
	// message.addRecipient(Message.RecipientType.TO, new
	// InternetAddress(recipient));
	//
	// MimeMultipart multipart = new MimeMultipart("related");
	//
	// // add html
	// MimeBodyPart messageBodyPart = new MimeBodyPart();
	// String htmlText = createBulletinEmailHtml(bulletins, lang);
	// messageBodyPart.setContent(htmlText, "text/html; charset=utf-8");
	// multipart.addBodyPart(messageBodyPart);
	//
	// message.setContent(multipart, GlobalVariables.getEmailEncoding());
	// Transport.send(message);
	//
	// logger.debug("Emails sent in " + lang + ".");
	// } else
	// logger.debug("No recipients for emails in " + lang + ".");
	// } catch (MessagingException e) {
	// logger.error("Emails could not be sent in " + lang + ": " + e.getMessage());
	// e.printStackTrace();
	// throw new RuntimeException(e);
	// } catch (UnsupportedEncodingException e) {
	// logger.error("Emails could not be sent in " + lang + ": " + e.getMessage());
	// e.printStackTrace();
	// throw new RuntimeException(e);
	// }
	// }

	public String createConfirmationEmailHtml(String token, LanguageCode lang) {
		try {
			// Create data model
			Map<String, Object> root = new HashMap<>();
			root.put("token", token);
			root.put("snowpackstyle", getSnowpackStyle(true));
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
			image.put("ci", GlobalVariables.getServerImagesUrl() + "logo/color/colorbar.gif");
			Map<String, Object> socialMediaImages = new HashMap<>();
			socialMediaImages.put("facebook", GlobalVariables.getServerImagesUrl() + "social_media/facebook.png");
			socialMediaImages.put("twitter", GlobalVariables.getServerImagesUrl() + "social_media/twitter.png");
			socialMediaImages.put("instagram", GlobalVariables.getServerImagesUrl() + "social_media/instagram.png");
			socialMediaImages.put("youtube", GlobalVariables.getServerImagesUrl() + "social_media/youtube.png");
			socialMediaImages.put("whatsapp", GlobalVariables.getServerImagesUrl() + "social_media/whatsapp.png");
			image.put("socialmedia", socialMediaImages);
			root.put("image", image);

			// add texts
			Map<String, Object> text = new HashMap<>();
			text.put("title", GlobalVariables.getTitle(lang));
			text.put("follow", GlobalVariables.getFollowUs(lang));
			switch (lang) {
			case de:
				text.put("title", "Lawinen.report");
				text.put("headline", "Hallo!");
				text.put("confirm", "Bestätigen");
				text.put("confirmation", "Anmeldebestätigung");
				break;
			case it:
				text.put("title", "Valanghe.report");
				text.put("headline", "Ciao!");
				text.put("confirm", "Confermare");
				text.put("confirmation", "Conferma d'iscrizione");
				break;
			case en:
				text.put("title", "Avalanche.report");
				text.put("headline", "Hello!");
				text.put("confirm", "Confirm");
				text.put("confirmation", "Registration confirmation");
				break;
			default:
				text.put("title", "Avalanche.report");
				text.put("headline", "Hello!");
				text.put("confirm", "Confirm");
				text.put("confirmation", "Registration confirmation");
				break;
			}
			text.put("body1", getConfirmationText1(lang));
			text.put("body2", getConfirmationText2(lang));
			root.put("text", text);

			Map<String, Object> links = new HashMap<>();
			links.put("confirm", GlobalVariables.avalancheReportBaseUrl + "subscribe/" + token);
			links.put("website", GlobalVariables.avalancheReportBaseUrl);
			Map<String, Object> socialMediaLinks = new HashMap<>();
			socialMediaLinks.put("facebook", "https://avalanche.report/facebook");
			socialMediaLinks.put("twitter", "https://avalanche.report/twitter");
			socialMediaLinks.put("instagram", "https://avalanche.report/instagram");
			socialMediaLinks.put("youtube", "https://avalanche.report/youtube");
			socialMediaLinks.put("whatsapp", "https://avalanche.report/whatsapp");
			links.put("socialmedia", socialMediaLinks);
			root.put("link", links);

			// Get template
			Template temp = cfg.getTemplate("confirmation-email.html");

			// Merge template and model
			Writer out = new StringWriter();
			// Writer out = new OutputStreamWriter(System.out);
			temp.process(root, out);

			return out.toString();
		} catch (IOException e) {
			logger.error("Confirmation email could not be created: " + e.getMessage());
			e.printStackTrace();
		} catch (TemplateException e) {
			logger.error("Confirmation email could not be created: " + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	private String getConfirmationText1(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Danke für deine Registrierung bei Lawinen.report.\n\nUm die Registrierung abzuschließen, klicke bitte auf folgenden Link:\n";
		case it:
			return "Grazie per esservi registrati su Avalanche.report.\n\nPer completare la registrazione, seguire il link:\n";
		case en:
			return "Thank you for registering at Avalanche.report.\n\nTo complete the registration, follow the link:\n";
		default:
			return "Thank you for registering at Avalanche.report.\n\nTo complete the registration, follow the link:\n";
		}
	}

	private String getConfirmationText2(LanguageCode lang) {
		switch (lang) {
		case de:
			return "Wenn du dich nicht bei Lawinen.report registriert hast, ignoriere einfach diese Nachricht.";
		case it:
			return "Se non si è registrato su Valanghe.report, è sufficiente ignorare questo messaggio.";
		case en:
			return "If you did not register at Avalanche.report, just ignore this message.";
		default:
			return "If you did not register at Avalanche.report, just ignore this message.";
		}
	}

	public String createBulletinEmailHtml(List<AvalancheBulletin> bulletins, LanguageCode lang, String region) {
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

			// maps
			if (AlbinaUtil.hasDaytimeDependency(bulletins)) {
				mapImage.put("overview",
						GlobalVariables.getMapsPath() + AlbinaUtil.getValidityDate(bulletins) + "/am_albina_map.jpg");
				mapImage.put("overviewPM",
						GlobalVariables.getMapsPath() + AlbinaUtil.getValidityDate(bulletins) + "/pm_albina_map.jpg");
			} else {
				mapImage.put("overview",
						GlobalVariables.getMapsPath() + AlbinaUtil.getValidityDate(bulletins) + "/fd_albina_map.jpg");
				mapImage.put("overviewPM", GlobalVariables.getServerImagesUrl() + "/empty.png");
			}

			image.put("map", mapImage);
			root.put("image", image);

			Map<String, Object> text = new HashMap<>();
			String publicationDate = AlbinaUtil.getPublicationDate(bulletins, lang);
			text.put("publicationDate", publicationDate);
			if (publicationDate.isEmpty())
				text.put("publishedAt", "");
			else
				text.put("publishedAt", GlobalVariables.getPublishedText(lang));
			text.put("date", AlbinaUtil.getDate(bulletins, lang));
			text.put("title", GlobalVariables.getTitle(lang));
			text.put("headline", GlobalVariables.getHeadline(lang));
			text.put("follow", GlobalVariables.getFollowUs(lang));
			text.put("unsubscribe", GlobalVariables.getUnsubscribe(lang));
			text.put("imprint", GlobalVariables.getImprint(lang));
			if (AlbinaUtil.hasDaytimeDependency(bulletins)) {
				text.put("am", GlobalVariables.getAMText(lang));
				text.put("pm", GlobalVariables.getPMText(lang));
			} else {
				text.put("am", "");
				text.put("pm", "");
			}

			Map<String, Object> dangerRatings = new HashMap<>();
			dangerRatings.put("low", GlobalVariables.getDangerRatingTextShort(DangerRating.low, lang));
			dangerRatings.put("moderate", GlobalVariables.getDangerRatingTextShort(DangerRating.moderate, lang));
			dangerRatings.put("considerable",
					GlobalVariables.getDangerRatingTextShort(DangerRating.considerable, lang));
			dangerRatings.put("high", GlobalVariables.getDangerRatingTextShort(DangerRating.high, lang));
			dangerRatings.put("veryHigh", GlobalVariables.getDangerRatingTextShort(DangerRating.very_high, lang));
			text.put("dangerRating", dangerRatings);

			root.put("text", text);

			ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
			for (AvalancheBulletin avalancheBulletin : bulletins) {
				Map<String, Object> bulletin = new HashMap<>();

				bulletin.put("stylepm", getPMStyle(avalancheBulletin.isHasDaytimeDependency()));
				if (avalancheBulletin.isHasDaytimeDependency()) {
					bulletin.put("textam", GlobalVariables.getAMText(lang));
					bulletin.put("textpm", GlobalVariables.getPMText(lang));
				} else {
					bulletin.put("textam", "");
					bulletin.put("textpm", "");
				}

				bulletin.put("warningLevelText",
						GlobalVariables.getDangerRatingTextLong(avalancheBulletin.getHighestDangerRating(), lang));

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
							bulletin.put("dangerpatternstyle", getDangerPatternStyle(true));
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
							bulletin.put("dangerpatternstyle", getDangerPatternStyle(false));
							bulletin.put("dangerPattern1", "");
							bulletin.put("dangerPattern2", "");
						}
					} else {
						bulletin.put("snowpackStructureHeadline", "");
						bulletin.put("snowpackStructureHighlights", "");
						bulletin.put("snowpackStructureComment", "");
						bulletin.put("dangerPatternsHeadline", "");
						bulletin.put("dangerPattern1", "");
						bulletin.put("dangerPattern2", "");
						bulletin.put("dangerpatternstyle", getDangerPatternStyle(false));
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
					bulletin.put("snowpackstyle", getSnowpackStyle(false));
					bulletin.put("snowpackStructureHeadline", "");
					bulletin.put("snowpackStructureHighlights", "");
					bulletin.put("snowpackStructureComment", "");
					bulletin.put("dangerPatternsHeadline", "");
					bulletin.put("dangerPattern1", "");
					bulletin.put("dangerPattern2", "");
					bulletin.put("dangerpatternstyle", getDangerPatternStyle(false));
					bulletin.put("tendencyHeadline", "");
					bulletin.put("tendencyComment", "");
				}

				Map<String, Object> tendency = new HashMap<>();
				tendency.put("text", GlobalVariables.getTendencyText(avalancheBulletin.getTendency(), lang));
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
			links.put("website", GlobalVariables.avalancheReportBaseUrl + "bulletin/"
					+ AlbinaUtil.getValidityDate(bulletins) + "?lang=" + lang.toString());
			links.put("unsubscribe", GlobalVariables.getUnsubscribeLink(lang, region));
			links.put("imprint", GlobalVariables.getImprintLink(lang));
			Map<String, Object> socialMediaLinks = new HashMap<>();
			socialMediaLinks.put("facebook",
					GlobalVariables.avalancheReportBaseUrl + "?lang=" + lang.toString() + "#followDialog");
			socialMediaLinks.put("instagram",
					GlobalVariables.avalancheReportBaseUrl + "?lang=" + lang.toString() + "#followDialog");
			socialMediaLinks.put("youtube",
					GlobalVariables.avalancheReportBaseUrl + "?lang=" + lang.toString() + "#followDialog");
			links.put("socialmedia", socialMediaLinks);
			root.put("link", links);

			// Get template
			Template temp = cfg.getTemplate("albina-email.html");

			// Merge template and model
			Writer out = new StringWriter();
			// Writer out = new OutputStreamWriter(System.out);
			temp.process(root, out);

			return out.toString();
		} catch (IOException e) {
			logger.error("Bulletin email could not be created: " + e.getMessage());
			e.printStackTrace();
		} catch (TemplateException e) {
			logger.error("Bulletin email could not be created: " + e.getMessage());
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
		if ((daytimeBulletin.getDangerRatingBelow() == null
				|| daytimeBulletin.getDangerRatingBelow() == DangerRating.missing
				|| daytimeBulletin.getDangerRatingBelow() == DangerRating.no_rating)
				&& (daytimeBulletin.getDangerRatingAbove() == null
						|| daytimeBulletin.getDangerRatingAbove() == DangerRating.missing
						|| daytimeBulletin.getDangerRatingAbove() == DangerRating.no_rating)) {
			dangerRating.put("symbol", "");
		} else {
			dangerRating.put("symbol", GlobalVariables.getServerImagesUrl() + "warning_pictos/color/level_"
					+ AlbinaUtil.getWarningLevelId(daytimeBulletin, avalancheBulletin.isHasElevationDependency())
					+ ".png");
		}
		// dangerRating.put("symbol", "cid:warning_picto/" +
		// getWarningLevelId(daytimeBulletin,
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

		// maps
		if (isAfternoon)
			bulletin.put("map", GlobalVariables.getMapsPath() + avalancheBulletin.getValidityDateString() + "/"
					+ avalancheBulletin.getId() + "_PM.jpg");
		else
			bulletin.put("map", GlobalVariables.getMapsPath() + avalancheBulletin.getValidityDateString() + "/"
					+ avalancheBulletin.getId() + ".jpg");

		// avalanche situation 1
		Map<String, Object> avalancheSituation1 = new HashMap<>();
		if (daytimeBulletin.getAvalancheSituation1() != null
				&& daytimeBulletin.getAvalancheSituation1().getAvalancheSituation() != null) {
			if (daytimeBulletin.getAvalancheSituation1().getAvalancheSituation() != null) {
				avalancheSituation1.put("symbol", GlobalVariables.getServerImagesUrl() + "avalanche_situations/color/"
						+ daytimeBulletin.getAvalancheSituation1().getAvalancheSituation().toStringId() + ".png");
				// avalancheSituation1.put("symbol", "cid:avalanche-situation/" +
				// daytimeBulletin
				// .getAvalancheSituation1().getAvalancheSituation().toStringId());
				avalancheSituation1.put("text",
						daytimeBulletin.getAvalancheSituation1().getAvalancheSituation().toString(lang));
			} else {
				avalancheSituation1.put("symbol",
						GlobalVariables.getServerImagesUrl() + "avalanche_situations/color/empty.png");
				avalancheSituation1.put("text", "");
			}

			String path = getAspectsImagePath(daytimeBulletin.getAvalancheSituation1().getAspects());
			avalancheSituation1.put("aspects", GlobalVariables.getServerImagesUrl() + path);

			Map<String, Object> elevation = new HashMap<>();
			if (daytimeBulletin.getAvalancheSituation1().getTreelineHigh()
					|| daytimeBulletin.getAvalancheSituation1().getElevationHigh() > 0) {
				if (daytimeBulletin.getAvalancheSituation1().getTreelineLow()
						|| daytimeBulletin.getAvalancheSituation1().getElevationLow() > 0) {
					// elevation high and low set
					elevation.put("symbol",
							GlobalVariables.getServerImagesUrl() + "elevation/color/levels_middle_two.png");
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
					elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/levels_below.png");
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
				elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/levels_above.png");
				// elevation.put("symbol", "cid:elevation/above");
				if (daytimeBulletin.getAvalancheSituation1().getTreelineLow())
					elevation.put("limitAbove", GlobalVariables.getTreelineString(lang));
				else if (daytimeBulletin.getAvalancheSituation1().getElevationLow() > 0)
					elevation.put("limitAbove", daytimeBulletin.getAvalancheSituation1().getElevationLow() + "m");
				elevation.put("limitBelow", "");
			} else {
				// no elevation set
				elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/levels_all.png");
				// elevation.put("symbol", "cid:elevation/all");
				elevation.put("limitAbove", "");
				elevation.put("limitBelow", "");
			}
			avalancheSituation1.put("elevation", elevation);
		} else {
			avalancheSituation1.put("symbol",
					GlobalVariables.getServerImagesUrl() + "avalanche_situations/color/empty.png");
			avalancheSituation1.put("text", "");

			String path = getAspectsImagePath(null);
			avalancheSituation1.put("aspects", GlobalVariables.getServerImagesUrl() + path);

			Map<String, Object> elevation = new HashMap<>();
			elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/empty.png");
			elevation.put("limitAbove", "");
			elevation.put("limitBelow", "");
			avalancheSituation1.put("elevation", elevation);
		}
		bulletin.put("avalancheSituation1", avalancheSituation1);

		// avalanche situation 2
		Map<String, Object> avalancheSituation2 = new HashMap<>();
		if (daytimeBulletin.getAvalancheSituation2() != null
				&& daytimeBulletin.getAvalancheSituation2().getAvalancheSituation() != null) {
			if (daytimeBulletin.getAvalancheSituation2().getAvalancheSituation() != null) {
				avalancheSituation2.put("symbol", GlobalVariables.getServerImagesUrl() + "avalanche_situations/color/"
						+ daytimeBulletin.getAvalancheSituation2().getAvalancheSituation().toStringId() + ".png");
				// avalancheSituation2.put("symbol", "cid:avalanche-situation/" +
				// daytimeBulletin
				// .getAvalancheSituation2().getAvalancheSituation().toStringId());
				avalancheSituation2.put("text",
						daytimeBulletin.getAvalancheSituation2().getAvalancheSituation().toString(lang));
			} else {
				avalancheSituation2.put("symbol",
						GlobalVariables.getServerImagesUrl() + "avalanche_situations/color/empty.png");
				avalancheSituation2.put("text", "");
			}

			String path = getAspectsImagePath(daytimeBulletin.getAvalancheSituation1().getAspects());
			avalancheSituation2.put("aspects", GlobalVariables.getServerImagesUrl() + path);

			Map<String, Object> elevation = new HashMap<>();
			if (daytimeBulletin.getAvalancheSituation2().getTreelineHigh()
					|| daytimeBulletin.getAvalancheSituation2().getElevationHigh() > 0) {
				if (daytimeBulletin.getAvalancheSituation2().getTreelineLow()
						|| daytimeBulletin.getAvalancheSituation2().getElevationLow() > 0) {
					// elevation high and low set
					elevation.put("symbol",
							GlobalVariables.getServerImagesUrl() + "elevation/color/levels_middle_two.png");
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
					elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/levels_below.png");
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
				elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/levels_above.png");
				// elevation.put("symbol", "cid:elevation/above");
				if (daytimeBulletin.getAvalancheSituation2().getTreelineLow())
					elevation.put("limitAbove", GlobalVariables.getTreelineString(lang));
				else if (daytimeBulletin.getAvalancheSituation2().getElevationLow() > 0)
					elevation.put("limitAbove", daytimeBulletin.getAvalancheSituation2().getElevationLow() + "m");
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
			avalancheSituation2.put("symbol",
					GlobalVariables.getServerImagesUrl() + "avalanche_situations/color/empty.png");
			avalancheSituation2.put("text", "");

			String path = getAspectsImagePath(null);
			avalancheSituation2.put("aspects", GlobalVariables.getServerImagesUrl() + path);

			Map<String, Object> elevation = new HashMap<>();
			elevation.put("symbol", GlobalVariables.getServerImagesUrl() + "elevation/color/empty.png");
			elevation.put("limitAbove", "");
			elevation.put("limitBelow", "");
			avalancheSituation2.put("elevation", elevation);
		}
		bulletin.put("avalancheSituation2", avalancheSituation2);
	}

	private String getAspectsImagePath(Set<Aspect> aspects) {
		int result = 0b00000000;
		if (aspects != null && !aspects.isEmpty()) {
			Iterator<Aspect> iterator = aspects.iterator();
			while (iterator.hasNext()) {
				switch (iterator.next()) {
				case N:
					result = result | 0b10000000;
					break;
				case NE:
					result = result | 0b01000000;
					break;
				case E:
					result = result | 0b00100000;
					break;
				case SE:
					result = result | 0b00010000;
					break;
				case S:
					result = result | 0b00001000;
					break;
				case SW:
					result = result | 0b00000100;
					break;
				case W:
					result = result | 0b00000010;
					break;
				case NW:
					result = result | 0b00000001;
					break;

				default:
					break;
				}
			}
			return GlobalVariables.getAspectSymbolPath(result, false);
		} else {
			return GlobalVariables.getAspectSymbolPath(-1, false);
		}
	}

	private String getDangerRatingColorStyle(DangerRating dangerRating) {
		if (dangerRating.equals(DangerRating.very_high)) {
			return "background=\"" + GlobalVariables.getServerImagesUrl() + "bg_checkered.png"
					+ "\" height=\"100%\" width=\"10px\" bgcolor=\"#FF0000\"";
		} else
			return "style=\"background-color: " + AlbinaUtil.getDangerRatingColor(dangerRating)
					+ "; height: 100%; width: 10px; min-width: 10px; padding: 0px; margin: 0px;\"";
	}

	private String getHeadlineStyle(DangerRating dangerRating) {
		if (dangerRating.equals(DangerRating.low) || dangerRating.equals(DangerRating.moderate)) {
			return "style=\"margin: 0; padding: 0; padding-left: 15px; text-decoration: none; font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif; line-height: 1.6; margin-bottom: 0px; font-weight: bold; font-size: 24px; color: "
					+ AlbinaUtil.greyDarkColor + "; background-color: " + AlbinaUtil.getDangerRatingColor(dangerRating)
					+ ";\"";
		} else {
			return "style=\"margin: 0; padding: 0; padding-left: 15px; text-decoration: none; font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif; line-height: 1.6; margin-bottom: 0px; font-weight: bold; font-size: 24px; color: "
					+ AlbinaUtil.getDangerRatingColor(dangerRating) + ";\"";
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
			return "style=\"display: none; overflow: hidden; height: 0px;\"";
		else
			return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; color: #565f61; width: 100%; margin-top: 10px; border-top: 1px solid #e6eef2; padding-top: 10px;\"";
	}
}