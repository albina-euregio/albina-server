package eu.albina.caaml;

import eu.albina.map.MapImageFormat;
import eu.albina.map.MapLevel;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.Complexity;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;
import eu.albina.model.enumerations.TextPart;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.LinkUtil;
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

interface Caaml6 {

	static String createCaamlv6(AvalancheReport avalancheReport, LanguageCode language) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = CaamlVersion.V6.setNamespaceAttributes(doc.createElement("bulletins"));

			// create meta data
			List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();
			if (bulletins != null && !bulletins.isEmpty()) {

				// metaData
				Element metaData = doc.createElement("metaData");
				for (Element extFile : createObsCollectionExtFiles(doc, bulletins, language, avalancheReport.getRegion(), avalancheReport.getServerInstance())) {
					metaData.appendChild(extFile);
				}
				rootElement.appendChild(metaData);

				String reportPublicationTime = AlbinaUtil.getPublicationTime(bulletins);

				for (AvalancheBulletin bulletin : bulletins) {
					List<Element> caaml = toCAAMLv6(bulletin, doc, language, avalancheReport.getRegion(), reportPublicationTime, avalancheReport.getServerInstance());
					if (caaml != null)
						for (Element element : caaml) {
							if (element != null)
								rootElement.appendChild(element);
						}
				}
			}

			doc.appendChild(rootElement);

