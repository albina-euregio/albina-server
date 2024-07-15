/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.jobs;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import eu.albina.controller.RegionController;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;

import eu.albina.util.AlbinaUtil;

/**
 * A {@code org.quartz.Job} handling all the tasks and logic necessary to
 * automatically update the bulletins at 8AM or triggered manually.
 *
 * @author Norbert Lanzanasto
 *
 */
public class UpdateJob extends PublicationJob {

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
