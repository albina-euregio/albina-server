// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import eu.albina.controller.PublicationController;
import eu.albina.controller.RegionController;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.util.AlbinaUtil;
import jakarta.inject.Singleton;

/**
 * A job handling all the tasks and logic necessary to
 * automatically update the bulletins at 8AM or triggered manually.
 *
 * @author Norbert Lanzanasto
 *
 */
public class UpdateJob extends PublicationJob {

	public UpdateJob(PublicationController publicationController) {
		super(publicationController);
	}

	@Override
	protected boolean isEnabled(ServerInstance serverInstance) {
		return serverInstance.isPublishAt8AM();
	}

	@Override
	protected boolean isChange() {
		return false;
	}

	@Override
	protected Instant getStartDate(Clock clock) {
		return ZonedDateTime.of(
			LocalDate.now(clock).minusDays(1),
			AlbinaUtil.validityStart(),
			clock.getZone()
		).toInstant();
	}

	@Override
	protected List<Region> getRegions() {
		return RegionController.getInstance().getPublishBulletinRegions();
	}

}
