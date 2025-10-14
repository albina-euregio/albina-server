// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.micronaut.serde.annotation.Serdeable;

/**
 * JSON schema for EAWS avalanche bulletin collection following the CAAMLv6 schema
 */
@JacksonXmlRootElement(localName = "bulletins")
@JsonPropertyOrder({"bulletins", "customData", "metaData"})
@Serdeable
public class AvalancheBulletins {
    @JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "bulletin")
    private List<AvalancheBulletin> bulletins;
    private AvalancheBulletinsCustomData customData;
    private MetaData metaData;

	public AvalancheBulletins() {
	}

	public AvalancheBulletins(List<AvalancheBulletin> bulletins) {
		this.bulletins = bulletins;
	}

	public List<AvalancheBulletin> getBulletins() { return bulletins; }
    public void setBulletins(List<AvalancheBulletin> value) { this.bulletins = value; }

    public AvalancheBulletinsCustomData getCustomData() { return customData; }
    public void setCustomData(AvalancheBulletinsCustomData value) { this.customData = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }
}
