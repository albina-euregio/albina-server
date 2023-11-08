package eu.albina.caaml;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;
import eu.albina.model.enumerations.TextPart;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.XmlUtil;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

interface Caaml5 {
	static String createCaamlv5(AvalancheReport avalancheReport, LanguageCode language) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = CaamlVersion.V5.setNamespaceAttributes(doc.createElement("ObsCollection"));

			// create meta data
			List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();
			if (bulletins != null && !bulletins.isEmpty()) {
				ZonedDateTime publicationDate = AlbinaUtil.getPublicationDate(bulletins);

				// metaData
				Element metaDataProperty = createMetaDataProperty(doc, publicationDate, language);
				rootElement.appendChild(metaDataProperty);

				// observations
				Element observations = doc.createElement("observations");

				for (AvalancheBulletin bulletin : bulletins) {
					List<Element> caaml = toCAAMLv5(bulletin, doc, language, avalancheReport.getRegion());
					if (caaml != null)
						for (Element element : caaml) {
							if (element != null)
								observations.appendChild(element);
						}
				}
				rootElement.appendChild(observations);

				// attributes
				if (language == null)
					language = LanguageCode.en;
				rootElement.setAttribute("xml:lang", language.toString());
			}

			doc.appendChild(rootElement);

			return XmlUtil.convertDocToString(doc);
		} catch (ParserConfigurationException | TransformerException e1) {
			LoggerFactory.getLogger(Caaml5.class).error("Error producing CAAML", e1);
			return null;
		}
	}

	static Element createCAAMLv5Bulletin(AvalancheBulletin bulletin, Document doc, LanguageCode languageCode, Region region, boolean isAfternoon) {
		AvalancheBulletinDaytimeDescription bulletinDaytimeDescription;

		if (isAfternoon)
			bulletinDaytimeDescription = bulletin.getAfternoon();
		else
			bulletinDaytimeDescription = bulletin.getForenoon();

		Element rootElement = doc.createElement("Bulletin");

		// attributes
		if (bulletin.getId() != null) {
			if (isAfternoon)
				rootElement.setAttribute("gml:id", bulletin.getId() + "_PM");
			else
				rootElement.setAttribute("gml:id", bulletin.getId());
		}
		if (languageCode == null)
			languageCode = LanguageCode.en;
		rootElement.setAttribute("xml:lang", languageCode.toString());

		// metaData
		Element metaDataProperty = createMetaDataProperty(doc, bulletin.getPublicationDate(), languageCode);
		rootElement.appendChild(metaDataProperty);

		// validTime
		if (bulletin.getValidFrom() != null && bulletin.getValidUntil() != null) {

			ZonedDateTime start = bulletin.getValidFrom();
			ZonedDateTime end = bulletin.getValidUntil();

			if (bulletin.isHasDaytimeDependency()) {
				if (isAfternoon)
					start = start.plusHours(12);
				else
					end = end.minusHours(12);
			}

			Element validTime = doc.createElement("validTime");
			Element timePeriod = doc.createElement("TimePeriod");
			Element beginPosition = doc.createElement("beginPosition");
			beginPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(start)));
			timePeriod.appendChild(beginPosition);
			Element endPosition = doc.createElement("endPosition");
			endPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(end)));
			timePeriod.appendChild(endPosition);
			validTime.appendChild(timePeriod);
			rootElement.appendChild(validTime);
		}

		// srcRef
		Element srcRef = doc.createElement("srcRef");
		Element operation = doc.createElement("Operation");
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode("Avalanche.report"));
		operation.appendChild(name);
		srcRef.appendChild(operation);
		rootElement.appendChild(srcRef);

		// locRef
		for (String publishedRegion : bulletin.getPublishedRegions()) {
			if (region == null || region.affects(publishedRegion)) {
				Element locRef = doc.createElement("locRef");
				locRef.setAttribute("xlink:href", publishedRegion);
				rootElement.appendChild(locRef);
			}
		}

		// bulletinResultsOf
		Element bulletinResultsOf = doc.createElement("bulletinResultsOf");
		Element bulletinMeasurements = doc.createElement("BulletinMeasurements");

		// danger ratings
		Element dangerRatings = doc.createElement("dangerRatings");
		if (bulletinDaytimeDescription.isHasElevationDependency()) {
			Element dangerRatingAbove = doc.createElement("DangerRating");
			Element validElevationAbove = doc.createElement("validElevation");
			validElevationAbove.setAttribute("xlink:href", createValidElevationAttribute(
					bulletinDaytimeDescription.getElevation(), true, bulletinDaytimeDescription.getTreeline()));
			dangerRatingAbove.appendChild(validElevationAbove);

			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingAbove() != null) {
				Element mainValueAbove = doc.createElement("mainValue");
				mainValueAbove.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv5String(bulletinDaytimeDescription.getDangerRatingAbove())));
				dangerRatingAbove.appendChild(mainValueAbove);
			}
			dangerRatings.appendChild(dangerRatingAbove);
			Element dangerRatingBelow = doc.createElement("DangerRating");
			Element validElevationBelow = doc.createElement("validElevation");
			validElevationBelow.setAttribute("xlink:href", createValidElevationAttribute(
					bulletinDaytimeDescription.getElevation(), false, bulletinDaytimeDescription.getTreeline()));
			dangerRatingBelow.appendChild(validElevationBelow);

			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingBelow() != null) {
				Element mainValueBelow = doc.createElement("mainValue");
				mainValueBelow.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv5String(bulletinDaytimeDescription.getDangerRatingBelow())));
				dangerRatingBelow.appendChild(mainValueBelow);
			}
			dangerRatings.appendChild(dangerRatingBelow);
		} else {
			// NOTE if no elevation dependency is set, the elevation description is above
			Element dangerRating = doc.createElement("DangerRating");

			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingAbove() != null) {
				Element mainValue = doc.createElement("mainValue");
				mainValue.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv5String(bulletinDaytimeDescription.getDangerRatingAbove())));
				dangerRating.appendChild(mainValue);
			}
			dangerRatings.appendChild(dangerRating);
		}
		bulletinMeasurements.appendChild(dangerRatings);

		// danger patterns
		if (bulletin.getDangerPattern1() != null || bulletin.getDangerPattern2() != null) {
			Element dangerPatterns = doc.createElement("dangerPatterns");
			if (bulletin.getDangerPattern1() != null) {
				Element dangerPatternOne = doc.createElement("DangerPattern");
				Element dangerPatternOneType = doc.createElement("type");
				dangerPatternOneType.appendChild(doc.createTextNode(DangerPattern.getCAAMLv5String(bulletin.getDangerPattern1())));
				dangerPatternOne.appendChild(dangerPatternOneType);
				dangerPatterns.appendChild(dangerPatternOne);
			}
			if (bulletin.getDangerPattern2() != null) {
				Element dangerPatternTwo = doc.createElement("DangerPattern");
				Element dangerPatternTwoType = doc.createElement("type");
				dangerPatternTwoType.appendChild(doc.createTextNode(DangerPattern.getCAAMLv5String(bulletin.getDangerPattern2())));
				dangerPatternTwo.appendChild(dangerPatternTwoType);
				dangerPatterns.appendChild(dangerPatternTwo);
			}
			bulletinMeasurements.appendChild(dangerPatterns);
		}

		// avalanche problems
		Element avProblems = doc.createElement("avProblems");
		for (AvalancheProblem problem : bulletinDaytimeDescription != null
				? bulletinDaytimeDescription.getAvalancheProblems()
				: Collections.<AvalancheProblem>emptyList()) {
			if (problem != null && problem.getAvalancheProblem() != null) {
				Element avProblem = getAvProblemCaamlv5(doc, problem);
				avProblems.appendChild(avProblem);
			}
		}
		bulletinMeasurements.appendChild(avProblems);

		// tendency
		if (bulletin.getTendency() != null) {
			Element tendencyElement = doc.createElement("tendency");
			Element type = doc.createElement("type");
			type.appendChild(doc.createTextNode(Tendency.getCaamlString(bulletin.getTendency())));
			tendencyElement.appendChild(type);

			if (bulletin.getValidFrom() != null && bulletin.getValidUntil() != null) {
				ZonedDateTime start = bulletin.getValidFrom();
				ZonedDateTime end = bulletin.getValidUntil();

				start = start.plusDays(1);
				end = end.plusDays(1);

				Element validTime = doc.createElement("validTime");
				Element timePeriod = doc.createElement("TimePeriod");
				Element beginPosition = doc.createElement("beginPosition");
				beginPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(start)));
				timePeriod.appendChild(beginPosition);
				Element endPosition = doc.createElement("endPosition");
				endPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(end)));
				timePeriod.appendChild(endPosition);
				validTime.appendChild(timePeriod);
				tendencyElement.appendChild(validTime);
			}
			bulletinMeasurements.appendChild(tendencyElement);
		}

		for (TextPart part : TextPart.values()) {
			final String text = bulletin.getTextPartIn(part, languageCode);
			if (text != null && !text.isEmpty()) {
				Element textPart = doc.createElement(toCaamlv5String(part));
				textPart.appendChild(doc.createTextNode(text));
				bulletinMeasurements.appendChild(textPart);
			}
		}

		bulletinResultsOf.appendChild(bulletinMeasurements);
		rootElement.appendChild(bulletinResultsOf);

		return rootElement;
	}

	static List<Element> toCAAMLv5(AvalancheBulletin bulletin, Document doc, LanguageCode languageCode, Region region) {
		if (bulletin.getPublishedRegions() != null && !bulletin.getPublishedRegions().isEmpty()) {
			List<Element> result = new ArrayList<Element>();
			result.add(createCAAMLv5Bulletin(bulletin, doc, languageCode, region, false));

			if (bulletin.isHasDaytimeDependency())
				result.add(createCAAMLv5Bulletin(bulletin, doc, languageCode, region, true));

			return result;
		} else
			return null;
	}

	static Element getAvProblemCaamlv5(Document doc, AvalancheProblem avalancheProblem) {
		Element avProblem = doc.createElement("AvProblem");
		Element type = doc.createElement("type");
		type.appendChild(doc.createTextNode(toCaamlv5String(avalancheProblem.getAvalancheProblem())));
		avProblem.appendChild(type);
		if (avalancheProblem.getAspects() != null) {
			for (Aspect aspect : avalancheProblem.getAspects()) {
				Element validAspect = doc.createElement("validAspect");
				validAspect.setAttribute("xlink:href", aspect.toCaamlString());
				avProblem.appendChild(validAspect);
			}
		}

		if (avalancheProblem.getTreelineHigh() || avalancheProblem.getElevationHigh() > 0) {
			if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				// elevation high and low set
				Element validElevation = doc.createElement("validElevation");
				Element elevationRange = doc.createElement("elevationRange");
				elevationRange.setAttribute("uom", "m");
				Element beginPosition = doc.createElement("beginPosition");
				if (avalancheProblem.getTreelineLow())
					beginPosition.appendChild(doc.createTextNode("Treeline"));
				else
					beginPosition.appendChild(doc.createTextNode(String.valueOf(avalancheProblem.getElevationLow())));
				Element endPosition = doc.createElement("endPosition");
				if (avalancheProblem.getTreelineHigh())
					endPosition.appendChild(doc.createTextNode("Treeline"));
				else
					endPosition.appendChild(doc.createTextNode(String.valueOf(avalancheProblem.getElevationHigh())));
				elevationRange.appendChild(beginPosition);
				elevationRange.appendChild(endPosition);
				validElevation.appendChild(elevationRange);
				avProblem.appendChild(validElevation);
			} else {
				// elevation high set
				Element validElevation = doc.createElement("validElevation");
				String elevationString;
				if (avalancheProblem.getTreelineHigh())
					elevationString = createValidElevationAttribute(0, false, true);
				else
					elevationString = createValidElevationAttribute(avalancheProblem.getElevationHigh(),
							false, false);
				validElevation.setAttribute("xlink:href", elevationString);
				avProblem.appendChild(validElevation);
			}
		} else if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
			// elevation low set
			Element validElevation = doc.createElement("validElevation");
			String elevationString;
			if (avalancheProblem.getTreelineLow())
				elevationString = createValidElevationAttribute(0, true, true);
			else
				elevationString = createValidElevationAttribute(avalancheProblem.getElevationLow(), true,
						false);
			validElevation.setAttribute("xlink:href", elevationString);
			avProblem.appendChild(validElevation);
		} else {
			// no elevation set
		}

		return avProblem;
	}

	static String toCaamlv5String(TextPart textPart) {
		switch (textPart) {
		case avActivityHighlights:
			return "avActivityHighlights";
		case avActivityComment:
			return "avActivityComment";
		case synopsisHighlights:
			return "wxSynopsisHighlights";
		case synopsisComment:
			return "wxSynopsisComment";
		case snowpackStructureHighlights:
			return "snowpackStructureHighlights";
		case snowpackStructureComment:
			return "snowpackStructureComment";
		case travelAdvisoryHighlights:
			return "travelAdvisoryHighlights";
		case travelAdvisoryComment:
			return "travelAdvisoryComment";
		case tendencyComment:
			return "tendencyComment";
		case highlights:
			return "highlights";

		default:
			return null;
		}
	}

	static String toCaamlv5String(eu.albina.model.enumerations.AvalancheProblem avalancheProblem) {
		switch (avalancheProblem) {
		case new_snow:
			return "new snow";
		case wind_slab:
			return "drifting snow";
		case persistent_weak_layers:
			return "old snow";
		case wet_snow:
			return "wet snow";
		case gliding_snow:
			return "gliding snow";
		case favourable_situation:
			return "favourable situation";
		case cornices:
			return "cornices";
		case no_distinct_problem:
			return "no distinct problem";

		default:
			return null;
		}
	}

	static String createValidElevationAttribute(int elevation, boolean above, boolean treeline) {
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

	static Element createMetaDataProperty(Document doc, ZonedDateTime dateTime, LanguageCode language) {
		Element metaDataProperty = doc.createElement("metaDataProperty");
		Element metaData = doc.createElement("MetaData");
		Element dateTimeReport = doc.createElement("dateTimeReport");
		if (dateTime != null) {
			dateTimeReport.appendChild(doc
					.createTextNode(DateTimeFormatter.ISO_INSTANT.format(dateTime)));
			metaData.appendChild(dateTimeReport);
		}
		Element srcRef = doc.createElement("srcRef");
		Element operation = doc.createElement("Operation");
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(language.getBundleString("website.name")));
		operation.appendChild(name);
		srcRef.appendChild(operation);
		metaData.appendChild(srcRef);

		metaDataProperty.appendChild(metaData);
		return metaDataProperty;
	}
}
