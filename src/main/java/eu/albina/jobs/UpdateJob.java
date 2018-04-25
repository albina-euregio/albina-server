package eu.albina.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.UserController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.User;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.GlobalVariables;

public class UpdateJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(UpdateJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("Update job triggered!");

		try {
			User user = UserController.getInstance().getUser(GlobalVariables.avalancheReportUsername);

			DateTime startDate = new DateTime().withTimeAtStartOfDay();
			DateTime endDate = startDate.plusDays(1).withTimeAtStartOfDay();

			List<String> regions = new ArrayList<String>();
			if (AlbinaUtil.publishBulletinsTyrol && AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeTyrol))
				regions.add(GlobalVariables.codeTyrol);
			if (AlbinaUtil.publishBulletinsSouthTyrol
					&& AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeSouthTyrol))
				regions.add(GlobalVariables.codeSouthTyrol);
			if (AlbinaUtil.publishBulletinsTrentino
					&& AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeTrentino))
				regions.add(GlobalVariables.codeTrentino);

			DateTime publicationDate = new DateTime();

			if (!regions.isEmpty()) {
				AvalancheBulletinController.getInstance().publishBulletins(startDate, endDate, regions,
						publicationDate);
				AvalancheReportController.getInstance().publishReport(startDate, regions, user, publicationDate);

				try {
					List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance()
							.getBulletins(startDate, endDate, regions);
					PublicationController.getInstance().update(bulletins, regions);
				} catch (AlbinaException e) {
					logger.warn("Error loading bulletins - " + e.getMessage());
					throw new AlbinaException(e.getMessage());
				} catch (MessagingException e) {
					logger.warn("Error sending emails - " + e.getMessage());
					throw new AlbinaException(e.getMessage());
				}
			} else {
				logger.info("No bulletins to update.");
			}
		} catch (AlbinaException e) {
			logger.error("Error publishing bulletins - " + e.getMessage());
			e.printStackTrace();
		}
	}
}