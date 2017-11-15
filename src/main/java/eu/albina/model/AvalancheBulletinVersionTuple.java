package eu.albina.model;

import java.util.List;

public final class AvalancheBulletinVersionTuple {

	public final List<AvalancheBulletin> bulletins;
	public final int version;

	public AvalancheBulletinVersionTuple(int version, List<AvalancheBulletin> bulletins) {
		this.version = version;
		this.bulletins = bulletins;
	}
}
