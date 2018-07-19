package eu.albina.controller;

import java.util.List;

import javax.mail.MessagingException;

import eu.albina.model.AvalancheBulletin;
import eu.albina.thread.EmailThread;
import eu.albina.thread.MapThread;
import eu.albina.thread.PdfThread;
import eu.albina.util.GlobalVariables;

/**
 * Controller for avalanche reports.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class PublicationController {

	// private static Logger logger =
	// LoggerFactory.getLogger(PublicationController.class);

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
		if (GlobalVariables.createMaps) {
			MapThread thread = new MapThread(avalancheReportIds, bulletins);
			thread.run();
		}

		// create pdfs
		if (GlobalVariables.createPdf) {
			PdfThread thread = new PdfThread(avalancheReportIds, bulletins);
			thread.run();
		}

		// send emails
		if (GlobalVariables.sendEmails) {
			EmailThread thread = new EmailThread(avalancheReportIds, bulletins, GlobalVariables.regions);
			thread.run();
		}

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
		if (GlobalVariables.createMaps) {
			MapThread thread = new MapThread(avalancheReportIds, bulletins);
			thread.run();
		}

		// create pdf
		if (GlobalVariables.createPdf) {
			PdfThread thread = new PdfThread(avalancheReportIds, bulletins);
			thread.run();
		}

		// send emails to regions
		if (GlobalVariables.sendEmails) {
			EmailThread thread = new EmailThread(avalancheReportIds, bulletins, regions);
			thread.run();
		}

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

		// TODO implement

		// create maps
		if (GlobalVariables.createMaps) {
			MapThread thread = new MapThread(avalancheReportIds, bulletins);
			thread.run();
		}

		// create pdfs
		if (GlobalVariables.createPdf) {
			PdfThread thread = new PdfThread(avalancheReportIds, bulletins);
			thread.run();
		}
	}
}
