// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Singleton
public class PublicationJobs {

	@Inject
	PublicationJob publicationJob;

	@Scheduled(cron = "0 0 17 * * ?")
	@Transactional
	public void triggerPublication() {
		publicationJob.execute(PublicationStrategy.publishAt5PM());
	}

	@Scheduled(cron = "0 0 8 * * ?")
	@Transactional
	public void triggerUpdate() {
		publicationJob.execute(PublicationStrategy.updateAt8AM());
	}
}
