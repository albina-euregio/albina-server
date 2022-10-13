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

import eu.albina.model.AvalancheReport;
import eu.albina.model.ServerInstance;
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
import eu.albina.util.XmlUtil;

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

			if (!regionBulletins.isEmpty()) {

				// create CAAML
				if (region.isCreateCaamlV5())
					createCaamlV5(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);
				if (region.isCreateCaamlV6())
					createCaamlV6(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);

				// create JSON
				if (region.isCreateJson())
					createJson(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);

				try {
					// create maps
					if (region.isCreateMaps()) {

						createMaps(avalancheReportId, bulletins, region, validityDateString, publicationTimeString, localServerInstance);

						Map<String, Thread> threads = new HashMap<String, Thread>();

						// create HTML
						if (region.isCreateSimpleHtml()) {
							Thread createSimpleHtmlThread = createSimpleHtml(avalancheReportId, regionBulletins, region);
							threads.put("simpleHtml_" + region.getId(), createSimpleHtmlThread);
							createSimpleHtmlThread.start();
						}

						// create pdfs
						if (region.isCreatePdf()) {
							Thread createPdfThread = createPdf(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);
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
							new Thread(() -> sendEmails(avalancheReportId, bulletins, region, false, false)).start();
							new Thread(() -> triggerTelegramChannel(avalancheReportId, bulletins, region,	false, null, false, localServerInstance)).start();
							new Thread(() -> triggerPushNotifications(avalancheReportId, bulletins, region, false, null, false, localServerInstance)).start();
						}
					}
				} catch (InterruptedException e) {
					logger.error("Map production interrupted", e);
				} catch (Exception e1) {
					logger.error("Error during map production", e1);
				}
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

			String avalancheReportId;
			if (region.getSubRegions().isEmpty()) {
				AvalancheReport avalancheReport = AvalancheReportController.getInstance().publishReport(regionBulletins, startDate, region, user, publicationDate);
				avalancheReport.setServerInstance(localServerInstance);
				avalancheReportId = avalancheReport.getId();
			} else {
				avalancheReportId = null;
			}

			if (!regionBulletins.isEmpty()) {

				// create CAAML
				if (region.isCreateCaamlV5())
					createCaamlV5(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);
				if (region.isCreateCaamlV6())
					createCaamlV6(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);

				// create JSON
				if (region.isCreateJson())
					createJson(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);

				try {
					// create maps
					if (region.isCreateMaps()) {

						createMaps(avalancheReportId, bulletins, region, validityDateString, publicationTimeString, localServerInstance);

						Map<String, Thread> threads = new HashMap<String, Thread>();

						// create HTML
						if (region.isCreateSimpleHtml()) {
							Thread createSimpleHtmlThread = createSimpleHtml(avalancheReportId, regionBulletins, region);
							threads.put("simpleHtml_" + region.getId(), createSimpleHtmlThread);
							createSimpleHtmlThread.start();
						}

						// create pdf
						if (region.isCreatePdf()) {
							Thread createPdfThread = createPdf(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);
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
							new Thread(() -> sendEmails(avalancheReportId, bulletins, region, true, false)).start();
							new Thread(() -> triggerTelegramChannel(avalancheReportId, bulletins, region, true, null, false, localServerInstance)).start();
							new Thread(() -> triggerPushNotifications(avalancheReportId, bulletins, region, true, null, false, localServerInstance)).start();
						}
					}
				} catch (InterruptedException e) {
					logger.error("Map production interrupted", e);
				} catch (Exception e1) {
					logger.error("Error during map production", e1);
				}
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

			if (!regionBulletins.isEmpty()) {

				// create CAAML
				if (region.isCreateCaamlV5())
					createCaamlV5(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);
				if (region.isCreateCaamlV6())
					createCaamlV6(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);

				// create JSON
				if (region.isCreateJson())
					createJson(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);

				try {
					// create maps
					if (region.isCreateMaps()) {

						createMaps(avalancheReportId, bulletins, region, validityDateString, publicationTimeString, localServerInstance);

						Map<String, Thread> threads = new HashMap<String, Thread>();

						// create HTML
						if (region.isCreateSimpleHtml()) {
							Thread createSimpleHtmlThread = createSimpleHtml(avalancheReportId, regionBulletins, region);
							threads.put("simpleHtml_" + region.getId(), createSimpleHtmlThread);
							createSimpleHtmlThread.start();
						}

						// create pdfs
						if (region.isCreatePdf()) {
							Thread createPdfThread = createPdf(avalancheReportId, regionBulletins, region, validityDateString, publicationTimeString, localServerInstance);
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
	 *
	 * @param bulletins
	 *            the bulletins contained in the JSON file
	 * @param validityDateString
	 *            point in time when the validity of the bulletin starts
	 * @param publicationTimeString
	 *            date and time of publication
	 */
	public boolean createJson(String avalancheReportId, List<AvalancheBulletin> bulletins, Region region, String validityDateString, String publicationTimeString, ServerInstance serverInstance) {
		try {
			logger.info("JSON production for " + region.getId() + " started");
			JsonUtil.createJsonFile(bulletins, region, validityDateString, publicationTimeString, serverInstance);
			if (avalancheReportId != null)
				AvalancheReportController.getInstance().setAvalancheReportJsonFlag(avalancheReportId);
			logger.info("JSON production for " + region.getId() + " finished");
			return true;
		} catch (TransformerException | IOException e) {
			logger.error("Error producing JSON for " + region.getId(), e);
			return false;
		}
	}

	/**
	 * Trigger the creation of the CAAMLv5 (XML) files.
	 *
	 * @param bulletins
	 *            the bulletins contained in the CAAML file
	 * @param validityDateString
	 *            point in time when the validity of the bulletin starts
	 * @param publicationTimeString
	 *            date and time of publication
	 */
	public boolean createCaamlV5(String avalancheReportId, List<AvalancheBulletin> bulletins, Region region, String validityDateString,
			String publicationTimeString, ServerInstance serverInstance) {
		try {
			logger.info("CAAMLv5 production for " + region.getId() + " started");
			XmlUtil.createCaamlFiles(bulletins, region, validityDateString, publicationTimeString, CaamlVersion.V5, serverInstance);
			if (avalancheReportId != null)
				AvalancheReportController.getInstance().setAvalancheReportCaamlV5Flag(avalancheReportId);
			logger.info("CAAMLv5 production for " + region.getId() + " finished");
			return true;
		} catch (TransformerException | IOException e) {
			logger.error("Error producing CAAMLv5 for " + region.getId(), e);
			return false;
		}
	}

	/**
	 * Trigger the creation of the CAAMLv6 (XML) files.
	 *
	 * @param bulletins
	 *            the bulletins contained in the CAAML file
	 * @param validityDateString
	 *            point in time when the validity of the bulletin starts
	 * @param publicationTimeString
	 *            date and time of publication
	 */
	public boolean createCaamlV6(String avalancheReportId, List<AvalancheBulletin> bulletins, Region region, String validityDateString,
			String publicationTimeString, ServerInstance serverInstance) {
		try {
			logger.info("CAAMLv6 production for " + region.getId() + " started");
			XmlUtil.createCaamlFiles(bulletins, region, validityDateString, publicationTimeString, CaamlVersion.V6, serverInstance);
			if (avalancheReportId != null)
				AvalancheReportController.getInstance().setAvalancheReportCaamlV6Flag(avalancheReportId);
			logger.info("CAAMLv6 production for " + region.getId() + " finished");
			return true;
		} catch (TransformerException | IOException e) {
			logger.error("Error producing CAAMLv6 for " + region.getId(), e);
			return false;
		}
	}

	/**
	 * Trigger the creation of the maps.
	 *
	 * @param bulletins
	 *            the bulletins contained in the maps
	 * @param publicationTimeString
	 *            the time of publication
	 * @param validityDateString
	 *            the start of the validity of the report
	 */
	public boolean createMaps(String avalancheReportId, List<AvalancheBulletin> bulletins, Region region, String validityDateString, String publicationTimeString, ServerInstance serverInstance)
			throws Exception {
		try {
			logger.info("Map production for " + region.getId() + " started");
			MapUtil.createMapyrusMaps(bulletins, region, validityDateString, publicationTimeString, serverInstance);
			if (avalancheReportId != null)
				AvalancheReportController.getInstance().setAvalancheReportMapFlag(avalancheReportId);
			logger.info("Map production " + region.getId() + " finished");
			return true;
		} catch (Exception e) {
			logger.error("Error producing maps for " + region.getId(), e);
			return false;
		}
	}

	/**
	 * Trigger the creation of the pdfs.
	 *
	 * @param bulletins
	 *            the bulletins contained in the pdfs
	 * @param publicationTimeString
	 *            the time of publication
	 * @param validityDateString
	 *            the start of the validity of the report
	 */
	public Thread createPdf(String avalancheReportId, List<AvalancheBulletin> bulletins, Region region, String validityDateString,
			String publicationTimeString, ServerInstance serverInstance) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("PDF production for " + region.getId() + " started");
					PdfUtil.getInstance().createRegionPdfs(bulletins, region, validityDateString,
							publicationTimeString, serverInstance);
					if (avalancheReportId != null)
						AvalancheReportController.getInstance().setAvalancheReportPdfFlag(avalancheReportId);
				} finally {
					logger.info("PDF production " + region.getId() + " finished");
				}
			}
		});
	}

	/**
	 * Trigger the creation of the simple html files.
	 *
	 * @param bulletins
	 *            the bulletins contained in the html files
	 */
	public Thread createSimpleHtml(String avalancheReportId, List<AvalancheBulletin> bulletins, Region region) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Simple HTML production for " + region.getId() + " started");
					SimpleHtmlUtil.getInstance().createRegionSimpleHtml(bulletins, region, ServerInstanceController.getInstance().getLocalServerInstance());
					if (avalancheReportId != null)
						AvalancheReportController.getInstance().setAvalancheReportHtmlFlag(avalancheReportId);
				} catch (Exception e) {
					logger.error("Error creating simple HTML for " + region.getId(), e);
				} finally {
					logger.info("Simple HTML production for " + region.getId() + " finished");
				}
			}
		});
	}

	/**
	 * Trigger the sending of the emails.
	 *
	 * @param bulletins
	 *            the bulletins contained in the emails
	 */
	public Thread sendEmails(String avalancheReportId, List<AvalancheBulletin> bulletins, Region region, boolean update, boolean test) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Email production for " + region.getId() + " started");
					ServerInstance localServerInstance = ServerInstanceController.getInstance().getLocalServerInstance();
					EmailUtil.getInstance().sendBulletinEmails(bulletins, region, update, test, localServerInstance);
					if (!test && avalancheReportId != null)
						AvalancheReportController.getInstance().setAvalancheReportEmailFlag(avalancheReportId);
				} catch (Exception e) {
					logger.error("Error preparing emails " + region, e);
				} finally {
					logger.info("Email production " + region.getId() + " finished");
				}
			}
		});
	}

	public void triggerTelegramChannel(String avalancheReportId, List<AvalancheBulletin> bulletins, Region region, boolean update, LanguageCode language, boolean test, ServerInstance serverInstance) {
		try {
			logger.info("Telegram channel for " + region.getId() + " triggered");
			if (language == null)
				TelegramChannelUtil.getInstance().sendBulletinNewsletters(bulletins, region, update, test, serverInstance);
			else
				TelegramChannelUtil.getInstance().sendBulletinNewsletters(bulletins, region, update, language, test, serverInstance);
			if (!test && avalancheReportId != null)
				AvalancheReportController.getInstance().setAvalancheReportTelegramFlag(avalancheReportId);
		} catch (Exception e) {
			logger.error("Error preparing telegram channel", e);
		} finally {
			logger.info("Telegram channel for " + region.getId() + " finished");
		}
	}

	public void triggerPushNotifications(String avalancheReportId, List<AvalancheBulletin> bulletins, Region region, boolean update, LanguageCode language, boolean test, ServerInstance serverInstance) {
		try {
			logger.info("Push notifications for " + region.getId() + " triggered");
			if (language == null)
				new PushNotificationUtil().sendBulletinNewsletters(bulletins, region, update, test, serverInstance);
			else
				new PushNotificationUtil().sendBulletinNewsletters(bulletins, region, update, language, test, serverInstance);
			if (!test && avalancheReportId != null)
				AvalancheReportController.getInstance().setAvalancheReportPushFlag(avalancheReportId);
		} catch (Exception e) {
			logger.error("Error sending push notifications for " + region.getId(), e);
		} finally {
			logger.info("Push notifications for " + region.getId() + " finished");
		}
	}
}
