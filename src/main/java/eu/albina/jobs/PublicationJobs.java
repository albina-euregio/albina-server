// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.publication.PublicationController;
import eu.albina.controller.RegionRepository;
import eu.albina.controller.ServerInstanceRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Singleton
public class PublicationJobs {

	@Inject
	PublicationController publicationController;

	@Inject
	AvalancheReportController avalancheReportController;

	@Inject
	AvalancheBulletinController avalancheBulletinController;

	@Inject
	RegionRepository regionRepository;

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	private ServerInstanceRepository serverInstanceRepository;

	@Scheduled(cron = "0 0 17 * * ?")
	@Transactional
	public void triggerPublication() {
		new PublicationJob(publicationController, avalancheReportController, avalancheBulletinController, regionRepository, serverInstanceRepository.getLocalServerInstance(), entityManager).execute();
	}

	@Scheduled(cron = "0 0 8 * * ?")
	@Transactional
	public void triggerUpdate() {
		new UpdateJob(publicationController, avalancheReportController, avalancheBulletinController, regionRepository, serverInstanceRepository.getLocalServerInstance(), entityManager).execute();
	}
}
