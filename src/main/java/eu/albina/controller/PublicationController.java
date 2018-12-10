package eu.albina.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.EmailUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.MapUtil;
import eu.albina.util.MessengerPeopleUtil;
import eu.albina.util.PdfUtil;
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

		AlbinaUtil.runDeleteFilesScript(AlbinaUtil.getValidityDate(bulletins));

		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runDeleteLatestFilesScript(AlbinaUtil.getValidityDate(bulletins));

		// create CAAML
		if (GlobalVariables.isCreateCaaml())
			createCaaml(avalancheReportIds, bulletins);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			Thread createMapsThread = createMaps(avalancheReportIds, bulletins);
			createMapsThread.start();
			try {
				createMapsThread.join();

				// create pdfs
				if (GlobalVariables.isCreatePdf())
					createPdf(avalancheReportIds, bulletins);

				// create static widgets
				if (GlobalVariables.isCreateStaticWidget())
					createStaticWidgets(avalancheReportIds, bulletins);

				// send emails
				if (GlobalVariables.isSendEmails())
					sendEmails(avalancheReportIds, bulletins, GlobalVariables.regionsEuregio);

				// publish on social media
				if (GlobalVariables.isPublishToSocialMedia())
					triggerMessengerpeople(avalancheReportIds, bulletins, GlobalVariables.regionsEuregio);

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
			List<String> regions) throws MessagingException {
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
	public void update(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins, List<String> regions)
			throws MessagingException {

		AlbinaUtil.runDeleteFilesScript(AlbinaUtil.getValidityDate(bulletins));

		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runDeleteLatestFilesScript(AlbinaUtil.getValidityDate(bulletins));

		// create CAAML
		if (GlobalVariables.isCreateCaaml())
			createCaaml(avalancheReportIds, bulletins);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			Thread createMapsThread = createMaps(avalancheReportIds, bulletins);
			createMapsThread.start();
			try {
				createMapsThread.join();

				// create pdf
				if (GlobalVariables.isCreatePdf())
					createPdf(avalancheReportIds, bulletins);

				// create static widgets
				if (GlobalVariables.isCreateStaticWidget())
					createStaticWidgets(avalancheReportIds, bulletins);

				// send emails to regions
				if (GlobalVariables.isSendEmails())
					sendEmails(avalancheReportIds, bulletins, regions);

				// publish on social media
				if (GlobalVariables.isPublishToSocialMedia())
					triggerMessengerpeople(avalancheReportIds, bulletins, regions);

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

		AlbinaUtil.runDeleteFilesScript(AlbinaUtil.getValidityDate(bulletins));

		if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
			AlbinaUtil.runDeleteLatestFilesScript(AlbinaUtil.getValidityDate(bulletins));

		// create CAAML
		if (GlobalVariables.isCreateCaaml())
			createCaaml(avalancheReportIds, bulletins);

		// create maps
		if (GlobalVariables.isCreateMaps()) {
			// Thread createMapsThread = createMaps(avalancheReportIds, bulletins);
			// createMapsThread.start();
			// try {
			// createMapsThread.join();

			// create pdfs
			if (GlobalVariables.isCreatePdf())
				createPdf(avalancheReportIds, bulletins);

			// create static widgets
			if (GlobalVariables.isCreateStaticWidget())
				createStaticWidgets(avalancheReportIds, bulletins);

			// } catch (InterruptedException e) {
			// logger.error("Map production interrupted: " + e.getMessage());
			// e.printStackTrace();
			// }
		}
	}

	public void startUpdateThread(DateTime startDate, DateTime endDate, List<String> regions,
			List<String> avalancheReportIds) {
		new Thread(new Runnable() {
			public void run() {
				try {
					List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance()
							.getBulletins(startDate, endDate, GlobalVariables.regionsEuregio);

					// TODO what if there is no bulletin? Delete all products?

					List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
					for (AvalancheBulletin avalancheBulletin : bulletins) {
						if (avalancheBulletin.getPublishedRegions() != null
								&& !avalancheBulletin.getPublishedRegions().isEmpty())
							result.add(avalancheBulletin);
					}
					if (result != null && !result.isEmpty())
						PublicationController.getInstance().update(avalancheReportIds, result, regions);

				} catch (AlbinaException e) {
					logger.warn("Error loading bulletins - " + e.getMessage());
					e.printStackTrace();
				} catch (MessagingException e) {
					logger.warn("Error sending emails - " + e.getMessage());
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void startChangeThread(DateTime startDate, DateTime endDate, List<String> avalancheReportIds) {
		new Thread(new Runnable() {
			public void run() {
				try {
					List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance()
							.getBulletins(startDate, endDate, GlobalVariables.regionsEuregio);

					// TODO what if there is no bulletin? Delete all products?

					List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
					for (AvalancheBulletin avalancheBulletin : bulletins) {
						if (avalancheBulletin.getPublishedRegions() != null
								&& !avalancheBulletin.getPublishedRegions().isEmpty())
							result.add(avalancheBulletin);
					}
					if (result != null && !result.isEmpty())
						PublicationController.getInstance().change(avalancheReportIds, result);

				} catch (AlbinaException e) {
					logger.warn("Error loading bulletins - " + e.getMessage());
					e.printStackTrace();
				}
			}
		}).start();
	}

	// LANG
	private void createCaaml(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		try {
			logger.info("CAAML production started");
			AvalancheReportController.getInstance().setAvalancheReportCaamlFlag(avalancheReportIds);
			XmlUtil.createCaamlFiles(bulletins);
			if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
				AlbinaUtil.runCopyLatestXmlsScript(AlbinaUtil.getValidityDate(bulletins));
			logger.info("CAAML production finished");
		} catch (TransformerException e) {
			logger.error("Error producing CAAML: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error producing CAAML: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private Thread createMaps(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		return new Thread(new Runnable() {
			public void run() {
				logger.info("Map production started");
				MapUtil.createDangerRatingMaps(bulletins);
				if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
					AlbinaUtil.runCopyLatestMapsScript(AlbinaUtil.getValidityDate(bulletins));
				AvalancheReportController.getInstance().setAvalancheReportMapFlag(avalancheReportIds);
				logger.info("Map production finished");
			}
		});
	}

	private void createPdf(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
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
						AlbinaUtil.runCopyLatestPdfsScript(AlbinaUtil.getValidityDate(bulletins));
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

	private void createStaticWidgets(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("Static widget production started");
					StaticWidgetUtil.getInstance().createStaticWidgets(bulletins);
					if (AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins)))
						AlbinaUtil.runCopyLatestPngsScript(AlbinaUtil.getValidityDate(bulletins));
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

	private void sendEmails(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins, List<String> regions) {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("Email production started");
					EmailUtil.getInstance().sendBulletinEmails(bulletins, regions);
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

	private void triggerMessengerpeople(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins,
			List<String> regions) {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("Messengerpeople production started");
					MessengerPeopleUtil.getInstance().sendBulletinNewsletters(bulletins, regions);
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
