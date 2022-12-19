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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;

import eu.albina.caaml.Caaml;
import eu.albina.model.AbstractPersistentObject;
import eu.albina.model.AvalancheReport;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.BulletinStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.caaml.CaamlVersion;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.EmailUtil;
import eu.albina.util.JsonUtil;
import eu.albina.map.MapUtil;
import eu.albina.util.PdfUtil;
import eu.albina.util.PushNotificationUtil;
import eu.albina.util.SimpleHtmlUtil;
import eu.albina.util.TelegramChannelUtil;

/**
 * Controller for avalanche reports.
 *
 * @author Norbert Lanzanasto
 *
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
	 *         Java application.
	 */
	public static PublicationController getInstance() {
		if (instance == null) {
			instance = new PublicationController();
		}
		return instance;
	}

	/**
	 * Triggers all tasks that have to take place after a small change in the
	 * bulletin has been published. This does not trigger the whole publication
	 * process.
	 *
	 * @param bulletins
	 *            The bulletins that were changed.
	 */
	public void change(List<AvalancheBulletin> bulletins, User user, Instant startDate, Region changedRegion) {
		List<Region> regions = new ArrayList<Region>();
		regions.add(changedRegion);
		publish(bulletins, regions, user, startDate, startDate, true);
	}

	/**
	 * Triggers all tasks that have to take place after a publication has been published.
	 *
	 * @param bulletins
	 *            The bulletins that were published.
	 * @param regions
	 *            The regions that were published.
	 */
	public void publish(List<AvalancheBulletin> bulletins, List<Region> regions, User user, Instant publicationDate, Instant startDate, boolean isChange) {
		logger.info("Publishing bulletins with publicationDate={} startDate={}", publicationDate, startDate);
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);
		ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

		Collections.sort(bulletins);

		Map<Region, AvalancheReport> reportMap = new HashMap<Region, AvalancheReport>();

		// update all regions to create complete maps
		for (Region region : RegionController.getInstance().getPublishBulletinRegions()) {

			AvalancheReport avalancheReport;

			// publish report and add report if region was updated (to send notifications later on)
			if (regions.contains(region)) {
				List<AvalancheBulletin> regionBulletins = bulletins.stream().filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region)).collect(Collectors.toList());
				logger.info("Publishing region {} with bulletins {}", region.getId(), regionBulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()));
				if (isChange) {
					avalancheReport = AvalancheReportController.getInstance().changeReport(regionBulletins, startDate, region, user);
				} else {
					avalancheReport = AvalancheReportController.getInstance().publishReport(regionBulletins, startDate, region, user, publicationDate);
				}
				avalancheReport.setBulletins(regionBulletins);
				avalancheReport.setGlobalBulletins(bulletins);
				avalancheReport.setServerInstance(localServerInstance);

				if (regionBulletins.isEmpty()) {
					continue;
				}

				reportMap.put(region, avalancheReport);
			} else {
				List<AvalancheBulletin> regionBulletins = bulletins.stream().filter(bulletin -> bulletin.affectsRegionOnlyPublished(region)).collect(Collectors.toList());
				avalancheReport = AvalancheReportController.getInstance().getPublicReport(startDate, region);

				if (avalancheReport == null || regionBulletins.isEmpty()) {
					continue;
				}

				avalancheReport.setBulletins(regionBulletins);
				avalancheReport.setGlobalBulletins(bulletins);
				avalancheReport.setServerInstance(localServerInstance);
			}

			// maybe another region was not published at all
			if (avalancheReport == null || (avalancheReport.getStatus() != BulletinStatus.published && avalancheReport.getStatus() != BulletinStatus.republished)) {
				continue;
			}

			createRegionResources(region, avalancheReport);
		}

		// update all super regions
		Set<Region> superRegions = new HashSet<Region>();
		for (Region region : regions) {
			for (Region superRegion : region.getSuperRegions()) {
				if (!superRegions.stream().anyMatch(updateRegion -> updateRegion.getId().equals(superRegion.getId())))
					superRegions.add(superRegion);
			}
		}
		for (Region region : superRegions) {
			logger.info("Publishing super region {} with bulletins {}", region.getId(), bulletins.stream().map(AbstractPersistentObject::getId).collect(Collectors.toList()));
			AvalancheReport avalancheReport = AvalancheReport.of(bulletins, region, localServerInstance);
			createRegionResources(region, avalancheReport);
		}

		// send notifications only for updated regions after all maps were created
		if (!isChange) {
			for (AvalancheReport avalancheReport : reportMap.values()) {
				if (!avalancheReport.getBulletins().isEmpty() && avalancheReport.getRegion().isCreateMaps()) {
					if (avalancheReport.getRegion().isSendEmails()) {
						new Thread(() -> sendEmails(avalancheReport)).start();
					}
					if (avalancheReport.getRegion().isSendTelegramMessages()) {
						new Thread(() -> triggerTelegramChannel(avalancheReport, null)).start();
					}
					if (avalancheReport.getRegion().isSendPushNotifications()) {
						new Thread(() -> triggerPushNotifications(avalancheReport, null)).start();
					}
				}
			}
		}

		// copy files
		AlbinaUtil.runUpdateFilesScript(validityDateString, publicationTimeString);
		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runUpdateLatestFilesScript(validityDateString);
	}

	private void createRegionResources(Region region, AvalancheReport avalancheReport) {
		// create CAAML
		if (region.isCreateCaamlV5())
			createCaamlV5(avalancheReport);
		if (region.isCreateCaamlV6())
			createCaamlV6(avalancheReport);

		// create JSON
		if (region.isCreateJson())
			createJson(avalancheReport);

		try {
			// create maps
			if (region.isCreateMaps()) {

				createMaps(avalancheReport);

				Map<String, Thread> threads = new HashMap<String, Thread>();

				// create HTML
				if (region.isCreateSimpleHtml()) {
					Thread createSimpleHtmlThread = createSimpleHtml(avalancheReport);
					threads.put("simpleHtml_" + region.getId(), createSimpleHtmlThread);
					createSimpleHtmlThread.start();
				}

				// create pdf
				if (region.isCreatePdf()) {
					Thread createPdfThread = createPdf(avalancheReport);
					threads.put("pdf_" + region.getId(), createPdfThread);
					createPdfThread.start();
				}

				for (String key : threads.keySet()) {
					try {
						threads.get(key).join();
					} catch (InterruptedException e) {
						logger.error(key + " thread interrupted", e);
					}
				}
			}
		} catch (InterruptedException e) {
			logger.error("Map production interrupted", e);
		} catch (Exception e1) {
			logger.error("Error during map production", e1);
		}
	}

	/**
	 * Start an own thread to trigger all tasks that have to take place after an
	 * update has been published.
	 *
	 * @param allBulletins
	 *            The bulletins that were updated.
	 * @param regions
	 *            The regions that were updated.
	 * @param publishedBulletins
	 * @param publicationDate
	 * @param user
	 * @param region
	 * @param startDate
	 */
	public void startUpdateThread(List<AvalancheBulletin> allBulletins, List<Region> regions,
			List<AvalancheBulletin> publishedBulletins, Instant startDate, Region region, User user,
			Instant publicationDate) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<AvalancheBulletin> result = allBulletins.stream()
					.filter(avalancheBulletin -> avalancheBulletin.getPublishedRegions() != null
						&& !avalancheBulletin.getPublishedRegions().isEmpty())
					.collect(Collectors.toList());
				if (result != null && !result.isEmpty())
					PublicationController.getInstance().publish(result, regions, user, publicationDate, startDate, false);

			}
		}).start();
	}

	/**
	 * Start an own thread to trigger all tasks that have to take place after a
	 * change has been published.
	 *
	 * @param allBulletins
	 *            The bulletins that were updated.
	 * @param user
	 * @param region
	 * @param startDate
	 * @param publishedBulletins
	 */
	public void startChangeThread(List<AvalancheBulletin> allBulletins, List<AvalancheBulletin> publishedBulletins,
			Instant startDate, Region region, User user) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<AvalancheBulletin> result = allBulletins.stream()
					.filter(avalancheBulletin -> avalancheBulletin.getPublishedRegions() != null
						&& !avalancheBulletin.getPublishedRegions().isEmpty())
					.collect(Collectors.toList());
				if (result != null && !result.isEmpty())
					PublicationController.getInstance().change(result, user, startDate, region);
			}
		}).start();
	}

	/**
	 * Trigger the creation of the JSON file.
	 */
	public boolean createJson(AvalancheReport avalancheReport) {
		try {
			logger.info("JSON production for " + avalancheReport.getRegion().getId() + " started");
			JsonUtil.createJsonFile(avalancheReport);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setJsonCreated);
			logger.info("JSON production for " + avalancheReport.getRegion().getId() + " finished");
			return true;
		} catch (TransformerException | IOException e) {
			logger.error("Error producing JSON for " + avalancheReport.getRegion().getId(), e);
			return false;
		}
	}

	/**
	 * Trigger the creation of the CAAMLv5 (XML) files.
	 */
	public boolean createCaamlV5(AvalancheReport avalancheReport) {
		try {
			logger.info("CAAMLv5 production for " + avalancheReport.getRegion().getId() + " started");
			Caaml.createCaamlFiles(avalancheReport, CaamlVersion.V5);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setCaamlV5Created);
			logger.info("CAAMLv5 production for " + avalancheReport.getRegion().getId() + " finished");
			return true;
		} catch (TransformerException | IOException e) {
			logger.error("Error producing CAAMLv5 for " + avalancheReport.getRegion().getId(), e);
			return false;
		}
	}

	/**
	 * Trigger the creation of the CAAMLv6 (XML) files.
	 */
	public boolean createCaamlV6(AvalancheReport avalancheReport) {
		try {
			logger.info("CAAMLv6 production for " + avalancheReport.getRegion().getId() + " started");
			Caaml.createCaamlFiles(avalancheReport, CaamlVersion.V6);
			Caaml.createCaamlFiles(avalancheReport, CaamlVersion.V6_2022);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setCaamlV6Created);
			logger.info("CAAMLv6 production for " + avalancheReport.getRegion().getId() + " finished");
			return true;
		} catch (TransformerException | IOException e) {
			logger.error("Error producing CAAMLv6 for " + avalancheReport.getRegion().getId(), e);
			return false;
		}
	}

	/**
	 * Trigger the creation of the maps.
	 */
	public boolean createMaps(AvalancheReport avalancheReport)
			throws Exception {
		final String regionId = avalancheReport.getRegion().getId();
		try {
			logger.info("Map production for " + regionId + " started");
			MapUtil.createMapyrusMaps(avalancheReport);
			AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setMapCreated);
			logger.info("Map production " + regionId + " finished");
			return true;
		} catch (Exception e) {
			logger.error("Error producing maps for " + regionId, e);
			return false;
		}
	}

	/**
	 * Trigger the creation of the pdfs.
	 */
	public Thread createPdf(AvalancheReport avalancheReport) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("PDF production for " + avalancheReport.getRegion().getId() + " started");
					PdfUtil.createRegionPdfs(avalancheReport);
					AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setPdfCreated);
				} finally {
					logger.info("PDF production " + avalancheReport.getRegion().getId() + " finished");
				}
			}
		});
	}

	/**
	 * Trigger the creation of the simple html files.
	 */
	public Thread createSimpleHtml(AvalancheReport avalancheReport) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Simple HTML production for " + avalancheReport.getRegion().getId() + " started");
					SimpleHtmlUtil.getInstance().createRegionSimpleHtml(avalancheReport);
					AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setHtmlCreated);
				} catch (Exception e) {
					logger.error("Error creating simple HTML for " + avalancheReport.getRegion().getId(), e);
				} finally {
					logger.info("Simple HTML production for " + avalancheReport.getRegion().getId() + " finished");
				}
			}
		});
	}

	/**
	 * Trigger the sending of the emails.
	 */
	public void sendEmails(AvalancheReport avalancheReport) {
		try {
			logger.info("Email production for " + avalancheReport.getRegion().getId() + " started");
			EmailUtil.getInstance().sendBulletinEmails(avalancheReport);
			if (avalancheReport.getStatus() != BulletinStatus.test)
				AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setEmailCreated);
		} catch (Exception e) {
			logger.error("Error preparing emails " + avalancheReport.getRegion().getId(), e);
		} finally {
			logger.info("Email production " + avalancheReport.getRegion().getId() + " finished");
		}
	}

	public void triggerTelegramChannel(AvalancheReport avalancheReport, LanguageCode language) {
		try {
			logger.info("Telegram channel for " + avalancheReport.getRegion().getId() + " triggered");
			if (language == null)
				TelegramChannelUtil.getInstance().sendBulletinNewsletters(avalancheReport);
			else
				TelegramChannelUtil.getInstance().sendBulletinNewsletters(avalancheReport, language);
			if (avalancheReport.getStatus() != BulletinStatus.test)
				AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setTelegramSent);
		} catch (Exception e) {
			logger.error("Error preparing telegram channel", e);
		} finally {
			logger.info("Telegram channel for " + avalancheReport.getRegion().getId() + " finished");
		}
	}

	public void triggerPushNotifications(AvalancheReport avalancheReport, LanguageCode language) {
		try {
			logger.info("Push notifications for " + avalancheReport.getRegion().getId() + " triggered");
			if (language == null)
				new PushNotificationUtil().sendBulletinNewsletters(avalancheReport);
			else
				new PushNotificationUtil().sendBulletinNewsletters(avalancheReport, language);
			if (avalancheReport.getStatus() != BulletinStatus.test)
				AvalancheReportController.getInstance().setAvalancheReportFlag(avalancheReport.getId(), AvalancheReport::setPushSent);
		} catch (Exception e) {
			logger.error("Error sending push notifications for " + avalancheReport.getRegion().getId(), e);
		} finally {
			logger.info("Push notifications for " + avalancheReport.getRegion().getId() + " finished");
		}
	}
}
