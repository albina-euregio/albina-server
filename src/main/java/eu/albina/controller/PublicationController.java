// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import eu.albina.caaml.Caaml;
import eu.albina.caaml.CaamlVersion;
import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.PdfUtil;
import eu.albina.util.SimpleHtmlUtil;
import eu.albina.util.TextToSpeech;

/**
 * Controller for avalanche reports.
 *
 * @author Norbert Lanzanasto
 */
@Singleton
public class PublicationController {

	private static final Logger logger = LoggerFactory.getLogger(PublicationController.class);

	@Inject
	Caaml caaml;

	@Inject
	AvalancheReportController avalancheReportController;

	public void createRegionResources(Region region, AvalancheReport avalancheReport) {
		// create CAAML
		if (region.isCreateCaamlV5()) {
			createCaamlV5(avalancheReport);
		}
		if (region.isCreateCaamlV6()) {
			createCaamlV6(avalancheReport);
		}

		// create JSON
		if (region.isCreateJson()) {
			createJson(avalancheReport);
		}

		try {
			// create maps
			if (region.isCreateMaps()) {

				createMaps(avalancheReport);

				// create HTML
				if (region.isCreateSimpleHtml()) {
					createSimpleHtml(avalancheReport);
				}

				// create pdf
				if (region.isCreatePdf()) {
					createPdf(avalancheReport);
				}

				if (region.getTTSLanguages() != null && !region.getTTSLanguages().isEmpty()) {
					createAudioFiles(avalancheReport);
				}

			}
		} catch (Exception e1) {
			logger.error("Error during map production", e1);
		}
	}

	/**
	 * Trigger the creation of the JSON file.
	 */
	public void createJson(AvalancheReport avalancheReport) {
		try {
			logger.info("JSON production for {} started", avalancheReport);
			avalancheReport.createJsonFile();
			avalancheReportController.setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setJsonCreated);
			logger.info("JSON production for {} finished", avalancheReport);
		} catch (IOException e) {
			logger.error("Error producing JSON for " + avalancheReport, e);
		}
	}

	/**
	 * Trigger the creation of the CAAMLv5 (XML) files.
	 */
	public void createCaamlV5(AvalancheReport avalancheReport) {
		try {
			logger.info("CAAMLv5 production for {} started", avalancheReport);
			caaml.createCaamlFiles(avalancheReport, CaamlVersion.V5);
			avalancheReportController.setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setCaamlV5Created);
			logger.info("CAAMLv5 production for {} finished", avalancheReport);
		} catch (IOException e) {
			logger.error("Error producing CAAMLv5 for " + avalancheReport, e);
		}
	}

	/**
	 * Trigger the creation of the CAAMLv6 (XML) files.
	 */
	public void createCaamlV6(AvalancheReport avalancheReport) {
		try {
			logger.info("CAAMLv6 production for {} started", avalancheReport);
			caaml.createCaamlFiles(avalancheReport, CaamlVersion.V6);
			caaml.createCaamlFiles(avalancheReport, CaamlVersion.V6_JSON);
			avalancheReportController.setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setCaamlV6Created);
			logger.info("CAAMLv6 production for {} finished", avalancheReport);
		} catch (IOException e) {
			logger.error("Error producing CAAMLv6 for " + avalancheReport, e);
		}
	}

	/**
	 * Trigger the creation of the maps.
	 */
	public void createMaps(AvalancheReport avalancheReport) {
		try {
			logger.info("Map production for {} started", avalancheReport);
			MapUtil.createMapyrusMaps(avalancheReport);
			avalancheReportController.setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setMapCreated);
			logger.info("Map production {} finished", avalancheReport);
		} catch (Exception e) {
			logger.error("Error producing maps for " + avalancheReport, e);
		}
	}

	/**
	 * Trigger the creation of the pdfs.
	 */
	public void createPdf(AvalancheReport avalancheReport) {
		try {
			logger.info("PDF production for {} started", avalancheReport);
			PdfUtil.createRegionPdfs(avalancheReport);
			avalancheReportController.setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setPdfCreated);
		} finally {
			logger.info("PDF production {} finished", avalancheReport);
		}
	}

	/**
	 * Trigger the creation of the simple html files.
	 */
	public void createSimpleHtml(AvalancheReport avalancheReport) {
		try {
			logger.info("Simple HTML production for {} started", avalancheReport);
			SimpleHtmlUtil.createRegionSimpleHtml(avalancheReport);
			avalancheReportController.setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setHtmlCreated);
		} catch (Exception e) {
			logger.error("Error creating simple HTML for " + avalancheReport, e);
		} finally {
			logger.info("Simple HTML production for {} finished", avalancheReport);
		}
	}

	public void createAudioFiles(AvalancheReport avalancheReport) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		try {
			logger.info("Synthesize speech for {} started", avalancheReport);
			TextToSpeech.createAudioFiles(avalancheReport);
		} catch (Exception e) {
			logger.error("Synthesize speech error for " + avalancheReport, e);
		} finally {
			logger.info("Synthesize speech for {} finished in {}", avalancheReport, stopwatch);
		}
	}

	public void sendToAllChannels(AvalancheReport avalancheReport) {
		if (!avalancheReport.getRegion().isPublishBulletins() || avalancheReport.getBulletins().isEmpty() || !avalancheReport.getRegion().isCreateMaps()) {
			return;
		}
		for (LanguageCode lang : avalancheReport.getRegion().getEnabledLanguages()) {
			MultichannelMessage posting = MultichannelMessage.of(avalancheReport, lang);
			try {
				posting.sendToAllChannels();
				AvalancheReportController c = avalancheReportController;
				c.setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setEmailCreated);
				c.setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setTelegramSent);
				c.setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setWhatsAppSent);
				c.setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setPushSent);
			} catch (Exception e) {
				logger.error("Error sending " + posting, e);
			}
		}
	}

}
