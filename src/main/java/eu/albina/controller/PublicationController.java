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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;

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
	 * Trigger all tasks that have to take place after an avalanche bulletin has
	 * been published automatically. This happens at 17:00 PM if this is defined in
	 * the settings.
	 *
	 * @param bulletins
	 *            The bulletins that were published.
	 */
	public void publishAutomatically(List<AvalancheBulletin> bulletins, User user, Instant publicationDate, Instant startDate) {
		if (ServerInstanceController.getInstance().getLocalServerInstance().isPublishAt5PM())
			publish(bulletins, user, publicationDate, startDate);
	}

	public void publish(List<AvalancheBulletin> bulletins, User user, Instant publicationDate, Instant startDate) {
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);
		ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

		Collections.sort(bulletins);

		for (Region region : RegionController.getInstance().getRegions()) {

			List<AvalancheBulletin> regionBulletins = bulletins.stream().filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region)).collect(Collectors.toList());

			AvalancheReport avalancheReport = AvalancheReportController.getInstance().publishReport(regionBulletins, startDate, region, user, publicationDate);
			avalancheReport.setServerInstance(localServerInstance);
			String avalancheReportId = avalancheReport.getId();

			if (regionBulletins.isEmpty()) {
				continue;
			}

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

					// create pdfs
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

					if (region.isCreateMaps()) {
						new Thread(() -> sendEmails(avalancheReport)).start();
						new Thread(() -> triggerTelegramChannel(avalancheReport, null)).start();
						new Thread(() -> triggerPushNotifications(avalancheReport, null)).start();
					}
				}
			} catch (InterruptedException e) {
				logger.error("Map production interrupted", e);
			} catch (Exception e1) {
				logger.error("Error during map production", e1);
			}
		}

		// copy files
		AlbinaUtil.runUpdateFilesScript(validityDateString, publicationTimeString);
		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runUpdateLatestFilesScript(validityDateString);
	}

	/**
	 * Trigger all tasks that have to take place after an avalanche bulletin has
	 * been updated automatically. This happens at 08:00 AM (if needed) if this is
	 * defined in the settings.
	 *
	 * @param bulletins
	 *            The bulletins that were published.
	 * @param regions
	 *            The regions that were updated.
	 */
	public void updateAutomatically(List<AvalancheBulletin> bulletins, List<Region> regions, User user, Instant publicationDate, Instant startDate) {
		if (ServerInstanceController.getInstance().getLocalServerInstance().isPublishAt8AM())
			update(bulletins, regions, user, publicationDate, startDate);
	}

	/**
	 * Triggers all tasks that have to take place after an update has been published
	 * (this can be at any time, triggered by one province).
	 *
	 * @param bulletins
	 *            The bulletins that were updated.
	 * @param regions
	 *            The region that was updated.
	 */
	public void update(List<AvalancheBulletin> bulletins, List<Region> regions, User user, Instant publicationDate, Instant startDate) {
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);
		ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

		// update also super regions
		Set<Region> updateRegions = new HashSet<Region>(regions);
		for (Region region : regions) {
			for (Region superRegion : region.getSuperRegions()) {
				if (!updateRegions.stream().anyMatch(updateRegion -> updateRegion.getId().equals(superRegion.getId())))
					updateRegions.add(superRegion);
			}
		}

		Collections.sort(bulletins);

		for (Region region : updateRegions) {

			List<AvalancheBulletin> regionBulletins = bulletins.stream().filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region)).collect(Collectors.toList());

			AvalancheReport avalancheReport;
			String avalancheReportId;
			if (region.getSubRegions().isEmpty()) {
				avalancheReport = AvalancheReportController.getInstance().publishReport(regionBulletins, startDate, region, user, publicationDate);
				avalancheReportId = avalancheReport.getId();
			} else {
				avalancheReport = new AvalancheReport();
				avalancheReport.setBulletins(regionBulletins);
				avalancheReport.setTimestamp(publicationDate.atZone(ZoneId.of("UTC")));
				avalancheReport.setUser(user);
				avalancheReport.setDate(startDate.atZone(ZoneId.of("UTC")));
				avalancheReport.setRegion(region);
				avalancheReportId = null;
			}
			avalancheReport.setServerInstance(localServerInstance);

			if (regionBulletins.isEmpty()) {
				continue;
			}

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

					if (region.isCreateMaps()) {
						new Thread(() -> sendEmails(avalancheReport)).start();
						new Thread(() -> triggerTelegramChannel(avalancheReport, null)).start();
						new Thread(() -> triggerPushNotifications(avalancheReport, null)).start();
					}
				}
			} catch (InterruptedException e) {
				logger.error("Map production interrupted", e);
			} catch (Exception e1) {
				logger.error("Error during map production", e1);
			}
		}

		// copy files
		AlbinaUtil.runUpdateFilesScript(validityDateString, publicationTimeString);
		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runUpdateLatestFilesScript(validityDateString);
	}

	/**
	 * Triggers all tasks that have to take place after a small change in the
	 * bulletin has been published. This does not trigger the whole publication
	 * process.
	 *
	 * @param bulletins
	 *            The bulletins that were changed.
	 */
	public void change(List<AvalancheBulletin> bulletins, User user, Instant startDate) {
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);
		ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();

		Collections.sort(bulletins);

		for (Region region : RegionController.getInstance().getRegions()) {

			List<AvalancheBulletin> regionBulletins = bulletins.stream().filter(bulletin -> bulletin.affectsRegionWithoutSuggestions(region)).collect(Collectors.toList());

			AvalancheReport avalancheReport = AvalancheReportController.getInstance().changeReport(regionBulletins, startDate, region, user);
			avalancheReport.setServerInstance(localServerInstance);
			String avalancheReportId = avalancheReport.getId();

			if (regionBulletins.isEmpty()) {
				continue;
			}

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

					// create pdfs
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
			} catch (Exception e) {
				logger.error("Error during map production", e);
			}
		}

		// copy files
		AlbinaUtil.runUpdateFilesScript(validityDateString, publicationTimeString);
		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runUpdateLatestFilesScript(validityDateString);
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
				List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
				for (AvalancheBulletin avalancheBulletin : allBulletins) {
					if (avalancheBulletin.getPublishedRegions() != null
							&& !avalancheBulletin.getPublishedRegions().isEmpty())
						result.add(avalancheBulletin);
				}
				if (result != null && !result.isEmpty())
					PublicationController.getInstance().update(result, regions, user, publicationDate, startDate);

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
				List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
				for (AvalancheBulletin avalancheBulletin : allBulletins) {
					if (avalancheBulletin.getPublishedRegions() != null
							&& !avalancheBulletin.getPublishedRegions().isEmpty())
						result.add(avalancheBulletin);
				}
				if (result != null && !result.isEmpty())
					PublicationController.getInstance().change(result, user, startDate);
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
			avalancheReport.toCAAML(CaamlVersion.V5);
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
			avalancheReport.toCAAML(CaamlVersion.V6);
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
