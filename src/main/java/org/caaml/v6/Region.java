package org.caaml.v6;

/**
 * Region element describes a (micro) region. The regionId follows the EAWS schema. It is
 * recommended to have the region shape's files with the same IDs in
 * gitlab.com/eaws/eaws-regions. Additionally, the region name can be added.
 */
public class Region {
    private Object customData;
    private MetaData metaData;
    private String name;
    // FIXME regionID (consistency with bulletinID)
    private String regionId;

	public Region() {
	}

	public Region(String regionId, String name) {
		this.name = name;
		this.regionId = regionId;
	}

	public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getRegionId() { return regionId; }
    public void setRegionId(String value) { this.regionId = value; }
}