			return Caaml.convertDocToString(doc);
		} catch (ParserConfigurationException | TransformerException e1) {
			LoggerFactory.getLogger(Caaml6.class).error("Error producing CAAMLv6", e1);
			return null;
		}
	}

	static List<Element> createObsCollectionExtFiles(Document doc, List<AvalancheBulletin> bulletins, LanguageCode lang, Region region, ServerInstance serverInstance) {
		List<Element> extFiles = new ArrayList<Element>();

		boolean hasDaytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
		String baseUri = LinkUtil.getMapsUrl(lang, region, serverInstance) + "/" + validityDateString + "/" + publicationTime + "/";

		extFiles.add(createExtFile(doc, "link", lang.getBundleString("ext-file.website-link.description"),
				lang.getBundleString("website.url") + "/bulletin/" + validityDateString));
		extFiles.add(createExtFile(doc, "simple_link", lang.getBundleString("ext-file.simple-link.description"),
				LinkUtil.getSimpleHtmlUrl(lang, region, serverInstance) + "/" + validityDateString + "/" + lang.toString()
						+ ".html"));
		extFiles.add(createExtFile(doc, "fd_albina_map.jpg",
				LinkUtil.getExtFileMapDescription(lang, "fd", ""), baseUri +
				MapUtil.filename(region, MapLevel.standard, DaytimeDependency.fd, false, MapImageFormat.jpg)));
		extFiles.add(createExtFile(doc, "pdf", LinkUtil.getExtFilePdfDescription(lang, ""),
				baseUri + validityDateString + "_" + lang.toString() + ".pdf"));

		if (!hasDaytimeDependency) {
			extFiles.add(createExtFile(doc, "fd_overlay.png",
					LinkUtil.getExtFileOverlayDescription(lang, "fd"), baseUri +
					MapUtil.filename(region, MapLevel.overlay, DaytimeDependency.fd, false, MapImageFormat.png)));
		} else {
			extFiles.add(createExtFile(doc, "am_albina_map.jpg",
					LinkUtil.getExtFileMapDescription(lang, "am", ""), baseUri +
					MapUtil.filename(region, MapLevel.standard, DaytimeDependency.am, false, MapImageFormat.jpg)));
			extFiles.add(createExtFile(doc, "pm_albina_map.jpg",
					LinkUtil.getExtFileMapDescription(lang, "pm", ""), baseUri +
					MapUtil.filename(region, MapLevel.standard, DaytimeDependency.pm, false, MapImageFormat.jpg)));
			extFiles.add(createExtFile(doc, "am_overlay.png",
					LinkUtil.getExtFileOverlayDescription(lang, "am"), baseUri +
					MapUtil.filename(region, MapLevel.overlay, DaytimeDependency.am, false, MapImageFormat.png)));
			extFiles.add(createExtFile(doc, "pm_overlay.png",
					LinkUtil.getExtFileOverlayDescription(lang, "pm"), baseUri +
					MapUtil.filename(region, MapLevel.overlay, DaytimeDependency.pm, false, MapImageFormat.png)));
		}

		return extFiles;
	}

	static Element createExtFile(Document doc, String id, String descr, String baseUri) {
		Element extFile = doc.createElement("extFile");
		Element typeElement = doc.createElement("type");
		typeElement.appendChild(doc.createTextNode(id));
		extFile.appendChild(typeElement);
		Element description = doc.createElement("description");
		description.appendChild(doc.createTextNode(descr));
		extFile.appendChild(description);
		Element fileReferenceURI = doc.createElement("fileReferenceURI");
		fileReferenceURI.appendChild(doc.createTextNode(baseUri));
		extFile.appendChild(fileReferenceURI);
		return extFile;
	}

	static Document createXmlError(String key, String value) throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(key);
		rootElement.appendChild(doc.createTextNode(value));
		return doc;
	}

	static Element createCAAMLv6Bulletin(AvalancheBulletin bulletin, Document doc, LanguageCode languageCode, Region region, boolean isAfternoon, String reportPublicationTime, ServerInstance serverInstance) {

		AvalancheBulletinDaytimeDescription bulletinDaytimeDescription;

		if (isAfternoon)
			bulletinDaytimeDescription = bulletin.getAfternoon();
		else
			bulletinDaytimeDescription = bulletin.getForenoon();

		Element rootElement = doc.createElement("bulletin");

		// attributes
		if (bulletin.getId() != null) {
			if (isAfternoon)
				rootElement.setAttribute("id", bulletin.getId() + "_PM");
			else
				rootElement.setAttribute("id", bulletin.getId());
		}
		if (languageCode == null)
			languageCode = LanguageCode.en;
		rootElement.setAttribute("lang", languageCode.toString());

		// metaData
		Element metaData = doc.createElement("metaData");
		rootElement.appendChild(metaData);
		if (!isAfternoon) {
			String fileReferenceURI = LinkUtil.getMapsUrl(languageCode, region, serverInstance) + "/" + bulletin.getValidityDateString() + "/"
					+ reportPublicationTime + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.am, false, MapImageFormat.jpg);
			metaData.appendChild(createExtFile(doc, "dangerRatingMap",
					languageCode.getBundleString("ext-file.thumbnail.description"), fileReferenceURI));
		} else {
			String fileReferenceURI = LinkUtil.getMapsUrl(languageCode, region, serverInstance) + "/" + bulletin.getValidityDateString() + "/"
					+ reportPublicationTime + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.pm, false, MapImageFormat.jpg);
			metaData.appendChild(createExtFile(doc, "dangerRatingMap",
					languageCode.getBundleString("ext-file.thumbnail.description"), fileReferenceURI));
		}
		String linkReferenceURI = languageCode.getBundleString("website.url") + "/bulletin/" + bulletin.getValidityDateString()
				+ "?region=" + bulletin.getId();
		metaData.appendChild(createExtFile(doc, "website",
				languageCode.getBundleString("ext-file.region-link.description"), linkReferenceURI));

		// publication time
		Element pubTime = doc.createElement("publicationTime");
		pubTime.appendChild(doc.createTextNode(
				DateTimeFormatter.ISO_INSTANT.format(bulletin.getPublicationDate())));
		rootElement.appendChild(pubTime);

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
			Element beginPosition = doc.createElement("startTime");
			beginPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(start)));
			validTime.appendChild(beginPosition);
			Element endPosition = doc.createElement("endTime");
			endPosition.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(end)));
			validTime.appendChild(endPosition);
			rootElement.appendChild(validTime);
		}

		// source
		Element source = doc.createElement("source");
		Element operation = doc.createElement("operation");
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(languageCode.getBundleString("website.name")));
		operation.appendChild(name);
		Element website = doc.createElement("website");
		website.appendChild(doc.createTextNode(languageCode.getBundleString("website.url")));
		operation.appendChild(website);
		source.appendChild(operation);
		rootElement.appendChild(source);

		// region
		for (String regionId : bulletin.getPublishedRegions()) {
			if (region.affects(regionId)) {
				Element regionElement = doc.createElement("region");
				// Element nameElement = doc.createElement("name");
				// nameElement.appendChild(doc.createTextNode(RegionController.getInstance().getRegionName(languageCode, regionId)));
				// regionElement.appendChild(nameElement);
				regionElement.setAttribute("id", regionId);
				rootElement.appendChild(regionElement);
			}
		}

		// complexity
		if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getComplexity() != null) {
			// Element complexity = doc.createElement("complexity");
			// complexity.appendChild(doc.createTextNode(Complexity.getCAAMLString(bulletin.getComplexity())));
			// rootElement.appendChild(complexity);
			if (bulletinDaytimeDescription.getComplexity() == Complexity.complex) {
				rootElement.setAttribute("complex", "true");
			}
		}

		// danger ratings
		if (bulletinDaytimeDescription.isHasElevationDependency()) {
			Element dangerRatingAbove = doc.createElement("dangerRating");
			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingAbove() != null) {
				Element mainValueAbove = doc.createElement("mainValue");
				mainValueAbove.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv6String(bulletinDaytimeDescription.getDangerRatingAbove())));
				dangerRatingAbove.appendChild(mainValueAbove);
			}
			Element elevationAbove = doc.createElement("elevation");
			elevationAbove.setAttribute("uom", "m");
			Element lowerBound = doc.createElement("lowerBound");
			if (bulletinDaytimeDescription.getTreeline())
				lowerBound.appendChild(doc.createTextNode("treeline"));
			else
				lowerBound.appendChild(doc.createTextNode(String.valueOf(bulletinDaytimeDescription.getElevation())));
			elevationAbove.appendChild(lowerBound);
			dangerRatingAbove.appendChild(elevationAbove);
			rootElement.appendChild(dangerRatingAbove);

			Element dangerRatingBelow = doc.createElement("dangerRating");
			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingBelow() != null) {
				Element mainValueBelow = doc.createElement("mainValue");
				mainValueBelow.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv6String(bulletinDaytimeDescription.getDangerRatingBelow())));
				dangerRatingBelow.appendChild(mainValueBelow);
			}
			Element elevationBelow = doc.createElement("elevation");
			elevationBelow.setAttribute("uom", "m");
			Element upperBound = doc.createElement("upperBound");
			if (bulletinDaytimeDescription.getTreeline())
				upperBound.appendChild(doc.createTextNode("treeline"));
			else
				upperBound.appendChild(doc.createTextNode(String.valueOf(bulletinDaytimeDescription.getElevation())));
			elevationBelow.appendChild(upperBound);
			dangerRatingBelow.appendChild(elevationBelow);
			rootElement.appendChild(dangerRatingBelow);
		} else {
			// NOTE if no elevation dependency is set, the elevation description is above
			Element dangerRating = doc.createElement("dangerRating");
			if (bulletinDaytimeDescription != null && bulletinDaytimeDescription.getDangerRatingAbove() != null) {
				Element mainValue = doc.createElement("mainValue");
				mainValue.appendChild(doc.createTextNode(
						DangerRating.getCAAMLv6String(bulletinDaytimeDescription.getDangerRatingAbove())));
				dangerRating.appendChild(mainValue);
			}
			rootElement.appendChild(dangerRating);
		}

		// danger patterns
		if (bulletin.getDangerPattern1() != null) {
			Element dangerPatternOne = doc.createElement("dangerPattern");
			Element dangerPatternOneType = doc.createElement("type");
			dangerPatternOneType.appendChild(doc.createTextNode(DangerPattern.getCAAMLv6String(bulletin.getDangerPattern1())));
			dangerPatternOne.appendChild(dangerPatternOneType);
			rootElement.appendChild(dangerPatternOne);
		}
		if (bulletin.getDangerPattern2() != null) {
			Element dangerPatternTwo = doc.createElement("dangerPattern");
			Element dangerPatternTwoType = doc.createElement("type");
			dangerPatternTwoType.appendChild(doc.createTextNode(DangerPattern.getCAAMLv6String(bulletin.getDangerPattern2())));
			dangerPatternTwo.appendChild(dangerPatternTwoType);
			rootElement.appendChild(dangerPatternTwo);
		}

		// avalanche problems
		for (AvalancheProblem problem : bulletinDaytimeDescription != null
				? bulletinDaytimeDescription.getAvalancheProblems()
				: Collections.<AvalancheProblem>emptyList()) {
			if (problem != null && problem.getAvalancheProblem() != null) {
				Element avalancheProblem = getAvalancheProblemCaamlv6(doc, problem, languageCode);
				rootElement.appendChild(avalancheProblem);
			}
		}

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
				Element startTime = doc.createElement("startTime");
				startTime.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(start)));
				validTime.appendChild(startTime);
				Element endTime = doc.createElement("endTime");
				endTime.appendChild(doc.createTextNode(DateTimeFormatter.ISO_INSTANT.format(end)));
				validTime.appendChild(endTime);
				tendencyElement.appendChild(validTime);
			}
			rootElement.appendChild(tendencyElement);
		}

		for (TextPart part : TextPart.values()) {
			final String text = bulletin.getTextPartIn(part, languageCode);
			if (text != null && !text.isEmpty()) {
				Element textPart = doc.createElement(part.toCaamlv6String());
				textPart.appendChild(doc.createTextNode(text));
				rootElement.appendChild(textPart);
			}
		}

		return rootElement;
	}

	static Element getAvalancheProblemCaamlv6(Document doc, AvalancheProblem avalancheProblem,
											  LanguageCode languageCode) {
		Element avProblem = doc.createElement("avalancheProblem");
		Element type = doc.createElement("type");
		type.appendChild(doc.createTextNode(avalancheProblem.getAvalancheProblem().toCaamlv6String()));
		avProblem.appendChild(type);
		Element dangerRating = doc.createElement("dangerRating");
		if (avalancheProblem.getAspects() != null) {
			for (Aspect aspect : avalancheProblem.getAspects()) {
				Element validAspect = doc.createElement("aspect");
				validAspect.appendChild(doc.createTextNode(aspect.toUpperCaseString()));
				dangerRating.appendChild(validAspect);
			}
		}

		if (avalancheProblem.getTreelineHigh() || avalancheProblem.getElevationHigh() > 0) {
			Element validElevation = doc.createElement("elevation");
			validElevation.setAttribute("uom", "m");
			if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				// elevation high and low set
				Element lowerBound = doc.createElement("lowerBound");
				if (avalancheProblem.getTreelineLow())
					lowerBound.appendChild(doc.createTextNode("treeline"));
				else
					lowerBound.appendChild(doc.createTextNode(String.valueOf(avalancheProblem.getElevationLow())));
				Element upperBound = doc.createElement("upperBound");
				if (avalancheProblem.getTreelineHigh())
					upperBound.appendChild(doc.createTextNode("treeline"));
				else
					upperBound.appendChild(doc.createTextNode(String.valueOf(avalancheProblem.getElevationHigh())));
				validElevation.appendChild(lowerBound);
				validElevation.appendChild(upperBound);
			} else {
				// elevation high set
				Element upperBound = doc.createElement("upperBound");
				if (avalancheProblem.getTreelineHigh())
					upperBound.appendChild(doc.createTextNode("treeline"));
				else
					upperBound.appendChild(doc.createTextNode(String.valueOf(avalancheProblem.getElevationHigh())));
				validElevation.appendChild(upperBound);
			}
			dangerRating.appendChild(validElevation);
		} else if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
			// elevation low set
			Element validElevation = doc.createElement("elevation");
			validElevation.setAttribute("uom", "m");
			if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				Element lowerBound = doc.createElement("lowerBound");
				if (avalancheProblem.getTreelineLow())
					lowerBound.appendChild(doc.createTextNode("treeline"));
				else
					lowerBound.appendChild(doc.createTextNode(String.valueOf(avalancheProblem.getElevationLow())));
				validElevation.appendChild(lowerBound);
			}
			dangerRating.appendChild(validElevation);
		} else {
			// no elevation set
		}

		// terrain feature
		if (avalancheProblem.getTerrainFeature() != null && !avalancheProblem.getTerrainFeature().isEmpty()
				&& avalancheProblem.getTerrainFeature(languageCode) != null) {
			Element textPart = doc.createElement("terrainFeature");
			textPart.appendChild(doc.createTextNode(avalancheProblem.getTerrainFeature(languageCode)));
			dangerRating.appendChild(textPart);
		}

		// obsolete matrix
		if (avalancheProblem.getMatrixInformation() != null) {
			DangerRating rating = null;
			if (avalancheProblem.getMatrixInformation().getArtificialDangerRating() != null) {
				if (avalancheProblem.getMatrixInformation().getNaturalDangerRating() != null) {
					if (avalancheProblem.getMatrixInformation().getArtificialDangerRating()
							.compareTo(avalancheProblem.getMatrixInformation().getNaturalDangerRating()) < 0)
						rating = avalancheProblem.getMatrixInformation().getNaturalDangerRating();
					else
						rating = avalancheProblem.getMatrixInformation().getArtificialDangerRating();
				} else {
					rating = avalancheProblem.getMatrixInformation().getArtificialDangerRating();
				}
			} else if (avalancheProblem.getMatrixInformation().getNaturalDangerRating() != null) {
				rating = avalancheProblem.getMatrixInformation().getNaturalDangerRating();
			}
			if (rating != null) {
				Element mainValue = doc.createElement("mainValue");
				mainValue.appendChild(doc.createTextNode(DangerRating.getCAAMLv6String(rating)));
				dangerRating.appendChild(mainValue);
			}

			avalancheProblem.getMatrixInformation().toCAAMLv6(doc, dangerRating);
		}

		// new matrix
		if (avalancheProblem.getEawsMatrixInformation() != null) {
			avalancheProblem.getEawsMatrixInformation().toCAAMLv6(doc, dangerRating);
		}

		avProblem.appendChild(dangerRating);

		return avProblem;
	}

	static List<Element> toCAAMLv6(AvalancheBulletin bulletin, Document doc, LanguageCode languageCode, Region region, String reportPublicationTime, ServerInstance serverInstance) {
		if (bulletin.getPublishedRegions() != null && !bulletin.getPublishedRegions().isEmpty()) {
			List<Element> result = new ArrayList<Element>();
			result.add(createCAAMLv6Bulletin(bulletin, doc, languageCode, region, false, reportPublicationTime, serverInstance));

			if (bulletin.isHasDaytimeDependency())
				result.add(createCAAMLv6Bulletin(bulletin, doc, languageCode, region, true, reportPublicationTime, serverInstance));

			return result;
		} else
			return null;
	}
}
