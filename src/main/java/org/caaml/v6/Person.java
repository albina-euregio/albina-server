package org.caaml.v6;

/**
 * Details on a person.
 */
public class Person {
    private CustomData[] customData;
    private MetaData metaData;
    private String name;
    private String website;

    public CustomData[] getCustomData() { return customData; }
    public void setCustomData(CustomData[] value) { this.customData = value; }

    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData value) { this.metaData = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getWebsite() { return website; }
    public void setWebsite(String value) { this.website = value; }
}
