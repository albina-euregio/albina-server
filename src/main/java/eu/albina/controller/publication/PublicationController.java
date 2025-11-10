/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package eu.albina.controller.publication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.publication.rapidmail.RapidMailController;
import eu.albina.model.ServerInstance;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import eu.albina.caaml.Caaml;
import eu.albina.caaml.CaamlVersion;
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
	private AvalancheReportController avalancheReportController;

	@Inject
	WhatsAppController whatsAppController;

	@Inject
	PushNotificationUtil pushNotificationUtil;

	@Inject
	TelegramController telegramController;

	@Inject
    RapidMailController rapidMailController;

	@Inject
	TextToSpeech textToSpeech;

	@Inject
	private ObjectMapper objectMapper;

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
			avalancheReport.createJsonFile(objectMapper);
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
			textToSpeech.createAudioFiles(avalancheReport);
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
				posting.sendToAllChannels(this);
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

	public void createSymbolicLinks(AvalancheReport avalancheReport) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		ServerInstance serverInstance = avalancheReport.getServerInstance();
		String validityDateString = avalancheReport.getValidityDateString();
		String publicationTimeString = avalancheReport.getPublicationTimeString();
		try {
			createSymbolicLinks(
				Paths.get(serverInstance.getPdfDirectory(), validityDateString, publicationTimeString),
				Paths.get(serverInstance.getPdfDirectory(), validityDateString)
			);
			if (avalancheReport.isLatest()) {
				createSymbolicLinks(
					Paths.get(serverInstance.getPdfDirectory(), validityDateString, publicationTimeString),
					Paths.get(serverInstance.getPdfDirectory(), "latest")
				);
				stripDateFromFilenames(Paths.get(serverInstance.getPdfDirectory(), "latest"), validityDateString);
				createSymbolicLinks(
					Paths.get(serverInstance.getHtmlDirectory(), validityDateString),
					Paths.get(serverInstance.getHtmlDirectory())
				);
			}
		} catch (IOException e) {
			logger.error("Failed to create symbolic links", e);
			throw new UncheckedIOException(e);
		} finally {
			logger.info("Creating symbolic links done after {}", stopwatch);
		}
	}

	void createSymbolicLinks(Path fromDirectory, Path toDirectory) throws IOException {
		// clean target directory
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(toDirectory)) {
			for (Path path : stream) {
				if (Files.isDirectory(path)) {
					continue;
				}
				logger.info("Removing existing file {}", path);
				Files.delete(path);
			}
		}
		// create symbolic links
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(fromDirectory)) {
			for (Path path : stream) {
				if (Files.isDirectory(path)) {
					continue;
				}
				Path link = toDirectory.resolve(path.getFileName());
				Path target = toDirectory.relativize(path);
				logger.info("Creating symbolic link {} to {}", link, target);
				Files.createSymbolicLink(link, target);
			}
		}
	}

	void stripDateFromFilenames(Path directory, String validityDateString) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, validityDateString + "*")) {
			for (Path path : stream) {
				Path target = path.resolveSibling(path.getFileName().toString().substring(validityDateString.length() + 1));
				logger.info("Renaming file {} to {}", path, target);
				Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

}
