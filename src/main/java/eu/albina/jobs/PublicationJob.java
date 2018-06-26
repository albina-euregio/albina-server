package eu.albina.jobs;

import java.util.ArrayList;
import java.util.List;

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

public class PublicationJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(PublicationJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("Publication job triggered!");

		List<String> regions = new ArrayList<String>();
		if (AlbinaUtil.publishBulletinsTyrol)
			regions.add(GlobalVariables.codeTyrol);
		if (AlbinaUtil.publishBulletinsSouthTyrol)
			regions.add(GlobalVariables.codeSouthTyrol);
		if (AlbinaUtil.publishBulletinsTrentino)
			regions.add(GlobalVariables.codeTrentino);

		if (!regions.isEmpty()) {
			try {
				User user = UserController.getInstance().getUser(GlobalVariables.avalancheReportUsername);

				DateTime startDate = new DateTime().plusDays(1).withTimeAtStartOfDay();
				DateTime endDate = startDate.plusDays(1).withTimeAtStartOfDay();

				DateTime publicationDate = new DateTime();

				AvalancheBulletinController.getInstance().publishBulletins(startDate, endDate, regions,
						publicationDate);
				AvalancheReportController.getInstance().publishReport(startDate, regions, user, publicationDate);

				try {
					List<AvalancheBulletin> bulletins = AvalancheBulletinController.getInstance()
							.getBulletins(startDate, endDate, regions);
					if (bulletins != null && !bulletins.isEmpty())
						PublicationController.getInstance().publish(bulletins);
				} catch (AlbinaException e) {
					logger.warn("Error loading bulletins - " + e.getMessage());
					throw new AlbinaException(e.getMessage());
				}
			} catch (AlbinaException e) {
				logger.error("Error publishing bulletins - " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			logger.info("No bulletins to publish.");
		}
	}
}