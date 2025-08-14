// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.jobs.BlogJob;
import eu.albina.jobs.PublicationJob;
import eu.albina.jobs.TmpDeletionJob;
import eu.albina.jobs.UpdateJob;

public class SchedulerUtil {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerUtil.class);

	private static SchedulerUtil instance = null;

	private Scheduler scheduler;

	public static SchedulerUtil getInstance() {
		if (instance == null) {
			instance = new SchedulerUtil();
		}
		return instance;
	}

	public SchedulerUtil() {
	}

	public void setUp() {
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
		} catch (SchedulerException se) {
			logger.warn("Scheduler could not be set up", se);
		}
	}

	public void start() {
		try {
			scheduler.start();

			// start publication job (5PM)
			JobDetail publicationJob = newJob(PublicationJob.class).withIdentity("jobPublication", "groupAlbina")
					.build();
			Trigger publicationTrigger = newTrigger().withIdentity("triggerPublication", "groupAlbina").startNow()
					.withSchedule(cronSchedule("0 0 17 * * ?")).build();
			scheduler.scheduleJob(publicationJob, publicationTrigger);

			// start update job (8AM)
			JobDetail updateJob = newJob(UpdateJob.class).withIdentity("jobUpdate", "groupAlbina").build();
			Trigger updateTrigger = newTrigger().withIdentity("triggerUpdate", "groupAlbina").startNow()
					.withSchedule(cronSchedule("0 0 8 * * ?")).build();
			scheduler.scheduleJob(updateJob, updateTrigger);

			// start blog job (every 10 min)
			JobDetail blogJob = newJob(BlogJob.class).withIdentity("jobBlog", "groupAlbina").build();
			Trigger blogTrigger = newTrigger().withIdentity("triggerBlog", "groupAlbina").startNow()
					.withSchedule(cronSchedule("0 0/10 * * * ?")).build();
			scheduler.scheduleJob(blogJob, blogTrigger);

			// start tmp deletion job (3AM)
			JobDetail tmpDeletionJob = newJob(TmpDeletionJob.class).withIdentity("tmpDeletionJob", "groupAlbina").build();
			Trigger tmpDeletionTrigger = newTrigger().withIdentity("triggerTmpDeletion", "groupAlbina").startNow()
					.withSchedule(cronSchedule("0 0 3 * * ?")).build();
			scheduler.scheduleJob(tmpDeletionJob, tmpDeletionTrigger);

		} catch (SchedulerException e) {
			logger.error("Scheduler could not be started", e);
		}
	}

	public void stop() {
		this.shutDown();
	}

	public void shutDown() {
		if (scheduler != null)
			try {
				scheduler.shutdown();
			} catch (SchedulerException e) {
				logger.warn("Scheduler could not be shut down", e);
			}
	}
}
