package eu.albina.controller;

import java.util.List;

import javax.mail.MessagingException;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.AlbinaUtil;
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
	public void publish(List<AvalancheBulletin> bulletins) throws MessagingException {

		// TODO implement

		// create maps
		if (AlbinaUtil.createMaps) {
			MapUtil.createDangerRatingMaps(bulletins);
		}

		// create pdfs
		if (AlbinaUtil.createPdf) {
			PdfUtil.createOverviewPdf(bulletins);
			PdfUtil.createRegionPdfs(bulletins);
		}

		// send emails
		if (AlbinaUtil.sendEmails) {
			EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.de, GlobalVariables.codeTyrol);
			EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.it, GlobalVariables.codeTyrol);
			EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.en, GlobalVariables.codeTyrol);
			EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.de, GlobalVariables.codeSouthTyrol);
			EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.it, GlobalVariables.codeSouthTyrol);
			EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.en, GlobalVariables.codeSouthTyrol);
			EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.de, GlobalVariables.codeTrentino);
			EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.it, GlobalVariables.codeTrentino);
			EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.en, GlobalVariables.codeTrentino);
		}

		// publish on social media
		if (AlbinaUtil.publishToSocialMedia) {

			// TODO publish on social media

		}
	}

	/**
	 * Triggers all tasks that have to take place after an update has been published
	 * (this can be at any time, triggered by one province).
	 * 
	 * @param bulletins
	 *            The bulletins that were updated.
	 * @param region
	 *            The region that was updated.
	 * @throws MessagingException
	 */
	public void update(List<AvalancheBulletin> bulletins, List<String> regions) throws MessagingException {

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

		// send emails to regions
		if (AlbinaUtil.sendEmails) {
			for (String region : regions) {
				EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.de, region);
				EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.it, region);
				EmailUtil.getInstance().sendEmail(bulletins, LanguageCode.en, region);
			}
		}

		// publish on social media
		if (AlbinaUtil.publishToSocialMedia) {

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
	public void change(List<AvalancheBulletin> bulletins) {

		// TODO implement

		// create maps
		if (AlbinaUtil.createMaps) {
			MapUtil.createDangerRatingMaps(bulletins);
		}

		// create pdfs
		if (AlbinaUtil.createPdf) {
			PdfUtil.createOverviewPdf(bulletins);
			PdfUtil.createRegionPdfs(bulletins);
		}
	}
}
