// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.PublicationController;
import eu.albina.controller.RegionRepository;
import eu.albina.model.ServerInstance;

/**
 * A job handling all the tasks and logic necessary to
 * manually change the bulletins without sending notifications.
 *
 * @author Norbert Lanzanasto
 *
 */
public class ChangeJob extends PublicationJob {

	public ChangeJob(PublicationController publicationController, AvalancheReportController avalancheReportController, AvalancheBulletinController avalancheBulletinController, RegionRepository regionRepository, ServerInstance serverInstance) {
		super(publicationController, avalancheReportController, avalancheBulletinController, regionRepository, serverInstance);
	}

	@Override
	protected boolean isEnabled(ServerInstance serverInstance) {
		return true;
	}

	@Override
	protected boolean isChange() {
		return true;
	}

}
