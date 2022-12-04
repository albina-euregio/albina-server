package org.caaml.v6;

import java.util.List;

/**
 * JSON schema for EAWS avalanche bulletin collection following the CAAMLv6 schema
 */
public class AvalancheBulletins {
    private List<AvalancheBulletin> bulletins;
    private Object customData;
    private MetaData metaData;

	public AvalancheBulletins() {
	}

	public AvalancheBulletins(List<AvalancheBulletin> bulletins) {
		this.bulletins = bulletins;
	}

	public List<AvalancheBulletin> getBulletins() { return bulletins; }
    public void setBulletins(List<AvalancheBulletin> value) { this.bulletins = value; }

    public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }
}
