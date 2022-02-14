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
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.caaml.CaamlVersion;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.User;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.EmailUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.JsonUtil;
import eu.albina.map.MapUtil;
import eu.albina.util.PdfUtil;
import eu.albina.util.PushNotificationUtil;
import eu.albina.util.SimpleHtmlUtil;
import eu.albina.util.StaticWidgetUtil;
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
	public void publishAutomatically(List<AvalancheBulletin> bulletins) {
		if (GlobalVariables.isPublishAt5PM())
			publish(bulletins);
	}

	public void publish(List<AvalancheBulletin> bulletins) {
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

		Collections.sort(bulletins);

		// create CAAML
		createCaaml(bulletins, validityDateString, publicationTimeString);

		// create JSON
		createJson(bulletins, validityDateString, publicationTimeString);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			try {
				createMaps(bulletins, validityDateString, publicationTimeString);

				Map<String, Thread> threads = new HashMap<String, Thread>();

				// create HTML
				if (GlobalVariables.isCreateSimpleHtml()) {
					Thread createSimpleHtmlThread = createSimpleHtml(bulletins);
					threads.put("simpleHtml", createSimpleHtmlThread);
					createSimpleHtmlThread.start();
				}

				// create pdfs
				if (GlobalVariables.isCreatePdf()) {
					Thread createPdfThread = createPdf(bulletins, validityDateString, publicationTimeString);
					threads.put("pdf", createPdfThread);
					createPdfThread.start();
				}

				// create static widgets
				if (GlobalVariables.isCreateStaticWidget()) {
					Thread createStaticWidgetsThread = createStaticWidgets(bulletins, validityDateString,
							publicationTimeString);
					threads.put("staticWidget", createStaticWidgetsThread);
					createStaticWidgetsThread.start();
				}

				for (String key : threads.keySet()) {
					try {
						threads.get(key).join();
					} catch (InterruptedException e) {
						logger.error(key + " thread interrupted", e);
					}
				}
			} catch (InterruptedException e) {
				logger.error("Map production interrupted", e);
			} catch (Exception e1) {
				logger.error("Error during map production", e1);
			}

			// copy files
			AlbinaUtil.runUpdateFilesScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestFilesScript(validityDateString);

			// send emails
			if (GlobalVariables.isCreateMaps() && GlobalVariables.isSendEmails()) {
				Thread sendEmailsThread = sendEmails(bulletins, GlobalVariables.getPublishRegions(), false, false);
				sendEmailsThread.start();
			}

			// publish on telegram channel
			if (GlobalVariables.isCreateMaps() && GlobalVariables.isPublishToTelegramChannel()) {
				Thread triggerTelegramChannelThread = triggerTelegramChannel(bulletins, GlobalVariables.getPublishRegions(),
						false, null, false);
				triggerTelegramChannelThread.start();
			}

			// publish via push notifications
			if (GlobalVariables.isCreateMaps()) {
				new Thread(() -> triggerPushNotifications(bulletins, GlobalVariables.getPublishRegions(), false, null, false)).start();
			}
		}
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
	public void updateAutomatically(List<AvalancheBulletin> bulletins, List<String> regions) {
		if (GlobalVariables.isPublishAt8AM())
			update(bulletins, regions);
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
	public void update(List<AvalancheBulletin> bulletins, List<String> regions) {
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

		Collections.sort(bulletins);

		// create CAAML
		createCaaml(bulletins, validityDateString, publicationTimeString);

		// create JSON
		createJson(bulletins, validityDateString, publicationTimeString);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			try {
				createMaps(bulletins, validityDateString, publicationTimeString);

				Map<String, Thread> threads = new HashMap<String, Thread>();

				// create HTML
				if (GlobalVariables.isCreateSimpleHtml()) {
					Thread createSimpleHtmlThread = createSimpleHtml(bulletins);
					threads.put("simpleHtml", createSimpleHtmlThread);
					createSimpleHtmlThread.start();
				}

				// create pdf
				if (GlobalVariables.isCreatePdf()) {
					Thread createPdfThread = createPdf(bulletins, validityDateString, publicationTimeString);
					threads.put("pdf", createPdfThread);
					createPdfThread.start();
				}

				// create static widgets
				if (GlobalVariables.isCreateStaticWidget()) {
					Thread createStaticWidgetsThread = createStaticWidgets(bulletins, validityDateString,
							publicationTimeString);
					threads.put("staticWidget", createStaticWidgetsThread);
					createStaticWidgetsThread.start();
				}

				for (String key : threads.keySet()) {
					try {
						threads.get(key).join();
					} catch (InterruptedException e) {
						logger.error(key + " thread interrupted", e);
					}
				}
			} catch (InterruptedException e) {
				logger.error("Map production interrupted", e);
			} catch (Exception e1) {
				logger.error("Error during map production", e1);
			}

			// copy files
			AlbinaUtil.runUpdateFilesScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestFilesScript(validityDateString);

			// send emails to regions
			if (GlobalVariables.isCreateMaps() && GlobalVariables.isSendEmails()) {
				Thread sendEmailsThread = sendEmails(bulletins, regions, true, false);
				sendEmailsThread.start();
			}

			// publish on telegram channel
			if (GlobalVariables.isCreateMaps() && GlobalVariables.isPublishToTelegramChannel()) {
				Thread triggerTelegramChannelThread = triggerTelegramChannel(bulletins, regions, true, null, false);
				triggerTelegramChannelThread.start();
			}

			// publish via push notifications
			if (GlobalVariables.isCreateMaps()) {
				new Thread(() -> triggerPushNotifications(bulletins, regions, true, null, false)).start();
			}
		}
	}

	/**
	 * Triggers all tasks that have to take place after a small change in the
	 * bulletin has been published. This does not trigger the whole publication
	 * process.
	 *
	 * @param bulletins
	 *            The bulletins that were changed.
	 */
	public void change(List<AvalancheBulletin> bulletins) {
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

		Collections.sort(bulletins);

		// create CAAML
		createCaaml(bulletins, validityDateString, publicationTimeString);

		// create JSON
		createJson(bulletins, validityDateString, publicationTimeString);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			try {
				createMaps(bulletins, validityDateString, publicationTimeString);

				Map<String, Thread> threads = new HashMap<String, Thread>();

				// create HTML
				if (GlobalVariables.isCreateSimpleHtml()) {
					Thread createSimpleHtmlThread = createSimpleHtml(bulletins);
					threads.put("simpleHtml", createSimpleHtmlThread);
					createSimpleHtmlThread.start();
				}

				// create pdfs
				if (GlobalVariables.isCreatePdf()) {
					Thread createPdfThread = createPdf(bulletins, validityDateString, publicationTimeString);
					threads.put("pdf", createPdfThread);
					createPdfThread.start();
				}

				// create static widgets
				if (GlobalVariables.isCreateStaticWidget()) {
					Thread createStaticWidgetsThread = createStaticWidgets(bulletins, validityDateString,
							publicationTimeString);
					threads.put("staticWidget", createStaticWidgetsThread);
					createStaticWidgetsThread.start();
				}

				for (String key : threads.keySet()) {
					try {
						threads.get(key).join();
					} catch (InterruptedException e) {
						logger.error(key + " thread interrupted", e);
					}
				}
			} catch (InterruptedException e) {
				logger.error("Map production interrupted", e);
			} catch (Exception e) {
				logger.error("Error during map production", e);
			}

			// copy files
			AlbinaUtil.runUpdateFilesScript(validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runUpdateLatestFilesScript(validityDateString);

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
	public void startUpdateThread(List<AvalancheBulletin> allBulletins, List<String> regions,
			List<AvalancheBulletin> publishedBulletins, Instant startDate, String region, User user,
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
					PublicationController.getInstance().update(result, regions);

				List<String> avalancheReportIds = new ArrayList<String>();
				String avalancheReportId = AvalancheReportController.getInstance().publishReport(publishedBulletins,
						startDate, region, user, publicationDate);
				avalancheReportIds.add(avalancheReportId);
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
			Instant startDate, String region, User user) {
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
					PublicationController.getInstance().change(result);

				List<String> avalancheReportIds = new ArrayList<String>();
				String avalancheReportId = AvalancheReportController.getInstance().changeReport(publishedBulletins,
						startDate, region, user);
				avalancheReportIds.add(avalancheReportId);
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
	public void createJson(List<AvalancheBulletin> bulletins, String validityDateString, String publicationTimeString) {
		try {
			logger.info("JSON production started");
			JsonUtil.createJsonFile(bulletins, validityDateString, publicationTimeString);
			logger.info("JSON production finished");
		} catch (TransformerException | IOException e) {
			logger.error("Error producing JSON", e);
		}
	}

	/**
	 * Trigger the creation of the CAAML (XML) files.
	 *
	 * @param bulletins
	 *            the bulletins contained in the CAAML file
	 * @param validityDateString
	 *            point in time when the validity of the bulletin starts
	 * @param publicationTimeString
	 *            date and time of publication
	 */
	public void createCaaml(List<AvalancheBulletin> bulletins, String validityDateString,
			String publicationTimeString) {
		try {
			logger.info("CAAML production started");
			XmlUtil.createCaamlFiles(bulletins, validityDateString, publicationTimeString, CaamlVersion.V5);
			XmlUtil.createCaamlFiles(bulletins, validityDateString, publicationTimeString, CaamlVersion.V6);
			logger.info("CAAML production finished");
		} catch (TransformerException | IOException e) {
			logger.error("Error producing CAAML", e);
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
	public void createMaps(List<AvalancheBulletin> bulletins, String validityDateString, String publicationTimeString)
			throws Exception {
		logger.info("Map production started");

		if (!bulletins.isEmpty()) {
			MapUtil.createDangerRatingMaps(bulletins, RegionController.getInstance().getRegions(), false);
			logger.info("Map production finished");
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
	public Thread createPdf(List<AvalancheBulletin> bulletins, String validityDateString,
			String publicationTimeString) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("PDF production started");
					for (String region : GlobalVariables.getPublishRegions(true))
						PdfUtil.getInstance().createRegionPdfs(bulletins, region, validityDateString,
								publicationTimeString);
				} finally {
					logger.info("PDF production finished");
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
	public Thread createSimpleHtml(List<AvalancheBulletin> bulletins) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Simple HTML production started");
					for (String region : GlobalVariables.getPublishRegions(true))
						SimpleHtmlUtil.getInstance().createRegionSimpleHtml(bulletins, region);
				} catch (Exception e) {
					logger.error("Error creating simple HTML", e);
				} finally {
					logger.info("Simple HTML production finished");
				}
			}
		});
	}

	/**
	 * Trigger the creation of the static widgets.
	 *
	 * @param bulletins
	 *            the bulletins contained in the static widgets
	 * @param publicationTimeString
	 *            the time of publication
	 * @param validityDateString
	 *            the start of the validity of the report
	 * @param publicationTimeString
	 *            the time of publication
	 * @param validityDateString
	 *            the start of the validity of the report
	 */
	public Thread createStaticWidgets(List<AvalancheBulletin> bulletins, String validityDateString,
			String publicationTimeString) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Static widget production started");
					StaticWidgetUtil.getInstance().createStaticWidgets(bulletins, validityDateString,
							publicationTimeString);
				} catch (Exception e) {
					logger.error("Error creating static widgets", e);
				} finally {
					logger.info("Static widget production finished");
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
	public Thread sendEmails(List<AvalancheBulletin> bulletins, List<String> regions, boolean update, boolean test) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Email production started");
					EmailUtil.getInstance().sendBulletinEmails(bulletins, regions, update, test);
				} catch (Exception e) {
					logger.error("Error preparing emails", e);
				} finally {
					logger.info("Email production finished");
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
	public Thread sendEmails(List<AvalancheBulletin> bulletins, List<String> regions, boolean update, boolean test, LanguageCode language) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Email production started");
					EmailUtil.getInstance().sendBulletinEmails(bulletins, regions, update, test, language);
				} catch (Exception e) {
					logger.error("Error preparing emails", e);
				} finally {
					logger.info("Email production finished");
				}
			}
		});
	}

	public Thread triggerTelegramChannel(List<AvalancheBulletin> bulletins, List<String> regions, boolean update, LanguageCode language, boolean test) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Telegram channel triggered");
					if (language == null)
						TelegramChannelUtil.getInstance().sendBulletinNewsletters(bulletins, regions, update, test);
					else
						TelegramChannelUtil.getInstance().sendBulletinNewsletters(bulletins, regions, update, language, test);
				} catch (Exception e) {
					logger.error("Error preparing telegram channel", e);
				} finally {
					logger.info("Telegram channel finished");
				}
			}
		});
	}

	public void triggerPushNotifications(List<AvalancheBulletin> bulletins, List<String> regions, boolean update, LanguageCode language, boolean test) {
		try {
			logger.info("Push notifications triggered");
			if (language == null)
				new PushNotificationUtil().sendBulletinNewsletters(bulletins, regions, update, test);
			else
				new PushNotificationUtil().sendBulletinNewsletters(bulletins, regions, update, language, test);
		} catch (Exception e) {
			logger.error("Error sending push notifications", e);
		} finally {
			logger.info("Push notifications finished");
		}
	}
}
