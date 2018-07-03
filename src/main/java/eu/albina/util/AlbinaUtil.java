package eu.albina.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.controller.AvalancheReportController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public class AlbinaUtil {

	private static final Logger logger = LoggerFactory.getLogger(AlbinaUtil.class);

	public static final boolean createMaps = false;
	public static final boolean createPdf = true;
	public static final boolean sendEmails = false;
	public static final boolean publishToSocialMedia = false;
	public static final boolean createCAAML = true;

	public static final int regionCountTyrol = 29;
	public static final int regionCountSouthTyrol = 20;
	public static final int regionCountTrentino = 21;

	public static final boolean publishBulletinsTyrol = true;
	public static final boolean publishBulletinsSouthTyrol = true;
	public static final boolean publishBulletinsTrentino = true;

	public static final String dangerRatingColorLow = "#CCFF66";
	public static final String dangerRatingColorModerate = "#FFFF00";
	public static final String dangerRatingColorConsiderable = "#FF9900";
	public static final String dangerRatingColorHigh = "#FF0000";
	public static final String dangerRatingColorVeryHigh = "#800000";
	public static final String dangerRatingColorMissing = "#969696";

	public static final String dp1De = "Bodennahe Schwachschicht vom Frühwinter";
	public static final String dp2De = "Gleitschnee";
	public static final String dp3De = "Regen";
	public static final String dp4De = "Kalt auf warm / warm auf kalt";
	public static final String dp5De = "Schnee nach langer Kälteperiode";
	public static final String dp6De = "Lockerer Schnee und Wind";
	public static final String dp7De = "Schneearm neben schneereich";
	public static final String dp8De = "Eingeschneiter Oberflächenreif";
	public static final String dp9De = "Eingeschneiter Graupel";
	public static final String dp10De = "Frühjahrssituation";

	public static final String dp1It = "La seconda nevicata";
	public static final String dp2It = "Valanga per scivolamento di neve";
	public static final String dp3It = "Pioggia";
	public static final String dp4It = "Freddo su caldo / caldo su freddo";
	public static final String dp5It = "Neve dopo un lungo periodo di freddo";
	public static final String dp6It = "Neve fresca fredda a debole coesione e vento";
	public static final String dp7It = "Zone con poca neve durante inverni ricchi di neve";
	public static final String dp8It = "Brina di superficie sepolta";
	public static final String dp9It = "Neve pallottolare coperta da neve fresca";
	public static final String dp10It = "Situazione primaverile";

	public static final String dp1En = "Deep persistent weak layer";
	public static final String dp2En = "Gliding avalanche";
	public static final String dp3En = "Rain";
	public static final String dp4En = "Cold following warm / warm following cold";
	public static final String dp5En = "Snowfall after a long period of cold";
	public static final String dp6En = "Cold, loose snow and wind";
	public static final String dp7En = "Snow-poor zones in snow-rich surrounding";
	public static final String dp8En = "Surface hoar blanketed with snow";
	public static final String dp9En = "Graupel blanketed with snow";
	public static final String dp10En = "Springtime scenario";

	public static int getRegionCount(String region) {
		switch (region) {
		case "AT-07":
			return regionCountTyrol;
		case "IT-32-BZ":
			return regionCountSouthTyrol;
		case "IT-32-TN":
			return regionCountTrentino;

		default:
			return -1;
		}
	}

	public static String getDangerPatternText(DangerPattern dp, LanguageCode lang) {
		switch (dp) {
		case dp1:
			switch (lang) {
			case en:
				return dp1En;
			case de:
				return dp1De;
			case it:
				return dp1It;
			default:
				return dp1En;
			}
		case dp2:
			switch (lang) {
			case en:
				return dp2En;
			case de:
				return dp2De;
			case it:
				return dp2It;
			default:
				return dp2En;
			}
		case dp3:
			switch (lang) {
			case en:
				return dp3En;
			case de:
				return dp3De;
			case it:
				return dp3It;
			default:
				return dp3En;
			}
		case dp4:
			switch (lang) {
			case en:
				return dp4En;
			case de:
				return dp4De;
			case it:
				return dp4It;
			default:
				return dp4En;
			}
		case dp5:
			switch (lang) {
			case en:
				return dp5En;
			case de:
				return dp5De;
			case it:
				return dp5It;
			default:
				return dp5En;
			}
		case dp6:
			switch (lang) {
			case en:
				return dp6En;
			case de:
				return dp6De;
			case it:
				return dp6It;
			default:
				return dp6En;
			}
		case dp7:
			switch (lang) {
			case en:
				return dp7En;
			case de:
				return dp7De;
			case it:
				return dp7It;
			default:
				return dp7En;
			}
		case dp8:
			switch (lang) {
			case en:
				return dp8En;
			case de:
				return dp8De;
			case it:
				return dp8It;
			default:
				return dp8En;
			}
		case dp9:
			switch (lang) {
			case en:
				return dp9En;
			case de:
				return dp9De;
			case it:
				return dp9It;
			default:
				return dp9En;
			}
		case dp10:
			switch (lang) {
			case en:
				return dp10En;
			case de:
				return dp10De;
			case it:
				return dp10It;
			default:
				return dp10En;
			}
		default:
			return null;
		}
	}

	public static String getDangerRatingColor(DangerRating dangerRating) {
		switch (dangerRating) {
		case low:
			return dangerRatingColorLow;
		case moderate:
			return dangerRatingColorModerate;
		case considerable:
			return dangerRatingColorConsiderable;
		case high:
			return dangerRatingColorHigh;
		case very_high:
			return dangerRatingColorVeryHigh;
		default:
			return dangerRatingColorMissing;
		}
	}

	public static String getWarningLevelId(AvalancheBulletinDaytimeDescription avalancheBulletinDaytimeDescription,
			boolean elevationDependency) {
		if (elevationDependency)
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingBelow()) + "_"
					+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
		else
			return DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove()) + "_"
					+ DangerRating.getString(avalancheBulletinDaytimeDescription.getDangerRatingAbove());
	}

	public static String getTendencyDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
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

	public static String getDangerRatingText(AvalancheBulletin bulletin, LanguageCode lang) {
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

	public static boolean hasBulletinChanged(DateTime startDate, String region) {
		boolean result = false;
		try {
			Map<DateTime, BulletinStatus> status = AvalancheReportController.getInstance().getStatus(startDate,
					startDate, region);
			if (status.size() == 1 && status.get(startDate) != BulletinStatus.published
					&& status.get(startDate) != BulletinStatus.republished)
				result = true;
		} catch (AlbinaException e) {
			logger.error("Change detection of bulletin failed: " + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	public static JSONObject createBulletinStatusUpdateJson(String region, DateTime date, BulletinStatus status) {
		JSONObject json = new JSONObject();

		if (region != null && region != "")
			json.put("region", region);
		if (date != null)
			json.put("date", date.toString(GlobalVariables.formatterDateTime));
		if (status != null)
			json.put("status", status);

		return json;
	}

	public static String encodeFileToBase64Binary(File file) {
		String encodedfile = null;
		try {
			FileInputStream fileInputStreamReader = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			fileInputStreamReader.read(bytes);
			encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
			fileInputStreamReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return encodedfile;
	}

	public static String getDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		StringBuilder result = new StringBuilder();
		DateTime date = getDate(bulletins);
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

	public static int getYear(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		return getDate(bulletins).getYear();
	}

	public static String getFilenameDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		DateTime date = getDate(bulletins);
		if (date != null)
			return " " + date.toString(DateTimeFormat.forPattern("dd-MM-yyyy"));
		else
			return "";
	}

	private static DateTime getDate(List<AvalancheBulletin> bulletins) {
		DateTime date = null;
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			DateTime bulletinDate = avalancheBulletin.getValidUntil();
			if (date == null)
				date = bulletinDate;
			else if (bulletinDate.isAfter(date))
				date = bulletinDate;
		}
		return date;
	}

	public static String getPublicationDate(List<AvalancheBulletin> bulletins, LanguageCode lang) {
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

	public static String getUrl(LanguageCode lang) {
		switch (lang) {
		case de:
			return "WWW.LAWINEN.REPORT";
		case it:
			return "WWW.VALANGHE.REPORT";
		case en:
			return "WWW.AVALANCHE.REPORT";
		default:
			return "WWW.AVALANCHE.REPORT";
		}
	}

	public static boolean hasDaytimeDependency(List<AvalancheBulletin> bulletins) {
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			if (avalancheBulletin.hasDaytimeDependency())
				return true;
		}
		return false;
	}
}
