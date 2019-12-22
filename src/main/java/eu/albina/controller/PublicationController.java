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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.User;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.EmailUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.MapUtil;
import eu.albina.util.MessengerPeopleUtil;
import eu.albina.util.PdfUtil;
import eu.albina.util.SimpleHtmlUtil;
import eu.albina.util.StaticWidgetUtil;
import eu.albina.util.XmlUtil;

/**
 * Controller for avalanche reports.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class PublicationController {

	private static Logger logger = LoggerFactory.getLogger(PublicationController.class);

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

	private void publish(List<AvalancheBulletin> bulletins) {
		String validityDateString = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTimeString = AlbinaUtil.getPublicationTime(bulletins);

		Collections.sort(bulletins);

		AlbinaUtil.runDeleteFilesScript(validityDateString);

		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runDeleteLatestFilesScript(validityDateString);

		// create CAAML
		if (GlobalVariables.isCreateCaaml())
			createCaaml(bulletins, validityDateString, publicationTimeString);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			Thread createMapsThread = createMaps(bulletins, validityDateString, publicationTimeString);
			createMapsThread.start();
			try {
				createMapsThread.join();

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
						logger.error(key + " thread interrupted: " + e.getMessage());
						e.printStackTrace();
					}
				}

				// send emails
				if (GlobalVariables.isSendEmails()) {
					Thread sendEmailsThread = sendEmails(bulletins, GlobalVariables.regionsEuregio, false);
					sendEmailsThread.start();
				}

				// publish on social media
				if (GlobalVariables.isPublishToSocialMedia()) {
					Thread triggerMessengerpeopleThread = triggerMessengerpeople(bulletins,
							GlobalVariables.regionsEuregio, false);
					triggerMessengerpeopleThread.start();
				}

			} catch (InterruptedException e) {
				logger.error("Map production interrupted: " + e.getMessage());
				e.printStackTrace();
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

		AlbinaUtil.runDeleteFilesScript(validityDateString);

		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runDeleteLatestFilesScript(validityDateString);

		// create CAAML
		if (GlobalVariables.isCreateCaaml())
			createCaaml(bulletins, validityDateString, publicationTimeString);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			Thread createMapsThread = createMaps(bulletins, validityDateString, publicationTimeString);
			createMapsThread.start();
			try {
				createMapsThread.join();

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
						logger.error(key + " thread interrupted: " + e.getMessage());
						e.printStackTrace();
					}
				}

				// send emails to regions
				if (GlobalVariables.isSendEmails()) {
					Thread sendEmailsThread = sendEmails(bulletins, regions, true);
					sendEmailsThread.start();
				}

				// publish on social media
				if (GlobalVariables.isPublishToSocialMedia()) {
					Thread triggerMessengerpeopleThread = triggerMessengerpeople(bulletins, regions, true);
					triggerMessengerpeopleThread.start();
				}

			} catch (InterruptedException e) {
				logger.error("Map production interrupted: " + e.getMessage());
				e.printStackTrace();
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

		AlbinaUtil.runDeleteFilesScript(validityDateString);

		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runDeleteLatestFilesScript(validityDateString);

		// create CAAML
		if (GlobalVariables.isCreateCaaml())
			createCaaml(bulletins, validityDateString, publicationTimeString);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			Thread createMapsThread = createMaps(bulletins, validityDateString, publicationTimeString);
			createMapsThread.start();
			try {
				createMapsThread.join();

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
						logger.error(key + " thread interrupted: " + e.getMessage());
						e.printStackTrace();
					}
				}

			} catch (InterruptedException e) {
				logger.error("Map production interrupted: " + e.getMessage());
				e.printStackTrace();
			}
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
			List<AvalancheBulletin> publishedBulletins, DateTime startDate, String region, User user,
			DateTime publicationDate) {
		new Thread(new Runnable() {
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
				try {
					String avalancheReportId = AvalancheReportController.getInstance().publishReport(publishedBulletins,
							startDate, region, user, publicationDate);
					avalancheReportIds.add(avalancheReportId);
				} catch (AlbinaException e) {
					logger.warn("Error updating bulletins - " + e.getMessage());
				}
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
			DateTime startDate, String region, User user) {
		new Thread(new Runnable() {
			public void run() {
				List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
				for (AvalancheBulletin avalancheBulletin : allBulletins) {
					if (avalancheBulletin.getPublishedRegions() != null
							&& !avalancheBulletin.getPublishedRegions().isEmpty())
						result.add(avalancheBulletin);
				}
				if (result != null && !result.isEmpty())
					PublicationController.getInstance().change(result);

				try {
					List<String> avalancheReportIds = new ArrayList<String>();
					String avalancheReportId = AvalancheReportController.getInstance().changeReport(publishedBulletins,
							startDate, region, user);
					avalancheReportIds.add(avalancheReportId);
				} catch (AlbinaException e) {
					logger.warn("Error changing bulletins - " + e.getMessage());
				}
			}
		}).start();
	}

	/**
	 * Trigger the creation of the CAAML (XML) files.
	 * 
	 * @param bulletins
	 *            the bulletins contained in the CAAML file
	 */
	public void createCaaml(List<AvalancheBulletin> bulletins, String validityDateString,
			String publicationTimeString) {
		try {
			logger.info("CAAML production started");
			XmlUtil.createCaamlFiles(bulletins, validityDateString, publicationTimeString);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runCopyLatestXmlsScript(validityDateString);
			logger.info("CAAML production finished");
		} catch (TransformerException | IOException e) {
			logger.error("Error producing CAAML: " + e.getMessage());
			e.printStackTrace();
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
	public Thread createMaps(List<AvalancheBulletin> bulletins, String validityDateString,
			String publicationTimeString) {
		return new Thread(new Runnable() {
			public void run() {
				logger.info("Map production started");
				MapUtil.createDangerRatingMaps(bulletins);
				if (GlobalVariables.isMapProductionUrlUnivie())
					AlbinaUtil.runCopyMapsUnivieScript(validityDateString, publicationTimeString);
				AlbinaUtil.runCopyMapsScript(validityDateString, publicationTimeString);
				if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
					AlbinaUtil.runCopyLatestMapsScript(validityDateString);
				logger.info("Map production finished");
			}
		});
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
			public void run() {
				try {
					logger.info("PDF production started");
					PdfUtil.getInstance().createOverviewPdfs(bulletins, validityDateString, publicationTimeString);
					for (String region : GlobalVariables.regionsEuregio)
						PdfUtil.getInstance().createRegionPdfs(bulletins, region, validityDateString,
								publicationTimeString);
					AlbinaUtil.runCopyPdfsScript(validityDateString, publicationTimeString);
					if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
						AlbinaUtil.runCopyLatestPdfsScript(validityDateString);
				} catch (IOException | URISyntaxException e) {
					logger.error("Error creating pdfs:" + e.getMessage());
					e.printStackTrace();
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
			public void run() {
				try {
					logger.info("Simple HTML production started");
					if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
						AlbinaUtil.runDeleteLatestHtmlsScript();
					SimpleHtmlUtil.getInstance().createOverviewSimpleHtml(bulletins);
					for (String region : GlobalVariables.regionsEuregio)
						SimpleHtmlUtil.getInstance().createRegionSimpleHtml(bulletins, region);
				} catch (IOException | URISyntaxException e) {
					logger.error("Error creating simple HTML:" + e.getMessage());
					e.printStackTrace();
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
			public void run() {
				try {
					logger.info("Static widget production started");
					StaticWidgetUtil.getInstance().createStaticWidgets(bulletins, validityDateString,
							publicationTimeString);
					AlbinaUtil.runCopyPngsScript(validityDateString, publicationTimeString);
					if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
						AlbinaUtil.runCopyLatestPngsScript(validityDateString);
				} catch (IOException | URISyntaxException e) {
					logger.error("Error creating static widgets:" + e.getMessage());
					e.printStackTrace();
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
	public Thread sendEmails(List<AvalancheBulletin> bulletins, List<String> regions, boolean update) {
		return new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("Email production started");
					EmailUtil.getInstance().sendBulletinEmails(bulletins, regions, update);
				} catch (IOException | URISyntaxException e) {
					logger.error("Error preparing emails:" + e.getMessage());
					e.printStackTrace();
				} finally {
					logger.info("Email production finished");
				}
			}
		});
	}

	/**
	 * Trigger the sending of the messages via messengerpeople.
	 * 
	 * @param bulletins
	 *            the bulletins contained in the messages
	 */
	public Thread triggerMessengerpeople(List<AvalancheBulletin> bulletins, List<String> regions, boolean update) {
		return new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("Messengerpeople production started");
					MessengerPeopleUtil.getInstance().sendBulletinNewsletters(bulletins, regions, update);
				} catch (IOException | URISyntaxException e) {
					logger.error("Error preparing messengerpeople:" + e.getMessage());
					e.printStackTrace();
				} finally {
					logger.info("Messengerpeople production finished");
				}
			}
		});
	}
}
