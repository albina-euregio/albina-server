// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import eu.albina.model.ServerInstance;

/**
 * A {@code org.quartz.Job} handling all the tasks and logic necessary to
 * manually change the bulletins without sending notifications.
 *
 * @author Norbert Lanzanasto
 *
 */
public class ChangeJob extends PublicationJob {

	@Override
	protected boolean isEnabled(ServerInstance serverInstance) {
		return true;
	}

	@Override
	protected boolean isChange() {
		return true;
	}

}
