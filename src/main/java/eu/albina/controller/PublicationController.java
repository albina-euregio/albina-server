package eu.albina.controller;

import java.util.List;

import eu.albina.model.AvalancheBulletin;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.MapUtil;
import eu.albina.util.PdfUtil;

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
	 * been published. This happens at 17:00 PM and 07:30 AM (if needed).
	 * 
	 * @param bulletins
	 *            The bulletins that were published.
	 */
	public void publish(List<AvalancheBulletin> bulletins) {
		// TODO implement
		// create maps
		if (AlbinaUtil.createMaps) {
			MapUtil.createDangerRatingMaps(bulletins);
		}

		// create pdf
		if (AlbinaUtil.createPdf) {
			PdfUtil.createOverviewPdf(bulletins);
			PdfUtil.createRegionPdfs(bulletins);
		}

		// send emails
		// publish to social media
	}

	/**
	 * Triggers all tasks that have to take place after an update has been published
	 * (this can be at any time, triggered by one province).
	 * 
	 * @param bulletins
	 *            The bulletins that were updated.
	 */
	public void update(List<AvalancheBulletin> bulletins) {
		// TODO implement
		// create maps
		// create pdf
		// send emails to region
		// publish to social media for region
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
		// TODO implement
		// create maps
		// create pdf
	}
}
