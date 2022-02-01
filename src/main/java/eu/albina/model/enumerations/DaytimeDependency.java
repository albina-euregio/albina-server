package eu.albina.model.enumerations;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.util.AlbinaUtil;

import java.util.EnumSet;
import java.util.List;

public enum DaytimeDependency {
	/**
	 * full day
	 */
	fd, am, pm;

	public static DaytimeDependency of(boolean isAfternoon, boolean hasDaytimeDependency) {
		return !hasDaytimeDependency ? fd : isAfternoon ? pm : am;
	}

	public static EnumSet<DaytimeDependency> of(List<AvalancheBulletin> bulletins) {
		return AlbinaUtil.hasDaytimeDependency(bulletins)
			? EnumSet.of(am, pm)
			: EnumSet.of(fd);
	}

	public AvalancheBulletinDaytimeDescription getBulletinDaytimeDescription(AvalancheBulletin bulletin) {
		return bulletin.isHasDaytimeDependency() && pm.equals(this)
				? bulletin.getAfternoon()
				: bulletin.getForenoon();
	}
}
