package eu.albina.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.util.EmailUtil;
import eu.albina.util.GlobalVariables;
import eu.albina.util.MapUtil;
import eu.albina.util.PdfUtil;

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
		if (GlobalVariables.publishAt5PM)
			publish(avalancheReportIds, bulletins);
	}

	private void publish(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		// create maps
		if (GlobalVariables.createMaps)
			createMaps(avalancheReportIds, bulletins);

		// create pdfs
		if (GlobalVariables.createPdf)
			createPdf(avalancheReportIds, bulletins);

		// send emails
		if (GlobalVariables.sendEmails)
			sendEmails(avalancheReportIds, bulletins, GlobalVariables.regions);

		// publish on social media
		if (GlobalVariables.publishToSocialMedia) {

			// TODO publish on social media

		}
	}

	public void updateAutomatically(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins,
			List<String> regions) throws MessagingException {
		if (GlobalVariables.publishAt8AM)
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

		// create maps
		if (GlobalVariables.createMaps)
			createMaps(avalancheReportIds, bulletins);

		// create pdf
		if (GlobalVariables.createPdf)
			createPdf(avalancheReportIds, bulletins);

		// send emails to regions
		if (GlobalVariables.sendEmails)
			sendEmails(avalancheReportIds, bulletins, regions);

		// publish on social media
		if (GlobalVariables.publishToSocialMedia) {

			// TODO publish on social media only for updated regions

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
		// create maps
		if (GlobalVariables.createMaps)
			createMaps(avalancheReportIds, bulletins);

		// create pdfs
		if (GlobalVariables.createPdf)
			createPdf(avalancheReportIds, bulletins);
	}

	private void createMaps(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		new Thread(new Runnable() {
			public void run() {
				logger.info("Map production started");
				MapUtil.createDangerRatingMaps(bulletins);
				AvalancheReportController.getInstance().setAvalancheReportMapFlag(avalancheReportIds);
				logger.info("Map production finished");
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

	private void createPdf(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		new Thread(new Runnable() {
			public void run() {
				try {
					logger.info("PDF production started");
					PdfUtil.getInstance().createOverviewPdfs(bulletins);
					PdfUtil.getInstance().createRegionPdfs(bulletins);
					AvalancheReportController.getInstance().setAvalancheReportPdfFlag(avalancheReportIds);
				} catch (IOException e) {
					logger.error("Error creating pdfs:" + e.getMessage());
					e.printStackTrace();
				} catch (URISyntaxException e) {
					logger.error("Error creating pdfs:" + e.getMessage());
					e.printStackTrace();
				} finally {
					logger.info("Map production finished");
				}
			}
		}).start();
	}
}
