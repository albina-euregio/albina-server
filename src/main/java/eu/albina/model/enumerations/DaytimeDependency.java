// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.enumerations;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;

public enum DaytimeDependency {
	/**
	 * full day
	 */
	fd, am, pm;

	public AvalancheBulletinDaytimeDescription getBulletinDaytimeDescription(AvalancheBulletin bulletin) {
		return bulletin.isHasDaytimeDependency() && pm.equals(this)
				? bulletin.getAfternoon()
				: bulletin.getForenoon();
	}
}
