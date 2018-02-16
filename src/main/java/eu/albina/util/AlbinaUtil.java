package eu.albina.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class AlbinaUtil {

	private static final Logger logger = LoggerFactory.getLogger(AlbinaUtil.class);

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

	public static String convertDocToString(Document doc) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		return result.getWriter().toString();
	}

	public static Document createXmlError(String key, String value) throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(key);
		rootElement.appendChild(doc.createTextNode(value));
		return doc;
	}

	public static JSONObject createRegionHeaderJson() {
		JSONObject json = new JSONObject();
		json.put("type", "FeatureCollection");
		JSONObject crs = new JSONObject();
		crs.put("type", "name");
		JSONObject properties = new JSONObject();
		properties.put("name", GlobalVariables.referenceSystemUrn);
		crs.put("properties", properties);
		json.put("crs", crs);
		return json;
	}

	public static Element createRegionHeaderCaaml(Document doc) throws ParserConfigurationException {
		Element rootElement = doc.createElement("LocationCollection");
		rootElement.setAttribute("xmlns", "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS");
		rootElement.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
		rootElement.setAttribute("xmlns:app", "ALBINA");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xmlns:schemaLocation",
				"http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS/CAAMLv5_BulletinEAWS.xsd");
		doc.appendChild(rootElement);

		Element metaDataProperty = doc.createElement("metaDataProperty");
		Element metaData = doc.createElement("MetaData");
		Element dateTimeReport = doc.createElement("dateTimeReport");
		dateTimeReport.appendChild(doc.createTextNode((new DateTime()).toString(GlobalVariables.formatterDateTime)));
		metaData.appendChild(dateTimeReport);
		Element srcRef = doc.createElement("srcRef");
		Element operation = doc.createElement("Operation");
		operation.setAttribute("gml:id", "ALBINA");
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode("ALBINA"));
		operation.appendChild(name);
		srcRef.appendChild(operation);
		metaData.appendChild(srcRef);
		metaDataProperty.appendChild(metaData);
		rootElement.appendChild(metaDataProperty);
		return rootElement;
	}

	public static Element createObsCollectionHeaderCaaml(Document doc) {
		Element rootElement = doc.createElement("ObsCollection");
		rootElement.setAttribute("xmlns", "http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS");
		rootElement.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
		rootElement.setAttribute("xmlns:albina", "http://212.47.231.185:8080/caaml/albina.xsd");
		rootElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xmlns:schemaLocation",
				"http://caaml.org/Schemas/V5.0/Profiles/BulletinEAWS/CAAMLv5_BulletinEAWS.xsd");
		rootElement.setAttribute("xmlns:app", "ALBINA");

		return rootElement;
	}

	public static String createValidElevationAttribute(int elevation, boolean above, boolean treeline) {
		// TODO allow treeline in CAAML
		if (treeline) {
			if (above)
				return "ElevationRange_TreelineHi";
			else
				return "ElevationRange_TreelineLw";
		} else {
			if (above)
				return "ElevationRange_" + elevation + "Hi";
			else
				return "ElevationRange_" + elevation + "Lw";
		}
	}

	public static String createMapUrlOverview(DateTime date, int version, String daytime, int resolution,
			String fileExtension) {
		StringBuilder result = new StringBuilder();
		result.append(GlobalVariables.univieBaseUrl);
		result.append(date.toString(GlobalVariables.formatterDate));
		result.append(GlobalVariables.urlSeperator);
		result.append(version);
		result.append(GlobalVariables.urlSeperator);
		result.append(daytime);
		result.append(GlobalVariables.urlSeperator);
		result.append(resolution);
		result.append(".");
		result.append(fileExtension);

		return result.toString();
	}

	public static String createMapUrlAggregatedRegion(DateTime date, int version, String id, String fileExtension) {
		StringBuilder result = new StringBuilder();
		result.append(GlobalVariables.univieBaseUrl);
		result.append(date.toString(GlobalVariables.formatterDate));
		result.append(GlobalVariables.urlSeperator);
		result.append(version);
		result.append(GlobalVariables.urlSeperator);
		result.append(id);
		result.append(".");
		result.append(fileExtension);

		return result.toString();
	}

	public static String triggerMapProduction(String caaml) throws AlbinaException {
		HttpURLConnection connection = null;

		try {
			// Create connection
			URL url = new URL(GlobalVariables.univieMapProductionUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length", Integer.toString(caaml.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(caaml);
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();

			if (connection.getResponseCode() != 200 && connection.getResponseCode() != 200)
				throw new AlbinaException("Error while triggering map production!");

			return response.toString();
		} catch (Exception e) {
			throw new AlbinaException(e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public static String createEmailHtml(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		try {
			// Create data model
			Map<String, Object> root = new HashMap<>();

			Map<String, Object> image = new HashMap<>();
			image.put("ci", "http://212.47.231.185:8080/images/ci.png");
			image.put("logo", "http://212.47.231.185:8080/images/Avalanche.png");
			Map<String, Object> socialMediaImages = new HashMap<>();
			// socialMediaImages.put("facebook",
			// "http://212.47.231.185:8080/images/facebook.png");
			// socialMediaImages.put("twitter",
			// "http://212.47.231.185:8080/images/twitter.png");
			// socialMediaImages.put("instagram",
			// "http://212.47.231.185:8080/images/instagram.png");
			// socialMediaImages.put("youtube",
			// "http://212.47.231.185:8080/images/youtube.png");
			// socialMediaImages.put("whatsapp",
			// "http://212.47.231.185:8080/images/whatsapp.png");
			socialMediaImages.put("facebook", "cid:facebook");
			socialMediaImages.put("twitter", "cid:twitter");
			socialMediaImages.put("instagram", "cid:instagram");
			socialMediaImages.put("youtube", "cid:youtube");
			socialMediaImages.put("whatsapp", "cid:whatsapp");
			image.put("socialmedia", socialMediaImages);
			Map<String, Object> mapImage = new HashMap<>();
			// mapImage.put("overview",
			// "http://212.47.231.185:8080/images/map_overview.png");
			mapImage.put("overview", "cid:map_overview");
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
				break;
			case de:
				text.put("headline", "Lawinen Report");
				text.put("website", "Webseite");
				text.put("follow", "Folge uns");
				text.put("unsubscribe", "Abmelden");
				// TODO
				text.put("date", "Freitag, 16.02.2018");
				break;
			case it:
				text.put("headline", "Valanghe Report");
				text.put("website", "Website");
				text.put("follow", "TODO");
				text.put("unsubscribe", "TODO");
				// TODO
				text.put("date", "Venerdi, 16.02.2018");
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
					bulletin.put("snowpackStructureHighlights", "");
				// TODO use correct map
				bulletin.put("map", "http://212.47.231.185:8080/images/map_detail.png");
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
		String htmlText = AlbinaUtil.createEmailHtml(bulletins, lang);
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

		// TODO add maps

		// TODO add PDF

		message.setContent(multipart);

		transport.connect();
		transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
		transport.close();
	}
}
