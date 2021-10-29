package eu.albina.map;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;

enum DaytimeDependency {
	/**
	 * full day
	 */
	fd, am, pm;

	static DaytimeDependency of(boolean isAfternoon, boolean hasDaytimeDependency) {
		return !hasDaytimeDependency ? fd : isAfternoon ? pm : am;
	}

	AvalancheBulletinDaytimeDescription getBulletinDaytimeDescription(AvalancheBulletin bulletin) {
		return bulletin.isHasDaytimeDependency() && pm.equals(this)
				? bulletin.getAfternoon()
				: bulletin.getForenoon();
	}
}
