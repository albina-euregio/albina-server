package org.caaml.v6;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * JSON schema for EAWS avalanche bulletin collection following the CAAMLv6 schema
 */
@JacksonXmlRootElement(localName = "bulletins")
@JsonPropertyOrder({"bulletins", "customData", "metaData"})
public class AvalancheBulletins {
    @JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "bulletin")
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
