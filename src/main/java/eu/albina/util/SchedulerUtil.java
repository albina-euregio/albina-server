// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.RegionController;
import eu.albina.controller.ServerInstanceController;
import eu.albina.jobs.BlogJob;
import eu.albina.jobs.HealthCheckJob;
import eu.albina.jobs.PublicationJob;
import eu.albina.jobs.TmpDeletionJob;
import eu.albina.jobs.UpdateJob;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SchedulerUtil {

	@Inject
	PublicationController publicationController;

	@Inject
	AvalancheReportController avalancheReportController;

	@Inject
	AvalancheBulletinController avalancheBulletinController;

	@Inject
	RegionController regionController;

	@Inject
	private ServerInstanceController serverInstanceController;

	@Scheduled(cron = "0 0 17 * * ?")
	public void triggerPublication() {
		new PublicationJob(publicationController, avalancheReportController, avalancheBulletinController, regionController, serverInstanceController.getLocalServerInstance()).execute();
	}

	@Scheduled(cron = "0 0 8 * * ?")
	public void triggerUpdate() {
		new UpdateJob(publicationController, avalancheReportController, avalancheBulletinController, regionController, serverInstanceController.getLocalServerInstance()).execute();
	}

	@Scheduled(cron = "0 0/10 * * * ?")
	public void triggerBlog() {
		new BlogJob().execute();
	}

	@Scheduled(cron = "0 0 3 * * ?")
	public void triggerTmpDeletion() {
		new TmpDeletionJob().execute();
	}

	@Scheduled(cron = "0 0 4 * * ?")
	public void triggerHealthCheck() {
		new HealthCheckJob().execute();
	}
}
