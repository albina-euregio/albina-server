package eu.albina.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

			List<String> changedRegions = new ArrayList<String>();
			if (GlobalVariables.isPublishBulletinsTyrol()
					&& AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeTyrol))
				changedRegions.add(GlobalVariables.codeTyrol);
			if (GlobalVariables.isPublishBulletinsSouthTyrol()
					&& AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeSouthTyrol))
				changedRegions.add(GlobalVariables.codeSouthTyrol);
			if (GlobalVariables.isPublishBulletinsTrentino()
					&& AlbinaUtil.hasBulletinChanged(startDate, GlobalVariables.codeTrentino))
				changedRegions.add(GlobalVariables.codeTrentino);

			DateTime publicationDate = new DateTime();

			if (!changedRegions.isEmpty()) {
				Map<String, AvalancheBulletin> publishedBulletins = AvalancheBulletinController.getInstance()
						.publishBulletins(startDate, endDate, changedRegions, publicationDate, user);
				List<String> avalancheReportIds = AvalancheReportController.getInstance()
						.publishReport(publishedBulletins.values(), startDate, changedRegions, user, publicationDate);

				if (publishedBulletins.values() != null && !publishedBulletins.values().isEmpty()) {
					List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
					for (AvalancheBulletin avalancheBulletin : publishedBulletins.values()) {
						if (avalancheBulletin.getPublishedRegions() != null
								&& !avalancheBulletin.getPublishedRegions().isEmpty())
							result.add(avalancheBulletin);
					}
					if (result != null && !result.isEmpty())
						PublicationController.getInstance().updateAutomatically(avalancheReportIds, result,
								changedRegions);
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