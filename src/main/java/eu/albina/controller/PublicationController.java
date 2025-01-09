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
package eu.albina.controller;

import java.io.IOException;

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
import eu.albina.util.JsonUtil;
import eu.albina.util.PdfUtil;
import eu.albina.util.SimpleHtmlUtil;
import eu.albina.util.TextToSpeech;

/**
 * Controller for avalanche reports.
 *
 * @author Norbert Lanzanasto
 */
public class PublicationController {

	private static final Logger logger = LoggerFactory.getLogger(PublicationController.class);

	private static PublicationController instance = null;

	/**
	 * Private constructor.
	 */
	private PublicationController() {
	}

	/**
	 * Returns the {@code PublicationController} object associated with the current
	 * Java application.
	 *
	 * @return the {@code PublicationController} object associated with the current
	 * Java application.
	 */
	public static PublicationController getInstance() {
		if (instance == null) {
			instance = new PublicationController();
		}
		return instance;
	}

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

				if (region.isCreateAudioFiles()) {
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
			logger.info("JSON production for {} started", avalancheReport.getRegion().getId());
			JsonUtil.createJsonFile(avalancheReport);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setJsonCreated);
			logger.info("JSON production for {} finished", avalancheReport.getRegion().getId());
		} catch (IOException e) {
			logger.error("Error producing JSON for " + avalancheReport.getRegion().getId(), e);
		}
	}

	/**
	 * Trigger the creation of the CAAMLv5 (XML) files.
	 */
	public void createCaamlV5(AvalancheReport avalancheReport) {
		try {
			logger.info("CAAMLv5 production for {} started", avalancheReport.getRegion().getId());
			Caaml.createCaamlFiles(avalancheReport, CaamlVersion.V5);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setCaamlV5Created);
			logger.info("CAAMLv5 production for {} finished", avalancheReport.getRegion().getId());
		} catch (IOException e) {
			logger.error("Error producing CAAMLv5 for " + avalancheReport.getRegion().getId(), e);
		}
	}

	/**
	 * Trigger the creation of the CAAMLv6 (XML) files.
	 */
	public void createCaamlV6(AvalancheReport avalancheReport) {
		try {
			logger.info("CAAMLv6 production for {} started", avalancheReport.getRegion().getId());
			Caaml.createCaamlFiles(avalancheReport, CaamlVersion.V6);
			Caaml.createCaamlFiles(avalancheReport, CaamlVersion.V6_JSON);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setCaamlV6Created);
			logger.info("CAAMLv6 production for {} finished", avalancheReport.getRegion().getId());
		} catch (IOException e) {
			logger.error("Error producing CAAMLv6 for " + avalancheReport.getRegion().getId(), e);
		}
	}

	/**
	 * Trigger the creation of the maps.
	 */
	public void createMaps(AvalancheReport avalancheReport) {
		final String regionId = avalancheReport.getRegion().getId();
		try {
			logger.info("Map production for {} started", regionId);
			MapUtil.createMapyrusMaps(avalancheReport);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setMapCreated);
			logger.info("Map production {} finished", regionId);
		} catch (Exception e) {
			logger.error("Error producing maps for " + regionId, e);
		}
	}

	/**
	 * Trigger the creation of the pdfs.
	 */
	public void createPdf(AvalancheReport avalancheReport) {
		try {
			logger.info("PDF production for {} started", avalancheReport.getRegion().getId());
			PdfUtil.createRegionPdfs(avalancheReport);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setPdfCreated);
		} finally {
			logger.info("PDF production {} finished", avalancheReport.getRegion().getId());
		}
	}

	/**
	 * Trigger the creation of the simple html files.
	 */
	public void createSimpleHtml(AvalancheReport avalancheReport) {
		try {
			logger.info("Simple HTML production for {} started", avalancheReport.getRegion().getId());
			SimpleHtmlUtil.getInstance().createRegionSimpleHtml(avalancheReport);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(),
				AvalancheReport::setHtmlCreated);
		} catch (Exception e) {
			logger.error("Error creating simple HTML for " + avalancheReport.getRegion().getId(), e);
		} finally {
			logger.info("Simple HTML production for {} finished", avalancheReport.getRegion().getId());
		}
	}

	public void createAudioFiles(AvalancheReport avalancheReport) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		try {
			logger.info("Synthesize speech for {} started", avalancheReport.getRegion().getId());
			TextToSpeech.createAudioFiles(avalancheReport);
		} catch (Exception e) {
			logger.error("Synthesize speech error for " + avalancheReport.getRegion().getId(), e);
		} finally {
			logger.info("Synthesize speech for {} finished in {}", avalancheReport.getRegion().getId(), stopwatch);
		}
	}

	public void sendToAllChannels(AvalancheReport avalancheReport) {
		if (!avalancheReport.getRegion().isPublishBulletins() || avalancheReport.getBulletins().isEmpty() || !avalancheReport.getRegion().isCreateMaps()) {
			return;
		}
		for (LanguageCode lang : LanguageCode.ENABLED) {
			MultichannelMessage posting = MultichannelMessage.of(avalancheReport, lang);
			try {
				posting.sendToAllChannels();
				AvalancheReportController c = AvalancheReportController.getInstance();
				c.setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setEmailCreated);
				c.setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setTelegramSent);
				c.setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setPushSent);
			} catch (Exception e) {
				logger.error("Error sending " + posting, e);
			}
		}
	}

}
