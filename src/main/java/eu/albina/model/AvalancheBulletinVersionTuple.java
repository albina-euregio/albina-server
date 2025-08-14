// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.util.Collection;

public final class AvalancheBulletinVersionTuple {

	public final Collection<AvalancheBulletin> bulletins;
	public final int version;

	public AvalancheBulletinVersionTuple(int version, Collection<AvalancheBulletin> bulletins) {
		this.version = version;
		this.bulletins = bulletins;
	}
}
