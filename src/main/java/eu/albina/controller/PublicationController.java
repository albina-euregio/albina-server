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
import java.util.List;

import javax.mail.MessagingException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
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

	private PublicationController() {
	}

	public static PublicationController getInstance() {
		if (instance == null) {
			instance = new PublicationController();
		}
		return instance;
	}

	/**
	 * Trigger all tasks that have to take place after an avalanche bulletin has
	 * been published. This happens at 17:00 PM and 08:00 AM (if needed).
	 * 
	 * @param bulletins
	 *            The bulletins that were published.
	 * @throws MessagingException
	 */
	public void publishAutomatically(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		if (GlobalVariables.isPublishAt5PM())
			publish(avalancheReportIds, bulletins);
	}

	private void publish(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {

		Collections.sort(bulletins, new AvalancheBulletinSortByDangerRating());

		AlbinaUtil.runDeleteFilesScript(AlbinaUtil.getValidityDateString(bulletins));

		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runDeleteLatestFilesScript(AlbinaUtil.getValidityDateString(bulletins));

		// create CAAML
		if (GlobalVariables.isCreateCaaml())
			createCaaml(avalancheReportIds, bulletins);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			Thread createMapsThread = createMaps(avalancheReportIds, bulletins);
			createMapsThread.start();
			try {
				createMapsThread.join();

				// create HTML
				if (GlobalVariables.isCreateSimpleHtml())
					createSimpleHtml(avalancheReportIds, bulletins);

				// create pdfs
				if (GlobalVariables.isCreatePdf())
					createPdf(avalancheReportIds, bulletins);

				// create static widgets
				if (GlobalVariables.isCreateStaticWidget())
					createStaticWidgets(avalancheReportIds, bulletins);

				// send emails
				if (GlobalVariables.isSendEmails())
					sendEmails(avalancheReportIds, bulletins, GlobalVariables.regionsEuregio, false);

				// publish on social media
				if (GlobalVariables.isPublishToSocialMedia())
					triggerMessengerpeople(avalancheReportIds, bulletins, GlobalVariables.regionsEuregio, false);

			} catch (InterruptedException e) {
				logger.error("Map production interrupted: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Trigger all tasks that have to take place after an avalanche bulletin has
	 * been published. This happens at 17:00 PM and 08:00 AM (if needed).
	 * 
	 * @param bulletins
	 *            The bulletins that were published.
	 * @param regions
	 *            The regions that were updated.
	 * @throws MessagingException
	 */
	public void updateAutomatically(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins,
			List<String> regions) {
		if (GlobalVariables.isPublishAt8AM())
			update(avalancheReportIds, bulletins, regions);
	}

	/**
	 * Triggers all tasks that have to take place after an update has been published
	 * (this can be at any time, triggered by one province).
	 * 
	 * @param bulletins
	 *            The bulletins that were updated.
	 * @param reportId
	 * @param region
	 *            The region that was updated.
	 * @throws MessagingException
	 */
	public void update(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins, List<String> regions) {

		Collections.sort(bulletins, new AvalancheBulletinSortByDangerRating());

		AlbinaUtil.runDeleteFilesScript(AlbinaUtil.getValidityDateString(bulletins));

		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runDeleteLatestFilesScript(AlbinaUtil.getValidityDateString(bulletins));

		// create CAAML
		if (GlobalVariables.isCreateCaaml())
			createCaaml(avalancheReportIds, bulletins);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			Thread createMapsThread = createMaps(avalancheReportIds, bulletins);
			createMapsThread.start();
			try {
				createMapsThread.join();

				// create HTML
				if (GlobalVariables.isCreateSimpleHtml())
					createSimpleHtml(avalancheReportIds, bulletins);

				// create pdf
				if (GlobalVariables.isCreatePdf())
					createPdf(avalancheReportIds, bulletins);

				// create static widgets
				if (GlobalVariables.isCreateStaticWidget())
					createStaticWidgets(avalancheReportIds, bulletins);

				// send emails to regions
				if (GlobalVariables.isSendEmails())
					sendEmails(avalancheReportIds, bulletins, regions, true);

				// publish on social media
				if (GlobalVariables.isPublishToSocialMedia())
					triggerMessengerpeople(avalancheReportIds, bulletins, regions, true);

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
	public void change(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {

		Collections.sort(bulletins, new AvalancheBulletinSortByDangerRating());

		AlbinaUtil.runDeleteFilesScript(AlbinaUtil.getValidityDateString(bulletins));

		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runDeleteLatestFilesScript(AlbinaUtil.getValidityDateString(bulletins));

		// create CAAML
		if (GlobalVariables.isCreateCaaml())
			createCaaml(avalancheReportIds, bulletins);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			Thread createMapsThread = createMaps(avalancheReportIds, bulletins);
			createMapsThread.start();
			try {
				createMapsThread.join();

				// create HTML
				if (GlobalVariables.isCreateSimpleHtml())
					createSimpleHtml(avalancheReportIds, bulletins);

				// create pdfs
				if (GlobalVariables.isCreatePdf())
					createPdf(avalancheReportIds, bulletins);

				// create static widgets
				if (GlobalVariables.isCreateStaticWidget())
					createStaticWidgets(avalancheReportIds, bulletins);

			} catch (InterruptedException e) {
				logger.error("Map production interrupted: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void startUpdateThread(List<AvalancheBulletin> publishedBulletins, List<String> regions,
			List<String> avalancheReportIds) {
		new Thread(new Runnable() {
			public void run() {
				List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
				for (AvalancheBulletin avalancheBulletin : publishedBulletins) {
					if (avalancheBulletin.getPublishedRegions() != null
							&& !avalancheBulletin.getPublishedRegions().isEmpty())
						result.add(avalancheBulletin);
				}
				if (result != null && !result.isEmpty())
					PublicationController.getInstance().update(avalancheReportIds, result, regions);

			}
		}).start();
	}

	public void startChangeThread(List<AvalancheBulletin> publishedBulletins, List<String> avalancheReportIds) {
		new Thread(new Runnable() {
			public void run() {
				List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
				for (AvalancheBulletin avalancheBulletin : publishedBulletins) {
					if (avalancheBulletin.getPublishedRegions() != null
							&& !avalancheBulletin.getPublishedRegions().isEmpty())
						result.add(avalancheBulletin);
				}
				if (result != null && !result.isEmpty())
					PublicationController.getInstance().change(avalancheReportIds, result);

			}
		}).start();
	}

	// LANG
	public void createCaaml(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		try {
			logger.info("CAAML production started");
			AvalancheReportController.getInstance().setAvalancheReportCaamlFlag(avalancheReportIds);
			XmlUtil.createCaamlFiles(bulletins);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runCopyLatestXmlsScript(AlbinaUtil.getValidityDateString(bulletins));
			logger.info("CAAML production finished");
		} catch (TransformerException e) {
			logger.error("Error producing CAAML: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error producing CAAML: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Thread createMaps(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		return new Thread(new Runnable() {
			public void run() {
				logger.info("Map production started");
				MapUtil.createDangerRatingMaps(bulletins);
				if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
					AlbinaUtil.runCopyLatestMapsScript(AlbinaUtil.getValidityDateString(bulletins));
				AvalancheReportController.getInstance().setAvalancheReportMapFlag(avalancheReportIds);
				logger.info("Map production finished");
			}
		});
	}

	public void createPdf(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("PDF production started");
					boolean result = true;
					if (!PdfUtil.getInstance().createOverviewPdfs(bulletins))
						result = false;
					for (String region : GlobalVariables.regionsEuregio)
						if (!PdfUtil.getInstance().createRegionPdfs(bulletins, region))
							result = false;
					if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
						AlbinaUtil.runCopyLatestPdfsScript(AlbinaUtil.getValidityDateString(bulletins));
					if (result)
						AvalancheReportController.getInstance().setAvalancheReportPdfFlag(avalancheReportIds);
				} catch (IOException e) {
					logger.error("Error creating pdfs:" + e.getMessage());
					e.printStackTrace();
				} catch (URISyntaxException e) {
					logger.error("Error creating pdfs:" + e.getMessage());
					e.printStackTrace();
				} finally {
					logger.info("PDF production finished");
				}
			}
		}).start();
	}

	public void createSimpleHtml(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("Simple HTML production started");
					boolean result = true;
					if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
						AlbinaUtil.runDeleteLatestHtmlsScript();
					if (!SimpleHtmlUtil.getInstance().createOverviewSimpleHtml(bulletins))
						result = false;
					for (String region : GlobalVariables.regionsEuregio)
						if (!SimpleHtmlUtil.getInstance().createRegionSimpleHtml(bulletins, region))
							result = false;
					if (result)
						AvalancheReportController.getInstance().setAvalancheReportHtmlFlag(avalancheReportIds);
				} catch (IOException e) {
					logger.error("Error creating simple HTML:" + e.getMessage());
					e.printStackTrace();
				} catch (URISyntaxException e) {
					logger.error("Error creating simple HTML:" + e.getMessage());
					e.printStackTrace();
				} finally {
					logger.info("Simple HTML production finished");
				}
			}
		}).start();
	}

	public void createStaticWidgets(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("Static widget production started");
					StaticWidgetUtil.getInstance().createStaticWidgets(bulletins);
					if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
						AlbinaUtil.runCopyLatestPngsScript(AlbinaUtil.getValidityDateString(bulletins));
					AvalancheReportController.getInstance().setAvalancheReportStaticWidgetFlag(avalancheReportIds);
				} catch (IOException e) {
					logger.error("Error creating static widgets:" + e.getMessage());
					e.printStackTrace();
				} catch (URISyntaxException e) {
					logger.error("Error creating static widgets:" + e.getMessage());
					e.printStackTrace();
				} finally {
					logger.info("Static widget production finished");
				}
			}
		}).start();
	}

	public void sendEmails(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins, List<String> regions,
			boolean update) {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("Email production started");
					EmailUtil.getInstance().sendBulletinEmails(bulletins, regions, update);
					AvalancheReportController.getInstance().setAvalancheReportEmailFlag(avalancheReportIds);
				} catch (IOException e) {
					logger.error("Error preparing emails:" + e.getMessage());
					e.printStackTrace();
				} catch (URISyntaxException e) {
					logger.error("Error preparing emails:" + e.getMessage());
					e.printStackTrace();
				} finally {
					logger.info("Email production finished");
				}
			}
		}).start();
	}

	public void triggerMessengerpeople(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins,
			List<String> regions, boolean update) {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("Messengerpeople production started");
					MessengerPeopleUtil.getInstance().sendBulletinNewsletters(bulletins, regions, update);
					AvalancheReportController.getInstance().setAvalancheReportWhatsappFlag(avalancheReportIds);
					AvalancheReportController.getInstance().setAvalancheReportTelegramFlag(avalancheReportIds);
				} catch (IOException e) {
					logger.error("Error preparing messengerpeople:" + e.getMessage());
					e.printStackTrace();
				} catch (URISyntaxException e) {
					logger.error("Error preparing messengerpeople:" + e.getMessage());
					e.printStackTrace();
				} finally {
					logger.info("Messengerpeople production finished");
				}
			}
		}).start();
	}
}
