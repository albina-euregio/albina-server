// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Region element describes a (micro) region. The regionID follows the EAWS schema. It is
 * recommended to have the region shape's files with the same IDs in
 * gitlab.com/eaws/eaws-regions. Additionally, the region name can be added.
 */
@Serdeable
public class Region {
    private Object customData;
    private MetaData metaData;
    private String name;
	@JacksonXmlProperty(isAttribute = true)
    private String regionID;

	public Region() {
	}

	public Region(String regionID, String name) {
		this.name = name;
		this.regionID = regionID;
	}

	public Object getCustomData() { return customData; }
    public void setCustomData(Object value) { this.customData = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getRegionID() { return regionID; }
    public void setRegionID(String value) { this.regionID = value; }
}
